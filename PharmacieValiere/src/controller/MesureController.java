package controller;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.*;
import data.FileManager;
import data.GenerateurID;
import java.util.ArrayList;

public class MesureController {

    //Fenêtre principale de l'application
    private Stage stage;

    //Liste des produits disponibles en stock
    private ArrayList<Produit> produits;

    //Liste des clients enregistrés
    private ArrayList<Client> clients;

    //Historique des ventes effectuées
    private ArrayList<Vente> ventes;

    //Liste des unités de mesure
    private ArrayList<Mesure> mesures;

    //Référence au contrôleur de menu pour la navigation
    private MenuController menuCtrl;

    /**
     * CONSTRUCTEUR - Initialise le contrôleur des mesures
     *
     * @param stage Fenêtre principale de l'application
     * @param mesures Liste des unités de mesure
     * @param produits Liste des produits (pour vérification dépendances)
     * @param menuCtrl Contrôleur du menu pour la navigation
     */
    public MesureController(Stage stage, ArrayList<Mesure> mesures,
            ArrayList<Produit> produits, MenuController menuCtrl) {
        this.stage = stage;
        this.mesures = mesures;
        this.produits = produits;
        this.menuCtrl = menuCtrl;
    }

    // MÉTHODES D'AFFICHAGE PRINCIPALES
    /**
     * Affiche l'interface principale de gestion des mesures Organisation en
     * deux panneaux : Formulaire d'ajout et Liste
     */
    public void afficherGestionMesures() {
        // Conteneur principal avec espacement
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Titre principal 
        Label titre = creerTitre("GESTION DES MESURES");

        // ──────────────────────────────────────────────────────────────
        // DISPOSITION HORIZONTALE
        // Gauche : Formulaire d'ajout
        // Droite : Liste et suppression
        // ──────────────────────────────────────────────────────────────
        HBox container = new HBox(20, creerFormAjout(), creerListe());
        container.setPadding(new Insets(10));

        // Bouton de retour au menu principal
        Button btnRetour = creerBouton("Retour",
                e -> menuCtrl.afficherMenuPrincipal(), "#6c757d");

        // Assemblage final dans un ScrollPane pour le défilement
        root.getChildren().addAll(titre, container, btnRetour);
        stage.setScene(new Scene(new ScrollPane(root), 900, 600));
    }

    // MÉTHODES DE FORMULAIRES - AJOUT MESURE
    /**
     * Crée le formulaire d'ajout d'une nouvelle unité de mesure
     *
     * @return VBox contenant tous les champs du formulaire
     */
    private VBox creerFormAjout() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        // Style visuel avec bordure et fond
        box.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10; -fx-border-color: #dee2e6;");
        box.setPrefWidth(350);

        // Titre du formulaire
        Label titre = creerLabel("AJOUTER UNE MESURE", 16, true);

        // Grille pour alignement propre des champs
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        // ──────────────────────────────────────────────────────────────
        // CHAMP CODE : Généré automatiquement
        // Format: "MES" + NUMÉRO SÉQUENTIEL (ex: MES001, MES002)
        // Désactivé pour éviter les erreurs de saisie
        // ──────────────────────────────────────────────────────────────
        TextField txtCode = new TextField();
        txtCode.setDisable(true);
        txtCode.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #495057;");
        txtCode.setPrefHeight(35);

        // Génération automatique du code initial
        String codeAuto = GenerateurID.genererCodeMesure();
        txtCode.setText(codeAuto);

        // Champs de saisie utilisateur
        TextField txtNom = creerTextField("Ex: Kilogramme, comprimés, ml, boîtes, etc.");

        // Zone de texte pour la description (multiligne)
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Description de la mesure");
        txtDesc.setPrefHeight(80);
        txtDesc.setPrefWidth(250);

        // Positionnement dans la grille avec labels
        form.add(new Label("Code:"), 0, 0);
        form.add(txtCode, 1, 0);
        form.add(new Label("Nom*:"), 0, 1);
        form.add(txtNom, 1, 1);
        form.add(new Label("Description:"), 0, 2);
        form.add(txtDesc, 1, 2);

        // Zone de message pour les retours utilisateur
        Label lblMsg = new Label();
        lblMsg.setStyle("-fx-text-fill: #dc3545;");

        // Conteneur pour le bouton d'ajout
        HBox btnBox = new HBox(10);
        Button btnAjouter = creerBouton("Ajouter",
                e -> ajouterMesure(txtCode, txtNom, txtDesc, lblMsg), "#4CAF50");
        btnBox.getChildren().add(btnAjouter);

