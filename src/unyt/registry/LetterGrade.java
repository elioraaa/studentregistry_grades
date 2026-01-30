package unyt.registry;

public enum LetterGrade {
    F("F", 0.00),
    D_MINUS("D-", 0.67),
    D("D", 1.00),
    D_PLUS("D+", 1.33),
    C_MINUS("C-", 1.67),
    C("C", 2.00),
    C_PLUS("C+", 2.33),
    B_MINUS("B-", 2.67),
    B("B", 3.00),
    B_PLUS("B+", 3.33),
    A_MINUS("A-", 3.67),
    A("A", 4.00);

    private final String code;
    private final double points;

    LetterGrade(String code, double points) {
        this.code = code;
        this.points = points;
    }

    public String code() {
        return code;
    }

    public double points() {
        return points;
    }
}
