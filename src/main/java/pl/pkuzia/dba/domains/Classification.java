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
}
