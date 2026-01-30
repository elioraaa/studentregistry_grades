package unyt.registry;

public class Evaluation {
    private final String studentId;
    private final String courseCode;
    private final String semester;
    private final int numericGrade;

    public Evaluation(String studentId, String courseCode, String semester, int numericGrade) {
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.semester = semester;
        this.numericGrade = numericGrade;
    }

    public String studentId() { return studentId; }
    public String courseCode() { return courseCode; }
    public String semester() { return semester; }
    public int numericGrade() { return numericGrade; }
}
