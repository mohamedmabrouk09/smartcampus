package ma.ensa.smartcampus.auth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Authentification via PostgreSQL (JDBC).
 * Si la base de données est indisponible, on retombe sur les comptes simulés
 * de LoginService pour éviter un plantage au démarrage (ex: DB pas encore prête).
 */
public class UserService {

    private final LoginService fallback = new LoginService();

    public boolean authenticate(String email, String password) {
        if (email == null || password == null || !email.endsWith(LoginService.DOMAINE_INSTITUTIONNEL)) {
            return false;
        }

        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");

        if (url == null || user == null || pass == null) {
            return fallback.authenticate(email, password);
        }

        String sql = "SELECT password FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return password.equals(rs.getString("password"));
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("DB unavailable, using fallback: " + e.getMessage());
            return fallback.authenticate(email, password);
        }
    }
}