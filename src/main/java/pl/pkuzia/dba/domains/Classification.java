package pl.pkuzia.dba.domains;

public enum Classification {
    SOFT {
        public String getType() {
            return "Soft";
        }
    },
    OPTIMAL {
        public String getType() {
            return "Optimal";
        }
    },
    HARD {
        public String getType() {
            return "Hard";
        }
    },
    UNKNOWN {
        public String getType() {
            return "Unknown";
        }
    };

    public abstract String getType();

    public static Classification mapToClassificationType(float label) {
        if (label == 0.0f) {
            return SOFT;
        }
        if (label == 1.0f) {
            return OPTIMAL;
        }
        if (label == 2.0f) {
            return HARD;
        }
        else return UNKNOWN;
    }
}
