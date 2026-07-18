package controller;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.effect.*;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;
import models.*;
import data.*;
import java.util.ArrayList;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PHARMACIE VALLIÈRE - APPLICATION PRINCIPALE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * @author Djeferly FELIX , Di-Enilson ETIENNE , Djedly Fitz GEROME
 * @version 2.0
 * @since 2025
 */
public class Main extends Application {

    // Fenêtre principale de l'application 
    private Stage stage;

    // Nom de l'utilisateur connecté
    private String user = "";

    // Liste des unités de mesure (comprimés, ml, boîtes, etc.)
    private ArrayList<Mesure> mesures;

    // Catalogue de tous les produits pharmaceutiques
    private ArrayList<Produit> produits;

    // Base de données des clients
    private ArrayList<Client> clients;

    // Historique de toutes les ventes
    private ArrayList<Vente> ventes;

    // Couleurs du thème pharmacie
    private static final String COULEUR_PRINCIPALE = "#2C82C9"; // Bleu médical
    private static final String COULEUR_SECONDAIRE = "#3CB371"; // Vert santé
    private static final String COULEUR_ACCENT = "#1A5F7A"; // Bleu foncé
    private static final String COULEUR_SUCCESS = "#27AE60"; // Vert succès
    private static final String COULEUR_ERROR = "#E74C3C"; // Rouge erreur
    private static final String COULEUR_FOND = "#F8F9FA"; // Blanc cassé
    private static final String COULEUR_TEXTE = "#2C3E50"; // Bleu-gris foncé

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Pharmacie Vallière");

        // Initialise les utilisateurs du système (admin, employés, etc.)
        FileManager.initialiserUtilisateurs();

        // Charge toutes les données depuis les fichiers
        chargerDonnees();

