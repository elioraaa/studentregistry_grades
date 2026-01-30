package unyt.registry;

public final class Validation {
    private Validation() {}

    public static void requireLoaded(boolean loaded) {
        if (!loaded) throw new IllegalArgumentException("data not loaded");
    }

    public static void validateStudentId(String id) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("missing student ID");
        if (!id.trim().matches("\\d+")) throw new IllegalArgumentException("invalid student ID");
    }

    public static void validateNonEmpty(String v, String fieldName) {
        if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException("missing " + fieldName);
    }

    public static void validateEmailOptional(String email) {
        if (email == null) return;
        String e = email.trim();
        if (e.isEmpty()) return;
        int at = e.indexOf('@');
        int dot = e.lastIndexOf('.');
        if (at <= 0 || dot <= at + 1 || dot >= e.length() - 1) {
            throw new IllegalArgumentException("invalid email");
        }
    }

    public static Level parseLevel(String s) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException("missing level");
        String t = s.trim();
        if (t.equals("UG")) return Level.UG;
        if (t.equals("G")) return Level.G;
        throw new IllegalArgumentException("invalid level");
    }

    public static void validateCourseCode(String code) {
        if (code == null || code.trim().isEmpty()) throw new IllegalArgumentException("missing course code");
        String c = code.trim();
        if (!c.matches("[A-Z]{2,4}\\d{3}")) throw new IllegalArgumentException("invalid course code");
    }

    public static CourseLevel computeCourseLevel(String code) {
        validateCourseCode(code);
        String c = code.trim();
        String num = c.substring(c.length() - 3);
        char first = num.charAt(0);
        if (first >= '1' && first <= '4') return CourseLevel.UNDERGRADUATE;
        if (first >= '5' && first <= '9') return CourseLevel.GRADUATE;
        throw new IllegalArgumentException("invalid course code");
    }

    public static int parseCredits(String s) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException("credits field missing");
        int v;
        try {
            v = Integer.parseInt(s.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid credits");
        }
        if (!(v == 2 || v == 3 || v == 4)) throw new IllegalArgumentException("invalid credits");
        return v;
    }

    public static void validateSemester(String semester) {
        if (semester == null || semester.trim().isEmpty()) throw new IllegalArgumentException("missing semester");
        String s = semester.trim();
        if (!s.matches("(Spring|Fall)\\d{4}")) throw new IllegalArgumentException("invalid semester");
    }

    public static int parseNumericGrade(String s) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException("missing grade");
        int g;
        try {
            g = Integer.parseInt(s.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid grade");
        }
        if (g < 0 || g > 100) throw new IllegalArgumentException("invalid grade");
        return g;
    }
}
