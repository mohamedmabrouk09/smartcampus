package ma.ensa.smartcampus.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.ensa.smartcampus.util.SessionManager;

import java.io.IOException;

public class LogoutHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String sessionId = SessionManager.getSessionIdFromCookie(exchange.getRequestHeaders().getFirst("Cookie"));
        SessionManager.invalidateSession(sessionId);
        exchange.getResponseHeaders().add("Set-Cookie", "SESSIONID=; Path=/; Max-Age=0");
        exchange.getResponseHeaders().add("Location", "/login");
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }
}
