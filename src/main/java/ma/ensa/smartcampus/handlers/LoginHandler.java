package ma.ensa.smartcampus.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.ensa.smartcampus.auth.AuthService;
import ma.ensa.smartcampus.model.User;
import ma.ensa.smartcampus.util.SessionManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LoginHandler implements HttpHandler {
    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        
        if ("GET".equals(method)) {
            // Afficher la page de login
            String html = getLoginPage();
            sendHtml(exchange, html);
            return;
        }
        
        if ("POST".equals(method)) {
            // Traiter la connexion
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String email = getParam(body, "email");
            String password = getParam(body, "password");
            
            User user = authService.authenticate(email, password);
            if (user != null) {
                String sessionId = SessionManager.createSession(user);
                exchange.getResponseHeaders().add("Set-Cookie", "SESSIONID=" + sessionId + "; Path=/; HttpOnly");
                
                // Rediriger vers le dashboard selon le rôle
                String redirect = switch (user.getRole()) {
                    case "ETUDIANT" -> "/dashboard/etudiant";
                    case "PROFESSEUR" -> "/dashboard/professeur";
                    case "ADMINISTRATEUR" -> "/dashboard/admin";
                    default -> "/";
                };
                exchange.getResponseHeaders().add("Location", redirect);
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
            } else {
                String html = getLoginPageError();
                sendHtml(exchange, html);
            }
            return;
        }
        
        exchange.sendResponseHeaders(405, -1);
        exchange.close();
    }
    
    private String getParam(String body, String key) {
        if (body == null) return null;
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                try {
                    return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                } catch (Exception e) {
                    return kv[1];
                }
            }
        }
        return null;
    }
    
    private void sendHtml(HttpExchange exchange, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
    
    private String getLoginPage() {
        return """
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartCampus - Connexion</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; }
        .login-container { background: white; padding: 40px; border-radius: 15px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); width: 400px; max-width: 90%; }
        .logo { text-align: center; margin-bottom: 30px; }
        .logo h1 { color: #667eea; font-size: 32px; margin-bottom: 5px; }
        .logo p { color: #666; font-size: 14px; }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 8px; color: #333; font-weight: 600; }
        input { width: 100%; padding: 12px; border: 2px solid #e0e0e0; border-radius: 8px; font-size: 14px; transition: border 0.3s; }
        input:focus { outline: none; border-color: #667eea; }
        .btn { width: 100%; padding: 14px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; border-radius: 8px; font-size: 16px; font-weight: 600; cursor: pointer; transition: transform 0.2s; }
        .btn:hover { transform: translateY(-2px); box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4); }
        .info { margin-top: 25px; padding: 15px; background: #f5f7ff; border-radius: 8px; font-size: 12px; }
        .info h4 { color: #667eea; margin-bottom: 8px; }
        .info p { color: #666; margin: 4px 0; }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="logo">
            <h1>🎓 SmartCampus</h1>
            <p>Portail Numérique de l'Établissement</p>
        </div>
        <form method="POST" action="/login">
            <div class="form-group">
                <label for="email">Adresse Email Institutionnelle</label>
                <input type="email" id="email" name="email" placeholder="nom.prenom@usms.ac.ma" required>
            </div>
            <div class="form-group">
                <label for="password">Mot de Passe</label>
                <input type="password" id="password" name="password" placeholder="••••••••" required>
            </div>
            <button type="submit" class="btn">Se Connecter</button>
        </form>
        <div class="info">
            <h4>Comptes de démonstration</h4>
            <p><strong>Étudiant:</strong> etudiant@usms.ac.ma / Passw0rd!2026</p>
            <p><strong>Professeur:</strong> prof.ahmed@usms.ac.ma / Passw0rd!2026</p>
            <p><strong>Administrateur:</strong> admin@usms.ac.ma / Passw0rd!2026</p>
        </div>
    </div>
</body>
</html>
        """;
    }
    
    private String getLoginPageError() {
        return """
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartCampus - Erreur</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; }
        .login-container { background: white; padding: 40px; border-radius: 15px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); width: 400px; max-width: 90%; }
        .logo { text-align: center; margin-bottom: 30px; }
        .logo h1 { color: #667eea; font-size: 32px; margin-bottom: 5px; }
        .error { background: #fee; color: #c33; padding: 12px; border-radius: 8px; margin-bottom: 20px; text-align: center; font-weight: 600; }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 8px; color: #333; font-weight: 600; }
        input { width: 100%; padding: 12px; border: 2px solid #e0e0e0; border-radius: 8px; font-size: 14px; }
        .btn { width: 100%; padding: 14px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; border-radius: 8px; font-size: 16px; font-weight: 600; cursor: pointer; }
        .back { display: block; text-align: center; margin-top: 15px; color: #667eea; text-decoration: none; }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="logo">
            <h1>🎓 SmartCampus</h1>
        </div>
        <div class="error">⚠️ Identifiants incorrects ou domaine non autorisé</div>
        <form method="POST" action="/login">
            <div class="form-group">
                <label for="email">Adresse Email</label>
                <input type="email" id="email" name="email" required>
            </div>
            <div class="form-group">
                <label for="password">Mot de Passe</label>
                <input type="password" id="password" name="password" required>
            </div>
            <button type="submit" class="btn">Réessayer</button>
        </form>
        <a href="/login" class="back">← Retour à la connexion</a>
    </div>
</body>
</html>
        """;
    }
}
