package com.javier.workout.service;

import com.javier.workout.model.*;
import com.javier.workout.model.PersonalRecord.RecordType;
import com.javier.workout.model.Session.SessionExercise;
import com.javier.workout.model.Session.SetEntry;
import com.javier.workout.model.UserConfig.RepRange;
import com.javier.workout.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Cálculo de récords. Estrategia híbrida:
 *  - PersonalRecord cacheado se actualiza al guardar sesión (recalcRecords).
 *  - Consulta puntual filtrando por ventana temporal / rango / posición
 *    se hace sobre el historial de sesiones (computeRecords).
 */
@Service
public class RecordService {

    private final SessionRepository sessionRepo;
    private final PersonalRecordRepository recordRepo;
    private final UserConfigRepository configRepo;

    public RecordService(SessionRepository sessionRepo,
                         PersonalRecordRepository recordRepo,
                         UserConfigRepository configRepo) {
        this.sessionRepo = sessionRepo;
        this.recordRepo = recordRepo;
        this.configRepo = configRepo;
    }

    /**
     * Calcula los récords de un ejercicio en una ventana temporal.
     * @param windowMonths 0 = histórico
     * @param position     null = cualquier posición; N = solo posición N (fatiga)
     */
    public List<RecordResult> computeRecords(String userId, String exerciseId,
                                             int windowMonths, Integer position) {
        LocalDate from = windowMonths == 0
                ? LocalDate.of(1970, 1, 1)
                : LocalDate.now().minusMonths(windowMonths);

        List<Session> sessions = sessionRepo.findByUserIdAndExercisesExerciseId(userId, exerciseId).stream()
                .filter(s -> !s.getDate().isBefore(from))
                .toList();

        UserConfig config = configRepo.findByUserId(userId).orElseGet(this::defaultConfig);

        RecordResult maxWeight = null;
        RecordResult bestVolume = null;
        RecordResult best1rm = null;
        Map<String, RecordResult> byRange = new LinkedHashMap<>();

        for (Session s : sessions) {
            for (SessionExercise se : s.getExercises()) {
                if (!se.getExerciseId().equals(exerciseId)) continue;
                if (position != null && se.getOrder() != position) continue;

                for (SetEntry set : se.getSets()) {
                    if (set.getWeight() == null) continue;
                    BigDecimal w = set.getWeight();
                    int reps = set.getReps();

                    // Peso máximo
                    if (maxWeight == null || w.compareTo(maxWeight.weight) > 0) {
                        maxWeight = build(RecordType.MAX_WEIGHT, null, se, s, w, reps, w);
                    }
                    // Volumen (peso × reps)
                    BigDecimal volume = w.multiply(BigDecimal.valueOf(reps));
                    if (bestVolume == null || volume.compareTo(bestVolume.value) > 0) {
                        bestVolume = build(RecordType.BEST_VOLUME, null, se, s, w, reps, volume);
                    }
                    // 1RM estimado (Epley): w * (1 + reps/30)
                    BigDecimal e1rm = estimate1rm(w, reps);
                    if (best1rm == null || e1rm.compareTo(best1rm.value) > 0) {
                        best1rm = build(RecordType.ESTIMATED_1RM, null, se, s, w, reps, e1rm);
                    }
                    // Récord por rango de reps (basado en peso máximo dentro del rango)
                    RepRange range = findRange(config.getRepRanges(), reps);
                    if (range != null) {
                        RecordResult current = byRange.get(range.getName());
                        if (current == null || w.compareTo(current.weight) > 0) {
                            byRange.put(range.getName(),
                                    build(RecordType.MAX_WEIGHT, range.getName(), se, s, w, reps, w));
                        }
                    }
                }
            }
        }

        List<RecordResult> results = new ArrayList<>();
        if (maxWeight != null) results.add(maxWeight);
        if (bestVolume != null) results.add(bestVolume);
        if (best1rm != null) results.add(best1rm);
        results.addAll(byRange.values());
        return results;
    }

