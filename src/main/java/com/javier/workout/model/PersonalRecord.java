package com.javier.workout.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Récord cacheado. Se actualiza al guardar sesión, pero SIEMPRE referencia
 * la sesión origen. Si se borra esa sesión, se recalcula desde el historial,
 * así nunca hay inconsistencia real.
 */
@Document(collection = "personal_records")
public class PersonalRecord {

    @Id
    private String id;

    private String userId;
    private String exerciseId;

    private RecordType type;      // MAX_WEIGHT, BEST_VOLUME, ESTIMATED_1RM
    private String repRangeName;  // null si no aplica rango
    private Integer position;     // posición de fatiga; null = cualquiera

    private BigDecimal weight;
    private int reps;
    private BigDecimal value;     // el valor que define el récord (peso, volumen o 1RM estimado)

    private LocalDate date;
    private String sourceSessionId; // clave para recalcular si se borra

    public PersonalRecord() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public RecordType getType() { return type; }
    public void setType(RecordType type) { this.type = type; }

    public String getRepRangeName() { return repRangeName; }
    public void setRepRangeName(String repRangeName) { this.repRangeName = repRangeName; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getSourceSessionId() { return sourceSessionId; }
    public void setSourceSessionId(String sourceSessionId) { this.sourceSessionId = sourceSessionId; }

    public enum RecordType {
        MAX_WEIGHT,      // mayor peso independientemente de reps
        BEST_VOLUME,     // mayor peso × reps en una serie
        ESTIMATED_1RM    // más peso con menos reps (fórmula Epley)
    }
}
