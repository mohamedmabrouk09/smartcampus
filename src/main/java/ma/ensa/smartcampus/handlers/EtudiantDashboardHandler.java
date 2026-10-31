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

public class EtudiantDashboardHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String sessionId = SessionManager.getSessionIdFromCookie(exchange.getRequestHeaders().getFirst("Cookie"));
        User user = SessionManager.getUser(sessionId);
        
        if (user == null || !"ETUDIANT".equals(user.getRole())) {
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
    <title>SmartCampus - Espace Étudiant</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f5f7fa; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px 40px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3); }
        .header h1 { font-size: 28px; }
        .user-info { display: flex; align-items: center; gap: 15px; }
        .user-badge { background: rgba(255,255,255,0.2); padding: 8px 16px; border-radius: 20px; }
        .logout { color: white; text-decoration: none; padding: 8px 16px; background: rgba(255,255,255,0.2); border-radius: 8px; transition: background 0.3s; }
        .logout:hover { background: rgba(255,255,255,0.3); }
        .container { max-width: 1400px; margin: 30px auto; padding: 0 20px; }
        .welcome { background: white; padding: 30px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); margin-bottom: 30px; }
        .welcome h2 { color: #333; margin-bottom: 10px; }
        .welcome p { color: #666; }
        .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); gap: 25px; }
        .card { background: white; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); overflow: hidden; transition: transform 0.3s; }
        .card:hover { transform: translateY(-5px); box-shadow: 0 5px 20px rgba(0,0,0,0.1); }
        .card-header { padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; display: flex; align-items: center; gap: 10px; }
        .card-header h3 { font-size: 18px; }
        .card-body { padding: 20px; }
        .item { padding: 12px; border-left: 3px solid #667eea; background: #f8f9ff; margin-bottom: 10px; border-radius: 0 8px 8px 0; }
        .item:last-child { margin-bottom: 0; }
        .item-title { font-weight: 600; color: #333; }
        .item-detail { color: #666; font-size: 13px; margin-top: 4px; }
        .note { display: flex; justify-content: space-between; padding: 12px; background: #f8f9ff; margin-bottom: 10px; border-radius: 8px; }
        .note-value { font-weight: bold; color: #667eea; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #f0f0f0; }
        th { background: #f8f9ff; color: #333; font-weight: 600; }
        .badge { display: inline-block; padding: 4px 10px; background: #e7f3ff; color: #0066cc; border-radius: 12px; font-size: 12px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🎓 SmartCampus - Espace Étudiant</h1>
        <div class="user-info">
            <span class="user-badge">👨‍🎓 """).append(HtmlUtil.esc(user.getNomComplet())).append(" | ").append(HtmlUtil.esc(user.getFiliere())).append(" - ").append(HtmlUtil.esc(user.getGroupe())).append("""
</span>
            <a href="/logout" class="logout">Déconnexion</a>
        </div>
    </div>
    
    <div class="container">
        <div class="welcome">
            <h2>Bienvenue, """).append(HtmlUtil.esc(user.getPrenom())).append("""
&nbsp;!</h2>
            <p>Consultez votre emploi du temps, vos examens, vos notes et vos annonces depuis votre espace personnel.</p>
        </div>
        
        <div class="grid">
            <div class="card">
                <div class="card-header">
                    <span>📅</span>
                    <h3>Emploi du Temps</h3>
                </div>
                <div class="card-body">
""");
        
        // Emploi du temps
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT e.jour, e.heure_debut, e.heure_fin, e.salle, e.type_seance, c.intitule, c.code " +
                         "FROM emploi_du_temps e JOIN cours c ON e.cours_id = c.id " +
                         "JOIN inscriptions i ON i.cours_id = c.id " +
                         "WHERE i.etudiant_id = ? ORDER BY CASE e.jour WHEN 'LUNDI' THEN 1 WHEN 'MARDI' THEN 2 WHEN 'MERCREDI' THEN 3 WHEN 'JEUDI' THEN 4 WHEN 'VENDREDI' THEN 5 WHEN 'SAMEDI' THEN 6 END, e.heure_debut";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ResultSet rs = ps.executeQuery();
            boolean hasEdt = false;
            while (rs.next()) {
                hasEdt = true;
                html.append("                    <div class='item'>\n");
                html.append("                        <div class='item-title'>").append(HtmlUtil.esc(rs.getString("intitule"))).append(" (").append(HtmlUtil.esc(rs.getString("code"))).append(")</div>\n");
                html.append("                        <div class='item-detail'>📆 ").append(HtmlUtil.esc(rs.getString("jour"))).append(" | ⏰ ").append(HtmlUtil.esc(rs.getString("heure_debut"))).append(" - ").append(HtmlUtil.esc(rs.getString("heure_fin"))).append(" | 🏛️ ").append(HtmlUtil.esc(rs.getString("salle"))).append(" | <span class='badge'>").append(HtmlUtil.esc(rs.getString("type_seance"))).append("</span></div>\n");
                html.append("                    </div>\n");
            }
            if (!hasEdt) {
                html.append("                    <p style='color:#666;'>Aucun cours programmé</p>\n");
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
                    <h3>Examens & Période d'Examen</h3>
                </div>
                <div class="card-body">
""");
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT ex.date_examen, ex.heure_debut, ex.heure_fin, ex.salle, ex.type_examen, c.intitule, c.code " +
                         "FROM examens ex JOIN cours c ON ex.cours_id = c.id " +
                         "WHERE ex.filiere = ? AND (ex.groupe = ? OR ex.groupe IS NULL) ORDER BY ex.date_examen, ex.heure_debut";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.getFiliere());
            ps.setString(2, user.getGroupe());
            ResultSet rs = ps.executeQuery();
            boolean hasExam = false;
            while (rs.next()) {
                hasExam = true;
                html.append("                    <div class='item' style='border-left-color: #ff6b6b; background: #fff5f5;'>\n");
                html.append("                        <div class='item-title'>").append(HtmlUtil.esc(rs.getString("intitule"))).append(" - ").append(HtmlUtil.esc(rs.getString("type_examen"))).append("</div>\n");
                html.append("                        <div class='item-detail'>📅 ").append(rs.getDate("date_examen")).append(" | ⏰ ").append(HtmlUtil.esc(rs.getString("heure_debut"))).append(" - ").append(HtmlUtil.esc(rs.getString("heure_fin"))).append(" | 🏛️ ").append(HtmlUtil.esc(rs.getString("salle"))).append("</div>\n");
                html.append("                    </div>\n");
            }
            if (!hasExam) {
                html.append("                    <p style='color:#666;'>Aucun examen programmé</p>\n");
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
                    <span>📊</span>
                    <h3>Mes Notes (CC, TP, Examen)</h3>
                </div>
                <div class="card-body">
""");
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT n.note_cc, n.note_tp, n.note_examen, n.note_finale, c.intitule, c.code " +
                         "FROM notes n JOIN cours c ON n.cours_id = c.id " +
                         "WHERE n.etudiant_id = ? ORDER BY c.intitule";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ResultSet rs = ps.executeQuery();
            boolean hasNotes = false;
            while (rs.next()) {
                hasNotes = true;
                html.append("                    <div class='note'>\n");
                html.append("                        <div><strong>").append(HtmlUtil.esc(rs.getString("intitule"))).append("</strong> (").append(HtmlUtil.esc(rs.getString("code"))).append(")<br><small>CC: ").append(rs.getDouble("note_cc")).append(" | TP: ").append(rs.getDouble("note_tp")).append(" | Examen: ").append(rs.getDouble("note_examen")).append("</small></div>\n");
                html.append("                        <div class='note-value'>").append(String.format("%.2f", rs.getDouble("note_finale"))).append("/20</div>\n");
                html.append("                    </div>\n");
            }
            if (!hasNotes) {
                html.append("                    <p style='color:#666;'>Aucune note disponible</p>\n");
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
                    <span>📢</span>
                    <h3>Annonces</h3>
                </div>
                <div class="card-body">
""");
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT a.titre, a.contenu, a.date_creation, u.nom, u.prenom " +
                         "FROM annonces a LEFT JOIN users u ON a.auteur_id = u.id " +
                         "WHERE a.cible IN ('TOUS', 'ETUDIANT') ORDER BY a.date_creation DESC LIMIT 5";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            boolean hasAnn = false;
            while (rs.next()) {
                hasAnn = true;
                html.append("                    <div class='item'>\n");
                html.append("                        <div class='item-title'>📌 ").append(HtmlUtil.esc(rs.getString("titre"))).append("</div>\n");
                html.append("                        <div class='item-detail'>").append(HtmlUtil.esc(rs.getString("contenu"))).append("<br><small>Par ").append(rs.getString("prenom") != null ? HtmlUtil.esc(rs.getString("prenom") + " " + rs.getString("nom")) : "Administration").append(" | ").append(rs.getTimestamp("date_creation")).append("</small></div>\n");
                html.append("                    </div>\n");
            }
            if (!hasAnn) {
                html.append("                    <p style='color:#666;'>Aucune annonce</p>\n");
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