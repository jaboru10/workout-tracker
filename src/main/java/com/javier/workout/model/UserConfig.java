package com.javier.workout.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuración del usuario: rangos de reps y ventanas temporales de récord.
 */
@Document(collection = "user_configs")
public class UserConfig {

    @Id
    private String id;

    private String userId;

    // Rangos de repeticiones configurables
    private List<RepRange> repRanges = new ArrayList<>();

    // Ventana por defecto en meses (0 = histórico)
    private int defaultRecordWindowMonths = 6;

    // Ventanas disponibles para consultar (meses; 0 = histórico)
    private List<Integer> recordWindowsMonths = new ArrayList<>(List.of(3, 6, 12, 0));

    public UserConfig() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<RepRange> getRepRanges() { return repRanges; }
    public void setRepRanges(List<RepRange> repRanges) { this.repRanges = repRanges; }

    public int getDefaultRecordWindowMonths() { return defaultRecordWindowMonths; }
    public void setDefaultRecordWindowMonths(int m) { this.defaultRecordWindowMonths = m; }

    public List<Integer> getRecordWindowsMonths() { return recordWindowsMonths; }
    public void setRecordWindowsMonths(List<Integer> w) { this.recordWindowsMonths = w; }

    public static class RepRange {
        private String name;  // "Fuerza"
        private int min;      // 1
        private int max;      // 5

        public RepRange() {}
        public RepRange(String name, int min, int max) {
            this.name = name; this.min = min; this.max = max;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getMin() { return min; }
        public void setMin(int min) { this.min = min; }

        public int getMax() { return max; }
        public void setMax(int max) { this.max = max; }
    }
}
