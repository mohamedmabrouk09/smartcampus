package ma.ensa.smartcampus.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    public static Connection getConnection() throws SQLException {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");
        
        if (url == null || user == null || pass == null) {
            // Valeurs par défaut pour dev local
            url = "jdbc:postgresql://localhost:5432/smartcampus";
            user = "smartcampus";
            pass = "smartcampus";
        }
        
        return DriverManager.getConnection(url, user, pass);
    }
}
