package ma.ensa.smartcampus.util;

import ma.ensa.smartcampus.model.User;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private static final Map<String, User> sessions = new HashMap<>();
    
    public static String createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, user);
        return sessionId;
    }
    
    public static User getUser(String sessionId) {
        if (sessionId == null) return null;
        return sessions.get(sessionId);
    }
    
    public static void invalidateSession(String sessionId) {
        if (sessionId != null) sessions.remove(sessionId);
    }
    
    public static String getSessionIdFromCookie(String cookieHeader) {
        if (cookieHeader == null) return null;
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && parts[0].equals("SESSIONID")) {
                return parts[1];
            }
        }
        return null;
    }
}
