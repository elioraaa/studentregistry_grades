package unyt.registry;

import java.util.Objects;

public class GradeKey {
    private final String studentId;
    private final String courseCode;

    public GradeKey(String studentId, String courseCode) {
        this.studentId = studentId;
        this.courseCode = courseCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GradeKey other)) return false;
        return Objects.equals(studentId, other.studentId)
                && Objects.equals(courseCode, other.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, courseCode);
    }
}
