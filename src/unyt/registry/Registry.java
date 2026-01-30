package unyt.registry;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Registry {

    private boolean loaded = false;
    private Path baseFolder;

    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, Course> courses = new HashMap<>();
    private final Map<GradeKey, Evaluation> grades = new HashMap<>();

    private final GradingService grading = new GradingService();

    public boolean isLoaded() { return loaded; }
    public Path getBaseFolder() { return baseFolder; }

    // ---------- LOAD ----------
    public void load(String folderPath) {
        if (loaded) {
            System.out.println("error: data already loaded, cannot load again!");
            return;
        }

        DataLoader.LoadResult res;
        try {
            res = DataLoader.loadAll(folderPath);
        } catch (IllegalArgumentException e) {
            // match examples
            if (e.getMessage().equals("invalid folder name")) System.out.println("error: invalid folder name");
            else if (e.getMessage().equals("data files not found")) System.out.println("error: data files not found");
            else System.out.println("error: " + e.getMessage());
            return;
        }

        students.clear();
        courses.clear();
        grades.clear();

        for (Student s : res.students()) {
            if (students.containsKey(s.id())) throw new IllegalArgumentException("duplicate student id in file: " + s.id());
            students.put(s.id(), s);
        }
        for (Course c : res.courses()) {
            if (courses.containsKey(c.code())) throw new IllegalArgumentException("duplicate course code in file: " + c.code());
            courses.put(c.code(), c);
        }
        for (Evaluation e : res.grades()) {
            addGradeInternal(e, false); // strict, no file append during load
        }

        this.loaded = true;
        this.baseFolder = res.folder();

        System.out.printf("loaded %d students, %d courses, and %d grades%n",
                students.size(), courses.size(), grades.size());
    }

    // ---------- FIND ----------
    public String findStudentDisplay(String id) {
        Validation.requireLoaded(loaded);

        Student s = students.get(id);
        if (s == null) return "error: no student found\n";

        StudentStats st = computeStudentStats(id);

        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(s.id()).append("\n");
        sb.append("name: ").append(s.name()).append("\n");
        sb.append("surname: ").append(s.surname()).append("\n");
        sb.append("email: ").append(s.email()).append("\n");
        sb.append("level: ").append(s.level().label()).append("\n");
        sb.append("courses: ").append(st.coursesTaken).append("\n");
        sb.append("credits: ").append(st.totalCredits).append("\n");
        sb.append("gpa: ").append(String.format(Locale.US, "%.2f", st.gpa)).append("\n");
        return sb.toString();
    }

    public String findCourseDisplay(String code) {
        Validation.requireLoaded(loaded);

        Course c = courses.get(code);
        if (c == null) return "error: no course found\n";

        CourseLevel cl = Validation.computeCourseLevel(c.code());

        StringBuilder sb = new StringBuilder();
        sb.append("code: ").append(c.code()).append("\n");
        sb.append("title: ").append(c.title()).append("\n");
        sb.append("credits: ").append(c.credits()).append("\n");
        sb.append("level: ").append(cl.label()).append("\n");
        return sb.toString();
    }

    public String findGradeDisplay(String studentId, String courseCode) {
        Validation.requireLoaded(loaded);

        Evaluation e = grades.get(new GradeKey(studentId, courseCode));
        if (e == null) return "error: no grade found\n";

        Student s = students.get(studentId);
        Course c = courses.get(courseCode);
        if (s == null || c == null) return "error: internal data inconsistency\n";

        LetterGrade lg = grading.letterFor(s.level(), e.numericGrade());

        StringBuilder sb = new StringBuilder();
        sb.append("student: (").append(s.id()).append(" - ").append(s.name()).append(" ").append(s.surname()).append(")\n");
        sb.append("course: (").append(c.code()).append(" - ").append(c.title()).append(", ").append(c.credits()).append(" cr.)\n");
        sb.append("semester: ").append(e.semester()).append("\n");
        sb.append("grade: ").append(e.numericGrade()).append("\n");
        sb.append("lettergrade: ").append(lg.code()).append("\n");
        return sb.toString();
    }

    // ---------- QUERY ----------
    public String queryDisplay(String entity, String criteriaString) {
        Validation.requireLoaded(loaded);

        List<Map<String, String>> rows;
        switch (entity) {
            case "student" -> rows = queryStudents(criteriaString);
            case "course" -> rows = queryCourses(criteriaString);
            case "grade" -> rows = queryGrades(criteriaString);
            default -> { return "error: no such entity\n"; }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(rows.size()).append(" records found\n");

        for (Map<String, String> r : rows) {
            if (entity.equals("student")) {
                sb.append(r.get("id")).append(", ")
                        .append(r.get("name")).append(", ")
                        .append(r.get("surname")).append(", ")
                        .append(r.get("email")).append(", ")
                        .append(r.get("level")).append("\n");
            } else if (entity.equals("course")) {
                sb.append(r.get("code")).append(", ")
                        .append(r.get("title")).append(", ")
                        .append(r.get("credits")).append("\n");
            } else {
                sb.append(r.get("studentID")).append(", ")
                        .append(r.get("courseCode")).append(", ")
                        .append(r.get("semester")).append(", ")
                        .append(r.get("grade")).append("\n");
            }
        }
        return sb.toString();
    }

    private record Criterion(String field, char op, String value) {}

    private List<Criterion> parseCriteria(String criteriaString) {
        if (criteriaString == null || criteriaString.trim().isEmpty()) return List.of();

        String[] tokens = criteriaString.split(", ", -1);
        List<Criterion> criteria = new ArrayList<>();

        for (String t : tokens) {
            String token = t.trim();
            if (token.isEmpty()) continue;

            int eq = token.indexOf('=');
            int til = token.indexOf('~');

            if (eq >= 0 && til >= 0) throw new IllegalArgumentException("invalid criteria: " + token);
            if (eq < 0 && til < 0) throw new IllegalArgumentException("invalid criteria: " + token);

            char op;
            int idx;
            if (eq >= 0) { op = '='; idx = eq; }
            else { op = '~'; idx = til; }

            String field = token.substring(0, idx).trim();
            String value = token.substring(idx + 1).trim();

            if (field.isEmpty()) throw new IllegalArgumentException("invalid criteria: " + token);
            criteria.add(new Criterion(field, op, value));
        }
        return criteria;
    }

    private boolean applyCriterion(String fieldValue, char op, String value) {
        if (op == '=') return fieldValue.equals(value);
        if (op == '~') return fieldValue.contains(value);
        throw new IllegalArgumentException("invalid operator");
    }

    private List<Map<String, String>> queryStudents(String criteriaString) {
        List<Criterion> criteria = parseCriteria(criteriaString);

        List<Student> filtered = students.values().stream()
                .filter(s -> {
                    for (Criterion c : criteria) {
                        String fieldValue = switch (c.field) {
                            case "id" -> s.id();
                            case "name" -> s.name();
                            case "surname" -> s.surname();
                            case "email" -> s.email();
                            case "level" -> s.level().name();
                            default -> throw new IllegalArgumentException("invalid field for student: " + c.field);
                        };
                        if (!applyCriterion(fieldValue, c.op, c.value)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        List<Map<String, String>> rows = new ArrayList<>();
        for (Student s : filtered) {
            Map<String, String> r = new HashMap<>();
            r.put("id", s.id());
            r.put("name", s.name());
            r.put("surname", s.surname());
            r.put("email", s.email());
            r.put("level", s.level().name());
            rows.add(r);
        }
        return rows;
    }

    private List<Map<String, String>> queryCourses(String criteriaString) {
        List<Criterion> criteria = parseCriteria(criteriaString);

        List<Course> filtered = courses.values().stream()
                .filter(c0 -> {
                    for (Criterion c : criteria) {
                        String fieldValue = switch (c.field) {
                            case "code" -> c0.code();
                            case "title" -> c0.title();
                            case "credits" -> String.valueOf(c0.credits());
                            default -> throw new IllegalArgumentException("invalid field for course: " + c.field);
                        };
                        if (!applyCriterion(fieldValue, c.op, c.value)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        List<Map<String, String>> rows = new ArrayList<>();
        for (Course c : filtered) {
            Map<String, String> r = new HashMap<>();
            r.put("code", c.code());
            r.put("title", c.title());
            r.put("credits", String.valueOf(c.credits()));
            rows.add(r);
        }
        return rows;
    }

    private List<Map<String, String>> queryGrades(String criteriaString) {
        List<Criterion> criteria = parseCriteria(criteriaString);

        List<Evaluation> filtered = grades.values().stream()
                .filter(e -> {
                    for (Criterion c : criteria) {
                        String fieldValue = switch (c.field) {
                            case "studentID" -> e.studentId();
                            case "courseCode" -> e.courseCode();
                            case "semester" -> e.semester();
                            case "grade" -> String.valueOf(e.numericGrade());
                            default -> throw new IllegalArgumentException("invalid field for grade: " + c.field);
                        };
                        if (!applyCriterion(fieldValue, c.op, c.value)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());

        List<Map<String, String>> rows = new ArrayList<>();
        for (Evaluation e : filtered) {
            Map<String, String> r = new HashMap<>();
            r.put("studentID", e.studentId());
            r.put("courseCode", e.courseCode());
            r.put("semester", e.semester());
            r.put("grade", String.valueOf(e.numericGrade()));
            rows.add(r);
        }
        return rows;
    }

    // ---------- ADD ----------
    public void addStudentFromCli(String values) {
        Validation.requireLoaded(loaded);

        String[] p = values.split(", ", -1);
        if (p.length != 5) throw new IllegalArgumentException("invalid number of fields for student");

        String id = p[0].trim();
        String name = p[1].trim();
        String surname = p[2].trim();
        String email = p[3]; // may be empty
        String levelStr = p[4].trim();

        Validation.validateStudentId(id);
        Validation.validateNonEmpty(name, "name");
        Validation.validateNonEmpty(surname, "surname");
        Validation.validateEmailOptional(email);
        Level level = Validation.parseLevel(levelStr);

        if (students.containsKey(id)) {
            System.out.println("error: student with id " + id + " is already present");
            return;
        }

        Student s = new Student(id, name, surname, email.trim(), level);
        students.put(id, s);
        DataWriter.appendStudent(baseFolder, s);

        System.out.println("1 record added");
    }

    public void addCourseFromCli(String values) {
        Validation.requireLoaded(loaded);

        String[] p = values.split(", ", -1);
        if (p.length != 3) throw new IllegalArgumentException("invalid number of fields for course");

        String code = p[0].trim();
        String title = p[1].trim();
        String creditsStr = p[2].trim();

        Validation.validateCourseCode(code);
        Validation.validateNonEmpty(title, "title");
        int credits = Validation.parseCredits(creditsStr);

        if (courses.containsKey(code)) {
            System.out.println("error: course with code " + code + " is already present");
            return;
        }

        Course c = new Course(code, title, credits);
        courses.put(code, c);
        DataWriter.appendCourse(baseFolder, c);

        System.out.println("1 record added");
    }

    public void addGradeFromCli(String values) {
        Validation.requireLoaded(loaded);

        String[] p = values.split(", ", -1);
        if (p.length != 4) throw new IllegalArgumentException("invalid number of fields for grade");

        String studentId = p[0].trim();
        String courseCode = p[1].trim();
        String semester = p[2].trim();
        int numericGrade = Validation.parseNumericGrade(p[3]);

        Validation.validateStudentId(studentId);
        Validation.validateCourseCode(courseCode);
        Validation.validateSemester(semester);

        if (!students.containsKey(studentId)) {
            System.out.println("error: no student found");
            return;
        }
        if (!courses.containsKey(courseCode)) {
            System.out.println("error: no course found");
            return;
        }

        GradeKey key = new GradeKey(studentId, courseCode);
        if (grades.containsKey(key)) {
            System.out.println("error: grade for (" + studentId + ", " + courseCode + ") is already present");
            return;
        }

        Student s = students.get(studentId);
        CourseLevel cl = Validation.computeCourseLevel(courseCode);
        if (s.level() == Level.G && cl == CourseLevel.UNDERGRADUATE) {
            System.out.println("error: graduate student may not take undergraduate course");
            return;
        }

        Evaluation e = new Evaluation(studentId, courseCode, semester, numericGrade);
        grades.put(key, e);
        DataWriter.appendGrade(baseFolder, e);

        System.out.println("1 record added");
    }

    // strict internal add used by load
    private void addGradeInternal(Evaluation e, boolean writeToFile) {
        Validation.validateStudentId(e.studentId());
        Validation.validateCourseCode(e.courseCode());
        Validation.validateSemester(e.semester());
        if (e.numericGrade() < 0 || e.numericGrade() > 100) throw new IllegalArgumentException("invalid grade");

        Student s = students.get(e.studentId());
        if (s == null) throw new IllegalArgumentException("grade references missing student: " + e.studentId());
        Course c = courses.get(e.courseCode());
        if (c == null) throw new IllegalArgumentException("grade references missing course: " + e.courseCode());

        GradeKey key = new GradeKey(e.studentId(), e.courseCode());
        if (grades.containsKey(key)) throw new IllegalArgumentException("duplicate grade key: (" + e.studentId() + ", " + e.courseCode() + ")");

        CourseLevel cl = Validation.computeCourseLevel(e.courseCode());
        if (s.level() == Level.G && cl == CourseLevel.UNDERGRADUATE) {
            throw new IllegalArgumentException("invalid grade: graduate student in undergraduate course: " + e.courseCode());
        }

        grades.put(key, e);
        if (writeToFile) DataWriter.appendGrade(baseFolder, e);
    }

    // ---------- COMPUTATIONS ----------
    private static class StudentStats {
        final int coursesTaken;
        final int totalCredits;
        final double gpa;

        StudentStats(int coursesTaken, int totalCredits, double gpa) {
            this.coursesTaken = coursesTaken;
            this.totalCredits = totalCredits;
            this.gpa = gpa;
        }
    }

    private StudentStats computeStudentStats(String studentId) {
        Student s = students.get(studentId);
        if (s == null) return new StudentStats(0, 0, 0.0);

        double totalPoints = 0.0;
        int totalCredits = 0;
        int coursesTaken = 0;

        for (Evaluation e : grades.values()) {
            if (!e.studentId().equals(studentId)) continue;
            Course c = courses.get(e.courseCode());
            if (c == null) continue;

            LetterGrade lg = grading.letterFor(s.level(), e.numericGrade());
            totalPoints += lg.points() * c.credits();
            totalCredits += c.credits();
            coursesTaken++;
        }

        double gpa = totalCredits == 0 ? 0.0 : GradingService.round2(totalPoints / totalCredits);
        return new StudentStats(coursesTaken, totalCredits, gpa);
    }

    // ---------- REPORT DATA ----------
    public record StudentRow(Student student, int totalCredits, double gpa) {}
    public record CourseRow(Course course, CourseLevel level, int gradeCount) {}

    public List<StudentRow> topStudents(int n) {
        Validation.requireLoaded(loaded);

        List<StudentRow> rows = new ArrayList<>();
        for (Student s : students.values()) {
            StudentStats st = computeStudentStats(s.id());
            rows.add(new StudentRow(s, st.totalCredits, st.gpa));
        }
        rows.sort((a, b) -> {
            int g = Double.compare(b.gpa(), a.gpa());
            if (g != 0) return g;
            int c = Integer.compare(b.totalCredits(), a.totalCredits());
            if (c != 0) return c;
            return a.student().id().compareTo(b.student().id());
        });

        return rows.subList(0, Math.min(n, rows.size()));
    }

    public List<CourseRow> topCourses(int n) {
        Validation.requireLoaded(loaded);

        Map<String, Integer> count = new HashMap<>();
        for (Evaluation e : grades.values()) {
            count.merge(e.courseCode(), 1, Integer::sum);
        }

        List<CourseRow> rows = new ArrayList<>();
        for (Course c : courses.values()) {
            CourseLevel cl = Validation.computeCourseLevel(c.code());
            rows.add(new CourseRow(c, cl, count.getOrDefault(c.code(), 0)));
        }

        rows.sort((a, b) -> {
            int cc = Integer.compare(b.gradeCount(), a.gradeCount());
            if (cc != 0) return cc;
            return a.course().code().compareTo(b.course().code());
        });

        return rows.subList(0, Math.min(n, rows.size()));
    }

    public Transcript transcript(String studentId) {
        Validation.requireLoaded(loaded);

        Student s = students.get(studentId);
        if (s == null) return null;

        Map<String, List<TranscriptLine>> bySem = new HashMap<>();

        for (Evaluation e : grades.values()) {
            if (!e.studentId().equals(studentId)) continue;

            Course c = courses.get(e.courseCode());
            if (c == null) continue;

            LetterGrade lg = grading.letterFor(s.level(), e.numericGrade());
            double gp = lg.points() * c.credits();

            TranscriptLine line = new TranscriptLine(
                    e.semester(),
                    c.code(),
                    c.title(),
                    c.credits(),
                    e.numericGrade(),
                    lg.code(),
                    GradingService.round2(gp)
            );

            bySem.computeIfAbsent(e.semester(), k -> new ArrayList<>()).add(line);
        }

        List<String> semesters = new ArrayList<>(bySem.keySet());
        semesters.sort(Registry::compareSemester);

        List<TranscriptSemester> blocks = new ArrayList<>();
        for (String sem : semesters) {
            List<TranscriptLine> lines = bySem.get(sem);
            lines.sort(Comparator.comparing(TranscriptLine::courseCode));
            blocks.add(new TranscriptSemester(sem, lines));
        }

        return new Transcript(s, blocks);
    }

    private static int compareSemester(String a, String b) {
        int ay = Integer.parseInt(a.substring(a.length() - 4));
        int by = Integer.parseInt(b.substring(b.length() - 4));
        if (ay != by) return Integer.compare(ay, by);

        boolean aSpring = a.startsWith("Spring");
        boolean bSpring = b.startsWith("Spring");
        if (aSpring == bSpring) return 0;
        return aSpring ? -1 : 1; // Spring before Fall
    }

    public record Transcript(Student student, List<TranscriptSemester> semesters) {}
    public record TranscriptSemester(String semester, List<TranscriptLine> lines) {}
    public record TranscriptLine(String semester, String courseCode, String courseTitle, int credits,
                                 int numericGrade, String letterGrade, double gradePoints) {}
}
