package ovroogs.sql.annotation;

public enum ActionType {
    NO_ACTION("NO ACTION"),
    SET_NULL("SET NULL"),
    SET_DEFAULT("SET DEFAULT"),
    CASCADE("CASCADE"),
    RESTRICT("RESTRICT");

    private String _action;

    ActionType(String action) {
        _action = action;
    }

    public String getAction() {
        return _action;
    }
}