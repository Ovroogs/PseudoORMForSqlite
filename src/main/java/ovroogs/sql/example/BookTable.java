package ovroogs.sql.example;

import ovroogs.sql.annotation.*;

@Entity(name = "book", foreignKeys = {
        @ForeignKey(
                targetEntity = AuthorTable.class, internalColumn = "id_author",
                externalColumn = "id", update = ActionType.CASCADE, delete = ActionType.CASCADE
        )
})
public class BookTable {
    @PrimaryKey
    @Column(type = ColumnType.INTEGER)
    private long id;
    @Column(type = ColumnType.TEXT, notNull = true, unique = true)
    private String name;
    @Column(name = "id_author",type = ColumnType.INTEGER)
    private long idAuthor;
}