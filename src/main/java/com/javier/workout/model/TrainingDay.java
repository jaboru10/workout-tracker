package com.javier.workout.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "training_days")
public class TrainingDay {

    @Id
    private String id;

    private String userId;
    private String routineId;   // rutina a la que pertenece (IL-004)
    private String name;   // "Día A - Pecho/Tríceps"
    private int order;
    private boolean active = true;

    private List<TemplateExercise> exercises = new ArrayList<>();

    public TrainingDay() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRoutineId() { return routineId; }
    public void setRoutineId(String routineId) { this.routineId = routineId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<TemplateExercise> getExercises() { return exercises; }
    public void setExercises(List<TemplateExercise> exercises) { this.exercises = exercises; }

    // --- Sub-documento ---
    public static class TemplateExercise {
        private String exerciseId;
        private int order;
        private int targetSets;
        private String targetReps; // "6-8"
        private String notes;

        public String getExerciseId() { return exerciseId; }
        public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }

        public int getTargetSets() { return targetSets; }
        public void setTargetSets(int targetSets) { this.targetSets = targetSets; }

        public String getTargetReps() { return targetReps; }
        public void setTargetReps(String targetReps) { this.targetReps = targetReps; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
