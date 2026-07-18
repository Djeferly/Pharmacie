package data;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.StageStyle;

public class Authentification {

    // Thème Couleur
    private static final String COULEUR_PRINCIPALE = "#2C82C9";
    private static final String COULEUR_SECONDAIRE = "#3CB371";
    private static final String COULEUR_SUCCESS = "#27AE60";
    private static final String COULEUR_ERROR = "#E74C3C";

    // MÉTHODES DE VÉRIFICATION D'IDENTITÉ
    /**
     * Vérifie les identifiants de connexion d'un utilisateur
     */
    public static boolean verifierCredentials(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            System.out.println("⚠ Tentative de connexion avec champs vides");
            return false;
        }
        
        // Validation de format basique
        if (username.contains(":") || password.contains(":")) {
            System.out.println("⚠ Caractère ':' interdit dans les identifiants");
            return false;
        }
        
        boolean estValide = FileManager.verifierUtilisateur(username, password);
        
        if (estValide) {
            String role = obtenirRole(username);
            System.out.println("✅ Connexion réussie pour: " + username + " (Rôle: " + role + ")");
            afficherMessageSucces(username, role);
        } else {
            System.out.println("❌ Échec de connexion pour: " + username);
        }
        
        return estValide;
    }

    /**
     * Récupère le rôle d'un utilisateur connecté
     */
    public static String obtenirRole(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Invité";
        }
        
        String role = FileManager.obtenirRoleUtilisateur(username);
        return role;
    }

    // MÉTHODES DE CRÉATION DE COMPTE
    /**
     * Crée un nouveau compte utilisateur avec validation complète
     */
    public static boolean creerCompte(String username, String password, String role) {
        // Validation des entrées
        if (username == null || username.trim().isEmpty()) {
            System.out.println("⚠ Nom d'utilisateur vide");
            afficherMessageErreur("Le nom d'utilisateur ne peut pas être vide");
            return false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            System.out.println("⚠ Mot de passe vide");
            afficherMessageErreur("Le mot de passe ne peut pas être vide");
            return false;
        }
        
        if (role == null || role.trim().isEmpty()) {
            System.out.println("⚠ Rôle vide");
            afficherMessageErreur("Veuillez sélectionner un rôle");
            return false;
        }
        
        // Validation de la longueur
        if (username.length() < 3) {
            System.out.println("⚠ Nom d'utilisateur trop court");
            afficherMessageErreur("Le nom d'utilisateur doit contenir au moins 3 caractères");
            return false;
        }
        
        if (password.length() < 6) {
            System.out.println("⚠ Mot de passe trop court");
            afficherMessageErreur("Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }
        
        // Validation des caractères spéciaux
        if (username.contains(":") || password.contains(":")) {
            System.out.println("⚠ Caractère ':' interdit");
            afficherMessageErreur("Les caractères ':' ne sont pas autorisés");
            return false;
        }
        
        // Ajout de l'utilisateur via FileManager
        boolean succes = FileManager.ajouterUtilisateur(username, password, role);
        
        if (succes) {
            System.out.println("✅ Compte créé avec succès pour: " + username + " (Rôle: " + role + ")");
            afficherMessageCreationSucces(username, role);
        } else {
            System.out.println("❌ Échec de création de compte pour: " + username);
            afficherMessageErreur("Ce nom d'utilisateur existe déjà");
        }
        
        return succes;
    }

    // MÉTHODES DE GESTION DES MESSAGES
    /**
     * Affiche un message d'erreur d'authentification personnalisé
     */
    public static void afficherMessageErreur(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur d'authentification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styliserAlerte(alert, COULEUR_ERROR);
        alert.showAndWait();
    }

    /**
     * Affiche un message d'erreur d'authentification standard
     */
    public static void afficherMessageErreur() {
        afficherMessageErreur("Nom d'utilisateur ou mot de passe incorrect!");
    }

    /**
     * Affiche un message de succès de connexion
     */
    public static void afficherMessageSucces(String username, String role) {
        System.out.println("✅ Connexion réussie - Utilisateur: " + username + ", Rôle: " + role);
        // Optionnel: afficher une notification système
    }

    /**
     * Affiche un message de succès de création de compte
     */
    public static void afficherMessageCreationSucces(String username) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Création de compte réussie");
        alert.setHeaderText(null);
        alert.setContentText("Le compte a été créé avec succès !\n\n"
                + "Nom d'utilisateur: " + username + "\n"
                + "Vous pouvez maintenant vous connecter.");
        styliserAlerte(alert, COULEUR_SUCCESS);
        alert.showAndWait();
        System.out.println("✅ Message de succès de création affiché pour: " + username);
    }

    /**
     * Affiche un message de succès de création de compte avec rôle
     */
    public static void afficherMessageCreationSucces(String username, String role) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Création de compte réussie");
        alert.setHeaderText("Bienvenue dans l'équipe !");
        alert.setContentText("Le compte a été créé avec succès.\n\n"
                + "👤 Nom d'utilisateur: " + username + "\n"
                + "👥 Rôle attribué: " + role + "\n"
                + "✅ Vous pouvez maintenant vous connecter.");
        styliserAlerte(alert, COULEUR_SUCCESS);
        alert.showAndWait();
    }

    /**
     * Affiche un message de confirmation pour la suppression de compte
     */
    public static boolean afficherConfirmationSuppression(String username) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le compte utilisateur");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le compte de " + username + " ?\n"
                + "Cette action est irréversible.");
        styliserAlerte(alert, COULEUR_ERROR);
        
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    /**
     * Stylise une alerte avec les couleurs du thème pharmacie
     */
    private static void styliserAlerte(Alert alert, String couleur) {
        // Style CSS pour l'alerte
        String style = "-fx-font-family: 'Segoe UI', Arial, sans-serif; "
                + "-fx-font-size: 14px; "
                + "-fx-padding: 20px;";
        
        alert.getDialogPane().setStyle(style);
        
        // Optionnel: personnaliser les boutons
        alert.getDialogPane().getButtonTypes().stream()
                .map(alert.getDialogPane()::lookupButton)
                .forEach(button -> {
                    if (button != null) {
                        button.setStyle("-fx-font-weight: 600; -fx-font-size: 13px; "
                                + "-fx-background-radius: 8; -fx-padding: 8 16;");
                    }
                });
    }

    // MÉTHODES DE VALIDATION
    /**
     * Valide la force d'un mot de passe
     */
    public static String validerMotDePasse(String password) {
        if (password == null || password.length() < 6) {
            return "Le mot de passe doit contenir au moins 6 caractères";
        }
        
        if (password.length() > 50) {
            return "Le mot de passe est trop long (max 50 caractères)";
        }
        
        if (password.contains(" ")) {
            return "Le mot de passe ne doit pas contenir d'espaces";
        }
        
        return null; // Null signifie que le mot de passe est valide
    }

    /**
     * Valide un nom d'utilisateur
     */
    public static String validerNomUtilisateur(String username) {
        if (username == null || username.length() < 3) {
            return "Le nom d'utilisateur doit contenir au moins 3 caractères";
        }
        
        if (username.length() > 20) {
            return "Le nom d'utilisateur est trop long (max 20 caractères)";
        }
        
        if (username.contains(" ")) {
            return "Le nom d'utilisateur ne doit pas contenir d'espaces";
        }
        
        if (username.contains(":")) {
            return "Le caractère ':' n'est pas autorisé";
        }
        
        return null; // Null signifie que le nom d'utilisateur est valide
    }

    /**
     * Génère un mot de passe sécurisé (optionnel, pour les admins)
     */
    public static String genererMotDePasseSecurise() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 10; i++) {
            int index = (int) (Math.random() * caracteres.length());
            password.append(caracteres.charAt(index));
        }
        
        return password.toString();
    }
}