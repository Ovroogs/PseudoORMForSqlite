package ovroogs.sql;

import ovroogs.sql.annotation.Database;
import ovroogs.sql.database.DatabaseClass;
import ovroogs.sql.example.Library;

public class Main {
    public static void main(String[] args) {
        DatabaseClass.setConfig(Library.class);
    }
}