package unyt.registry;

public enum Level {
    UG, G;

    public String label() {
        return this == UG ? "undergraduate" : "graduate";
    }
}
