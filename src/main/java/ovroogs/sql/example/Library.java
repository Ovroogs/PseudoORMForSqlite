package ovroogs.sql.example;

import ovroogs.sql.annotation.Database;

@Database(name = "library")
public class Library {
    private AuthorTable author;
    private BookTable book;
}
