package ovroogs.sql.example;

import ovroogs.sql.annotation.*;

@Entity(name = "author", uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "surname" }) })
public class AuthorTable {
    @PrimaryKey(autoincrement = true)
    @Column(type = ColumnType.INTEGER)
    private long id;
    @Column(type = ColumnType.TEXT)
    private String name;
    @Column(type = ColumnType.TEXT)
    private String surname;
}