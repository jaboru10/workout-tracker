package com.javier.workout.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "sessions")
public class Session {

    @Id
    private String id;

    private String userId;
    private LocalDate date;          // fecha local, NUNCA timestamp UTC
    private String trainingDayId;    // plantilla usada (referencia informativa)

    // Contexto de rutina CONGELADO al crear la sesión (IL-004). Se guarda el
    // nombre además del id para que el historial no se corrompa aunque luego
    // se renombre, archive o cambie de rutina activa.
    private String routineId;
    private String routineName;

    private boolean badDay = false;  // "hoy estoy flojo"
    private String generalNotes;

    private List<SessionExercise> exercises = new ArrayList<>();

    public Session() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getTrainingDayId() { return trainingDayId; }
    public void setTrainingDayId(String trainingDayId) { this.trainingDayId = trainingDayId; }

    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { this.routineId = routineId; }

    public String getRoutineName() { return routineName; }
    public void setRoutineName(String routineName) { this.routineName = routineName; }

    public boolean isBadDay() { return badDay; }
    public void setBadDay(boolean badDay) { this.badDay = badDay; }

    public String getGeneralNotes() { return generalNotes; }
    public void setGeneralNotes(String generalNotes) { this.generalNotes = generalNotes; }

    public List<SessionExercise> getExercises() { return exercises; }
    public void setExercises(List<SessionExercise> exercises) { this.exercises = exercises; }

    // --- Ejercicio dentro de una sesión ---
    public static class SessionExercise {
        private String exerciseId;

        // Posición REAL en la que se hizo ese día (contexto de fatiga).
        // Se congela aquí, nunca se lee desde TrainingDay para históricos.
        private int order;

        // Si se movió desde otro día de la plantilla
        private String movedFromDayId;

        // Dato informativo: qué ejercicios se hicieron ANTES (para contexto
        // de fatiga completo). En v1 solo se compara por 'order', esto se
        // guarda para análisis futuro.
        private List<String> precedingExerciseIds = new ArrayList<>();

        private List<SetEntry> sets = new ArrayList<>();

        public String getExerciseId() { return exerciseId; }
        public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }

        public String getMovedFromDayId() { return movedFromDayId; }
        public void setMovedFromDayId(String movedFromDayId) { this.movedFromDayId = movedFromDayId; }

        public List<String> getPrecedingExerciseIds() { return precedingExerciseIds; }
        public void setPrecedingExerciseIds(List<String> precedingExerciseIds) { this.precedingExerciseIds = precedingExerciseIds; }

        public List<SetEntry> getSets() { return sets; }
        public void setSets(List<SetEntry> sets) { this.sets = sets; }
    }

    // --- Una serie ---
    public static class SetEntry {
        private int setNumber;
        private BigDecimal weight;   // decimal: discos de 1.25, mancuernas 2.5
        private String unit = "kg";  // fijado desde v1 aunque solo uses kg
        private int reps;
        private Double rpe;          // opcional (esfuerzo percibido)

        public int getSetNumber() { return setNumber; }
        public void setSetNumber(int setNumber) { this.setNumber = setNumber; }

        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public int getReps() { return reps; }
        public void setReps(int reps) { this.reps = reps; }

        public Double getRpe() { return rpe; }
        public void setRpe(Double rpe) { this.rpe = rpe; }
    }
}
