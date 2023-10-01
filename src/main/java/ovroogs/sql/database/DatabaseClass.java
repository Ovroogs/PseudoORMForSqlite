package ovroogs.sql.database;

import org.sqlite.SQLiteConfig;
import ovroogs.sql.annotation.Database;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.Arrays;

public class DatabaseClass {
    private static String connectionString;

    private static Connection _connection;
    private static Statement _statement;
    private static ResultSet _result;

    private static final SQLiteConfig _config = new SQLiteConfig();

    // --------ПОДКЛЮЧЕНИЕ К БАЗЕ ДАННЫХ--------
    public static boolean connect() {
        if (connectionString == null && connectionString.isEmpty()) throw new RuntimeException("No Database parameter");
        try {
            Class.forName("org.sqlite.JDBC");

            _connection = DriverManager.getConnection(connectionString, _config.toProperties());
            _statement = _connection.createStatement();

            System.out.println("База подключена!");
            return true;
        }
        catch (SQLException | ClassNotFoundException ex) {
            disconnect();
            System.out.println("База не подключена!");
            return false;
        }
    }

    public static void setConfig(Class<?> database) {
        if (!database.isAnnotationPresent(Database.class)) throw new RuntimeException("No Database parameter");
        _config.enforceForeignKeys(true);

        var db = database.getAnnotation(Database.class);
        connectionString = db.path() + db.name() + "." + db.extension();

        if (connect()) {
            createTables(database.getDeclaredFields());
            disconnect();
        }
    }

    public static void createTables(Field... tables) {
        Arrays.stream(tables).map(Field::getType).forEach(Table::create);
    }

    public static void createTables(Class<?>... tables) {
        Arrays.stream(tables).forEach(Table::create);
    }

    public static Statement getStatement() {
        return _statement;
    }

    // --------Закрытие--------
    public static void disconnect() {
        close(_connection);
        close(_statement);
        close(_result);
    }

    private static void close(AutoCloseable closedElement) {
        try {
            if (closedElement != null) {
                closedElement.close();
            }
        }
        catch (Exception ex) {
            System.out.println("Ошибка закрытия");
        }
    }
}