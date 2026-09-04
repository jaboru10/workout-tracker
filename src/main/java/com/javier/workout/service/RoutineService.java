package com.javier.workout.service;

import com.javier.workout.model.Exercise;
import com.javier.workout.model.Routine;
import com.javier.workout.model.TrainingDay;
import com.javier.workout.repository.ExerciseRepository;
import com.javier.workout.repository.RoutineRepository;
import com.javier.workout.repository.TrainingDayRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lógica de rutinas (IL-004): activación exclusiva y copia de predefinidas.
 */
@Service
public class RoutineService {

    private final RoutineRepository routineRepo;
    private final TrainingDayRepository dayRepo;
    private final ExerciseRepository exerciseRepo;

    public RoutineService(RoutineRepository routineRepo,
                          TrainingDayRepository dayRepo,
                          ExerciseRepository exerciseRepo) {
        this.routineRepo = routineRepo;
        this.dayRepo = dayRepo;
        this.exerciseRepo = exerciseRepo;
    }

    /** Marca una rutina como la única activa del usuario. */
    public Routine activate(String userId, Routine routine) {
        for (Routine r : routineRepo.findByUserIdAndArchivedFalse(userId)) {
            if (r.isActive() && !r.getId().equals(routine.getId())) {
                r.setActive(false);
                routineRepo.save(r);
            }
        }
        routine.setActive(true);
        return routineRepo.save(routine);
    }

    /** Crea una rutina vacía. Se activa si el usuario aún no tiene ninguna activa. */
    public Routine createEmpty(String userId, String name, String level, String type) {
        Routine r = new Routine();
        r.setUserId(userId);
        r.setName(name);
        r.setLevel(level);
        r.setType(type);
        r.setPreset(false);
        boolean noneActive = routineRepo.findByUserIdAndActiveTrueAndArchivedFalse(userId).isEmpty();
        r.setActive(noneActive);
        return routineRepo.save(r);
    }

    /**
     * Copia una predefinida a una rutina personal modificable: crea la rutina,
     * sus días (TrainingDay) y resuelve/crea los ejercicios del usuario por
     * nombre. La predefinida original queda intacta. La copia queda activa.
     */
    public Routine copyFromPreset(String userId, Routine preset) {
        Routine copy = new Routine();
        copy.setUserId(userId);
        copy.setName(preset.getName());
        copy.setLevel(preset.getLevel());
        copy.setType(preset.getType());
        copy.setPreset(false);
        copy.setSourceRoutineId(preset.getId());
        copy = routineRepo.save(copy);

        // Mapa de ejercicios existentes del usuario por nombre normalizado.
        Map<String, Exercise> byName = new LinkedHashMap<>();
        for (Exercise ex : exerciseRepo.findByUserIdAndActiveTrue(userId)) {
            byName.put(norm(ex.getName()), ex);
        }

        for (Routine.TemplateDay td : preset.getTemplateDays()) {
            TrainingDay day = new TrainingDay();
            day.setUserId(userId);
            day.setRoutineId(copy.getId());
            day.setName(td.getName());
            day.setOrder(td.getOrder());
            day.setActive(true);

            int order = 1;
            for (Routine.TemplateExercise te : td.getExercises()) {
                Exercise ex = byName.get(norm(te.getName()));
                if (ex == null) {
                    ex = new Exercise();
                    ex.setUserId(userId);
                    ex.setName(te.getName());
                    ex.setMuscleGroup(te.getMuscleGroup());
                    ex.setBodyweight(te.isBodyweight());
                    ex.setActive(true);
                    ex = exerciseRepo.save(ex);
                    byName.put(norm(te.getName()), ex);
                }

                TrainingDay.TemplateExercise dayEx = new TrainingDay.TemplateExercise();
                dayEx.setExerciseId(ex.getId());
                dayEx.setOrder(order++);
                dayEx.setTargetSets(te.getTargetSets());
                dayEx.setTargetReps(te.getTargetReps());
                dayEx.setNotes(te.getNotes());
                day.getExercises().add(dayEx);
            }
            dayRepo.save(day);
        }

        // Activar la copia como única activa.
        return activate(userId, copy);
    }

    private String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}
