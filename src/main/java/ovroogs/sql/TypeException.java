package ovroogs.sql;

public class TypeException extends Exception {
    public TypeException() {
        super("Value type does not match column type");
    }
}