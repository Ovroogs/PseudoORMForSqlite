package ovroogs.sql.database;

import org.sqlite.SQLiteConfig;
import ovroogs.sql.annotation.Column;
import ovroogs.sql.annotation.Database;
import ovroogs.sql.annotation.Entity;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class DatabaseClass {
    private static String connectionString;

    private static Connection _connection;
    private static Statement _statement;
    private static ResultSet _result;
    private static final SQLiteConfig _config = new SQLiteConfig();

    // --------ПОДКЛЮЧЕНИЕ К БАЗЕ ДАННЫХ--------
    public static boolean connect() {
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
        connectionString = db.path() + db.name() + ".sqlite";

        if (connect()) createTables(database.getDeclaredFields());
    }

    public static void createTables(Field... tables) {
        Arrays.stream(tables).map(Field::getType).forEach(type -> Table.create(type, _statement));
    }

    public static void createTables(Class<?>... tables) {
        Arrays.stream(tables).forEach(table -> Table.create(table, _statement));
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