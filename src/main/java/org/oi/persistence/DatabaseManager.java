package org.oi.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.home") + "/oi-notes/notes.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS notes (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    content TEXT,
                    created_at TEXT,
                    last_modified TEXT,
                    metadata TEXT  -- JSON column
                );
            """;
            stmt.execute(sql);
            System.out.println("Database initialized.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize database.");
            e.printStackTrace();
        }
    }
}
