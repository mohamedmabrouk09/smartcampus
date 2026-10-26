package ma.ensa.smartcampus;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import ma.ensa.smartcampus.auth.LoginService;
import ma.ensa.smartcampus.auth.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SmartCampusApp {

    public static void main(String[] args) throws IOException {
        int port = readPort(args);
        System.out.println("SmartCampus API starting on port " + port);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));

        server.createContext("/login", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String query = exchange.getRequestURI().getQuery();
                String email = param(query, "email");
                String password = param(query, "password");
                boolean ok = new UserService().authenticate(email, password);
                sendJson(exchange, "{\"login\":" + ok + "}");
            }
        });

        server.createContext("/health", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                sendJson(exchange, "{\"status\":\"UP\"}");
            }
        });

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                sendJson(exchange, "{\"service\":\"SmartCampus\",\"version\":\"1.0\"}");
            }
        });

        server.start();
        System.out.println("SmartCampus API started on port " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
    }

    private static int readPort(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port argument: " + args[0] + ", using default 8080");
            }
        }
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try {
                return Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT env var: " + envPort + ", using default 8080");
            }
        }
        return 8080;
    }

    private static void sendJson(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String param(String query, String key) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}