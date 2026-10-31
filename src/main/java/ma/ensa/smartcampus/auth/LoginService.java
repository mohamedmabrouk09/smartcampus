package ma.ensa.smartcampus.auth;

import java.util.Map;

public class LoginService {
    static final String DOMAINE_INSTITUTIONNEL = "@usms.ac.ma";

    // Comptes simulés (pas de base de données)
    private final Map<String, String> comptes =
            Map.of("etudiant@usms.ac.ma", "Passw0rd!2026");

    public boolean validateCredentials(String email, String password) {
        return email != null && !email.isBlank()
                && email.matches("^[\\w.+-]+@usms\\.ac\\.ma$")
                && password != null && !password.isBlank();
    }

    public boolean authenticate(String email, String password) {
        if (!validateCredentials(email, password)) {
            return false;
        }
        return password.equals(comptes.get(email));
    }

        public String login(String email, String password) {
        if (email == null || email.isBlank()) {
            return "Email obligatoire";
        }
        if (!email.endsWith(DOMAINE_INSTITUTIONNEL)) {
            return "Email institutionnel (@usms.ac.ma) requis";
        }
        if (password == null || password.isBlank()) {
            return "Mot de passe obligatoire";
        }
        if (!authenticate(email, password)) {
            return "Identifiants incorrects";
        }
        return "Connexion réussie";
    }
}