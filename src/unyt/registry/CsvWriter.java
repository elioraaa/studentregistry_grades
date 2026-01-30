package unyt.registry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;

public class CsvWriter {

    public static void writeTopStudents(Path out, List<Registry.StudentRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,name,surname,level,totalCredits,gpa\n");
        for (Registry.StudentRow r : rows) {
            sb.append(esc(r.student().id())).append(",")
                    .append(esc(r.student().name())).append(",")
                    .append(esc(r.student().surname())).append(",")
                    .append(esc(r.student().level().label())).append(",")
                    .append(r.totalCredits()).append(",")
                    .append(String.format(Locale.US, "%.2f", r.gpa()))
                    .append("\n");
        }
        write(out, sb.toString());
    }

    public static void writeTopCourses(Path out, List<Registry.CourseRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("code,title,credits,level,gradeCount\n");
        for (Registry.CourseRow r : rows) {
            sb.append(esc(r.course().code())).append(",")
                    .append(esc(r.course().title())).append(",")
                    .append(r.course().credits()).append(",")
                    .append(esc(r.level().label())).append(",")
                    .append(r.gradeCount())
                    .append("\n");
        }
        write(out, sb.toString());
    }

    public static void writeTranscript(Path out, Registry.Transcript t) {
        StringBuilder sb = new StringBuilder();
        sb.append("studentId,studentName,studentSurname,level\n");
        sb.append(esc(t.student().id())).append(",")
                .append(esc(t.student().name())).append(",")
                .append(esc(t.student().surname())).append(",")
                .append(esc(t.student().level().label())).append("\n\n");

        sb.append("semester,courseCode,courseTitle,credits,numericGrade,letterGrade,gradePoints\n");
        for (Registry.TranscriptSemester sem : t.semesters()) {
            for (Registry.TranscriptLine line : sem.lines()) {
                sb.append(esc(sem.semester())).append(",")
                        .append(esc(line.courseCode())).append(",")
                        .append(esc(line.courseTitle())).append(",")
                        .append(line.credits()).append(",")
                        .append(line.numericGrade()).append(",")
                        .append(esc(line.letterGrade())).append(",")
                        .append(String.format(Locale.US, "%.2f", line.gradePoints()))
                        .append("\n");
            }
        }
        write(out, sb.toString());
    }

    private static String esc(String s) {
        if (s == null) return "";
        String v = s;
        boolean needs = v.contains(",") || v.contains("\"") || v.contains("\n");
        if (v.contains("\"")) v = v.replace("\"", "\"\"");
        return needs ? "\"" + v + "\"" : v;
    }

    private static void write(Path out, String content) {
        try {
            Files.writeString(out, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot write report file");
        }
    }
}
