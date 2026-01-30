package unyt.registry;

public class GradingService {

    public static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    public LetterGrade letterFor(Level level, int numeric) {
        if (numeric < 0 || numeric > 100) throw new IllegalArgumentException("invalid grade");

        if (level == Level.UG) {
            if (numeric >= 95) return LetterGrade.A;
            if (numeric >= 90) return LetterGrade.A_MINUS;
            if (numeric >= 87) return LetterGrade.B_PLUS;
            if (numeric >= 84) return LetterGrade.B;
            if (numeric >= 80) return LetterGrade.B_MINUS;
            if (numeric >= 77) return LetterGrade.C_PLUS;
            if (numeric >= 74) return LetterGrade.C;
            if (numeric >= 70) return LetterGrade.C_MINUS;
            if (numeric >= 67) return LetterGrade.D_PLUS;
            if (numeric >= 64) return LetterGrade.D;
            if (numeric >= 60) return LetterGrade.D_MINUS;
            return LetterGrade.F;
        } else {
            if (numeric >= 90) return LetterGrade.A;
            if (numeric >= 85) return LetterGrade.B_PLUS;
            if (numeric >= 80) return LetterGrade.B;
            if (numeric >= 75) return LetterGrade.C;
            return LetterGrade.F;
        }
    }
}
