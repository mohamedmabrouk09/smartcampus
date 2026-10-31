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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfesseurDashboardHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String sessionId = SessionManager.getSessionIdFromCookie(exchange.getRequestHeaders().getFirst("Cookie"));
        User user = SessionManager.getUser(sessionId);
        
        if (user == null || !"PROFESSEUR".equals(user.getRole())) {
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
    <title>SmartCampus - Espace Professeur</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f5f7fa; }
        .header { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 20px 40px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4px 20px rgba(245, 87, 108, 0.3); }
        .header h1 { font-size: 28px; }
        .user-info { display: flex; align-items: center; gap: 15px; }
        .user-badge { background: rgba(255,255,255,0.2); padding: 8px 16px; border-radius: 20px; }
        .logout { color: white; text-decoration: none; padding: 8px 16px; background: rgba(255,255,255,0.2); border-radius: 8px; transition: background 0.3s; }
        .logout:hover { background: rgba(255,255,255,0.3); }
        .container { max-width: 1400px; margin: 30px auto; padding: 0 20px; }
        .welcome { background: white; padding: 30px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); margin-bottom: 30px; }
        .welcome h2 { color: #333; margin-bottom: 10px; }
        .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(380px, 1fr)); gap: 25px; }
        .card { background: white; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); overflow: hidden; }
        .card:hover { transform: translateY(-5px); box-shadow: 0 5px 20px rgba(0,0,0,0.1); transition: transform 0.3s; }
        .card-header { padding: 20px; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; display: flex; align-items: center; gap: 10px; }
        .card-header h3 { font-size: 18px; }
        .card-body { padding: 20px; }
        .item { padding: 12px; border-left: 3px solid #f5576c; background: #fff5f8; margin-bottom: 10px; border-radius: 0 8px 8px 0; }
        .item:last-child { margin-bottom: 0; }
        .item-title { font-weight: 600; color: #333; }
        .item-detail { color: #666; font-size: 13px; margin-top: 4px; }
        .badge { display: inline-block; padding: 4px 10px; background: #ffe0e6; color: #d6336c; border-radius: 12px; font-size: 12px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #f0f0f0; }
        th { background: #fff5f8; color: #333; font-weight: 600; }
    </style>
</head>
<body>
    <div class="header">
        <h1>👨‍🏫 SmartCampus - Espace Professeur</h1>
        <div class="user-info">
            <span class="user-badge">Prof. """).append(HtmlUtil.esc(user.getNomComplet())).append("""
</span>
            <a href="/logout" class="logout">Déconnexion</a>
        </div>
    </div>
    
    <div class="container">
        <div class="welcome">
            <h2>Bienvenue, Prof. """).append(HtmlUtil.esc(user.getPrenom())).append("""
&nbsp;!</h2>
            <p>Gérez vos cours, vos séances (Cours/TD/TP), vos réunions, vos notes et suivez vos étudiants.</p>
        </div>
        
        <div class="grid">
            <div class="card">
                <div class="card-header">
                    <span>📚</span>
                    <h3>Mes Cours</h3>
                </div>
                <div class="card-body">
""");
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT intitule, code, filiere, semestre FROM cours WHERE prof_id = ? ORDER BY filiere";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ResultSet rs = ps.executeQuery();
            boolean hasCours = false;
            while (rs.next()) {
                hasCours = true;
                html.append("                    <div class='item'>\n");
                html.append("                        <div class='item-title'>").append(HtmlUtil.esc(rs.getString("intitule"))).append(" (").append(HtmlUtil.esc(rs.getString("code"))).append(")</div>\n");
                html.append("                        <div class='item-detail'>🎓 ").append(HtmlUtil.esc(rs.getString("filiere"))).append(" | Semestre ").append(HtmlUtil.esc(rs.getString("semestre"))).append("</div>\n");
                html.append("                    </div>\n");
            }
            if (!hasCours) {
                html.append("                    <p style='color:#666;'>Aucun cours assigné</p>\n");
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
                    <span>📅</span>
                    <h3>Mon Emploi du Temps (Cours, TD, TP)</h3>
                </div>
                <div class="card-body">
""");
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT e.jour, e.heure_debut, e.heure_fin, e.salle, e.type_seance, c.intitule, c.code " +
                         "FROM emploi_du_temps e JOIN cours c ON e.cours_id = c.id " +
                         "WHERE c.prof_id = ? ORDER BY CASE e.jour WHEN 'LUNDI' THEN 1 WHEN 'MARDI' THEN 2 WHEN 'MERCREDI' THEN 3 WHEN 'JEUDI' THEN 4 WHEN 'VENDREDI' THEN 5 WHEN 'SAMEDI' THEN 6 END, e.heure_debut";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ResultSet rs = ps.executeQuery();
            boolean hasEdt = false;
            while (rs.next()) {
                hasEdt = true;
                html.append("                    <div class='item'>\n");
                html.append("                        <div class='item-title'>").append(HtmlUtil.esc(rs.getString("intitule"))).append(" <span class='badge'>").append(HtmlUtil.esc(rs.getString("type_seance"))).append("</span></div>\n");
                html.append("                        <div class='item-detail'>📆 ").append(HtmlUtil.esc(rs.getString("jour"))).append(" | ⏰ ").append(HtmlUtil.esc(rs.getString("heure_debut"))).append(" - ").append(HtmlUtil.esc(rs.getString("heure_fin"))).append(" | 🏛️ ").append(HtmlUtil.esc(rs.getString("salle"))).append("</div>\n");
                html.append("                    </div>\n");
            }
            if (!hasEdt) {
                html.append("                    <p style='color:#666;'>Aucun créneau</p>\n");
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
                    <h3>Mes Réunions</h3>
                </div>
                <div class="card-body">
""");
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT titre, description, date_reunion, heure_debut, heure_fin, salle, participants FROM reunions WHERE prof_id = ? ORDER BY date_reunion, heure_debut";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ResultSet rs = ps.executeQuery();
            boolean hasReunion = false;
            while (rs.next()) {
                hasReunion = true;
                html.append("                    <div class='item' style='border-left-color: #4dabf7; background: #f0f9ff;'>\n");
                html.append("                        <div class='item-title'>🎯 ").append(HtmlUtil.esc(rs.getString("titre"))).append("</div>\n");
                html.append("                        <div class='item-detail'>📅 ").append(rs.getDate("date_reunion")).append(" | ⏰ ").append(HtmlUtil.esc(rs.getString("heure_debut"))).append(" - ").append(HtmlUtil.esc(rs.getString("heure_fin"))).append(" | 🏛️ ").append(HtmlUtil.esc(rs.getString("salle"))).append("<br>👥 ").append(HtmlUtil.esc(rs.getString("participants"))).append("<br><em>").append(HtmlUtil.esc(rs.getString("description"))).append("</em></div>\n");
                html.append("                    </div>\n");
            }
            if (!hasReunion) {
                html.append("                    <p style='color:#666;'>Aucune réunion</p>\n");
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
                    <span>🧑‍🎓</span>
                    <h3>Étudiants & Notes</h3>
                </div>
                <div class="card-body">
                    <table>
                        <thead>
                            <tr>
                                <th>Étudiant</th>
                                <th>Cours</th>
                                <th>CC</th>
                                <th>TP</th>
                                <th>Examen</th>
                                <th>Finale</th>
                            </tr>
                        </thead>
                        <tbody>
""");
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT u.nom, u.prenom, c.intitule, n.note_cc, n.note_tp, n.note_examen, n.note_finale " +
                         "FROM notes n JOIN users u ON n.etudiant_id = u.id JOIN cours c ON n.cours_id = c.id " +
                         "WHERE c.prof_id = ? ORDER BY c.intitule, u.nom";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ResultSet rs = ps.executeQuery();
            boolean hasNotes = false;
            while (rs.next()) {
                hasNotes = true;
                html.append("                            <tr>\n");
                html.append("                                <td>").append(HtmlUtil.esc(rs.getString("prenom"))).append(" ").append(HtmlUtil.esc(rs.getString("nom"))).append("</td>\n");
                html.append("                                <td>").append(HtmlUtil.esc(rs.getString("intitule"))).append("</td>\n");
                html.append("                                <td>").append(rs.getDouble("note_cc")).append("</td>\n");
                html.append("                                <td>").append(rs.getDouble("note_tp")).append("</td>\n");
                html.append("                                <td>").append(rs.getDouble("note_examen")).append("</td>\n");
                html.append("                                <td><strong>").append(String.format("%.2f", rs.getDouble("note_finale"))).append("</strong></td>\n");
                html.append("                            </tr>\n");
            }
            if (!hasNotes) {
                html.append("                            <tr><td colspan='6' style='text-align:center; color:#666;'>Aucune note enregistrée</td></tr>\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            html.append("                            <tr><td colspan='6' style='text-align:center; color:#666;'>Erreur de chargement</td></tr>\n");
        }
        
        html.append("""
                        </tbody>
                    </table>
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