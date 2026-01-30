package unyt.registry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;

public class XmlWriter {

    public static void writeTopStudents(Path out, List<Registry.StudentRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<topStudents>\n");
        for (Registry.StudentRow r : rows) {
            sb.append("  <student>\n");
            sb.append("    <id>").append(x(r.student().id())).append("</id>\n");
            sb.append("    <name>").append(x(r.student().name())).append("</name>\n");
            sb.append("    <surname>").append(x(r.student().surname())).append("</surname>\n");
            sb.append("    <level>").append(x(r.student().level().label())).append("</level>\n");
            sb.append("    <totalCredits>").append(r.totalCredits()).append("</totalCredits>\n");
            sb.append("    <gpa>").append(String.format(Locale.US, "%.2f", r.gpa())).append("</gpa>\n");
            sb.append("  </student>\n");
        }
        sb.append("</topStudents>\n");
        write(out, sb.toString());
    }

    public static void writeTopCourses(Path out, List<Registry.CourseRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<topCourses>\n");
        for (Registry.CourseRow r : rows) {
            sb.append("  <course>\n");
            sb.append("    <code>").append(x(r.course().code())).append("</code>\n");
            sb.append("    <title>").append(x(r.course().title())).append("</title>\n");
            sb.append("    <credits>").append(r.course().credits()).append("</credits>\n");
            sb.append("    <level>").append(x(r.level().label())).append("</level>\n");
            sb.append("    <gradeCount>").append(r.gradeCount()).append("</gradeCount>\n");
            sb.append("  </course>\n");
        }
        sb.append("</topCourses>\n");
        write(out, sb.toString());
    }

    public static void writeTranscript(Path out, Registry.Transcript t) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<transcript>\n");
        sb.append("  <student>\n");
        sb.append("    <id>").append(x(t.student().id())).append("</id>\n");
        sb.append("    <name>").append(x(t.student().name())).append("</name>\n");
        sb.append("    <surname>").append(x(t.student().surname())).append("</surname>\n");
        sb.append("    <level>").append(x(t.student().level().label())).append("</level>\n");
        sb.append("  </student>\n");
        sb.append("  <semesters>\n");
        for (Registry.TranscriptSemester sem : t.semesters()) {
            sb.append("    <semester name=\"").append(x(sem.semester())).append("\">\n");
            for (Registry.TranscriptLine line : sem.lines()) {
                sb.append("      <record>\n");
                sb.append("        <courseCode>").append(x(line.courseCode())).append("</courseCode>\n");
                sb.append("        <courseTitle>").append(x(line.courseTitle())).append("</courseTitle>\n");
                sb.append("        <credits>").append(line.credits()).append("</credits>\n");
                sb.append("        <numericGrade>").append(line.numericGrade()).append("</numericGrade>\n");
                sb.append("        <letterGrade>").append(x(line.letterGrade())).append("</letterGrade>\n");
                sb.append("        <gradePoints>").append(String.format(Locale.US, "%.2f", line.gradePoints())).append("</gradePoints>\n");
                sb.append("      </record>\n");
            }
            sb.append("    </semester>\n");
        }
        sb.append("  </semesters>\n");
        sb.append("</transcript>\n");
        write(out, sb.toString());
    }

    private static String x(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static void write(Path out, String content) {
        try {
            Files.writeString(out, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot write report file");
        }
    }
}
