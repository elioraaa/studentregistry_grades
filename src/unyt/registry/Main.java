package unyt.registry;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Registry registry = new Registry();
        ReportService reportService = new ReportService(registry);

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("? ");
            String line = sc.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] first = line.split("\\s+", 2);
            String cmd = first[0];
            String rest = first.length > 1 ? first[1].trim() : "";

            try {
                switch (cmd) {
                    case "load" -> {
                        if (rest.isEmpty()) {
                            System.out.println("error: invalid folder name");
                        } else {
                            registry.load(rest);
                        }
                    }
                    case "find" -> handleFind(registry, rest);
                    case "query" -> handleQuery(registry, rest);
                    case "add" -> handleAdd(registry, rest);
                    case "report" -> handleReport(reportService, rest);
                    case "quit" -> {
                        System.out.println("The program is terminated.");
                        return;
                    }
                    default -> System.out.println("error: unknown command");
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("error: unexpected failure: " + ex.getMessage());
            }
        }
    }

    private static void handleFind(Registry registry, String rest) {
        if (rest.isEmpty()) {
            System.out.println("error: missing entity");
            return;
        }

        String[] p = rest.split("\\s+", 2);
        String entity = p[0].trim();
        String key = p.length > 1 ? p[1].trim() : "";

        switch (entity) {
            case "student" -> {
                if (key.isEmpty()) {
                    System.out.println("error: missing student ID");
                    return;
                }
                System.out.print(registry.findStudentDisplay(key));
            }
            case "course" -> {
                if (key.isEmpty()) {
                    System.out.println("error: missing course code");
                    return;
                }
                System.out.print(registry.findCourseDisplay(key));
            }
            case "grade" -> {
                if (key.isEmpty()) {
                    System.out.println("error: missing grade key");
                    return;
                }
                String[] kk = key.split(", ", -1);
                if (kk.length != 2) {
                    System.out.println("error: invalid grade key");
                    return;
                }
                System.out.print(registry.findGradeDisplay(kk[0].trim(), kk[1].trim()));
            }
            default -> System.out.println("error: no such entity");
        }
    }

    private static void handleQuery(Registry registry, String rest) {
        if (rest.isEmpty()) {
            System.out.println("error: missing entity");
            return;
        }
        String[] p = rest.split("\\s+", 2);
        String entity = p[0].trim();
        String criteria = p.length > 1 ? p[1].trim() : "";
        System.out.print(registry.queryDisplay(entity, criteria));
    }

    private static void handleAdd(Registry registry, String rest) {
        if (rest.isEmpty()) {
            System.out.println("error: missing entity");
            return;
        }
        String[] p = rest.split("\\s+", 2);
        String entity = p[0].trim();
        String values = p.length > 1 ? p[1].trim() : "";

        switch (entity) {
            case "student" -> registry.addStudentFromCli(values);
            case "course" -> registry.addCourseFromCli(values);
            case "grade" -> registry.addGradeFromCli(values);
            default -> System.out.println("error: no such entity");
        }
    }

    private static void handleReport(ReportService reportService, String rest) {
        if (rest.isEmpty()) {
            System.out.println("error: missing report type");
            return;
        }
        String[] p = rest.split("\\s+");
        String type = p[0].trim();

        switch (type) {
            case "topStudents", "bestStudents" -> {
                if (p.length < 3) {
                    System.out.println("error: missing parameters");
                    return;
                }
                int n = parseInt(p[1], "invalid n");
                String fileName = p[2];
                boolean limited = reportService.reportTopStudents(n, fileName);
                System.out.println(limited ? "Parameter limited at 100. Report generated." : "Report generated.");
            }
            case "topCourses" -> {
                if (p.length < 3) {
                    System.out.println("error: missing parameters");
                    return;
                }
                int n = parseInt(p[1], "invalid n");
                String fileName = p[2];
                boolean limited = reportService.reportTopCourses(n, fileName);
                System.out.println(limited ? "Parameter limited at 100. Report generated." : "Report generated.");
            }
            case "transcript" -> {
                if (p.length < 3) {
                    System.out.println("error: missing parameters");
                    return;
                }
                String id = p[1];
                String fileName = p[2];
                reportService.reportTranscript(id, fileName);
            }
            default -> System.out.println("error: unknown report type");
        }
    }

    private static int parseInt(String s, String err) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(err);
        }
    }
}
