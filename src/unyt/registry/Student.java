package unyt.registry;

public class Student {
    private final String id;
    private final String name;
    private final String surname;
    private final String email; // optional, may be ""
    private final Level level;

    public Student(String id, String name, String surname, String email, Level level) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email == null ? "" : email;
        this.level = level;
    }

    public String id() { return id; }
    public String name() { return name; }
    public String surname() { return surname; }
    public String email() { return email; }
    public Level level() { return level; }
}
