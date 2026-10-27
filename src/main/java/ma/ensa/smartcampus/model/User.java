package ma.ensa.smartcampus.model;

public class User {
    private int id;
    private String email;
    private String password;
    private String role; // ETUDIANT, PROFESSEUR, ADMINISTRATEUR
    private String nom;
    private String prenom;
    private String filiere;
    private String groupe;

    public User() {}

    public User(int id, String email, String role, String nom, String prenom, String filiere, String groupe) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.filiere = filiere;
        this.groupe = groupe;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }

    public String getNomComplet() {
        if (nom != null && prenom != null) return prenom + " " + nom;
        return email;
    }
}
