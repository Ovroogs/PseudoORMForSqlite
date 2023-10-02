package ovroogs.sql.example;

import ovroogs.sql.annotation.*;

@Entity(name = "author", uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "surname" }) })
public class AuthorTable {
    @PrimaryKey(autoincrement = true)
    @Column(type = ColumnType.INTEGER)
    private long id;
    @DefaultString("ne_komar")
    @Column(type = ColumnType.TEXT)
    private String name;
    @DefaultString
    @Column(type = ColumnType.TEXT)
    private String surname;

    public AuthorTable() {}

    public AuthorTable(long id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }
}