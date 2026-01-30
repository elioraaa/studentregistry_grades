package unyt.registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    public static LoadResult loadAll(String folderPath) {
        Path folder = Path.of(folderPath);

        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            throw new IllegalArgumentException("invalid folder name");
        }

        Path studentsFile = folder.resolve("students.txt");
        Path coursesFile = folder.resolve("courses.txt");
        Path gradesFile = folder.resolve("grades.txt");

        if (!Files.exists(studentsFile) || !Files.exists(coursesFile) || !Files.exists(gradesFile)) {
            throw new IllegalArgumentException("data files not found");
        }

        List<Student> students = loadStudents(studentsFile);
        List<Course> courses = loadCourses(coursesFile);
        List<Evaluation> grades = loadGrades(gradesFile);

        return new LoadResult(students, courses, grades, folder);
    }

    private static List<Student> loadStudents(Path file) {
        List<Student> list = new ArrayList<>();
        int lineNo = 0;

        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] p = line.split(", ", -1);
                if (p.length != 5) throw new IllegalArgumentException("students.txt invalid line " + lineNo);

                String id = p[0].trim();
                String name = p[1].trim();
                String surname = p[2].trim();
                String email = p[3]; // can be empty
                String levelStr = p[4].trim();

                Validation.validateStudentId(id);
                Validation.validateNonEmpty(name, "name");
                Validation.validateNonEmpty(surname, "surname");
                Validation.validateEmailOptional(email);
                Level level = Validation.parseLevel(levelStr);

                list.add(new Student(id, name, surname, email.trim(), level));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot read students.txt");
        }
        return list;
    }

    private static List<Course> loadCourses(Path file) {
        List<Course> list = new ArrayList<>();
        int lineNo = 0;

        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] p = line.split(", ", -1);
                if (p.length != 3) throw new IllegalArgumentException("courses.txt invalid line " + lineNo);

                String code = p[0].trim();
                String title = p[1].trim();
                String creditsStr = p[2].trim();

                Validation.validateCourseCode(code);
                Validation.validateNonEmpty(title, "title");
                int credits = Validation.parseCredits(creditsStr);
                Validation.computeCourseLevel(code);

                list.add(new Course(code, title, credits));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot read courses.txt");
        }
        return list;
    }

    private static List<Evaluation> loadGrades(Path file) {
        List<Evaluation> list = new ArrayList<>();
        int lineNo = 0;

        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] p = line.split(", ", -1);
                if (p.length != 4) throw new IllegalArgumentException("grades.txt invalid line " + lineNo);

                String studentId = p[0].trim();
                String courseCode = p[1].trim();
                String semester = p[2].trim();
                int grade = Validation.parseNumericGrade(p[3]);

                Validation.validateStudentId(studentId);
                Validation.validateCourseCode(courseCode);
                Validation.validateSemester(semester);

                list.add(new Evaluation(studentId, courseCode, semester, grade));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot read grades.txt");
        }
        return list;
    }

    public record LoadResult(List<Student> students, List<Course> courses, List<Evaluation> grades, Path folder) {
    }
}
