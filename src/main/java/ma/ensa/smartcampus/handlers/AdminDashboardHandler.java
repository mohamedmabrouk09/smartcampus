package ma.ensa.smartcampus.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.ensa.smartcampus.model.User;
import ma.ensa.smartcampus.util.DBUtil;
import ma.ensa.smartcampus.util.SessionManager;
import ma.ensa.smartcampus.util.HtmlUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String sessionId = SessionManager.getSessionIdFromCookie(exchange.getRequestHeaders().getFirst("Cookie"));
        User user = SessionManager.getUser(sessionId);
        
        if (user == null || !"ADMINISTRATEUR".equals(user.getRole())) {
            exchange.getResponseHeaders().add("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
            return;
        }
        
        String html = buildDashboard(user);
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
    
    private String buildDashboard(User user) {
        StringBuilder html = new StringBuilder();
        html.append("""
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartCampus - Espace Administrateur</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f5f7fa; }
        .header { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); color: #1a4d2e; padding: 20px 40px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4px 20px rgba(67, 233, 123, 0.3); }
        .header h1 { font-size: 28px; }
        .user-info { display: flex; align-items: center; gap: 15px; }
        .user-badge { background: rgba(26,77,46,0.1); padding: 8px 16px; border-radius: 20px; font-weight: 600; }
        .logout { color: #1a4d2e; text-decoration: none; padding: 8px 16px; background: rgba(26,77,46,0.1); border-radius: 8px; transition: background 0.3s; font-weight: 600; }
        .logout:hover { background: rgba(26,77,46,0.2); }
        .container { max-width: 1600px; margin: 30px auto; padding: 0 20px; }
        .welcome { background: white; padding: 30px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); margin-bottom: 30px; }
        .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .stat-card { background: white; padding: 25px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); text-align: center; }
        .stat-card:nth-child(1) { border-top: 4px solid #667eea; }
        .stat-card:nth-child(2) { border-top: 4px solid #f5576c; }
        .stat-card:nth-child(3) { border-top: 4px solid #43e97b; }
        .stat-card:nth-child(4) { border-top: 4px solid #fa709a; }
        .stat-number { font-size: 42px; font-weight: bold; margin: 10px 0; }
        .stat-card:nth-child(1) .stat-number { color: #667eea; }
        .stat-card:nth-child(2) .stat-number { color: #f5576c; }
        .stat-card:nth-child(3) .stat-number { color: #43e97b; }
        .stat-card:nth-child(4) .stat-number { color: #fa709a; }
        .stat-label { color: #666; font-size: 14px; }
        .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); gap: 25px; }
        .card { background: white; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); overflow: hidden; }
        .card:hover { transform: translateY(-5px); box-shadow: 0 5px 20px rgba(0,0,0,0.1); transition: transform 0.3s; }
        .card-header { padding: 20px; background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); color: #1a4d2e; display: flex; align-items: center; gap: 10px; }
        .card-header h3 { font-size: 18px; }
        .card-body { padding: 20px; max-height: 400px; overflow-y: auto; }
        .item { padding: 12px; border-left: 3px solid #43e97b; background: #f0fff4; margin-bottom: 10px; border-radius: 0 8px 8px 0; }
        .item:last-child { margin-bottom: 0; }
        .item-title { font-weight: 600; color: #333; }
        .item-detail { color: #666; font-size: 13px; margin-top: 4px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #f0f0f0; }
        th { background: #f0fff4; color: #333; font-weight: 600; }
        .badge { display: inline-block; padding: 4px 10px; border-radius: 12px; font-size: 12px; }
        .badge-etudiant { background: #e7f3ff; color: #0066cc; }
        .badge-prof { background: #ffe0e6; color: #d6336c; }
        .badge-admin { background: #d3f9d8; color: #2f9e44; }
    </style>
</head>
<body>
    <div class="header">
        <h1>⚙️ SmartCampus - Espace Administrateur</h1>
        <div class="user-info">
            <span class="user-badge">👑 Admin: """).append(HtmlUtil.esc(user.getNomComplet())).append("""
</span>
            <a href="/logout" class="logout">Déconnexion</a>
        </div>
    </div>
    
    <div class="container">
        <div class="welcome">
            <h2>Tableau de Bord Administratif</h2>
            <p>Gestion complète de l'établissement - Utilisateurs, Cours, Emplois du temps, Examens, Réunions et Annonces</p>
        </div>
        
        <div class="stats">
""");
        
        try (Connection conn = DBUtil.getConnection()) {
            // Compter utilisateurs
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) as c FROM users WHERE role='ETUDIANT'");
            if (rs.next()) html.append("            <div class='stat-card'><div class='stat-label'>ÉTUDIANTS</div><div class='stat-number'>").append(rs.getInt("c")).append("</div></div>\n");
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) as c FROM users WHERE role='PROFESSEUR'");
            if (rs.next()) html.append("            <div class='stat-card'><div class='stat-label'>PROFESSEURS</div><div class='stat-number'>").append(rs.getInt("c")).append("</div></div>\n");
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) as c FROM cours");
            if (rs.next()) html.append("            <div class='stat-card'><div class='stat-label'>COURS</div><div class='stat-number'>").append(rs.getInt("c")).append("</div></div>\n");
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) as c FROM examens");
            if (rs.next()) html.append("            <div class='stat-card'><div class='stat-label'>EXAMENS</div><div class='stat-number'>").append(rs.getInt("c")).append("</div></div>\n");
        } catch (SQLException e) {
            e.printStackTrace();
            html.append("            <p style='color:#666;'>Erreur de chargement</p>\n");
        }
        
        html.append("""
        </div>
        
        <div class="grid">
            <div class="card">
                <div class="card-header">
                    <span>👥</span>
                    <h3>Gestion des Utilisateurs</h3>
                </div>
                <div class="card-body">
                    <table>
                        <thead>
                            <tr>
                                <th>Nom & Prénom</th>
                                <th>Email</th>
                                <th>Rôle</th>
                                <th>Filière/Groupe</th>
                            </tr>
                        </thead>
                        <tbody>
""");
        
        try (Connection conn = DBUtil.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT nom, prenom, email, role, filiere, groupe FROM users ORDER BY role, nom");
            while (rs.next()) {
                String role = rs.getString("role");
                String badgeClass = switch (role) {
                    case "ETUDIANT" -> "badge-etudiant";
                    case "PROFESSEUR" -> "badge-prof";
                    case "ADMINISTRATEUR" -> "badge-admin";
                    default -> "";
                };
                html.append("                            <tr>\n");
                html.append("                                <td>").append(HtmlUtil.esc(rs.getString("prenom"))).append(" ").append(HtmlUtil.esc(rs.getString("nom"))).append("</td>\n");
                html.append("                                <td>").append(HtmlUtil.esc(rs.getString("email"))).append("</td>\n");
                html.append("                                <td><span class='badge ").append(badgeClass).append("'>").append(HtmlUtil.esc(role)).append("</span></td>\n");
                html.append("                                <td>").append(rs.getString("filiere") != null ? HtmlUtil.esc(rs.getString("filiere") + " - " + (rs.getString("groupe") != null ? rs.getString("groupe") : "")) : "-").append("</td>\n");
                html.append("                            </tr>\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            html.append("                            <tr><td colspan='4' style='text-align:center; color:#666;'>Erreur de chargement</td></tr>\n");
        }
        
        html.append("""
                        </tbody>
                    </table>
                </div>
            </div>
            
            <div class="card">
                <div class="card-header">
                    <span>📚</span>
                    <h3>Catalogue des Cours</h3>
                </div>
                <div class="card-body">
""");
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT c.intitule, c.code, c.filiere, c.semestre, u.nom, u.prenom FROM cours c LEFT JOIN users u ON c.prof_id = u.id ORDER BY c.filiere";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                html.append("                    <div class='item'>\n");
                html.append("                        <div class='item-title'>").append(HtmlUtil.esc(rs.getString("intitule"))).append(" (").append(HtmlUtil.esc(rs.getString("code"))).append(")</div>\n");
                html.append("                        <div class='item-detail'>🎓 ").append(HtmlUtil.esc(rs.getString("filiere"))).append(" | Semestre ").append(HtmlUtil.esc(rs.getString("semestre"))).append(" | 👨‍🏫 Prof: ").append(rs.getString("prenom") != null ? HtmlUtil.esc(rs.getString("prenom") + " " + rs.getString("nom")) : "Non assigné").append("</div>\n");
                html.append("                    </div>\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            html.append("                    <p style='color:#666;'>Erreur de chargement</p>\n");
        }
        
        html.append("""
                </div>
            </div>
            
            <div class="card">
                <div class="card-header">
                    <span>📝</span>
                    <h3>Planning des Examens</h3>
                </div>
                <div class="card-body">
""");
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT ex.date_examen, ex.heure_debut, ex.heure_fin, ex.salle, ex.type_examen, ex.filiere, ex.groupe, c.intitule " +
                         "FROM examens ex JOIN cours c ON ex.cours_id = c.id ORDER BY ex.date_examen";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                html.append("                    <div class='item' style='border-left-color: #ff6b6b; background: #fff5f5;'>\n");
                html.append("                        <div class='item-title'>").append(HtmlUtil.esc(rs.getString("intitule"))).append(" - ").append(HtmlUtil.esc(rs.getString("type_examen"))).append("</div>\n");
                html.append("                        <div class='item-detail'>📅 ").append(rs.getDate("date_examen")).append(" | ⏰ ").append(HtmlUtil.esc(rs.getString("heure_debut"))).append(" - ").append(HtmlUtil.esc(rs.getString("heure_fin"))).append(" | 🏛️ ").append(HtmlUtil.esc(rs.getString("salle"))).append(" | ").append(HtmlUtil.esc(rs.getString("filiere"))).append(" ").append(HtmlUtil.esc(rs.getString("groupe"))).append("</div>\n");
                html.append("                    </div>\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            html.append("                    <p style='color:#666;'>Erreur de chargement</p>\n");
        }
        
        html.append("""
                </div>
            </div>
            
            <div class="card">
                <div class="card-header">
                    <span>🤝</span>
                    <h3>Réunions & Annonces</h3>
                </div>
                <div class="card-body">
""");
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT r.titre, r.date_reunion, r.heure_debut, r.heure_fin, r.salle, u.nom, u.prenom FROM reunions r JOIN users u ON r.prof_id = u.id ORDER BY r.date_reunion";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                html.append("                    <div class='item' style='border-left-color: #4dabf7; background: #f0f9ff;'>\n");
                html.append("                        <div class='item-title'>🎯 ").append(HtmlUtil.esc(rs.getString("titre"))).append("</div>\n");
                html.append("                        <div class='item-detail'>📅 ").append(rs.getDate("date_reunion")).append(" | ⏰ ").append(HtmlUtil.esc(rs.getString("heure_debut"))).append(" - ").append(HtmlUtil.esc(rs.getString("heure_fin"))).append(" | 🏛️ ").append(HtmlUtil.esc(rs.getString("salle"))).append(" | Organisé par Prof. ").append(HtmlUtil.esc(rs.getString("prenom"))).append(" ").append(HtmlUtil.esc(rs.getString("nom"))).append("</div>\n");
                html.append("                    </div>\n");
            }
            html.append("                    <hr style='margin:15px 0; border:none; border-top:1px solid #eee;'><p style='font-weight:600; margin-bottom:10px; color:#333;'>📢 Dernières Annonces</p>\n");
            rs = conn.createStatement().executeQuery("SELECT titre, contenu, cible, date_creation FROM annonces ORDER BY date_creation DESC LIMIT 3");
            while (rs.next()) {
                html.append("                    <div class='item'>\n");
                html.append("                        <div class='item-title'>📌 ").append(HtmlUtil.esc(rs.getString("titre"))).append(" <span class='badge' style='background:#f1f3f5; color:#495057;'>").append(HtmlUtil.esc(rs.getString("cible"))).append("</span></div>\n");
                html.append("                        <div class='item-detail'>").append(HtmlUtil.esc(rs.getString("contenu"))).append("<br><small>").append(rs.getTimestamp("date_creation")).append("</small></div>\n");
                html.append("                    </div>\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            html.append("                    <p style='color:#666;'>Erreur de chargement</p>\n");
        }
        
        html.append("""
                </div>
            </div>
        </div>
    </div>
</body>
</html>
        """);
        
        return html.toString();
    }
}