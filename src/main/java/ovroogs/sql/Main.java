package ovroogs.sql;

import ovroogs.sql.database.DatabaseClass;
import ovroogs.sql.database.Table;
import ovroogs.sql.example.AuthorTable;
import ovroogs.sql.example.Library;
import ovroogs.sql.exception.TypeException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseClass.setConfig(Library.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        var authors = new ArrayList<AuthorTable>();
        if (DatabaseClass.connect()) {
//            Table.insert(author);
            try {
//                Table.insert(author);
//                Table.insert(author2);
//                Table.insert(author3);
                var pairs = new HashMap<String, String>();
                pairs.put("name", "fel");
                pairs.put("surname", "fez1");

                var result = Table.selectByFields(AuthorTable.class, pairs);

                while (result.next()) {
                    authors.add(new AuthorTable(result));
                }
            } catch (SQLException | TypeException e) {
                throw new RuntimeException(e);
            }
        }
    }
}