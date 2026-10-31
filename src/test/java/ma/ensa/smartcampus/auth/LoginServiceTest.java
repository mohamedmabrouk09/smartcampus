package ma.ensa.smartcampus.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginServiceTest {

    private final LoginService loginService = new LoginService();

    @Test
    public void testLoginSuccess() {
        // Test avec un email et un mot de passe valides
        boolean result = loginService.authenticate("etudiant@usms.ac.ma", "Passw0rd!2026");
        assertTrue(result, "La connexion devrait réussir avec les bons identifiants.");
    }

    @Test
    public void testLoginFailureWrongPassword() {
        // Test avec un mauvais mot de passe
        boolean result = loginService.authenticate("etudiant@usms.ac.ma", "MotDePasseFaux");
        assertFalse(result, "La connexion devrait échouer avec un mauvais mot de passe.");
    }

    @Test
    public void testLoginFailureWrongDomain() {
        // Test avec un domaine non institutionnel
        boolean result = loginService.authenticate("etudiant@gmail.com", "Passw0rd!2026");
        assertFalse(result, "La connexion devrait échouer si le domaine n'est pas @usms.ac.ma.");
    }
        @Test
    public void testValidationRejectsBlankEmail() {
        assertFalse(loginService.validateCredentials("  ", "Passw0rd!2026"));
    }

    @Test
    public void testValidationRejectsNullPassword() {
        assertFalse(loginService.validateCredentials("etudiant@usms.ac.ma", null));
    }

    @Test
    public void testValidationRejectsBadEmailFormat() {
        assertFalse(loginService.validateCredentials("@usms.ac.ma", "Passw0rd!2026"));
    }

    @Test
    public void testValidationRejectsShortPassword() {
        assertFalse(loginService.validateCredentials("etudiant@usms.ac.ma", "Passw0rd!"));
    }

        @Test
    public void testLoginMessageSuccess() {
        assertEquals("Connexion réussie",
                loginService.login("etudiant@usms.ac.ma", "Passw0rd!2026"));
    }

    @Test
    public void testLoginMessageWrongDomain() {
        assertEquals("Email institutionnel (@usms.ac.ma) requis",
                loginService.login("etudiant@gmail.com", "Passw0rd!2026"));
    }

    @Test
    public void testLoginMessageWrongPassword() {
        assertEquals("Identifiants incorrects",
                loginService.login("etudiant@usms.ac.ma", "faux"));
    }
}