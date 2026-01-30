package unyt.registry;

import java.nio.file.Path;
import java.util.List;

public class ReportService {
    private final Registry registry;

    public ReportService(Registry registry) {
        this.registry = registry;
    }

    public boolean reportTopStudents(int n, String fileName) {
        Validation.requireLoaded(registry.isLoaded());

        boolean limited = false;
        if (n > 100) {
            n = 100;
            limited = true;
        }
        if (n < 0) throw new IllegalArgumentException("invalid n");

        List<Registry.StudentRow> rows = registry.topStudents(n);
        writeByExt(fileName, () -> CsvWriter.writeTopStudents(Path.of(fileName), rows), () -> JsonWriter.writeTopStudents(Path.of(fileName), rows), () -> XmlWriter.writeTopStudents(Path.of(fileName), rows));
        return limited;
    }

    public boolean reportTopCourses(int n, String fileName) {
        Validation.requireLoaded(registry.isLoaded());

        boolean limited = false;
        if (n > 100) {
            n = 100;
            limited = true;
        }
        if (n < 0) throw new IllegalArgumentException("invalid n");

        List<Registry.CourseRow> rows = registry.topCourses(n);
        writeByExt(fileName, () -> CsvWriter.writeTopCourses(Path.of(fileName), rows), () -> JsonWriter.writeTopCourses(Path.of(fileName), rows), () -> XmlWriter.writeTopCourses(Path.of(fileName), rows));
        return limited;
    }

    public void reportTranscript(String studentId, String fileName) {
        Validation.requireLoaded(registry.isLoaded());

        Registry.Transcript t = registry.transcript(studentId);
        if (t == null) {
            System.out.println("error: no student found for given id");
            return;
        }

        writeByExt(fileName, () -> CsvWriter.writeTranscript(Path.of(fileName), t), () -> JsonWriter.writeTranscript(Path.of(fileName), t), () -> XmlWriter.writeTranscript(Path.of(fileName), t));

        System.out.println("Report generated.");
    }

    private void writeByExt(String fileName, Runnable csv, Runnable json, Runnable xml) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".csv")) csv.run();
        else if (lower.endsWith(".json")) json.run();
        else if (lower.endsWith(".xml")) xml.run();
        else throw new IllegalArgumentException("unsupported report format");
    }
}
