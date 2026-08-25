package com.javier.workout.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "exercises")
public class Exercise {

    @Id
    private String id;

    private String userId;
    private String name;
    private String muscleGroup;

    // Ejercicio de peso corporal (dominadas, fondos...). Si es true,
    // el peso registrado en el set es el LASTRE añadido, no el total.
    private boolean bodyweight = false;

    // Soft delete: nunca borramos de verdad para no dejar sesiones huérfanas
    private boolean active = true;

    public Exercise() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public boolean isBodyweight() { return bodyweight; }
    public void setBodyweight(boolean bodyweight) { this.bodyweight = bodyweight; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
