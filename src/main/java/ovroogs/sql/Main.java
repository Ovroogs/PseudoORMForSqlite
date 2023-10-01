package ovroogs.sql;

import ovroogs.sql.annotation.Database;
import ovroogs.sql.database.DatabaseClass;
import ovroogs.sql.database.Table;
import ovroogs.sql.example.AuthorTable;
import ovroogs.sql.example.Library;

public class Main {
    public static void main(String[] args) {
        DatabaseClass.setConfig(Library.class);
        var author = new AuthorTable(1L, "felz", "fez");
        if (DatabaseClass.connect()) {
//            Table.insert(author);
            Table.update(author);
        }
    }
}