package ovroogs.sql.database;

import ovroogs.sql.TypeException;
import ovroogs.sql.annotation.*;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class Table {
    protected static void create(Class<?> table) throws SQLException {
        var entityClass = Entity.class;
        if (!table.isAnnotationPresent(entityClass)) return;
        System.out.println();

        var entity = table.getAnnotation(entityClass);
        var tableName = entity.name().isEmpty() ? table.getSimpleName() : entity.name();
        var query = new StringBuilder("CREATE TABLE if not exists ").append(tableName).append('(');

        var fields = Arrays.stream(table.getDeclaredFields()).filter(item -> item.isAnnotationPresent(Column.class)).toList();
        var length = fields.size();
        var primary = false;

        for (int i = 0; i < length; i++) {
            var field = fields.get(i);

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

            var defaultStr = DefaultString.class;
            var defaultReal = DefaultReal.class;
            var defaultInt = DefaultInteger.class;

            switch (column.type()){
                case INTEGER -> {
                    if (field.isAnnotationPresent(defaultStr) && field.isAnnotationPresent(defaultReal))
                        throw new IllegalStateException("Unexpected value: " + column.type());

                    if (field.isAnnotationPresent(defaultInt))
                        query.append(" DEFAULT ").append(field.getAnnotation(defaultInt).value());
                }
                case REAL -> {
                    if (field.isAnnotationPresent(defaultStr) && field.isAnnotationPresent(defaultInt))
                        throw new IllegalStateException("Unexpected value: " + column.type());

                    if (field.isAnnotationPresent(defaultReal))
                        query.append(" DEFAULT ").append(field.getAnnotation(defaultReal).value());
                }
                case TEXT -> {
                    if (field.isAnnotationPresent(defaultReal) && field.isAnnotationPresent(defaultInt))
                        throw new IllegalStateException("Unexpected value: " + column.type());

                    if (field.isAnnotationPresent(defaultStr))
                        query.append(" DEFAULT ").append('\'').append(field.getAnnotation(defaultStr).value()).append('\'');
                }
                case NUMERIC, BLOB -> { }
                default -> throw new IllegalStateException("Unexpected value: " + column.type());
            }
            query.append(i != length - 1 ? "," : "");
        }

        var foreignKeys = entity.foreignKeys();
        length = foreignKeys.length;

        for(var i = 0; i < length; i++) {
            var foreignKey = foreignKeys[i];
            var target = foreignKey.targetEntity().getSimpleName();
            var name = foreignKey.targetEntity().getAnnotation(entityClass).name();

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
        System.out.println();

        System.out.println(query);
        DatabaseClass.getStatement().execute(query.toString());
    }

    public static ResultSet select(Class<?> table) throws SQLException {
        if (!table.isAnnotationPresent(Entity.class)) return null;

        var name = table.getAnnotation(Entity.class).name();
        if (name.isEmpty()) name = table.getName();

        ResultSet result;
        result = DatabaseClass.getStatement().executeQuery("SELECT * FROM " + name + ";");

        return result;
    }

    // сделать selectByField(Class<?> table, Hashmap nameFields)

    public static <T> ResultSet selectByField(Class<?> table, String nameField, T value) throws TypeException, SQLException {
        if (!table.isAnnotationPresent(Entity.class)) return null;

        var name = table.getAnnotation(Entity.class).name();
        if (name.isEmpty()) name = table.getName();

        var fields = Arrays.stream(table.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Column.class)).toList();

        var existColumn = false;

        for (var field : fields) {
            var column = field.getAnnotation(Column.class);
            var columnName = column.name();
            var columnType = column.type();

            if (columnName.isEmpty()) columnName = field.getName();

            if (columnName.equals(nameField)) {
                if (!(value instanceof String) && !(value instanceof Number)) throw new TypeException();

                switch (columnType) {
                    case TEXT -> {
                        if (!(value instanceof String)) throw new TypeException();
                    }
                    case INTEGER -> {
                        if ((value instanceof String || value instanceof Double || value instanceof Float))
                            throw new TypeException();
                    }
                    case REAL -> {
                        if (value instanceof String) throw new TypeException();
                    }
                    default -> throw new TypeException();
                }

                existColumn = true;
                break;
            }
        }

        if (!existColumn) return null;

        ResultSet result;
        String query;

        if (value instanceof String) query = "SELECT * FROM " + name + " WHERE " + nameField + " = '" + value +"';";
        else query = "SELECT * FROM " + name + " WHERE " + nameField + " = " + value +";";

        result = DatabaseClass.getStatement().executeQuery(query);

        return result;
    }

    public static <T> boolean insert(T table) throws IllegalAccessException, SQLException {
        var data = table.getClass();
        if (!data.isAnnotationPresent(Entity.class)) return false;

        var name = data.getAnnotation(Entity.class).name();
        if (name.isEmpty()) name = data.getName();

        var query = new StringBuilder("INSERT INTO ").append(name).append(" ");

        var columns = new StringBuilder();
        var values = new StringBuilder();

        var fields = Arrays.stream(data.getDeclaredFields()).filter(item -> item.isAnnotationPresent(Column.class))
                .filter(item -> !(item.isAnnotationPresent(PrimaryKey.class) && item.getAnnotation(PrimaryKey.class).autoincrement())).toList();
        var length = fields.size();

        for (var i = 0; i < length; i++) {
            var field = fields.get(i);
            var column = field.getAnnotation(Column.class);
            var columnName = column.name();
            if (columnName.isEmpty()) columnName = field.getName();

            columns.append(columnName);

            var value = getValue(table, field, column);

            if (column.type().equals(ColumnType.TEXT) && value != null) values.append('\'').append(value).append('\'');
            else values.append(value);

            columns.append(i != length - 1 ? "," : "");
            values.append(i != length - 1 ? "," : "");
        }
        query.append("(").append(columns).append(") VALUES (").append(values).append(")");
        System.out.println(query);
        DatabaseClass.getStatement().execute(query.toString());

        return true;
    }
    public static <T> boolean update(T table) throws IllegalAccessException, SQLException {
        var data = table.getClass();
        if (!data.isAnnotationPresent(Entity.class)) return false;

        var name = data.getAnnotation(Entity.class).name();
        if (name.isEmpty()) name = data.getName();

        var query = new StringBuilder("Update [").append(name).append("] SET \n");
        var where = new StringBuilder();
        var fields = Arrays.stream(data.getDeclaredFields()).filter(item -> item.isAnnotationPresent(Column.class)).toList();

        for (var field : fields) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                var column = field.getAnnotation(Column.class);
                var columnName = column.name();
                if (columnName.isEmpty()) columnName = field.getName();

                var value = getValue(table, field, column);

                where.append("Where ");
                if (column.type().equals(ColumnType.TEXT) && value != null) where.append(columnName).append(" = ").append('\'').append(value).append('\'');
                else where.append(columnName).append(" = ").append(value);
            }
        }

        var primary = PrimaryKey.class;
        fields = fields.stream().filter(item -> !(item.isAnnotationPresent(primary) && item.getAnnotation(primary).autoincrement())).toList();

        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            var column = field.getAnnotation(Column.class);
            var columnName = column.name();
            if (columnName.isEmpty()) columnName = field.getName();

            field.setAccessible(true);

            var value = field.get(table);

            if (column.type().equals(ColumnType.TEXT) && value != null) query.append(columnName).append(" = ").append('\'').append(value).append('\'');
            else query.append(columnName).append(" = ").append(value);

            query.append(i != fields.size() - 1 ? ", \n" : "\n");

            field.setAccessible(false);
        }

        query.append(where);

        query.append(";");

        System.out.println(query);
        DatabaseClass.getStatement().execute(query.toString());

        return true;
    }

    private static <T> Object getValue(T table, Field field, Column column) throws IllegalAccessException {
        field.setAccessible(true);

        var value = field.get(table);

        var defaultStr = DefaultString.class;
        var defaultReal = DefaultReal.class;
        var defaultInt = DefaultInteger.class;

        field.setAccessible(false);

        if (column.notNull()) {
            switch (column.type()){
                case INTEGER -> {
                    if (field.isAnnotationPresent(defaultInt)) value = field.getAnnotation(defaultInt).value();
                    else throw new IllegalStateException("null value is not accepted");
                }
                case REAL -> {
                    if (field.isAnnotationPresent(defaultReal)) value = field.getAnnotation(defaultReal).value();
                    else throw new IllegalStateException("null value is not accepted");
                }
                case TEXT -> {
                    if (field.isAnnotationPresent(defaultStr)) value = field.getAnnotation(defaultStr).value();
                    else throw new IllegalStateException("null value is not accepted");
                }
                case NUMERIC, BLOB -> { }
                default -> throw new IllegalStateException("Unexpected value: " + column.type());
            }
        }

        return value;
    }

    public static <T> ResultSet delete(Class<?> table, T id) throws SQLException {
        if (!table.isAnnotationPresent(Entity.class)) return null;

        var name = table.getAnnotation(Entity.class).name();
        if (name.isEmpty()) name = table.getName();

        ResultSet result;
        var query = new StringBuilder("DELETE FROM ").append(name).append(" WHERE id = ");
        if (id == null) throw new IllegalStateException("Unexpected value");
        if (id instanceof String) query.append('\'').append(id).append('\'');
        else query.append(id);

        query.append(";");

        result = DatabaseClass.getStatement().executeQuery(query.toString());

        return result;
    }
}