package unyt.registry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;

public class JsonWriter {

    public static void writeTopStudents(Path out, List<Registry.StudentRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < rows.size(); i++) {
            Registry.StudentRow r = rows.get(i);
            sb.append("  {\n");
            sb.append("    \"id\": ").append(q(r.student().id())).append(",\n");
            sb.append("    \"name\": ").append(q(r.student().name())).append(",\n");
            sb.append("    \"surname\": ").append(q(r.student().surname())).append(",\n");
            sb.append("    \"level\": ").append(q(r.student().level().label())).append(",\n");
            sb.append("    \"totalCredits\": ").append(r.totalCredits()).append(",\n");
            sb.append("    \"gpa\": ").append(String.format(Locale.US, "%.2f", r.gpa())).append("\n");
            sb.append("  }").append(i == rows.size() - 1 ? "\n" : ",\n");
        }
        sb.append("]\n");
        write(out, sb.toString());
    }

    public static void writeTopCourses(Path out, List<Registry.CourseRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < rows.size(); i++) {
            Registry.CourseRow r = rows.get(i);
            sb.append("  {\n");
            sb.append("    \"code\": ").append(q(r.course().code())).append(",\n");
            sb.append("    \"title\": ").append(q(r.course().title())).append(",\n");
            sb.append("    \"credits\": ").append(r.course().credits()).append(",\n");
            sb.append("    \"level\": ").append(q(r.level().label())).append(",\n");
            sb.append("    \"gradeCount\": ").append(r.gradeCount()).append("\n");
            sb.append("  }").append(i == rows.size() - 1 ? "\n" : ",\n");
        }
        sb.append("]\n");
        write(out, sb.toString());
    }

    public static void writeTranscript(Path out, Registry.Transcript t) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"student\": {\n");
        sb.append("    \"id\": ").append(q(t.student().id())).append(",\n");
        sb.append("    \"name\": ").append(q(t.student().name())).append(",\n");
        sb.append("    \"surname\": ").append(q(t.student().surname())).append(",\n");
        sb.append("    \"level\": ").append(q(t.student().level().label())).append("\n");
        sb.append("  },\n");
        sb.append("  \"semesters\": [\n");

        for (int i = 0; i < t.semesters().size(); i++) {
            Registry.TranscriptSemester sem = t.semesters().get(i);
            sb.append("    {\n");
            sb.append("      \"semester\": ").append(q(sem.semester())).append(",\n");
            sb.append("      \"records\": [\n");
            for (int j = 0; j < sem.lines().size(); j++) {
                Registry.TranscriptLine line = sem.lines().get(j);
                sb.append("        {\n");
                sb.append("          \"courseCode\": ").append(q(line.courseCode())).append(",\n");
                sb.append("          \"courseTitle\": ").append(q(line.courseTitle())).append(",\n");
                sb.append("          \"credits\": ").append(line.credits()).append(",\n");
                sb.append("          \"numericGrade\": ").append(line.numericGrade()).append(",\n");
                sb.append("          \"letterGrade\": ").append(q(line.letterGrade())).append(",\n");
                sb.append("          \"gradePoints\": ").append(String.format(Locale.US, "%.2f", line.gradePoints())).append("\n");
                sb.append("        }").append(j == sem.lines().size() - 1 ? "\n" : ",\n");
            }
            sb.append("      ]\n");
            sb.append("    }").append(i == t.semesters().size() - 1 ? "\n" : ",\n");
        }

        sb.append("  ]\n");
        sb.append("}\n");
        write(out, sb.toString());
    }

    private static String q(String s) {
        if (s == null) return "\"\"";
        String v = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + v + "\"";
    }

    private static void write(Path out, String content) {
        try {
            Files.writeString(out, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot write report file");
        }
    }
}
