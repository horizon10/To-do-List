import java.sql.*;

public class DatabaseManager {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/todolist";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "12345";

    private Connection connection;

    public DatabaseManager() {
        try {
            connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            System.out.println("Database connection failed:");
            e.printStackTrace();
        }
    }

    public void executeUpdate(String query) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet executeSelectQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        return rs;
    }

    public String executeSelect(String query) {
        String result = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            // Sorgudan gelen veriyi al
            if (rs.next()) {
                result = rs.getString(1); // İlk sütunu alıyoruz
            }
            // ResultSet, Statement ve Connection kapatılmalı
            rs.close();
            statement.close();
            connection.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return result; // Veriyi geri döndür
    }

    public Connection getConnection() {
        return connection;
    }

    public void createListTable(String listName) {
        String sql = "CREATE TABLE IF NOT EXISTS " + listName + " (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "task_description VARCHAR(255) NOT NULL," +
                "is_completed BOOLEAN DEFAULT FALSE," +
                "durum_no INT DEFAULT 1" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Table " + listName + " created successfully.");
        } catch (SQLException e) {
            System.out.println("Error creating table " + listName + ":");
            e.printStackTrace();
        }
    }

    public boolean isTableEmpty(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                return count == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true; // Varsayılan olarak tabloyu boş kabul ederiz
    }

    public void executePreparedStatement(String sql, Object... params) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error executing prepared statement:");
            e.printStackTrace();
        }
    }
}
