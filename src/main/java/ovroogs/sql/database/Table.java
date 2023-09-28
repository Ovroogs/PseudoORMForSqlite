package ovroogs.sql.database;

import ovroogs.sql.annotation.Column;
import ovroogs.sql.annotation.Entity;
import ovroogs.sql.annotation.PrimaryKey;

import java.sql.SQLException;
import java.sql.Statement;

public class Table {
    protected static void create(Class<?> table, Statement statement) {
        if (!table.isAnnotationPresent(Entity.class)) return;

        var entity = table.getAnnotation(Entity.class);
        var tableName = entity.name().isEmpty() ? table.getSimpleName() : entity.name();
        var query = new StringBuilder("CREATE TABLE if not exists ").append(tableName).append('(');

        var fields = table.getDeclaredFields();
        var primary = false;

        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            if (!field.isAnnotationPresent(Column.class)) continue;

            var column = field.getAnnotation(Column.class);
            var columnName = column.name().isEmpty() ? field.getName() : column.name();

            var type = column.type();

            query.append("\n").append(columnName).append(' ').append(type);

            var isPrimary = field.isAnnotationPresent(PrimaryKey.class) && !primary;
            if (isPrimary) {
                query.append(" PRIMARY KEY ");

                if (field.getAnnotation(PrimaryKey.class).autoincrement()) query.append("AUTOINCREMENT ");
                primary = true;
            }
            else {
                if (column.notNull()) query.append(" NOT NULL ");
                if (column.unique()) query.append(" UNIQUE ");
            }

            query.append(i != fields.length - 1 ? "," : "");
        }


        var foreignKeys = entity.foreignKeys();

        for(var i = 0; i < foreignKeys.length; i++) {
            var foreignKey = foreignKeys[i];
            var target = foreignKey.targetEntity().getSimpleName();
            var name = foreignKey.targetEntity().getAnnotation(Entity.class).name();

            if (name.isEmpty()) name = target;

            query.append(", \nFOREIGN KEY (").append(foreignKey.internalColumn()).append(") REFERENCES ").append(name)
            .append(" (").append(foreignKey.externalColumn()).append(") ON DELETE ").append(foreignKey.delete().getAction())
            .append(" ON UPDATE ").append(foreignKey.update().getAction()).append(i != foreignKeys.length - 1 ? ", \n" : "\n");
        }

        var uniqueConstraints = entity.uniqueConstraints();

        for (var uniqueConstraint : uniqueConstraints) {
            var unique = new StringBuilder(", UNIQUE(");
            var columnNames = uniqueConstraint.columnNames();

            for (var i = 0; i < columnNames.length; i++) {
                unique.append(columnNames[i]).append(i != columnNames.length - 1 ? ", " : ")");
            }

            query.append(unique);
        }

        query.append(");");

        try {
            System.out.println(query);
            statement.execute(query.toString());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}