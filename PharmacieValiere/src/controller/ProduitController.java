package controller;

import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.*;
import data.FileManager;
import data.GenerateurID;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ProduitController {

    private Stage stage;
    private ArrayList<Produit> produits;
    private ArrayList<Mesure> mesures;
    private MenuController menuCtrl;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ProduitController(Stage stage, ArrayList<Mesure> mesures,
            ArrayList<Produit> produits, MenuController menuCtrl) {
        this.stage = stage;
        this.mesures = mesures;
        this.produits = produits;
        this.menuCtrl = menuCtrl;
    }

    public void afficherGestionProduits() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // ====================================================
        // EN-TÊTE AVEC TITRE ET BOUTON D'ACTUALISATION
        // ====================================================
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titre = creerTitre("🛒 GESTION DES PRODUITS - DÉTAIL & GROS");

        // Bouton d'actualisation
        Button btnActualiser = new Button("🔄 Actualiser");
        btnActualiser.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        btnActualiser.setTooltip(new Tooltip("Recharger les dernières données des produits"));

        btnActualiser.setOnAction(e -> {
            // Animation de désactivation temporaire
            btnActualiser.setDisable(true);
            String texteOriginal = btnActualiser.getText();
            btnActualiser.setText("🔄 Actualisation...");

            // Recharger les données
            menuCtrl.rechargerDonnees("produits");
            produits = menuCtrl.getProduits();
            mesures = menuCtrl.getMesures();

            // Rafraîchir l'écran
            afficherGestionProduits();

            // Réactiver le bouton après un court délai
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                javafx.application.Platform.runLater(() -> {
                    btnActualiser.setDisable(false);
                    btnActualiser.setText(texteOriginal);
                });
            }).start();
        });

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);
        headerBox.getChildren().addAll(titre, espace, btnActualiser);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // ====================================================
        // CRÉATION DES ONGLETS
        // ====================================================
        tabs.getTabs().addAll(
                creerTab("➕ Ajouter Produit", creerFormAjout()),
                creerTab("📋 Liste des Produits", creerListe()),
                creerTab("💰 Modifier Prix", creerFormModifier()),
                creerTab("📦 Gérer Inventaire", creerFormInventaire()),
                creerTab("🗑️ Supprimer Produit", creerFormSupprimer())
        );

        // Rafraîchir l'onglet Liste quand on y accède
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab.getText().equals("📋 Liste des Produits")) {
                menuCtrl.rechargerDonnees("produits");
                produits = menuCtrl.getProduits();
                rafraichirListeProduits((ScrollPane) newTab.getContent());
            }
        });

        // ====================================================
        // BOUTONS DE NAVIGATION
        // ====================================================
        HBox boutonsBox = new HBox(10);
        boutonsBox.setAlignment(Pos.CENTER_RIGHT);
        Button btnRetour = creerBouton("⬅️ Retour",
                e -> menuCtrl.afficherMenuPrincipal(), "#6c757d");
        boutonsBox.getChildren().addAll(btnRetour);

        root.getChildren().addAll(headerBox, tabs, boutonsBox);
        stage.setScene(new Scene(root, 1200, 800));
    }

    /**
     * Rafraîchit la liste des produits
     */
    private void rafraichirListeProduits(ScrollPane scrollPane) {
        VBox content = (VBox) scrollPane.getContent();

        // Trouver et mettre à jour la table
        for (javafx.scene.Node node : content.getChildren()) {
            if (node instanceof TableView) {
                TableView<Produit> table = (TableView<Produit>) node;
                ObservableList<Produit> obs = FXCollections.observableArrayList(produits);
                table.setItems(obs);

                // Mettre à jour les statistiques
                for (javafx.scene.Node statNode : content.getChildren()) {
                    if (statNode instanceof VBox && statNode.getStyle().contains("-fx-background-color: #f8f9fa")) {
                        content.getChildren().remove(statNode);
                        content.getChildren().add(creerStatsProduits());
                        break;
                    }
                }
                break;
            }
        }
    }

    private VBox creerFormAjout() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.getChildren().add(creerLabel("➕ NOUVEAU PRODUIT - CONFIGURATION DÉTAIL/GROS", 18, true));

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(10));

        TextField txtCode = new TextField();
        txtCode.setDisable(true);
        txtCode.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #495057;");
        txtCode.setPrefHeight(35);

        TextField txtNom = creerTextField("Nom du produit");
        ComboBox<String> cbCategorie = creerCombo(
                new String[]{"Pharmaceutique", "Cosmétique", "Médical", "Hygiène"},
                "Pharmaceutique");
        ComboBox<String> cbMesure = creerComboMesures();

        ComboBox<String> cbModeVente = creerCombo(
                new String[]{"Détail", "Gros", "Gros et Détail"},
                "Détail");

        VBox champsDynamiques = new VBox(10);
        champsDynamiques.setPadding(new Insets(10, 0, 0, 0));

        GridPane champsDetail = creerChampsDetail();
        GridPane champsGros = creerChampsGros();
        GridPane champsMixte = creerChampsMixte();

        champsDynamiques.getChildren().add(champsDetail);

        cbModeVente.setOnAction(e -> {
            String mode = cbModeVente.getValue();
            champsDynamiques.getChildren().clear();

            switch (mode) {
                case "Détail":
                    champsDynamiques.getChildren().add(champsDetail);
                    break;
                case "Gros":
                    champsDynamiques.getChildren().add(champsGros);
                    break;
                case "Gros et Détail":
                    champsDynamiques.getChildren().add(champsMixte);
                    break;
            }
        });

        cbCategorie.setOnAction(e -> {
            if (cbCategorie.getValue() != null) {
                String code = GenerateurID.genererCodeProduit(cbCategorie.getValue());
                txtCode.setText(code);
            }
        });

        txtNom.textProperty().addListener((obs, old, nouveau) -> {
            if (!nouveau.trim().isEmpty() && cbCategorie.getValue() != null) {
                String code = GenerateurID.genererCodeProduit(cbCategorie.getValue());
                txtCode.setText(code);
            }
        });

        int r = 0;
        form.add(new Label("Code:"), 0, r);
        form.add(txtCode, 1, r++);
        form.add(new Label("Nom*:"), 0, r);
        form.add(txtNom, 1, r++);
        form.add(new Label("Catégorie*:"), 0, r);
        form.add(cbCategorie, 1, r++);
        form.add(new Label("Mesure*:"), 0, r);
        form.add(cbMesure, 1, r++);
        form.add(new Label("Mode de vente*:"), 0, r);
        form.add(cbModeVente, 1, r++);

        String codeInitial = GenerateurID.genererCodeProduit(cbCategorie.getValue());
        txtCode.setText(codeInitial);

        Label lblMsg = new Label();
        lblMsg.setStyle("-fx-text-fill: #dc3545;");

        Button btnAjouter = creerBouton("✅ Enregistrer Produit", e -> {
            enregistrerProduit(txtCode, txtNom, cbCategorie, cbMesure, cbModeVente,
                    champsDetail, champsGros, champsMixte, lblMsg);
        }, "#4CAF50");
        btnAjouter.setPrefSize(250, 40);

        box.getChildren().addAll(form, champsDynamiques, lblMsg, btnAjouter);
        return box;
    }

    private GridPane creerChampsDetail() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        Label titre = new Label("🏪 CONFIGURATION POUR VENTE AU DÉTAIL");
        titre.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db; -fx-font-size: 14px;");

        TextField txtPrixAchatDetail = creerTextField("0.00");
        txtPrixAchatDetail.setText("0.00");
        txtPrixAchatDetail.setId("detail-prix-achat");

        TextField txtPrixVenteDetail = creerTextField("0.00");
        txtPrixVenteDetail.setText("0.00");
        txtPrixVenteDetail.setId("detail-prix-vente");

        TextField txtQuantiteDetail = creerTextField("0");
        txtQuantiteDetail.setText("0");
        txtQuantiteDetail.setId("detail-quantite");

        int r = 0;
        grid.add(titre, 0, r++, 2, 1);
        grid.add(new Label("Prix d'achat*:"), 0, r);
        grid.add(txtPrixAchatDetail, 1, r++);
        grid.add(new Label("Prix de vente*:"), 0, r);
        grid.add(txtPrixVenteDetail, 1, r++);
        grid.add(new Label("Quantité initiale:"), 0, r);
        grid.add(txtQuantiteDetail, 1, r++);

        return grid;
    }

    private GridPane creerChampsGros() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        Label titre = new Label("📦 CONFIGURATION POUR VENTE EN GROS");
        titre.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71; -fx-font-size: 14px;");

        TextField txtPrixAchatGros = creerTextField("0.00");
        txtPrixAchatGros.setText("0.00");
        txtPrixAchatGros.setId("gros-prix-achat");

        TextField txtPrixVenteGros = creerTextField("0.00");
        txtPrixVenteGros.setText("0.00");
        txtPrixVenteGros.setId("gros-prix-vente");

        TextField txtQuantiteGros = creerTextField("0");
        txtQuantiteGros.setText("0");
        txtQuantiteGros.setId("gros-quantite");

        int r = 0;
        grid.add(titre, 0, r++, 2, 1);
        grid.add(new Label("Prix d'achat*:"), 0, r);
        grid.add(txtPrixAchatGros, 1, r++);
        grid.add(new Label("Prix de vente*:"), 0, r);
        grid.add(txtPrixVenteGros, 1, r++);
        grid.add(new Label("Quantité initiale:"), 0, r);
        grid.add(txtQuantiteGros, 1, r++);

        return grid;
    }

    private GridPane creerChampsMixte() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        Label titre = new Label("🔄 CONFIGURATION POUR VENTE MIXTE (DÉTAIL + GROS)");
        titre.setStyle("-fx-font-weight: bold; -fx-text-fill: #9b59b6; -fx-font-size: 14px;");

        TextField txtPrixAchatMixte = creerTextField("0.00");
        txtPrixAchatMixte.setText("0.00");
        txtPrixAchatMixte.setId("mixte-prix-achat");

        TextField txtQuantiteMixte = creerTextField("0");
        txtQuantiteMixte.setText("0");
        txtQuantiteMixte.setId("mixte-quantite");

        TextField txtPrixVenteDetailMixte = creerTextField("0.00");
        txtPrixVenteDetailMixte.setText("0.00");
        txtPrixVenteDetailMixte.setId("mixte-prix-detail");

        TextField txtPrixVenteGrosMixte = creerTextField("0.00");
        txtPrixVenteGrosMixte.setText("0.00");
        txtPrixVenteGrosMixte.setId("mixte-prix-gros");

        TextField txtQuantiteGrosMixte = creerTextField("0");
        txtQuantiteGrosMixte.setText("0");
        txtQuantiteGrosMixte.setPromptText("Quantité pour le prix gros (ex: 100)");
        txtQuantiteGrosMixte.setId("mixte-quantite-gros");

        int r = 0;
        grid.add(titre, 0, r++, 2, 1);
        grid.add(new Label("Prix d'achat (pour le gros)*:"), 0, r);
        grid.add(txtPrixAchatMixte, 1, r++);
        grid.add(new Label("Quantité totale disponible (unités):"), 0, r);
        grid.add(txtQuantiteMixte, 1, r++);
        grid.add(new Label("Prix de vente unitaire (détail)*:"), 0, r);
        grid.add(txtPrixVenteDetailMixte, 1, r++);
        grid.add(new Label("Prix de vente en gros*:"), 0, r);
        grid.add(txtPrixVenteGrosMixte, 1, r++);
        grid.add(new Label("Quantité pour le prix gros*:"), 0, r);
        grid.add(txtQuantiteGrosMixte, 1, r++);

        return grid;
    }

    private void enregistrerProduit(TextField txtCode, TextField txtNom,
            ComboBox<String> cbCategorie, ComboBox<String> cbMesure,
            ComboBox<String> cbModeVente,
            GridPane champsDetail, GridPane champsGros, GridPane champsMixte,
            Label lblMsg) {

        try {
            String code = txtCode.getText().trim();
            String nom = txtNom.getText().trim();
            String mode = cbModeVente.getValue();

            if (nom.isEmpty()) {
                lblMsg.setText("❌ Le nom est obligatoire!");
                return;
            }

            if (trouverProduit(code) != null) {
                code = GenerateurID.genererCodeProduit(cbCategorie.getValue());
                txtCode.setText(code);
            }

            String codeMesure = cbMesure.getValue().split(" - ")[0];

            double prixAchat = 0;
            double prixVente = 0;
            double prixVenteGros = 0;
            int qte = 0;
            int quantiteGrosValue = 1;

            switch (mode) {
                case "Détail":
                    TextField txtPrixAchatDetail = (TextField) champsDetail.lookup("#detail-prix-achat");
                    TextField txtPrixVenteDetail = (TextField) champsDetail.lookup("#detail-prix-vente");
                    TextField txtQuantiteDetail = (TextField) champsDetail.lookup("#detail-quantite");

                    try {
                        prixAchat = Double.parseDouble(txtPrixAchatDetail.getText().trim());
                        prixVente = Double.parseDouble(txtPrixVenteDetail.getText().trim());
                        qte = Integer.parseInt(txtQuantiteDetail.getText().trim());
                    } catch (NumberFormatException e) {
                        lblMsg.setText("❌ Format numérique invalide pour le mode Détail!");
                        return;
                    }

                    if (prixAchat <= 0) {
                        lblMsg.setText("❌ Le prix d'achat doit être positif!");
                        return;
                    }
                    if (prixVente <= 0) {
                        lblMsg.setText("❌ Le prix de vente détail doit être positif!");
                        return;
                    }
                    if (prixVente <= prixAchat) {
                        lblMsg.setText("❌ Le prix de vente doit être supérieur au prix d'achat!");
                        return;
                    }
                    break;

                case "Gros":
                    TextField txtPrixAchatGros = (TextField) champsGros.lookup("#gros-prix-achat");
                    TextField txtPrixVenteGros = (TextField) champsGros.lookup("#gros-prix-vente");
                    TextField txtQuantiteGros = (TextField) champsGros.lookup("#gros-quantite");

                    try {
                        prixAchat = Double.parseDouble(txtPrixAchatGros.getText().trim());
                        prixVenteGros = Double.parseDouble(txtPrixVenteGros.getText().trim());
                        qte = Integer.parseInt(txtQuantiteGros.getText().trim());
                        quantiteGrosValue = 1;
                    } catch (NumberFormatException e) {
                        lblMsg.setText("❌ Format numérique invalide pour le mode Gros!");
                        return;
                    }

                    prixVente = prixVenteGros;

                    if (prixAchat <= 0) {
                        lblMsg.setText("❌ Le prix d'achat doit être positif!");
                        return;
                    }
                    if (prixVenteGros <= 0) {
                        lblMsg.setText("❌ Le prix de vente gros doit être positif!");
                        return;
                    }
                    if (prixVenteGros <= prixAchat) {
                        lblMsg.setText("❌ Le prix de vente doit être supérieur au prix d'achat!");
                        return;
                    }
                    break;

                case "Gros et Détail":
                    TextField txtPrixAchatMixte = (TextField) champsMixte.lookup("#mixte-prix-achat");
                    TextField txtQuantiteMixte = (TextField) champsMixte.lookup("#mixte-quantite");
                    TextField txtPrixVenteDetailMixte = (TextField) champsMixte.lookup("#mixte-prix-detail");
                    TextField txtPrixVenteGrosMixte = (TextField) champsMixte.lookup("#mixte-prix-gros");
                    TextField txtQuantiteGrosMixte = (TextField) champsMixte.lookup("#mixte-quantite-gros");

                    try {
                        prixAchat = Double.parseDouble(txtPrixAchatMixte.getText().trim());
                        qte = Integer.parseInt(txtQuantiteMixte.getText().trim());
                        double prixDetail = Double.parseDouble(txtPrixVenteDetailMixte.getText().trim());
                        prixVenteGros = Double.parseDouble(txtPrixVenteGrosMixte.getText().trim());
                        quantiteGrosValue = Integer.parseInt(txtQuantiteGrosMixte.getText().trim());

                        prixVente = prixDetail;
                    } catch (NumberFormatException e) {
                        lblMsg.setText("❌ Format numérique invalide pour le mode Mixte!");
                        return;
                    }

                    if (prixAchat <= 0) {
                        lblMsg.setText("❌ Le prix d'achat doit être positif!");
                        return;
                    }
                    if (prixVente <= 0) {
                        lblMsg.setText("❌ Le prix de vente détail doit être positif!");
                        return;
                    }
                    if (prixVenteGros <= 0) {
                        lblMsg.setText("❌ Le prix de vente gros doit être positif!");
                        return;
                    }
                    if (quantiteGrosValue <= 0) {
                        lblMsg.setText("❌ La quantité pour le prix gros doit être positive!");
                        return;
                    }
                    if (prixVenteGros <= prixAchat) {
                        lblMsg.setText("❌ Le prix de vente gros doit être supérieur au prix d'achat!");
                        return;
                    }
                    break;

                default:
                    lblMsg.setText("❌ Mode de vente non reconnu!");
                    return;
            }

            Produit nouveauProduit = new Produit(code, cbCategorie.getValue(), codeMesure, nom,
                    prixAchat, prixVente, qte, mode, LocalDate.now().format(DATE_FMT));

            if (mode.equals("Gros") || mode.equals("Gros et Détail")) {
                nouveauProduit.setPrixVenteGros(prixVenteGros);
                nouveauProduit.setQuantiteGros(quantiteGrosValue);
            } else {
                nouveauProduit.setPrixVenteGros(0);
                nouveauProduit.setQuantiteGros(1);
            }

            produits.add(nouveauProduit);
            FileManager.sauvegarderProduits(produits);

            String confirmation = String.format("✅ Produit enregistré!\n"
                    + "Code: %s | Nom: %s\n"
                    + "Mode: %s",
                    code, nom, mode);

            switch (mode) {
                case "Détail":
                    confirmation += String.format("\nPrix Achat: %.2f HTG | Prix Vente: %.2f HTG",
                            prixAchat, prixVente);
                    confirmation += String.format("\nMarge: %.2f HTG", prixVente - prixAchat);
                    confirmation += String.format("\nStock initial: %d unités", qte);
                    break;
                case "Gros":
                    confirmation += String.format("\nPrix Achat: %.2f HTG | Prix Vente: %.2f HTG",
                            prixAchat, prixVenteGros);
                    confirmation += String.format("\nMarge: %.2f HTG", prixVenteGros - prixAchat);
                    confirmation += String.format("\nStock initial: %d unités", qte);
                    break;
                case "Gros et Détail":
                    double prixUnitaireGros = prixVenteGros / quantiteGrosValue;
                    confirmation += String.format("\nPrix Achat (pour %d unités): %.2f HTG", quantiteGrosValue, prixAchat);
                    confirmation += String.format("\nPrix Détail unitaire: %.2f HTG", prixVente);
                    confirmation += String.format("\nPrix Gros (%d unités): %.2f HTG", quantiteGrosValue, prixVenteGros);
                    confirmation += String.format("\nPrix unitaire gros: %.2f HTG", prixUnitaireGros);
                    confirmation += String.format("\nStock initial: %d unités", qte);
                    break;
            }

            lblMsg.setText(confirmation);
            lblMsg.setStyle("-fx-text-fill: #28a745;");

            reinitialiserFormulaire(txtCode, cbCategorie, cbModeVente,
                    champsDetail, champsGros, champsMixte);

        } catch (Exception e) {
            lblMsg.setText("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reinitialiserFormulaire(TextField txtCode, ComboBox<String> cbCategorie,
            ComboBox<String> cbModeVente,
            GridPane champsDetail, GridPane champsGros, GridPane champsMixte) {

        String nouveauCode = GenerateurID.genererCodeProduit(cbCategorie.getValue());
        txtCode.setText(nouveauCode);

        String mode = cbModeVente.getValue();

        switch (mode) {
            case "Détail":
                TextField txtPrixAchatDetail = (TextField) champsDetail.lookup("#detail-prix-achat");
                TextField txtPrixVenteDetail = (TextField) champsDetail.lookup("#detail-prix-vente");
                TextField txtQuantiteDetail = (TextField) champsDetail.lookup("#detail-quantite");

                txtPrixAchatDetail.setText("0.00");
                txtPrixVenteDetail.setText("0.00");
                txtQuantiteDetail.setText("0");
                break;

            case "Gros":
                TextField txtPrixAchatGros = (TextField) champsGros.lookup("#gros-prix-achat");
                TextField txtPrixVenteGros = (TextField) champsGros.lookup("#gros-prix-vente");
                TextField txtQuantiteGros = (TextField) champsGros.lookup("#gros-quantite");

                txtPrixAchatGros.setText("0.00");
                txtPrixVenteGros.setText("0.00");
                txtQuantiteGros.setText("0");
                break;

            case "Gros et Détail":
                TextField txtPrixAchatMixte = (TextField) champsMixte.lookup("#mixte-prix-achat");
                TextField txtQuantiteMixte = (TextField) champsMixte.lookup("#mixte-quantite");
                TextField txtPrixVenteDetailMixte = (TextField) champsMixte.lookup("#mixte-prix-detail");
                TextField txtPrixVenteGrosMixte = (TextField) champsMixte.lookup("#mixte-prix-gros");
                TextField txtQuantiteGrosMixte = (TextField) champsMixte.lookup("#mixte-quantite-gros");

                txtPrixAchatMixte.setText("0.00");
                txtQuantiteMixte.setText("0");
                txtPrixVenteDetailMixte.setText("0.00");
                txtPrixVenteGrosMixte.setText("0.00");
                txtQuantiteGrosMixte.setText("0");
                break;
        }
    }

    private ScrollPane creerListe() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.getChildren().add(creerLabel("📋 LISTE DES PRODUITS - DÉTAIL & GROS (" + produits.size() + ")", 18, true));

        TableView<Produit> table = new TableView<>();
        table.setPrefHeight(450);

        table.getColumns().addAll(
                creerCol("Code", "code", 80),
                creerCol("Nom", "nom", 150),
                creerCol("Catégorie", "categorie", 100),
                creerColPrix("Prix Achat", "prixAchat"),
                creerColPrix("Prix Détail", "prixVente"),
                creerColPrix("Prix Gros", "prixVenteGros"),
                creerColQuantiteGros(),
                creerCol("Stock", "quantite", 80),
                creerColModeVente(),
                creerColStatutStock()
        );

        ObservableList<Produit> obs = FXCollections.observableArrayList(produits);
        table.setItems(obs);

        TextField txtFilter = new TextField();
        txtFilter.setPromptText("Filtrer par nom, code...");
        txtFilter.setPrefWidth(200);

        ComboBox<String> cbFilterCat = new ComboBox<>();
        cbFilterCat.getItems().addAll("Toutes catégories", "Pharmaceutique", "Cosmétique", "Médical", "Hygiène");
        cbFilterCat.setValue("Toutes catégories");

        ComboBox<String> cbFilterMode = new ComboBox<>();
        cbFilterMode.getItems().addAll("Tous modes", "Détail", "Gros", "Gros et Détail");
        cbFilterMode.setValue("Tous modes");

        Button btnFilter = creerBouton("🔍 Filtrer", null, "#3498db");
        Button btnReset = creerBouton("🗑️ Réinitialiser", null, "#95a5a6");

        btnFilter.setOnAction(e -> filtrerProduits(txtFilter, cbFilterCat, cbFilterMode, table, obs));
        btnReset.setOnAction(e -> {
            txtFilter.clear();
            cbFilterCat.setValue("Toutes catégories");
            cbFilterMode.setValue("Tous modes");
            table.setItems(obs);
        });

        HBox filterBox = new HBox(10,
                new Label("Filtres:"), txtFilter,
                new Label("Catégorie:"), cbFilterCat,
                new Label("Mode:"), cbFilterMode,
                btnFilter, btnReset);
        filterBox.setPadding(new Insets(10));

        VBox statsBox = creerStatsProduits();

        box.getChildren().addAll(filterBox, table, statsBox);
        return new ScrollPane(box);
    }

    private TableColumn<Produit, Integer> creerColQuantiteGros() {
        TableColumn<Produit, Integer> col = new TableColumn<>("Qté Gros");
        col.setCellValueFactory(new PropertyValueFactory<>("quantiteGros"));
        col.setPrefWidth(80);

        col.setCellFactory(c -> new TableCell<Produit, Integer>() {
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 1) {
                    setText("-");
                    setStyle("-fx-text-fill: #95a5a6;");
                } else {
                    setText(item.toString());
                }
            }
        });
        return col;
    }

    private void filtrerProduits(TextField txtFilter, ComboBox<String> cbFilterCat,
            ComboBox<String> cbFilterMode, TableView<Produit> table, ObservableList<Produit> obsOriginal) {
        String filter = txtFilter.getText().toLowerCase();
        String cat = cbFilterCat.getValue();
        String mode = cbFilterMode.getValue();

        ObservableList<Produit> filtered = FXCollections.observableArrayList();
        for (Produit p : produits) {
            boolean matchTexte = filter.isEmpty()
                    || p.getCode().toLowerCase().contains(filter)
                    || p.getNom().toLowerCase().contains(filter);

            boolean matchCat = cat.equals("Toutes catégories") || p.getCategorie().equals(cat);

            boolean matchMode = mode.equals("Tous modes") || p.getModeVente().equals(mode);

            if (matchTexte && matchCat && matchMode) {
                filtered.add(p);
            }
        }
        table.setItems(filtered);
    }

    private VBox creerFormModifier() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.getChildren().add(creerLabel("💰 MODIFIER LES PRIX - DÉTAIL & GROS", 18, true));

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(10));

        TextField txtCode = creerTextField("Code du produit");
        Label lblInfo = new Label();
        lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        VBox champsModif = new VBox(10);
        champsModif.setPadding(new Insets(10, 0, 0, 0));

        TextField txtPrixAchat = creerTextField("Laisser vide si inchangé");
        TextField txtPrixVenteDetail = creerTextField("Laisser vide si inchangé");
        TextField txtPrixVenteGros = creerTextField("Laisser vide si inchangé");
        TextField txtQuantiteGros = creerTextField("Laisser vide si inchangé");

        int r = 0;
        form.add(new Label("Code produit*:"), 0, r);
        form.add(txtCode, 1, r++);
        form.add(lblInfo, 1, r++);

        txtCode.textProperty().addListener((obs, old, val) -> {
            champsModif.getChildren().clear();

            if (val != null && !val.trim().isEmpty()) {
                Produit p = trouverProduit(val.trim().toUpperCase());
                if (p != null) {
                    StringBuilder info = new StringBuilder();
                    info.append(String.format("Produit: %s | Mode: %s\n",
                            p.getNom(), p.getModeVente()));

                    switch (p.getModeVente()) {
                        case "Détail":
                            info.append(String.format("Prix Achat: %.2f HTG | Prix Vente: %.2f HTG",
                                    p.getPrixAchat(), p.getPrixVente()));
                            champsModif.getChildren().addAll(
                                    new Label("Nouveau prix achat:"), txtPrixAchat,
                                    new Label("Nouveau prix vente:"), txtPrixVenteDetail
                            );
                            break;

                        case "Gros":
                            info.append(String.format("Prix Achat: %.2f HTG | Prix Vente: %.2f HTG pour %d unités",
                                    p.getPrixAchat(), p.getPrixVenteGros(), p.getQuantiteGros()));
                            champsModif.getChildren().addAll(
                                    new Label("Nouveau prix achat:"), txtPrixAchat,
                                    new Label("Nouveau prix vente:"), txtPrixVenteGros,
                                    new Label("Nouvelle quantité:"), txtQuantiteGros
                            );
                            break;

                        case "Gros et Détail":
                            info.append(String.format("Prix Achat: %.2f HTG\n", p.getPrixAchat()));
                            info.append(String.format("Prix Détail: %.2f HTG | Prix Gros: %.2f HTG pour %d unités\n",
                                    p.getPrixVente(), p.getPrixVenteGros(), p.getQuantiteGros()));
                            champsModif.getChildren().addAll(
                                    new Label("Nouveau prix achat:"), txtPrixAchat,
                                    new Label("Nouveau prix détail:"), txtPrixVenteDetail,
                                    new Label("Nouveau prix gros:"), txtPrixVenteGros,
                                    new Label("Nouvelle quantité gros:"), txtQuantiteGros
                            );
                            break;
                    }

                    lblInfo.setText(info.toString());
                    lblInfo.setStyle("-fx-text-fill: #666;");
                } else {
                    lblInfo.setText("❌ Produit introuvable");
                    lblInfo.setStyle("-fx-text-fill: #dc3545;");
                }
            } else {
                lblInfo.setText("");
                champsModif.getChildren().clear();
            }
        });

        Label lblMsg = new Label();
        Button btnModifier = creerBouton("💾 Modifier Prix",
                e -> modifierPrix(txtCode, txtPrixAchat, txtPrixVenteDetail,
                        txtPrixVenteGros, txtQuantiteGros, lblInfo, lblMsg), "#2196F3");
        btnModifier.setPrefHeight(40);

        box.getChildren().addAll(form, champsModif, lblMsg, btnModifier);
        return box;
    }

    private void modifierPrix(TextField txtCode, TextField txtPrixAchat, TextField txtPrixVenteDetail,
            TextField txtPrixVenteGros, TextField txtQuantiteGros,
            Label lblInfo, Label lblMsg) {

        Produit p = trouverProduit(txtCode.getText().trim().toUpperCase());
        if (p == null) {
            lblMsg.setText("❌ Produit introuvable!");
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
            return;
        }

        try {
            boolean modif = false;

            if (!txtPrixAchat.getText().trim().isEmpty()) {
                double prix = Double.parseDouble(txtPrixAchat.getText());
                if (prix > 0) {
                    p.setPrixAchat(prix);
                    modif = true;
                }
            }

            switch (p.getModeVente()) {
                case "Détail":
                    if (!txtPrixVenteDetail.getText().trim().isEmpty()) {
                        double prix = Double.parseDouble(txtPrixVenteDetail.getText());
                        if (prix > 0 && prix > p.getPrixAchat()) {
                            p.setPrixVente(prix);
                            modif = true;
                        }
                    }
                    break;

                case "Gros":
                    if (!txtPrixVenteGros.getText().trim().isEmpty()) {
                        double prix = Double.parseDouble(txtPrixVenteGros.getText());
                        if (prix > 0 && prix > p.getPrixAchat()) {
                            p.setPrixVenteGros(prix);
                            p.setPrixVente(prix);
                            modif = true;
                        }
                    }

                    if (!txtQuantiteGros.getText().trim().isEmpty()) {
                        int qte = Integer.parseInt(txtQuantiteGros.getText());
                        if (qte > 0) {
                            p.setQuantiteGros(qte);
                            modif = true;
                        }
                    }
                    break;

                case "Gros et Détail":
                    if (!txtPrixVenteDetail.getText().trim().isEmpty()) {
                        double prixDetail = Double.parseDouble(txtPrixVenteDetail.getText());
                        if (prixDetail > 0) {
                            p.setPrixVente(prixDetail);
                            modif = true;
                        }
                    }

                    if (!txtPrixVenteGros.getText().trim().isEmpty()) {
                        double prixGros = Double.parseDouble(txtPrixVenteGros.getText());
                        if (prixGros > 0 && prixGros > p.getPrixAchat()) {
                            p.setPrixVenteGros(prixGros);
                            modif = true;
                        }
                    }

                    if (!txtQuantiteGros.getText().trim().isEmpty()) {
                        int qte = Integer.parseInt(txtQuantiteGros.getText());
                        if (qte > 0) {
                            p.setQuantiteGros(qte);
                            modif = true;
                        }
                    }
                    break;
            }

            if (modif) {
                FileManager.sauvegarderProduits(produits);
                lblMsg.setText("✅ Prix modifiés avec succès!");
                lblMsg.setStyle("-fx-text-fill: #28a745;");
                txtCode.clear();
                txtPrixAchat.clear();
                txtPrixVenteDetail.clear();
                txtPrixVenteGros.clear();
                txtQuantiteGros.clear();
                lblInfo.setText("");
            } else {
                lblMsg.setText("ℹ️ Aucune modification effectuée.");
                lblMsg.setStyle("-fx-text-fill: #666;");
            }

        } catch (NumberFormatException e) {
            lblMsg.setText("❌ Prix invalides! Vérifiez les nombres.");
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
        }
    }

    private VBox creerFormInventaire() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.getChildren().add(creerLabel("📦 GÉRER L'INVENTAIRE", 18, true));

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);

        TextField txtCode = creerTextField("Code du produit");
        Label lblInfo = new Label();
        lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        TextField txtQte = creerTextField("Quantité à ajouter (négative pour retirer)");

        form.add(new Label("Code produit*:"), 0, 0);
        form.add(txtCode, 1, 0);
        form.add(lblInfo, 1, 1);
        form.add(new Label("Quantité:"), 0, 2);
        form.add(txtQte, 1, 2);

        txtCode.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.trim().isEmpty()) {
                Produit p = trouverProduit(val.trim().toUpperCase());
                if (p != null) {
                    String info = String.format("Produit: %s | Mode: %s\n",
                            p.getNom(), p.getModeVente());
                    info += String.format("Stock actuel: %d unités", p.getQuantite());

                    if (p.getModeVente().equals("Gros") || p.getModeVente().equals("Gros et Détail")) {
                        info += String.format("\nQuantité pour prix gros: %d unités", p.getQuantiteGros());
                    }

                    lblInfo.setText(info);
                }
            } else {
                lblInfo.setText("");
            }
        });

        Label lblMsg = new Label();
        Button btnAjuster = creerBouton("⚙️ Ajuster Stock",
                e -> ajusterStock(txtCode, txtQte, lblInfo, lblMsg), "#FF9800");
        btnAjuster.setPrefHeight(40);

        box.getChildren().addAll(form, lblMsg, btnAjuster);
        return box;
    }

    private void ajusterStock(TextField txtCode, TextField txtQte,
            Label lblInfo, Label lblMsg) {
        Produit p = trouverProduit(txtCode.getText().trim().toUpperCase());
        if (p == null) {
            lblMsg.setText("❌ Produit introuvable!");
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
            return;
        }

        try {
            int qte = Integer.parseInt(txtQte.getText());
            int nouveau = p.getQuantite() + qte;

            if (nouveau < 0) {
                lblMsg.setText(String.format("❌ Stock insuffisant! Actuel: %d unités", p.getQuantite()));
                lblMsg.setStyle("-fx-text-fill: #dc3545;");
                return;
            }

            p.setQuantite(nouveau);
            FileManager.sauvegarderProduits(produits);

            lblMsg.setText(String.format("✅ Stock ajusté! Nouveau: %d unités",
                    p.getQuantite()));
            lblMsg.setStyle("-fx-text-fill: #28a745;");

            txtCode.clear();
            txtQte.clear();
            lblInfo.setText("");

        } catch (NumberFormatException e) {
            lblMsg.setText("❌ Quantité invalide!");
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
        }
    }

    private VBox creerFormSupprimer() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.getChildren().add(creerLabel("🗑️ SUPPRIMER UN PRODUIT", 18, true));

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(10));

        TextField txtCode = creerTextField("Code du produit à supprimer");
        Label lblInfo = new Label();
        lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        CheckBox cbConfirmation = new CheckBox("Je confirme vouloir supprimer ce produit");
        cbConfirmation.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");

        form.add(new Label("Code Produit*:"), 0, 0);
        form.add(txtCode, 1, 0);
        form.add(lblInfo, 1, 1);
        form.add(cbConfirmation, 0, 2, 2, 1);

        txtCode.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.trim().isEmpty()) {
                Produit p = trouverProduit(val.trim().toUpperCase());
                if (p != null) {
                    String nomMesure = "";
                    for (Mesure m : mesures) {
                        if (m.getCode().equals(p.getCodeMesure())) {
                            nomMesure = m.getNom();
                            break;
                        }
                    }

                    String info = String.format(
                            "Produit: %s\n"
                            + "Catégorie: %s | Mesure: %s | Mode: %s\n",
                            p.getNom(), p.getCategorie(), nomMesure, p.getModeVente());

                    if (p.getModeVente().equals("Détail") || p.getModeVente().equals("Gros et Détail")) {
                        info += String.format("Prix Détail: %.2f HTG\n", p.getPrixVente());
                    }

                    if (p.getModeVente().equals("Gros") || p.getModeVente().equals("Gros et Détail")) {
                        info += String.format("Prix Gros: %.2f HTG pour %d unités\n",
                                p.getPrixVenteGros(), p.getQuantiteGros());
                    }

                    info += String.format("Stock actuel: %d unités",
                            p.getQuantite());

                    if (p.getQuantite() > 0) {
                        info += String.format("\n⚠️ ATTENTION: Ce produit a %d unités en stock!", p.getQuantite());
                        lblInfo.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        lblInfo.setStyle("-fx-text-fill: #666; -fx-font-weight: normal;");
                    }

                    lblInfo.setText(info);
                } else {
                    lblInfo.setText("❌ Produit introuvable");
                    lblInfo.setStyle("-fx-text-fill: #dc3545;");
                }
            } else {
                lblInfo.setText("");
                lblInfo.setStyle("-fx-text-fill: #666;");
            }
        });

        Label lblMsg = new Label();
        Button btnSupprimer = creerBouton("🗑️ Supprimer Définitivement",
                e -> supprimerProduit(txtCode, cbConfirmation, lblInfo, lblMsg), "#dc3545");
        btnSupprimer.setPrefHeight(40);
        btnSupprimer.setDisable(true);

        cbConfirmation.selectedProperty().addListener((obs, old, selected) -> {
            btnSupprimer.setDisable(!selected);
        });

        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffeaa7; -fx-border-radius: 5;");

        Label lblAvertissement = new Label("⚠️ MESURES DE SÉCURITÉ - SUPPRESSION PRODUIT");
        lblAvertissement.setStyle("-fx-font-weight: bold; -fx-text-fill: #856404;");

        TextArea txtAvertissement = new TextArea();
        txtAvertissement.setText("""
            AVANT DE SUPPRIMER UN PRODUIT, VEUILLEZ VÉRIFIER:
            
            1. Le produit n'est PAS en rupture de stock (idéal: stock = 0)
            2. Aucune vente en cours n'utilise ce produit
            3. Le produit n'est plus vendu/disponible
            4. Vous avez une RAISON VALABLE (ex: produit périmé, retiré du marché)
            
            RECOMMANDATIONS:
            • Pour les produits avec stock > 0 : Écoulez d'abord le stock
            • Pour les produits fréquemment vendus : Archivez plutôt que supprimer
            • Conservez un historique des produits supprimés
            
            ATTENTION: Cette action est IRREVERSIBLE!
            Toutes les données du produit seront définitivement effacées.
            Le code produit ne pourra PAS être réutilisé.
            """);
        txtAvertissement.setEditable(false);
        txtAvertissement.setWrapText(true);
        txtAvertissement.setPrefHeight(200);
        txtAvertissement.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        infoBox.getChildren().addAll(lblAvertissement, txtAvertissement);

        box.getChildren().addAll(form, infoBox, lblMsg, btnSupprimer);
        return box;
    }

    private void supprimerProduit(TextField txtCode, CheckBox cbConfirmation,
            Label lblInfo, Label lblMsg) {

        String codeProduit = txtCode.getText().trim().toUpperCase();

        if (!cbConfirmation.isSelected()) {
            lblMsg.setText("❌ Veuillez confirmer la suppression en cochant la case!");
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
            return;
        }

        Produit produit = trouverProduit(codeProduit);
        if (produit == null) {
            lblMsg.setText("❌ Produit introuvable!");
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
            return;
        }

        if (produit.getQuantite() > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("⚠️ STOCK DÉTECTÉ");
            alert.setHeaderText("Ce produit a du stock en inventaire!");
            alert.setContentText(String.format(
                    "Le produit '%s' a %d unités en stock.\n"
                    + "Valeur estimée du stock: %.2f HTG\n\n"
                    + "Êtes-vous ABSOLUMENT SURE de vouloir le supprimer?\n\n"
                    + "Recommandation: Écoulez d'abord le stock ou ajustez à 0.",
                    produit.getNom(), produit.getQuantite(),
                    produit.getQuantite() * produit.getPrixAchat()));

            ButtonType btnOui = new ButtonType("Supprimer quand même", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnNon = new ButtonType("Écouler stock d'abord", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType btnAnnuler = new ButtonType("Annuler", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(btnOui, btnNon, btnAnnuler);

            alert.showAndWait().ifPresent(response -> {
                if (response == btnOui) {
                    procederSuppression(produit, codeProduit, txtCode, cbConfirmation, lblInfo, lblMsg);
                } else if (response == btnNon) {
                    lblMsg.setText("ℹ️ Veuillez ajuster le stock à 0 via l'onglet 'Gérer Inventaire'.");
                    lblMsg.setStyle("-fx-text-fill: #ff9800;");
                } else {
                    lblMsg.setText("❌ Suppression annulée.");
                    lblMsg.setStyle("-fx-text-fill: #dc3545;");
                }
            });
        } else {
            procederSuppression(produit, codeProduit, txtCode, cbConfirmation, lblInfo, lblMsg);
        }
    }

    private void procederSuppression(Produit produit, String codeProduit, TextField txtCode,
            CheckBox cbConfirmation, Label lblInfo, Label lblMsg) {

        try {
            boolean supprime = produits.removeIf(p -> p.getCode().equalsIgnoreCase(codeProduit));

            if (supprime) {
                FileManager.sauvegarderProduits(produits);
                lblMsg.setText(String.format("✅ Produit '%s' (Code: %s) supprimé avec succès!\n"
                        + "Mode: %s | Stock final: %d unités",
                        produit.getNom(), produit.getCode(), produit.getModeVente(),
                        produit.getQuantite()));
                lblMsg.setStyle("-fx-text-fill: #28a745;");

                txtCode.clear();
                cbConfirmation.setSelected(false);
                lblInfo.setText("");

                afficherGestionProduits();
            } else {
                lblMsg.setText("❌ Erreur lors de la suppression!");
                lblMsg.setStyle("-fx-text-fill: #dc3545;");
            }

        } catch (Exception e) {
            lblMsg.setText("❌ Erreur système: " + e.getMessage());
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
        }
    }

    private TableColumn<Produit, String> creerColModeVente() {
        TableColumn<Produit, String> col = new TableColumn<>("Mode Vente");
        col.setCellValueFactory(new PropertyValueFactory<>("modeVente"));
        col.setPrefWidth(120);

        col.setCellFactory(c -> new TableCell<Produit, String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Détail":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; "
                                    + "-fx-background-color: #ebf5fb; -fx-alignment: center;");
                            break;
                        case "Gros":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; "
                                    + "-fx-background-color: #d5f4e6; -fx-alignment: center;");
                            break;
                        case "Gros et Détail":
                            setStyle("-fx-text-fill: #9b59b6; -fx-font-weight: bold; "
                                    + "-fx-background-color: #f4ecf7; -fx-alignment: center;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        return col;
    }

    private VBox creerStatsProduits() {
        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10; -fx-border-color: #dee2e6;");

        Label lblTitreStats = new Label("📊 STATISTIQUES PRODUITS");
        lblTitreStats.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c5aa0;");

        int totalProduits = produits.size();
        int produitsDetail = 0;
        int produitsGros = 0;
        int produitsMixte = 0;
        int totalStock = 0;
        double valeurStock = 0;

        for (Produit p : produits) {
            switch (p.getModeVente()) {
                case "Détail":
                    produitsDetail++;
                    break;
                case "Gros":
                    produitsGros++;
                    break;
                case "Gros et Détail":
                    produitsMixte++;
                    break;
            }
            totalStock += p.getQuantite();
            valeurStock += p.getQuantite() * p.getPrixAchat();
        }

        Label lblStatsDetail = new Label(String.format("Produits Détail: %d (%.0f%%)",
                produitsDetail, totalProduits > 0 ? (produitsDetail * 100.0 / totalProduits) : 0));
        Label lblStatsGros = new Label(String.format("Produits Gros: %d (%.0f%%)",
                produitsGros, totalProduits > 0 ? (produitsGros * 100.0 / totalProduits) : 0));
        Label lblStatsMixte = new Label(String.format("Produits Mixte: %d (%.0f%%)",
                produitsMixte, totalProduits > 0 ? (produitsMixte * 100.0 / totalProduits) : 0));
        Label lblStatsStock = new Label(String.format("Stock total: %d unités",
                totalStock));
        Label lblStatsValeur = new Label(String.format("Valeur stock: %.2f HTG", valeurStock));

        statsBox.getChildren().addAll(lblTitreStats, lblStatsDetail, lblStatsGros,
                lblStatsMixte, lblStatsStock, lblStatsValeur);
        return statsBox;
    }

    private TextField creerTextField(String prompt) {
        TextField txt = new TextField();
        txt.setPromptText(prompt);
        txt.setPrefHeight(35);
        return txt;
    }

    private ComboBox<String> creerCombo(String[] items, String defaut) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(items);
        cb.setValue(defaut);
        cb.setPrefHeight(35);
        return cb;
    }

    private ComboBox<String> creerComboMesures() {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPrefHeight(35);
        cb.setPrefWidth(200);
        for (Mesure m : mesures) {
            cb.getItems().add(m.getCode() + " - " + m.getNom());
        }
        if (!mesures.isEmpty()) {
            cb.setValue(mesures.get(0).getCode() + " - " + mesures.get(0).getNom());
        }
        return cb;
    }

    private TableColumn creerCol(String titre, String prop, int width) {
        TableColumn col = new TableColumn(titre);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(width);
        return col;
    }

    private TableColumn<Produit, Double> creerColPrix(String titre, String prop) {
        TableColumn<Produit, Double> col = new TableColumn<>(titre);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(120);

        col.setCellFactory(c -> new TableCell<Produit, Double>() {
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText("-");
                    setStyle("-fx-text-fill: #95a5a6;");
                } else {
                    setText(String.format("%.2f HTG", item));
                    setStyle("");
                }
            }
        });
        return col;
    }

    private TableColumn<Produit, String> creerColStatutStock() {
        TableColumn<Produit, String> col = new TableColumn<>("Statut Stock");
        col.setCellValueFactory(cellData -> {
            Produit p = cellData.getValue();
            int qte = p.getQuantite();
            String statut = "";

            if (qte == 0) {
                statut = "RUPTURE";
            } else if (qte < 10) {
                statut = "CRITIQUE";
            } else if (qte < 50) {
                statut = "FAIBLE";
            } else if (qte < 200) {
                statut = "NORMAL";
            } else {
                statut = "BON";
            }
            return new javafx.beans.property.SimpleStringProperty(statut);
        });

        col.setCellFactory(c -> new TableCell<Produit, String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "RUPTURE":
                            setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-background-color: #ffebee;");
                            break;
                        case "CRITIQUE":
                            setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold; -fx-background-color: #fff3e0;");
                            break;
                        case "FAIBLE":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "NORMAL":
                            setStyle("-fx-text-fill: #3498db;");
                            break;
                        case "BON":
                            setStyle("-fx-text-fill: #2ecc71;");
                            break;
                    }
                }
            }
        });
        col.setPrefWidth(100);
        return col;
    }

    private Tab creerTab(String titre, Region content) {
        Tab tab = new Tab(titre);
        tab.setContent(content);
        return tab;
    }

    private Label creerTitre(String texte) {
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c5aa0;");
        return lbl;
    }

    private Label creerLabel(String texte, int size, boolean bold) {
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-font-size: " + size + "px;" + (bold ? " -fx-font-weight: bold;" : ""));
        return lbl;
    }

    private Button creerBouton(String texte, javafx.event.EventHandler e, String couleur) {
        Button btn = new Button(texte);
        if (e != null) {
            btn.setOnAction(e);
        }
        btn.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; -fx-font-weight: bold;");
        return btn;
    }

    private Produit trouverProduit(String code) {
        for (Produit p : produits) {
            if (p.getCode().equalsIgnoreCase(code)) {
                return p;
            }
        }
        return null;
    }
}
