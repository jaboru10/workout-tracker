package com.javier.workout.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Rutina: contenedor de días de entrenamiento (Weider, PPL, 5x5…).
 * Solo una rutina activa a la vez por usuario.
 *
 * Dos sabores:
 *  - Rutina de usuario: userId != null, preset = false. Sus días viven como
 *    documentos TrainingDay con routineId apuntando aquí.
 *  - Predefinida (biblioteca): userId = null, preset = true. Es intocable;
 *    lleva sus días embebidos en templateDays. Al "usarla" se clona a una
 *    copia personal (rutina + días + ejercicios) y la original queda intacta.
 */
@Document(collection = "routines")
public class Routine {

    @Id
    private String id;

    private String userId;            // null = predefinida global
    private String name;
    private String category;          // "Fuerza", "Hipertrofia", "Principiante"…

    private boolean active = false;   // la rutina seleccionada (solo una true por usuario)
    private boolean preset = false;   // true = plantilla de la biblioteca
    private String sourceRoutineId;   // predefinida de la que se copió (informativo)

    private boolean archived = false; // soft delete

    // Solo para predefinidas: días + ejercicios embebidos (por nombre, no por id).
    private List<TemplateDay> templateDays = new ArrayList<>();

    public Routine() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isPreset() { return preset; }
    public void setPreset(boolean preset) { this.preset = preset; }

    public String getSourceRoutineId() { return sourceRoutineId; }
    public void setSourceRoutineId(String sourceRoutineId) { this.sourceRoutineId = sourceRoutineId; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public List<TemplateDay> getTemplateDays() { return templateDays; }
    public void setTemplateDays(List<TemplateDay> templateDays) { this.templateDays = templateDays; }

    // --- Día embebido de una predefinida ---
    public static class TemplateDay {
        private String name;   // "Día A - Empuje"
        private int order;
        private List<TemplateExercise> exercises = new ArrayList<>();

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }

        public List<TemplateExercise> getExercises() { return exercises; }
        public void setExercises(List<TemplateExercise> exercises) { this.exercises = exercises; }
    }

    // --- Ejercicio embebido: por NOMBRE, no por id (los ids son por usuario).
    // Al copiar la predefinida se resuelve/crea el Exercise del usuario. ---
    public static class TemplateExercise {
        private String name;         // "Press banca"
        private String muscleGroup;  // "Pecho"
        private boolean bodyweight;
        private int targetSets;
        private String targetReps;   // "6-8"
        private String notes;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getMuscleGroup() { return muscleGroup; }
        public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

        public boolean isBodyweight() { return bodyweight; }
        public void setBodyweight(boolean bodyweight) { this.bodyweight = bodyweight; }

        public int getTargetSets() { return targetSets; }
        public void setTargetSets(int targetSets) { this.targetSets = targetSets; }

        public String getTargetReps() { return targetReps; }
        public void setTargetReps(String targetReps) { this.targetReps = targetReps; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
