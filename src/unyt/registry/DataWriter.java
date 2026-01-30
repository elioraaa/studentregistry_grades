package unyt.registry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class DataWriter {

    public static void appendStudent(Path folder, Student s) {
        Path file = folder.resolve("students.txt");
        String line = s.id() + ", " + s.name() + ", " + s.surname() + ", " + (s.email() == null ? "" : s.email()) + ", " + s.level().name() + "\n";
        append(file, line);
    }

    public static void appendCourse(Path folder, Course c) {
        Path file = folder.resolve("courses.txt");
        String line = c.code() + ", " + c.title() + ", " + c.credits() + "\n";
        append(file, line);
    }

    public static void appendGrade(Path folder, Evaluation e) {
        Path file = folder.resolve("grades.txt");
        String line = e.studentId() + ", " + e.courseCode() + ", " + e.semester() + ", " + e.numericGrade() + "\n";
        append(file, line);
    }

    private static void append(Path file, String line) {
        try {
            Files.writeString(file, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            throw new IllegalArgumentException("cannot write to data file: " + file.getFileName());
        }
    }
}
