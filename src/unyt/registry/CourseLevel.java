package unyt.registry;

public enum CourseLevel {
    UNDERGRADUATE, GRADUATE;

    public String label() {
        return this == UNDERGRADUATE ? "undergraduate" : "graduate";
    }
}