        // Affiche l'écran de connexion 
        afficherConnexion();
    }

    /**
     * Permet de redémarrer l'application (utile après déconnexion).
     *
     * @param stage La fenêtre à réinitialiser
     */
    public void redemarrer(Stage stage) {
        start(stage);
    }

    /**
     * Charge toutes les données de l'application depuis les fichiers
     * persistants. Cette méthode est appelée au démarrage pour restaurer l'état
     * précédent.
     */
    private void chargerDonnees() {
        mesures = FileManager.chargerMesures();
        produits = FileManager.chargerProduits();
        clients = FileManager.chargerClients();
        ventes = FileManager.chargerVentes();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                    INTERFACE DE CONNEXION 
    // ═══════════════════════════════════════════════════════════════════════
    private void afficherConnexion() {
        // Conteneur principal avec BorderPane pour un layout flexible
        BorderPane bdp = new BorderPane();
        bdp.setStyle("-fx-background-color: linear-gradient(to bottom right, " + COULEUR_PRINCIPALE + ", " + COULEUR_ACCENT + ");");

        // Section centrale contenant le logo et le formulaire
        VBox centerContent = new VBox(25);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(40));

        // En-tête avec logo animé et titres
        VBox header = creerHeader();

        // Formulaire de connexion 
        VBox formBox = creerFormConnexion();

        centerContent.getChildren().addAll(header, formBox);
        bdp.setCenter(centerContent);

        // Pied de page avec copyright
        Label footer = new Label("© 2025 Pharmacie Vallière | Port-au-Prince, Haïti");
        footer.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");
        BorderPane.setAlignment(footer, Pos.CENTER);
        BorderPane.setMargin(footer, new Insets(15));
        bdp.setBottom(footer);

        // Création de la scène avec dimensions optimales
        Scene scene = new Scene(bdp, 900, 950);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        // Lance l'animation d'entrée fluide
        animerEntree(centerContent);
    }

    /**
     * Crée l'en-tête de la page de connexion avec logo animé et titres.
     *
     * @return VBox contenant le logo et les titres stylisés
     */
    private VBox creerHeader() {
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);

        // Logo médical avec effet de lueur
        Label logo = new Label("⚕");
        logo.setStyle("-fx-font-size: 80px; -fx-text-fill: white;");

        // Effet de lueur blanche
        DropShadow glow = new DropShadow();
        glow.setColor(javafx.scene.paint.Color.rgb(255, 255, 255, 0.8));
        glow.setRadius(25);
        glow.setSpread(0.3);
        logo.setEffect(glow);

        // Animation du logo 
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2.0), logo);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.15);
        pulse.setToY(1.15);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        pulse.play();

        // Cadre autour du logo
        StackPane logoContainer = new StackPane();
        logoContainer.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 50; "
                + "-fx-padding: 20; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 50; -fx-border-width: 2;");
        logoContainer.getChildren().add(logo);

        // Titres avec ombre portée
        Label titre = new Label("PHARMACIE VALLIÈRE");
        titre.setStyle("-fx-font-size: 38px; -fx-font-weight: 900; -fx-text-fill: white; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0.5, 0, 2); "
                + "-fx-letter-spacing: 1.5px;");

        Label sousTitre = new Label("Votre santé, notre engagement");
        sousTitre.setStyle("-fx-font-size: 16px; -fx-text-fill: rgba(255,255,255,0.95); "
                + "-fx-font-style: italic; -fx-font-weight: 300;");

        // Séparateur décoratif
        Separator separator = new Separator();
        separator.setPrefWidth(200);
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.4);");

        header.getChildren().addAll(logoContainer, titre, separator, sousTitre);
        return header;
    }

    /**
     * Crée le formulaire de connexion
     *
     * @return VBox contenant le formulaire complet de connexion
     */
    private VBox creerFormConnexion() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(35, 40, 35, 40));
        form.setMaxWidth(420);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 20; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 8); "
                + "-fx-border-color: rgba(44, 130, 201, 0.1); -fx-border-width: 1; -fx-border-radius: 20;");

        // Titres du formulaire
        Label titre = new Label("Connexion");
        titre.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: " + COULEUR_TEXTE + ";");

        Label instruction = new Label("Accédez à votre espace professionnel");
        instruction.setStyle("-fx-font-size: 14px; -fx-text-fill: #7F8C8D;");

        // CHAMP NOM D'UTILISATEUR
        VBox userBox = new VBox(8);
        Label lblUser = new Label("👤 Nom d'utilisateur");
        lblUser.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COULEUR_TEXTE + "; -fx-font-weight: 600;");

        TextField txtUser = new TextField();
        txtUser.setPromptText("Entrez votre nom d'utilisateur");
        txtUser.setPrefHeight(45);
        txtUser.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; "
                + "-fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                + "-fx-background-color: #FAFAFA;");

        // Effet de focus
        txtUser.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                txtUser.setStyle("-fx-background-radius: 10; -fx-border-color: " + COULEUR_PRINCIPALE + "; "
                        + "-fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                        + "-fx-background-color: white;");
            } else {
                txtUser.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; "
                        + "-fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                        + "-fx-background-color: #FAFAFA;");
            }
        });

        userBox.getChildren().addAll(lblUser, txtUser);

        // CHAMP MOT DE PASSE
        VBox passBox = new VBox(8);
        Label lblPass = new Label("🔒 Mot de passe");
        lblPass.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COULEUR_TEXTE + "; -fx-font-weight: 600;");

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Entrez votre mot de passe");
        txtPass.setPrefHeight(45);
        txtPass.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; "
                + "-fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                + "-fx-background-color: #FAFAFA;");

        // Effet de focus
        txtPass.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                txtPass.setStyle("-fx-background-radius: 10; -fx-border-color: " + COULEUR_PRINCIPALE + "; "
                        + "-fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                        + "-fx-background-color: white;");
            } else {
                txtPass.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; "
                        + "-fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                        + "-fx-background-color: #FAFAFA;");
            }
        });

        passBox.getChildren().addAll(lblPass, txtPass);

        // Message d'erreur (caché par défaut)
        Label lblMsg = new Label();
        lblMsg.setStyle("-fx-text-fill: " + COULEUR_ERROR + "; -fx-font-size: 13px; -fx-font-weight: 600;");
        lblMsg.setVisible(false);
        lblMsg.setManaged(false);

        // BOUTON DE CONNEXION
        Button btnConnexion = new Button("SE CONNECTER");
        btnConnexion.setPrefHeight(50);
        btnConnexion.setMaxWidth(Double.MAX_VALUE);
        btnConnexion.setStyle("-fx-background-color: linear-gradient(to right, " + COULEUR_PRINCIPALE + ", " + COULEUR_SECONDAIRE + "); "
                + "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 700; "
                + "-fx-background-radius: 12; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);");

        // Effets hover
        btnConnexion.setOnMouseEntered(e -> {
            btnConnexion.setStyle("-fx-background-color: linear-gradient(to right, " + COULEUR_ACCENT + ", #2E8B57); "
                    + "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 700; "
                    + "-fx-background-radius: 12; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 4);");
        });

        btnConnexion.setOnMouseExited(e -> {
            btnConnexion.setStyle("-fx-background-color: linear-gradient(to right, " + COULEUR_PRINCIPALE + ", " + COULEUR_SECONDAIRE + "); "
                    + "-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 700; "
                    + "-fx-background-radius: 12; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);");
        });

        // Effet de pression
        btnConnexion.setOnMousePressed(e -> {
            btnConnexion.setStyle("-fx-background-color: linear-gradient(to right, " + COULEUR_ACCENT + ", #228B22); "
                    + "-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 15.5px; -fx-font-weight: 700; "
                    + "-fx-background-radius: 12; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);");
        });

        // Action du bouton : validation et connexion
        btnConnexion.setOnAction(e -> {
            lblMsg.setVisible(false);
            lblMsg.setManaged(false);

            if (txtUser.getText().trim().isEmpty() || txtPass.getText().trim().isEmpty()) {
                lblMsg.setText("⚠ Veuillez remplir tous les champs");
                lblMsg.setVisible(true);
                lblMsg.setManaged(true);
                secouerChamp(txtUser.getText().trim().isEmpty() ? txtUser : txtPass);
            } else {
                connecter(txtUser.getText().trim(), txtPass.getText().trim(), lblMsg);
            }
        });

        // Permet de se connecter en appuyant sur Entrée
        txtPass.setOnAction(e -> btnConnexion.fire());

        // Lien "Créer un compte"
        HBox lienBox = new HBox();
        lienBox.setAlignment(Pos.CENTER);
        lienBox.setPadding(new Insets(10, 0, 0, 0));

        Label lblCompte = new Label("Nouveau collaborateur ? ");
        lblCompte.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 13px;");

        Hyperlink lienCreation = new Hyperlink("Créer un compte");
        lienCreation.setStyle("-fx-text-fill: " + COULEUR_PRINCIPALE + "; -fx-font-size: 13px; -fx-font-weight: 600; "
                + "-fx-border-color: transparent; -fx-padding: 0;");

        // Effet hover pour le lien
        lienCreation.setOnMouseEntered(e -> {
            lienCreation.setStyle("-fx-text-fill: " + COULEUR_SECONDAIRE + "; -fx-font-size: 13px; -fx-font-weight: 600; "
                    + "-fx-underline: true; -fx-border-color: transparent; -fx-padding: 0;");
        });

        lienCreation.setOnMouseExited(e -> {
            lienCreation.setStyle("-fx-text-fill: " + COULEUR_PRINCIPALE + "; -fx-font-size: 13px; -fx-font-weight: 600; "
                    + "-fx-underline: false; -fx-border-color: transparent; -fx-padding: 0;");
        });

        // Action du lien : afficher la page de création de compte
        lienCreation.setOnAction(e -> afficherCreationCompte());

        lienBox.getChildren().addAll(lblCompte, lienCreation);

        // Assemblage final du formulaire
        form.getChildren().addAll(titre, instruction, userBox, passBox, lblMsg, btnConnexion, lienBox);

        // Animation d'entrée du formulaire
        FadeTransition fadeForm = new FadeTransition(Duration.seconds(0.5), form);
        fadeForm.setFromValue(0);
        fadeForm.setToValue(1);
        fadeForm.play();

        return form;
    }

    // LOGIQUE DE CONNEXION
    /**
     * Vérifie les identifiants et connecte l'utilisateur si valides.
     *
     * @param username Le nom d'utilisateur saisi
     * @param password Le mot de passe saisi
     * @param lblMsg Le label pour afficher les messages de retour
     */
    private void connecter(String username, String password, Label lblMsg) {
        if (Authentification.verifierCredentials(username, password)) {
            user = username;
            String role = Authentification.obtenirRole(username);

            lblMsg.setStyle("-fx-text-fill: " + COULEUR_SUCCESS + "; -fx-font-size: 13px; -fx-font-weight: 600;");
            lblMsg.setText("✓ Connexion réussie ! Bonjour " + username + " (" + role + ")");
            lblMsg.setVisible(true);
            lblMsg.setManaged(true);

            // Animation de succès
            FadeTransition successFade = new FadeTransition(Duration.seconds(0.3), lblMsg);
            successFade.setFromValue(0);
            successFade.setToValue(1);
            successFade.play();

            // Pause avant transition vers le menu
            PauseTransition pause = new PauseTransition(Duration.seconds(1.0));
            pause.setOnFinished(e -> {
                // Animation de sortie
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), lblMsg);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> {
                    new MenuController(stage, user, mesures, produits, clients, ventes)
                            .afficherMenuPrincipal();
                });
                fadeOut.play();
            });
            pause.play();
        } else {
            lblMsg.setStyle("-fx-text-fill: " + COULEUR_ERROR + "; -fx-font-size: 13px; -fx-font-weight: 600;");
            lblMsg.setText("✗ Nom d'utilisateur ou mot de passe incorrect");
            lblMsg.setVisible(true);
            lblMsg.setManaged(true);

            Authentification.afficherMessageErreur();

            // Animation d'erreur
            FadeTransition errorFade = new FadeTransition(Duration.seconds(0.3), lblMsg);
            errorFade.setFromValue(0);
            errorFade.setToValue(1);
            errorFade.play();
        }
    }

    // PAGE DE CRÉATION DE COMPTE
    /**
     * Affiche le formulaire de création de compte
     */
    private void afficherCreationCompte() {
        BorderPane bdp = new BorderPane();
        bdp.setStyle("-fx-background-color: linear-gradient(to bottom right, " + COULEUR_SECONDAIRE + ", " + COULEUR_PRINCIPALE + ");");

        VBox centerContent = new VBox(25);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(40));

        VBox header = creerHeaderCreation();
        VBox formBox = creerFormulaireCreation();

        centerContent.getChildren().addAll(header, formBox);
        bdp.setCenter(centerContent);

        // Pied de page
        Label footer = new Label("© 2025 Pharmacie Vallière | Création de compte collaborateur");
        footer.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 11px;");
        BorderPane.setAlignment(footer, Pos.CENTER);
        BorderPane.setMargin(footer, new Insets(15));
        bdp.setBottom(footer);

        Scene scene = new Scene(bdp, 800, 800);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        animerEntree(centerContent);
    }

    /**
     * Crée l'en-tête pour la page de création de compte
     */
    private VBox creerHeaderCreation() {
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);

        Label logo = new Label("📋");
        logo.setStyle("-fx-font-size: 70px; -fx-text-fill: white;");

        DropShadow glow = new DropShadow();
        glow.setColor(javafx.scene.paint.Color.rgb(255, 255, 255, 0.8));
        glow.setRadius(20);
        logo.setEffect(glow);

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.8), logo);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.1);
        pulse.setToY(1.1);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        Label titre = new Label("NOUVEAU COLLABORATEUR");
        titre.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: white; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0.5, 0, 2);");

        Label sousTitre = new Label("Enregistrez un nouveau membre de l'équipe");
        sousTitre.setStyle("-fx-font-size: 15px; -fx-text-fill: rgba(255,255,255,0.95); "
                + "-fx-font-weight: 300;");

        Separator separator = new Separator();
        separator.setPrefWidth(250);
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.4);");

        header.getChildren().addAll(logo, titre, separator, sousTitre);
        return header;
    }

    /**
     * Crée le formulaire de création de compte
     */
    private VBox creerFormulaireCreation() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(30, 40, 30, 40));
        form.setMaxWidth(450);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 20; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 8); "
                + "-fx-border-color: rgba(60, 179, 113, 0.1); -fx-border-width: 1; -fx-border-radius: 20;");

        Label titre = new Label("Formulaire d'inscription");
        titre.setStyle("-fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: " + COULEUR_TEXTE + ";");

        // Champ Nom d'utilisateur
        VBox userBox = new VBox(5);
        Label lblUser = new Label("👤 Nom d'utilisateur");
        lblUser.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COULEUR_TEXTE + "; -fx-font-weight: 600;");

        TextField txtUser = new TextField();
        txtUser.setPromptText("Choisissez un nom d'utilisateur unique");
        txtUser.setPrefHeight(42);
        txtUser.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; "
                + "-fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                + "-fx-background-color: #FAFAFA;");

        txtUser.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                txtUser.setStyle("-fx-background-radius: 10; -fx-border-color: " + COULEUR_SECONDAIRE + "; "
                        + "-fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                        + "-fx-background-color: white;");
            } else {
                txtUser.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; "
                        + "-fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                        + "-fx-background-color: #FAFAFA;");
            }
        });

        userBox.getChildren().addAll(lblUser, txtUser);

        // Champ Mot de passe
        VBox passBox = new VBox(5);
        Label lblPass = new Label("🔒 Mot de passe");
        lblPass.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COULEUR_TEXTE + "; -fx-font-weight: 600;");

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Minimum 6 caractères");
        txtPass.setPrefHeight(42);
        txtPass.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; "
                + "-fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                + "-fx-background-color: #FAFAFA;");

        txtPass.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                txtPass.setStyle("-fx-background-radius: 10; -fx-border-color: " + COULEUR_SECONDAIRE + "; "
                        + "-fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                        + "-fx-background-color: white;");
            } else {
                txtPass.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; "
                        + "-fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                        + "-fx-background-color: #FAFAFA;");
            }
        });

        passBox.getChildren().addAll(lblPass, txtPass);

        // Champ Confirmation
        VBox confirmBox = new VBox(5);
        Label lblConfirm = new Label("✓ Confirmer le mot de passe");
        lblConfirm.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COULEUR_TEXTE + "; -fx-font-weight: 600;");

        PasswordField txtConfirm = new PasswordField();
        txtConfirm.setPromptText("Retapez votre mot de passe");
        txtConfirm.setPrefHeight(42);
        txtConfirm.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; "
                + "-fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                + "-fx-background-color: #FAFAFA;");

        txtConfirm.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                txtConfirm.setStyle("-fx-background-radius: 10; -fx-border-color: " + COULEUR_SECONDAIRE + "; "
                        + "-fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                        + "-fx-background-color: white;");
            } else {
                txtConfirm.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; "
                        + "-fx-border-radius: 10; -fx-padding: 12; -fx-font-size: 14px; "
                        + "-fx-background-color: #FAFAFA;");
            }
        });

        confirmBox.getChildren().addAll(lblConfirm, txtConfirm);

        // Champ Rôle
        VBox roleBox = new VBox(5);
        Label lblRole = new Label("👥 Rôle professionnel");
        lblRole.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COULEUR_TEXTE + "; -fx-font-weight: 600;");

        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("Vendeur", "Pharmacien", "Administrateur", "Assistant");
        cbRole.setPromptText("Sélectionnez un rôle");
        cbRole.setPrefHeight(42);
        cbRole.setStyle("-fx-background-radius: 10; -fx-border-color: #E0E0E0; "
                + "-fx-border-radius: 10; -fx-padding: 5; -fx-font-size: 14px; "
                + "-fx-background-color: #FAFAFA;");

        roleBox.getChildren().addAll(lblRole, cbRole);

        // Message d'erreur/succès
        Label lblMsg = new Label();
        lblMsg.setStyle("-fx-text-fill: " + COULEUR_ERROR + "; -fx-font-size: 13px; -fx-font-weight: 600;");
        lblMsg.setVisible(false);
        lblMsg.setManaged(false);

        // Boutons
        HBox boutonsBox = new HBox(15);
        boutonsBox.setAlignment(Pos.CENTER);

        Button btnCreer = new Button("CRÉER LE COMPTE");
        btnCreer.setPrefHeight(48);
        btnCreer.setPrefWidth(180);
        btnCreer.setStyle("-fx-background-color: linear-gradient(to right, " + COULEUR_SECONDAIRE + ", " + COULEUR_PRINCIPALE + "); "
                + "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700; "
                + "-fx-background-radius: 10; -fx-cursor: hand;");

        Button btnAnnuler = new Button("ANNULER");
        btnAnnuler.setPrefHeight(48);
        btnAnnuler.setPrefWidth(180);
        btnAnnuler.setStyle("-fx-background-color: #95A5A6; "
                + "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700; "
                + "-fx-background-radius: 10; -fx-cursor: hand;");

        // Effets hover
        btnCreer.setOnMouseEntered(e -> {
            btnCreer.setStyle("-fx-background-color: linear-gradient(to right, #2E8B57, " + COULEUR_ACCENT + "); "
                    + "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700; "
                    + "-fx-background-radius: 10; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);");
        });

        btnCreer.setOnMouseExited(e -> {
            btnCreer.setStyle("-fx-background-color: linear-gradient(to right, " + COULEUR_SECONDAIRE + ", " + COULEUR_PRINCIPALE + "); "
                    + "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700; "
                    + "-fx-background-radius: 10; -fx-cursor: hand;");
        });

        btnAnnuler.setOnMouseEntered(e -> {
            btnAnnuler.setStyle("-fx-background-color: #7F8C8D; "
                    + "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700; "
                    + "-fx-background-radius: 10; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);");
        });

        btnAnnuler.setOnMouseExited(e -> {
            btnAnnuler.setStyle("-fx-background-color: #95A5A6; "
                    + "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700; "
                    + "-fx-background-radius: 10; -fx-cursor: hand;");
        });

        boutonsBox.getChildren().addAll(btnCreer, btnAnnuler);

        // Actions des boutons
        btnCreer.setOnAction(e -> {
            lblMsg.setVisible(false);
            lblMsg.setManaged(false);

            String username = txtUser.getText().trim();
            String password = txtPass.getText().trim();
            String confirmation = txtConfirm.getText().trim();
            String role = cbRole.getValue();

            // Validation
            if (username.isEmpty() || password.isEmpty() || confirmation.isEmpty() || role == null) {
                lblMsg.setText("⚠ Veuillez remplir tous les champs");
                lblMsg.setVisible(true);
                lblMsg.setManaged(true);
                return;
            }

            if (!password.equals(confirmation)) {
                lblMsg.setText("✗ Les mots de passe ne correspondent pas");
                lblMsg.setVisible(true);
                lblMsg.setManaged(true);
                secouerChamp(txtConfirm);
                return;
            }

            if (password.length() < 6) {
                lblMsg.setText("✗ Le mot de passe doit contenir au moins 6 caractères");
                lblMsg.setVisible(true);
                lblMsg.setManaged(true);
                return;
            }

            // Création du compte
            boolean succes = Authentification.creerCompte(username, password, role);

            if (succes) {
                lblMsg.setStyle("-fx-text-fill: " + COULEUR_SUCCESS + ";");
                lblMsg.setText("✓ Compte créé avec succès pour " + username + " (" + role + ")");
                lblMsg.setVisible(true);
                lblMsg.setManaged(true);

                Authentification.afficherMessageCreationSucces(username);

                // Animation de succès
                FadeTransition fadeSuccess = new FadeTransition(Duration.seconds(0.5), lblMsg);
                fadeSuccess.setFromValue(0);
                fadeSuccess.setToValue(1);
                fadeSuccess.play();

                // Retour à la page de connexion après 2 secondes
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(ev -> afficherConnexion());
                pause.play();
            } else {
                lblMsg.setStyle("-fx-text-fill: " + COULEUR_ERROR + ";");
                lblMsg.setText("✗ Ce nom d'utilisateur existe déjà");
                lblMsg.setVisible(true);
                lblMsg.setManaged(true);
                secouerChamp(txtUser);
            }
        });

        btnAnnuler.setOnAction(e -> afficherConnexion());

        // Assemblage final
        form.getChildren().addAll(titre, userBox, passBox, confirmBox, roleBox, lblMsg, boutonsBox);
        return form;
    }

    // ANIMATIONS VISUELLES
    /**
     * Anime l'entrée du contenu principal
     */
    private void animerEntree(VBox content) {
        content.setOpacity(0);
        content.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), content);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.8), content);
        slide.setFromY(30);
        slide.setToY(0);

        ParallelTransition parallel = new ParallelTransition(fade, slide);
        parallel.play();
    }

    /**
     * Anime un champ de texte avec un effet de secousse
     */
    private void secouerChamp(TextField field) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), field);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();

        // Changement temporaire de couleur de bordure
        String originalStyle = field.getStyle();
        field.setStyle(originalStyle + "; -fx-border-color: " + COULEUR_ERROR + ";");

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> field.setStyle(originalStyle));
        pause.play();
    }

    // POINT D'ENTRÉE
    public static void main(String[] args) {
        launch(args);
    }
}