        // Assemblage final
        box.getChildren().addAll(titre, form, btnBox, lblMsg);
        return box;
    }

    /**
     * Ajoute une nouvelle unité de mesure à la liste après validation
     *
     * @param txtCode Champ code (généré automatiquement)
     * @param txtNom Champ nom (obligatoire)
     * @param txtDesc Zone de texte description
     * @param lblMsg Label pour les messages de retour
     */
    private void ajouterMesure(TextField txtCode, TextField txtNom, TextArea txtDesc, Label lblMsg) {
        // Récupération et nettoyage des données
        String code = txtCode.getText().trim();
        String nom = txtNom.getText().trim();

        // ⚠️ VALIDATION : Le nom est obligatoire
        if (nom.isEmpty()) {
            lblMsg.setText("Le nom est obligatoire!");
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
            return;
        }

        // Vérifie si le code existe déjà (sécurité anti-collision)
        // Normalement improbable grâce au générateur, mais sécurité supplémentaire
        for (Mesure m : mesures) {
            if (m.getCode().equals(code)) {
                // Génère un nouveau code si collision détectée
                code = GenerateurID.genererCodeMesure();
                txtCode.setText(code);
            }
        }

        // Création et ajout de la nouvelle mesure
        mesures.add(new Mesure(code, nom, txtDesc.getText().trim()));

        // Sauvegarde persistante
        FileManager.sauvegarderMesures(mesures);

        // Message de confirmation
        lblMsg.setText("Mesure ajoutée! Code: " + code);
        lblMsg.setStyle("-fx-text-fill: #28a745;");

        // Préparation pour la prochaine mesure
        String nouveauCode = GenerateurID.genererCodeMesure();
        txtCode.setText(nouveauCode);
        txtNom.clear();
        txtDesc.clear();

        // Rafraîchissement de l'interface
        afficherGestionMesures();
    }

    // MÉTHODES D'AFFICHAGE ET SUPPRESSION
    /**
     * Crée le panneau de liste et suppression des mesures
     *
     * @return VBox contenant la table et les contrôles de suppression
     */
    private VBox creerListe() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));

        // Titre avec compteur
        Label titre = creerLabel("LISTE DES MESURES (" + mesures.size() + ")", 16, true);

        // Création de la table
        TableView<Mesure> table = new TableView<>();
        table.setPrefHeight(300);

        // Configuration des colonnes
        table.getColumns().addAll(
                creerCol("Code", "code", 80),
                creerCol("Nom", "nom", 150),
                creerCol("Description", "description", 250)
        );

        // Chargement des données
        table.getItems().addAll(mesures);

        // SECTION SUPPRESSION
        // Vérifie d'abord si la mesure est utilisée par des produits
        TextField txtCodeSuppr = creerTextField("Code à supprimer");
        Button btnSuppr = creerBouton("Supprimer", null, "#dc3545");
        HBox supprBox = new HBox(10, txtCodeSuppr, btnSuppr);

        // Zone de message pour les retours utilisateur
        Label lblMsg = new Label();
        lblMsg.setStyle("-fx-text-fill: #dc3545;");

        // Gestionnaire d'événement pour la suppression
        btnSuppr.setOnAction(e -> supprimerMesure(txtCodeSuppr, lblMsg));

        // Assemblage final
        box.getChildren().addAll(titre, table, supprBox, lblMsg);
        return box;
    }

    /**
     * Supprime une unité de mesure après vérification des dépendances
     *
     * @param txtCode Champ code de la mesure à supprimer
     * @param lblMsg Label pour les messages de retour
     */
    private void supprimerMesure(TextField txtCode, Label lblMsg) {
        // Récupération et normalisation du code
        String code = txtCode.getText().trim().toUpperCase();

        // VÉRIFICATION DES DÉPENDANCES CRITIQUE
        // Une mesure ne peut être supprimée si elle est utilisée par des produits
        // ──────────────────────────────────────────────────────────────
        for (Produit p : produits) {
            if (p.getCodeMesure().equals(code)) {
                lblMsg.setText("Cette mesure est utilisée par des produits!");
                lblMsg.setStyle("-fx-text-fill: #dc3545;");
                return;
            }
        }

        // Recherche et suppression de la mesure
        for (int i = 0; i < mesures.size(); i++) {
            if (mesures.get(i).getCode().equals(code)) {
                // Suppression de la liste
                mesures.remove(i);

                // Sauvegarde persistante
                FileManager.sauvegarderMesures(mesures);

                // Message de confirmation
                lblMsg.setText("Mesure supprimée!");
                lblMsg.setStyle("-fx-text-fill: #28a745;");

                // Réinitialisation du champ
                txtCode.clear();

                // Rafraîchissement de l'interface
                afficherGestionMesures();
                return;
            }
        }

        // Message d'erreur si mesure introuvable
        lblMsg.setText("Mesure introuvable!");
        lblMsg.setStyle("-fx-text-fill: #dc3545;");
    }

    // MÉTHODES UTILITAIRES - CRÉATION DES COMPOSANTS
    /**
     * Crée un champ texte stylisé avec placeholder
     *
     * @param prompt Texte d'aide à afficher dans le champ vide
     * @return TextField configuré
     */
    private TextField creerTextField(String prompt) {
        TextField txt = new TextField();
        txt.setPromptText(prompt);
        txt.setPrefHeight(35);
        return txt;
    }

    /**
     * Crée une colonne de table générique
     *
     * @param titre Titre affiché de la colonne
     * @param prop Nom de la propriété du modèle liée à la colonne
     * @param width Largeur fixe de la colonne
     * @return TableColumn configurée
     */
    private TableColumn creerCol(String titre, String prop, int width) {
        TableColumn col = new TableColumn(titre);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(width);
        return col;
    }

    // MÉTHODES UTILITAIRES - MISE EN PAGE
    /**
     * Crée un titre principal stylisé
     *
     * @param texte Texte du titre
     * @return Label stylisé
     */
    private Label creerTitre(String texte) {
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c5aa0;");
        return lbl;
    }

    /**
     * Crée un label avec taille et poids personnalisés
     *
     * @param texte Texte à afficher
     * @param size Taille de police
     * @param bold true pour gras
     * @return Label configuré
     */
    private Label creerLabel(String texte, int size, boolean bold) {
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-font-size: " + size + "px;" + (bold ? " -fx-font-weight: bold;" : ""));
        return lbl;
    }

    /**
     * Crée un bouton stylisé avec couleur
     *
     * @param texte Texte du bouton (avec emoji)
     * @param e Gestionnaire d'événement (peut être null pour suppression)
     * @param couleur Code couleur hexadécimal
     * @return Button configuré
     */
    private Button creerBouton(String texte, javafx.event.EventHandler e, String couleur) {
        Button btn = new Button(texte);
        if (e != null) {
            btn.setOnAction(e);
        }
        btn.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; -fx-font-weight: bold;");
        return btn;
    }
}
