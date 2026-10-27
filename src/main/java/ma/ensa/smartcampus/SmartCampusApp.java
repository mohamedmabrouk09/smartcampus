package ma.ensa.smartcampus;

import com.sun.net.httpserver.HttpServer;
import ma.ensa.smartcampus.handlers.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class SmartCampusApp {

    public static void main(String[] args) throws IOException {
        int port = readPort(args);
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║       SmartCampus Platform v2.0       ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("Démarrage sur le port " + port);
        System.out.println();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(8));

        // Authentification
        server.createContext("/login", new LoginHandler());
        server.createContext("/logout", new LogoutHandler());

        // Dashboards par rôle
        server.createContext("/dashboard/etudiant", new EtudiantDashboardHandler());
        server.createContext("/dashboard/professeur", new ProfesseurDashboardHandler());
        server.createContext("/dashboard/admin", new AdminDashboardHandler());

        // API endpoints (compatibilité)
        server.createContext("/health", exchange -> {
            String json = "{\"status\":\"UP\",\"service\":\"SmartCampus\",\"version\":\"2.0\"}";
            sendJson(exchange, json);
        });

        server.createContext("/", exchange -> {
            String json = "{\"service\":\"SmartCampus\",\"version\":\"2.0\",\"roles\":[\"ETUDIANT\",\"PROFESSEUR\",\"ADMINISTRATEUR\"]}";
            sendJson(exchange, json);
        });

        server.start();
        System.out.println("✅ SmartCampus API démarrée avec succès!");
        System.out.println("🌐 Accès: http://localhost:" + port);
        System.out.println("🔐 Connexion: http://localhost:" + port + "/login");
        System.out.println();
        System.out.println("Comptes de test:");
        System.out.println("  👨‍🎓 Étudiant:      etudiant@usms.ac.ma / Passw0rd!2026");
        System.out.println("  👨‍🏫 Professeur:    prof.ahmed@usms.ac.ma / Passw0rd!2026");
        System.out.println("  👑 Administrateur: admin@usms.ac.ma / Passw0rd!2026");
        System.out.println();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nArrêt de SmartCampus...");
            server.stop(0);
        }));
    }

    private static int readPort(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Port invalide: " + args[0] + ", utilisation du port par défaut 8080");
            }
        }
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try {
                return Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                System.err.println("PORT invalide: " + envPort + ", utilisation du port par défaut 8080");
            }
        }
        return 8080;
    }

    private static void sendJson(com.sun.net.httpserver.HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
