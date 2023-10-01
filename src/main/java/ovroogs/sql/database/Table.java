package ovroogs.sql.database;

import ovroogs.sql.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Table {
    protected static void create(Class<?> table) {
        if (!table.isAnnotationPresent(Entity.class)) return;

        var entity = table.getAnnotation(Entity.class);
        var tableName = entity.name().isEmpty() ? table.getSimpleName() : entity.name();
        var query = new StringBuilder("CREATE TABLE if not exists ").append(tableName).append('(');

        var fields = table.getDeclaredFields();
        var length = fields.length;
        var primary = false;

        for (int i = 0; i < length; i++) {
            var field = fields[i];
            if (!field.isAnnotationPresent(Column.class)) continue;

            var column = field.getAnnotation(Column.class);
            var columnName = column.name();
            if (columnName.isEmpty()) columnName = field.getName();

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

            query.append(i != length - 1 ? "," : "");
        }

        var foreignKeys = entity.foreignKeys();
        length = foreignKeys.length;

        for(var i = 0; i < length; i++) {
            var foreignKey = foreignKeys[i];
            var target = foreignKey.targetEntity().getSimpleName();
            var name = foreignKey.targetEntity().getAnnotation(Entity.class).name();

            if (name.isEmpty()) name = target;

            query.append(", \nFOREIGN KEY (").append(foreignKey.internalColumn()).append(") REFERENCES ").append(name)
            .append(" (").append(foreignKey.externalColumn()).append(") ON DELETE ").append(foreignKey.delete().getAction())
            .append(" ON UPDATE ").append(foreignKey.update().getAction()).append(i != length - 1 ? ", \n" : "\n");
        }

        var uniqueConstraints = entity.uniqueConstraints();

        for (var uniqueConstraint : uniqueConstraints) {
            var unique = new StringBuilder(", UNIQUE(");
            var columnNames = uniqueConstraint.columnNames();
            length = columnNames.length;

            for (var i = 0; i < length; i++) {
                unique.append(columnNames[i]).append(i != length - 1 ? ", " : ")");
            }

            query.append(unique);
        }

        query.append(");");

        try {
            System.out.println(query);
            DatabaseClass.getStatement().execute(query.toString());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ResultSet select(Class<?> table) {
        if (!table.isAnnotationPresent(Entity.class)) return null;

        var name = table.getAnnotation(Entity.class).name();
        if (name.isEmpty()) name = table.getName();

        ResultSet result;
        try {
            result = DatabaseClass.getStatement().executeQuery("SELECT * FROM " + name + ";");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static <T> boolean insert(T table) {
        var data = table.getClass();
        if (!data.isAnnotationPresent(Entity.class)) return false;

        var name = data.getAnnotation(Entity.class).name();
        if (name.isEmpty()) name = data.getName();

        var query = new StringBuilder("INSERT INTO [").append(name).append("] ");
        var fields = data.getDeclaredFields();
        var colums = new StringBuilder();
        var values = new StringBuilder();
        var length = fields.length;

        for (var i = 0; i < length; i++) {
            var field = fields[i];
            try {
                if (!field.isAnnotationPresent(Column.class)) continue;
                if (field.isAnnotationPresent(PrimaryKey.class) && field.getAnnotation(PrimaryKey.class).autoincrement()) continue;

                var column = field.getAnnotation(Column.class);
                var columnName = column.name();
                if (columnName.isEmpty()) columnName = field.getName();

                field.setAccessible(true);

                var value = field.get(table);

                colums.append(columnName);

                if (column.type().equals(ColumnType.TEXT)) values.append('\'').append(value).append('\'');
                else values.append(value);

                colums.append(i != length - 1 ? "," : "");
                values.append(i != length - 1 ? "," : "");

                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                System.out.println(e.getMessage());
            }
        }
        query.append("(").append(colums).append(") VALUES (").append(values).append(")");
        try {
            System.out.println(query);
            DatabaseClass.getStatement().execute(query.toString());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return true;
    }

    public static <T> boolean update(T table) {
        var data = table.getClass();
        if (!data.isAnnotationPresent(Entity.class)) return false;

        var name = data.getAnnotation(Entity.class).name();
        if (name.isEmpty()) name = data.getName();

        var query = new StringBuilder("Update [").append(name).append("] SET \n");
        var where = new StringBuilder();
        var fields = data.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            try {
                if (!field.isAnnotationPresent(Column.class)) continue;

                var column = field.getAnnotation(Column.class);
                var columnName = column.name();
                if (columnName.isEmpty()) columnName = field.getName();

                field.setAccessible(true);

                var value = field.get(table);

                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    where.append("Where ");
                    if (column.type().equals(ColumnType.TEXT)) where.append(columnName).append(" = ").append('\'').append(value).append('\'');
                    else where.append(columnName).append(" = ").append(value);

                    if (field.getAnnotation(PrimaryKey.class).autoincrement()) continue;
                }

                if (column.type().equals(ColumnType.TEXT)) query.append(columnName).append(" = ").append('\'').append(value).append('\'');
                else query.append(columnName).append(" = ").append(value);

                query.append(i != fields.length - 1 ? ", \n" : "\n");

                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                System.out.println(e.getMessage());
            }
        }

        query.append(where);

        query.append(";");

        try {
            System.out.println(query);
            DatabaseClass.getStatement().execute(query.toString());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return true;
    }

    public static <T> ResultSet delete(Class<?> table, T id) {
        if (!table.isAnnotationPresent(Entity.class)) return null;

        var name = table.getAnnotation(Entity.class).name();
        if (name.isEmpty()) name = table.getName();

        ResultSet result;
        try {
            var query = new StringBuilder("DELETE FROM ").append(name).append(" WHERE id = ");

            if (id instanceof String) query.append('\'').append(id).append('\'');
            else query.append(id);

            query.append(";");

            result = DatabaseClass.getStatement().executeQuery(query.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}