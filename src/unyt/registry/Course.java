package unyt.registry;

public class Course {
    private final String code;
    private final String title;
    private final int credits;

    public Course(String code, String title, int credits) {
        this.code = code;
        this.title = title;
        this.credits = credits;
    }

    public String code() { return code; }
    public String title() { return title; }
    public int credits() { return credits; }
}