    /**
     * Detecta si la sesión recién guardada bate algún récord histórico.
     * Se llama al crear una sesión y actualiza la caché PersonalRecord.
     * Devuelve los récords nuevos batidos (para mostrarlos en la respuesta).
     */
    public List<RecordResult> recalcRecords(Session saved) {
        List<RecordResult> newRecords = new ArrayList<>();
        Set<String> exerciseIds = new HashSet<>();
        for (SessionExercise se : saved.getExercises()) {
            exerciseIds.add(se.getExerciseId());
        }

        for (String exId : exerciseIds) {
            // Récord histórico (ventana 0) sin filtrar posición
            List<RecordResult> hist = computeRecords(saved.getUserId(), exId, 0, null);
            for (RecordResult r : hist) {
                // ¿El récord proviene de esta sesión? Entonces es nuevo.
                if (saved.getId() != null && saved.getId().equals(r.sourceSessionId)) {
                    newRecords.add(r);
                    upsertCachedRecord(r);
                }
            }
        }
        return newRecords;
    }

    /**
     * Si se borra una sesión que era origen de récords, se recalculan.
     */
    public void handleSessionDeletion(String userId, String sessionId) {
        List<PersonalRecord> affected = recordRepo.findBySourceSessionId(sessionId);
        Set<String> exercisesToRecalc = new HashSet<>();
        for (PersonalRecord pr : affected) {
            exercisesToRecalc.add(pr.getExerciseId());
        }
        recordRepo.deleteAll(affected);
        for (String exId : exercisesToRecalc) {
            for (RecordResult r : computeRecords(userId, exId, 0, null)) {
                upsertCachedRecord(r);
            }
        }
    }

    // ---------- helpers ----------

    private void upsertCachedRecord(RecordResult r) {
        PersonalRecord pr = new PersonalRecord();
        pr.setUserId(r.userId);
        pr.setExerciseId(r.exerciseId);
        pr.setType(r.type);
        pr.setRepRangeName(r.repRangeName);
        pr.setPosition(r.position);
        pr.setWeight(r.weight);
        pr.setReps(r.reps);
        pr.setValue(r.value);
        pr.setDate(r.date);
        pr.setSourceSessionId(r.sourceSessionId);
        pr.setRoutineId(r.routineId);
        pr.setRoutineName(r.routineName);
        recordRepo.save(pr);
    }

    private BigDecimal estimate1rm(BigDecimal weight, int reps) {
        if (reps <= 1) return weight;
        // Epley: w * (1 + reps/30)
        return weight
                .multiply(BigDecimal.ONE.add(
                        BigDecimal.valueOf(reps).divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private RepRange findRange(List<RepRange> ranges, int reps) {
        for (RepRange r : ranges) {
            if (reps >= r.getMin() && reps <= r.getMax()) return r;
        }
        return null;
    }

    private RecordResult build(RecordType type, String rangeName, SessionExercise se,
                               Session s, BigDecimal weight, int reps, BigDecimal value) {
        RecordResult r = new RecordResult();
        r.userId = s.getUserId();
        r.exerciseId = se.getExerciseId();
        r.type = type;
        r.repRangeName = rangeName;
        r.position = se.getOrder();
        r.weight = weight;
        r.reps = reps;
        r.value = value;
        r.date = s.getDate();
        r.sourceSessionId = s.getId();
        r.routineId = s.getRoutineId();
        r.routineName = s.getRoutineName();
        r.precedingExerciseIds = se.getPrecedingExerciseIds();
        return r;
    }

    private UserConfig defaultConfig() {
        UserConfig c = new UserConfig();
        c.getRepRanges().add(new RepRange("Fuerza", 1, 5));
        c.getRepRanges().add(new RepRange("Hipertrofia", 6, 12));
        c.getRepRanges().add(new RepRange("Resistencia", 13, 20));
        return c;
    }

    // DTO de resultado de récord
    public static class RecordResult {
        public String userId;
        public String exerciseId;
        public RecordType type;
        public String repRangeName;
        public Integer position;
        public BigDecimal weight;
        public int reps;
        public BigDecimal value;
        public LocalDate date;
        public String sourceSessionId;
        public String routineId;
        public String routineName;
        public List<String> precedingExerciseIds;
    }
}
