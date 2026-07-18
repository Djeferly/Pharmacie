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
import data.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class VenteController {

    private Stage stage;
    private ArrayList<Produit> produits;
    private ArrayList<Client> clients;
    private ArrayList<Vente> ventes;
    private MenuController menuCtrl;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public VenteController(Stage stage, ArrayList<Produit> produits, ArrayList<Client> clients,
            ArrayList<Vente> ventes, MenuController menuCtrl) {
        this.stage = stage;
        this.produits = produits;
        this.clients = clients;
        this.ventes = ventes;
        this.menuCtrl = menuCtrl;
    }

    public void afficherGestionVentes() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titre = creerTitre("💰 GESTION DES VENTES - DÉTAIL & GROS");

        Button btnActualiser = new Button("🔄 Actualiser");
        btnActualiser.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        btnActualiser.setTooltip(new Tooltip("Recharger les dernières données des ventes"));

        btnActualiser.setOnAction(e -> {
            btnActualiser.setDisable(true);
            String texteOriginal = btnActualiser.getText();
            btnActualiser.setText("🔄 Actualisation...");

            menuCtrl.rechargerDonnees("ventes");
            ventes = menuCtrl.getVentes();

            afficherGestionVentes();

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

        Tab tabNouvelleVente = creerTab("➕ Nouvelle vente", creerFormNouvelleVente());
        Tab tabToutesVentes = creerTab("📋 Toutes les ventes", creerListeVentes("Toutes"));
        Tab tabPaiements = creerTab("💳 Paiements Chèque/Carte", creerListeVentes("Electronique"));

        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabToutesVentes || newTab == tabPaiements) {
                menuCtrl.rechargerDonnees("ventes");
                ventes = menuCtrl.getVentes();
                rafraichirOngletVente((ScrollPane) newTab.getContent(),
                        newTab == tabToutesVentes ? "Toutes" : "Electronique");
            }
        });

        tabs.getTabs().addAll(tabNouvelleVente, tabToutesVentes, tabPaiements);

        HBox boutonsBox = new HBox(10);
        boutonsBox.setAlignment(Pos.CENTER_RIGHT);
        Button btnRetour = creerBouton("⬅️ Retour",
                e -> menuCtrl.afficherMenuPrincipal(), "#6c757d");
        boutonsBox.getChildren().addAll(btnRetour);

        root.getChildren().addAll(headerBox, tabs, boutonsBox);
        stage.setScene(new Scene(root, 1200, 800));
    }

    private void rafraichirOngletVente(ScrollPane scrollPane, String filtre) {
        VBox content = (VBox) scrollPane.getContent();
        content.getChildren().clear();

        String titre = filtre.equals("Toutes") ? "📋 TOUTES LES VENTES" : "💳 VENTES PAR CHÈQUE/CARTE";
        Label titreLabel = new Label(titre + " (" + ventes.size() + ")");
        titreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c5aa0;");
        content.getChildren().add(titreLabel);

        TableView<Vente> table = new TableView<>();
        table.setPrefHeight(500);
        table.getColumns().addAll(
                creerCol("ID Vente", "id", 100),
                creerColClient(),
                creerColNbProduits(),
                creerCol("Paiement", "modePaiement", 120),
                creerCol("Date", "dateVente", 100),
                creerColTotal(),
                creerColModeVente()
        );

        ObservableList<Vente> filtered = FXCollections.observableArrayList();
        for (Vente v : ventes) {
            if (filtre.equals("Toutes") || (filtre.equals("Electronique")
                    && (v.getModePaiement().contains("Chèque") || v.getModePaiement().contains("Carte")))) {
                filtered.add(v);
            }
        }
        table.setItems(filtered);

        TextField txtFilter = new TextField();
        txtFilter.setPromptText("Filtrer par ID, client ou produit...");
        txtFilter.setPrefWidth(250);

        ComboBox<String> cbFilterMode = new ComboBox<>();
        cbFilterMode.getItems().addAll("Tous modes", "Vente Détail", "Vente Gros", "Mixte");
        cbFilterMode.setValue("Tous modes");
        cbFilterMode.setPrefWidth(120);

        Button btnFilter = new Button("🔍 Filtrer");
        Button btnReset = new Button("🗑️ Réinitialiser");

        btnFilter.setOnAction(e -> {
            String filtreTexte = txtFilter.getText().toLowerCase();
            String filtreMode = cbFilterMode.getValue();

            ObservableList<Vente> resultat = FXCollections.observableArrayList();
            for (Vente v : ventes) {
                boolean matchTexte = filtreTexte.isEmpty()
                        || v.getId().toLowerCase().contains(filtreTexte)
                        || contientClient(v, filtreTexte)
                        || contientProduit(v, filtreTexte);

                boolean matchMode = filtreMode.equals("Tous modes")
                        || correspondMode(v, filtreMode);

                if (matchTexte && matchMode) {
                    resultat.add(v);
                }
            }
            table.setItems(resultat);
        });

        btnReset.setOnAction(e -> {
            txtFilter.clear();
            cbFilterMode.setValue("Tous modes");
            table.setItems(filtered);
        });

        HBox filterBox = new HBox(10, new Label("Filtres:"), txtFilter,
                new Label("Mode:"), cbFilterMode, btnFilter, btnReset);
        filterBox.setPadding(new Insets(10));

        content.getChildren().addAll(filterBox, table);
    }

    private VBox creerFormNouvelleVente() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        Label titre = new Label("NOUVELLE VENTE - GESTION DÉTAIL & GROS");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c5aa0;");

        ComboBox<String> cbClient = creerComboClient();
        ComboBox<String> cbPaiement = creerComboPaiement();

        HBox topSection = new HBox(20,
                creerSection("👤 INFORMATIONS CLIENT", cbClient, creerBoutonNouveauClient()),
                creerSection("💳 MODE DE PAIEMENT", cbPaiement)
        );

        TableView<Vente.ProduitVente> table = creerTableProduitsAmelioree();
        ObservableList<Vente.ProduitVente> lignes = FXCollections.observableArrayList();
        table.setItems(lignes);

        Label lblIdVente = new Label("ID Vente: " + GenerateurID.genererProchainIdVente());
        lblIdVente.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c5aa0;");

        Label lblTotal = new Label("Total: 0.00 HTG");
        lblTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ComboBox<String> cbProduit = creerComboProduitsAvecMode();
        ComboBox<String> cbModeProduit = new ComboBox<>();
        cbModeProduit.setPromptText("Mode de vente");
        cbModeProduit.setPrefWidth(120);
        cbModeProduit.setDisable(true);

        TextField txtQte = new TextField();
        txtQte.setPromptText("Quantité");
        txtQte.setPrefWidth(80);
        txtQte.setDisable(true);

        Label lblInfoProduit = new Label();
        lblInfoProduit.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        lblInfoProduit.setWrapText(true);

        cbProduit.setOnAction(e -> {
            if (cbProduit.getValue() != null) {
                String code = extraireCodeProduit(cbProduit.getValue());
                Produit p = trouverProduit(code);
                if (p != null) {
                    cbModeProduit.setDisable(false);
                    txtQte.setDisable(false);
                    mettreAJourModesDisponibles(p, cbModeProduit);
                    afficherInfosProduit(p, lblInfoProduit);
                }
            }
        });

        cbModeProduit.setOnAction(e -> {
            if (cbProduit.getValue() != null && cbModeProduit.getValue() != null) {
                String code = extraireCodeProduit(cbProduit.getValue());
                Produit p = trouverProduit(code);
                if (p != null) {
                    String mode = cbModeProduit.getValue();
                    mettreAJourInfosSelonMode(p, mode, lblInfoProduit, txtQte);
                }
            }
        });

        Button btnAjouter = new Button("➕ Ajouter");
        btnAjouter.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAjouter.setOnAction(e -> ajouterProduitAmeliore(cbProduit, cbModeProduit,
                txtQte, lignes, lblTotal, lblInfoProduit));

        VBox produitsSection = new VBox(10);
        Label lblProduits = new Label("🛒 AJOUTER DES PRODUITS - CHOIX DÉTAIL/GROS");
        lblProduits.setStyle("-fx-font-weight: bold;");

        HBox selectionBox = new HBox(10);
        selectionBox.getChildren().addAll(cbProduit, cbModeProduit, txtQte, btnAjouter);

        produitsSection.getChildren().addAll(lblIdVente, lblProduits,
                selectionBox, lblInfoProduit, table, lblTotal);

        HBox btnSection = new HBox(10);
        Button btnEnreg = creerBouton("✅ Enregistrer Vente",
                e -> enregistrerVenteAmelioree(cbClient, cbPaiement, lignes, lblTotal, lblIdVente), "#4CAF50");
        Button btnAnnuler = creerBouton("❌ Annuler",
                e -> reinitialiserVente(cbClient, cbPaiement, cbProduit, cbModeProduit,
                        txtQte, lignes, lblTotal, lblIdVente, lblInfoProduit), "#dc3545");

        btnSection.getChildren().addAll(btnEnreg, btnAnnuler);

        VBox statsBox = creerStatsVente(lignes);

        box.getChildren().addAll(titre, topSection, produitsSection, statsBox, btnSection);
        return box;
    }

    private String extraireCodeProduit(String valeurCombobox) {
        if (valeurCombobox == null) {
            return "";
        }
        String[] parts = valeurCombobox.split(" - ");
        return parts.length > 0 ? parts[0].replaceAll("[^A-Za-z0-9\\-]", "") : "";
    }

    private void mettreAJourModesDisponibles(Produit p, ComboBox<String> cbModeProduit) {
        cbModeProduit.getItems().clear();

        switch (p.getModeVente()) {
            case "Détail":
                cbModeProduit.getItems().add("Détail");
                cbModeProduit.setValue("Détail");
                break;
            case "Gros":
                cbModeProduit.getItems().add("Gros");
                cbModeProduit.setValue("Gros");
                break;
            case "Gros et Détail":
                cbModeProduit.getItems().addAll("Détail", "Gros");
                cbModeProduit.setValue("Détail");
                break;
        }
    }

    private void afficherInfosProduit(Produit p, Label lblInfo) {
        StringBuilder info = new StringBuilder();
        info.append("📊 INFORMATIONS PRODUIT:\n");
        info.append(String.format("• Nom: %s\n", p.getNom()));
        info.append(String.format("• Catégorie: %s\n", p.getCategorie()));
        info.append(String.format("• Stock total: %d unités\n", p.getQuantite()));

        if (p.getModeVente().equals("Gros et Détail")) {
            info.append(String.format("• Prix Détail: %.2f HTG/unité\n", p.getPrixVente()));
            info.append(String.format("• Prix Gros: %.2f HTG pour %d unités\n",
                    p.getPrixVenteGros(), p.getQuantiteGros()));
            info.append(String.format("• Prix unitaire gros: %.2f HTG\n",
                    p.getPrixVenteGros() / p.getQuantiteGros()));
        } else if (p.getModeVente().equals("Gros")) {
            info.append(String.format("• Prix Gros: %.2f HTG/unité\n", p.getPrixVenteGros()));
        } else {
            info.append(String.format("• Prix Détail: %.2f HTG/unité\n", p.getPrixVente()));
        }

        lblInfo.setText(info.toString());
        lblInfo.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 11px;");
    }

    private void mettreAJourInfosSelonMode(Produit p, String mode, Label lblInfo, TextField txtQte) {
        double prix = p.getPrixSelonMode(mode);
        String info = lblInfo.getText();

        if (mode.equals("Gros")) {
            if (p.getModeVente().equals("Gros et Détail") && p.getQuantiteGros() > 1) {
                info += String.format("\n💰 Prix appliqué: %.2f HTG pour %d unités (VENTE EN GROS)",
                        prix, p.getQuantiteGros());
                info += String.format("\n💡 Quantité doit être un multiple de %d", p.getQuantiteGros());
                txtQte.setPromptText("Multiples de " + p.getQuantiteGros());
            } else {
                info += String.format("\n💰 Prix appliqué: %.2f HTG (VENTE EN GROS)", prix);
                txtQte.setPromptText("Quantité");
            }
            lblInfo.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
        } else {
            info += String.format("\n💰 Prix appliqué: %.2f HTG (VENTE AU DÉTAIL)", prix);
            txtQte.setPromptText("Quantité");
            lblInfo.setStyle("-fx-text-fill: #3498db; -fx-font-size: 11px;");
        }

        lblInfo.setText(info);
    }

    private void ajouterProduitAmeliore(ComboBox<String> cbProduit, ComboBox<String> cbModeProduit,
            TextField txtQte, ObservableList<Vente.ProduitVente> lignes,
            Label lblTotal, Label lblInfoProduit) {

        if (cbProduit.getValue() == null || cbModeProduit.getValue() == null
                || txtQte.getText().isEmpty()) {
            msg("❌ Veuillez sélectionner un produit, un mode et saisir une quantité!");
            return;
        }

        try {
            String code = extraireCodeProduit(cbProduit.getValue());
            String mode = cbModeProduit.getValue();
            int qte = Integer.parseInt(txtQte.getText());

            Produit p = trouverProduit(code);
            if (p == null) {
                msg("❌ Produit introuvable!");
                return;
            }

            if (mode.equals("Gros") && p.getModeVente().equals("Gros et Détail") && p.getQuantiteGros() > 1) {
                if (qte % p.getQuantiteGros() != 0) {
                    msg(String.format("❌ Pour vendre en gros, la quantité doit être un multiple de %d!\n"
                            + "Quantité saisie: %d\n"
                            + "Multiples acceptés: %d, %d, %d, ...",
                            p.getQuantiteGros(), qte,
                            p.getQuantiteGros(), p.getQuantiteGros() * 2, p.getQuantiteGros() * 3));
                    return;
                }
            }

            if (!p.peutVendre(mode, qte)) {
                msg(String.format("❌ Stock insuffisant pour une vente en %s!\n"
                        + "Stock disponible: %d unités\n"
                        + "Quantité demandée: %d unités",
                        mode, p.getQuantite(), qte));
                return;
            }

            double prixUnitaire = p.getPrixSelonMode(mode);

            String nomAffiche = p.getNom();
            if (mode.equals("Gros") && p.getQuantiteGros() > 1) {
                int nombrePaquets = qte / p.getQuantiteGros();
                nomAffiche = p.getNom() + " [" + nombrePaquets + " x " + p.getQuantiteGros() + " unités]";
            } else {
                nomAffiche = p.getNom() + " [" + mode + "]";
            }

            Vente.ProduitVente ligne = new Vente.ProduitVente(
                    p.getCode(), nomAffiche, qte, prixUnitaire);

            lignes.add(ligne);

            lblTotal.setText(String.format("Total: %.2f HTG", calculerTotal(lignes)));

            String messageInfo;
            if (mode.equals("Gros") && p.getQuantiteGros() > 1) {
                int nombrePaquets = qte / p.getQuantiteGros();
                messageInfo = String.format(
                        "✅ AJOUTÉ: %d paquets de %d unités (%s) @ %.2f HTG/paquet = %.2f HTG",
                        nombrePaquets, p.getQuantiteGros(), mode, prixUnitaire, qte * prixUnitaire);
            } else {
                messageInfo = String.format(
                        "✅ AJOUTÉ: %d x %s (%s) @ %.2f HTG = %.2f HTG",
                        qte, p.getNom(), mode, prixUnitaire, qte * prixUnitaire);
            }

            lblInfoProduit.setText(messageInfo);
            lblInfoProduit.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

            txtQte.clear();

        } catch (NumberFormatException e) {
            msg("❌ Quantité invalide! Veuillez saisir un nombre entier.");
        }
    }

    private void enregistrerVenteAmelioree(ComboBox<String> cbClient, ComboBox<String> cbPaiement,
            ObservableList<Vente.ProduitVente> lignes, Label lblTotal, Label lblIdVente) {

        if (lignes.isEmpty()) {
            msg("❌ Ajoutez au moins un produit avant d'enregistrer!");
            return;
        }

        String idClient = "";
        if (cbClient.getValue() != null && !cbClient.getValue().startsWith("ANONYME")) {
            idClient = cbClient.getValue().split(" - ")[0];
        }

        String idVente = GenerateurID.genererProchainIdVente();

        Vente v = new Vente(idVente, idClient,
                new ArrayList<>(lignes), cbPaiement.getValue(), LocalDate.now().format(DATE_FMT));

        boolean erreurStock = false;
        StringBuilder erreurs = new StringBuilder();

        for (Vente.ProduitVente ligne : lignes) {
            Produit p = trouverProduit(ligne.getCodeProduit());
            if (p != null) {
                String mode = "Détail";
                if (ligne.getNomProduit().contains("[Gros]") || ligne.getNomProduit().contains("paquets")) {
                    mode = "Gros";
                }

                if (!p.deduireStock(mode, ligne.getQuantite())) {
                    erreurStock = true;
                    erreurs.append(String.format("• %s: Stock insuffisant pour vente en %s\n",
                            p.getNom(), mode));
                }
            }
        }

        if (erreurStock) {
            msg("❌ Erreur de stock! Vente annulée.\n\n" + erreurs.toString());
            return;
        }

        if (cbPaiement.getValue().equals("Crédit") && !idClient.isEmpty()) {
            Client c = trouverClient(idClient);
            if (c != null) {
                c.setMontantDette(c.getMontantDette() + v.getMontantTotal());
            }
        }

        ventes.add(v);

        FileManager.sauvegarderProduits(produits);
        FileManager.sauvegarderClients(clients);
        FileManager.sauvegarderVentes(ventes);

        Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
        confirmation.setTitle("✅ Vente enregistrée avec succès!");
        confirmation.setHeaderText("ID Vente: " + v.getId());
        confirmation.setContentText("Montant total: " + String.format("%.2f HTG", v.getMontantTotal()));
        confirmation.showAndWait();

        reinitialiserVente(cbClient, cbPaiement, null, null, null,
                lignes, lblTotal, lblIdVente, null);
    }

    private ScrollPane creerListeVentes(String filtre) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        String titre = filtre.equals("Toutes") ? "📋 TOUTES LES VENTES" : "💳 VENTES PAR CHÈQUE/CARTE";
        box.getChildren().add(new Label(titre + " (" + ventes.size() + ")"));

        TableView<Vente> table = new TableView<>();
        table.setPrefHeight(500);

        table.getColumns().addAll(
                creerCol("ID Vente", "id", 100),
                creerColClient(),
                creerColNbProduits(),
                creerCol("Paiement", "modePaiement", 120),
                creerCol("Date", "dateVente", 100),
                creerColTotal(),
                creerColModeVente()
        );

        ObservableList<Vente> filtered = FXCollections.observableArrayList();
        for (Vente v : ventes) {
            if (filtre.equals("Toutes") || (filtre.equals("Electronique")
                    && (v.getModePaiement().contains("Chèque") || v.getModePaiement().contains("Carte")))) {
                filtered.add(v);
            }
        }
        table.setItems(filtered);

        TextField txtFilter = new TextField();
        txtFilter.setPromptText("Filtrer par ID, client ou produit...");
        txtFilter.setPrefWidth(250);

        ComboBox<String> cbFilterMode = new ComboBox<>();
        cbFilterMode.getItems().addAll("Tous modes", "Vente Détail", "Vente Gros", "Mixte");
        cbFilterMode.setValue("Tous modes");
        cbFilterMode.setPrefWidth(120);

        Button btnFilter = new Button("🔍 Filtrer");
        Button btnReset = new Button("🗑️ Réinitialiser");

        btnFilter.setOnAction(e -> {
            String filtreTexte = txtFilter.getText().toLowerCase();
            String filtreMode = cbFilterMode.getValue();

            ObservableList<Vente> resultat = FXCollections.observableArrayList();
            for (Vente v : ventes) {
                boolean matchTexte = filtreTexte.isEmpty()
                        || v.getId().toLowerCase().contains(filtreTexte)
                        || contientClient(v, filtreTexte)
                        || contientProduit(v, filtreTexte);

                boolean matchMode = filtreMode.equals("Tous modes")
                        || correspondMode(v, filtreMode);

                if (matchTexte && matchMode) {
                    resultat.add(v);
                }
            }
            table.setItems(resultat);
        });

        btnReset.setOnAction(e -> {
            txtFilter.clear();
            cbFilterMode.setValue("Tous modes");
            table.setItems(filtered);
        });

        HBox filterBox = new HBox(10, new Label("Filtres:"), txtFilter,
                new Label("Mode:"), cbFilterMode, btnFilter, btnReset);
        filterBox.setPadding(new Insets(10));

        box.getChildren().addAll(filterBox, table);
        return new ScrollPane(box);
    }

    private boolean contientClient(Vente v, String filtre) {
        if (v.getIdClient() == null || v.getIdClient().isEmpty()) {
            return "anonyme".contains(filtre);
        }
        Client c = trouverClient(v.getIdClient());
        if (c != null) {
            return (c.getNom() + " " + c.getPrenom()).toLowerCase().contains(filtre);
        }
        return false;
    }

    private boolean contientProduit(Vente v, String filtre) {
        for (Vente.ProduitVente pv : v.getProduits()) {
            if (pv.getNomProduit().toLowerCase().contains(filtre)) {
                return true;
            }
        }
        return false;
    }

    private boolean correspondMode(Vente v, String filtreMode) {
        if (filtreMode.equals("Vente Détail")) {
            return estVenteDetail(v);
        } else if (filtreMode.equals("Vente Gros")) {
            return estVenteGros(v);
        } else if (filtreMode.equals("Mixte")) {
            return estVenteMixte(v);
        }
        return true;
    }

    private TableColumn<Vente, String> creerColModeVente() {
        TableColumn<Vente, String> col = new TableColumn<>("Mode");
        col.setCellValueFactory(cellData -> {
            Vente v = cellData.getValue();
            if (estVenteDetail(v)) {
                return new javafx.beans.property.SimpleStringProperty("DÉTAIL");
            } else if (estVenteGros(v)) {
                return new javafx.beans.property.SimpleStringProperty("GROS");
            } else {
                return new javafx.beans.property.SimpleStringProperty("MIXTE");
            }
        });
        col.setPrefWidth(80);
        col.setCellFactory(c -> new TableCell<Vente, String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "DÉTAIL":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; "
                                    + "-fx-background-color: #ebf5fb; -fx-alignment: center;");
                            break;
                        case "GROS":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; "
                                    + "-fx-background-color: #d5f4e6; -fx-alignment: center;");
                            break;
                        case "MIXTE":
                            setStyle("-fx-text-fill: #9b59b6; -fx-font-weight: bold; "
                                    + "-fx-background-color: #f4ecf7; -fx-alignment: center;");
                            break;
                    }
                }
            }
        });
        return col;
    }

    private TableView<Vente.ProduitVente> creerTableProduitsAmelioree() {
        TableView<Vente.ProduitVente> table = new TableView<>();
        table.setPrefHeight(250);

        TableColumn<Vente.ProduitVente, String> colCode = new TableColumn<>("Code");
        colCode.setCellValueFactory(new PropertyValueFactory<>("codeProduit"));
        colCode.setPrefWidth(80);

        TableColumn<Vente.ProduitVente, String> colNom = new TableColumn<>("Produit");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colNom.setPrefWidth(200);

        TableColumn<Vente.ProduitVente, Integer> colQte = new TableColumn<>("Quantité");
        colQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colQte.setPrefWidth(80);

        TableColumn<Vente.ProduitVente, String> colMode = new TableColumn<>("Mode");
        colMode.setCellValueFactory(cellData -> {
            String nom = cellData.getValue().getNomProduit();
            if (nom.contains("[Gros]") || nom.contains("paquets")) {
                return new javafx.beans.property.SimpleStringProperty("GROS");
            } else {
                return new javafx.beans.property.SimpleStringProperty("DÉTAIL");
            }
        });
        colMode.setCellFactory(c -> new TableCell<Vente.ProduitVente, String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("GROS")) {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; "
                                + "-fx-background-color: #d5f4e6; -fx-alignment: center;");
                    } else {
                        setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; "
                                + "-fx-background-color: #ebf5fb; -fx-alignment: center;");
                    }
                }
            }
        });
        colMode.setPrefWidth(80);

        TableColumn<Vente.ProduitVente, Double> colPrix = new TableColumn<>("Prix U.");
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colPrix.setCellFactory(c -> new TableCell<Vente.ProduitVente, Double>() {
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Vente.ProduitVente pv = getTableView().getItems().get(getIndex());
                    if (pv.getNomProduit().contains("[Gros]") || pv.getNomProduit().contains("paquets")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2980b9;");
                    }
                    setText(String.format("%.2f HTG", item));
                }
            }
        });
        colPrix.setPrefWidth(100);

        TableColumn<Vente.ProduitVente, Double> colSousTotal = new TableColumn<>("Sous-total");
        colSousTotal.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
        colSousTotal.setCellFactory(c -> new TableCell<Vente.ProduitVente, Double>() {
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f HTG", item));
            }
        });
        colSousTotal.setPrefWidth(120);

        TableColumn<Vente.ProduitVente, Void> colAction = new TableColumn<>("Action");
        colAction.setCellFactory(param -> new TableCell<Vente.ProduitVente, Void>() {
            private final Button btnSupprimer = new Button("🗑️");

            {
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px;");
                btnSupprimer.setOnAction(event -> {
                    Vente.ProduitVente produit = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(produit);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnSupprimer);
                }
            }
        });
        colAction.setPrefWidth(70);

        table.getColumns().addAll(colCode, colNom, colQte, colMode, colPrix, colSousTotal, colAction);
        return table;
    }

    private ComboBox<String> creerComboProduitsAvecMode() {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPromptText("Sélectionner un produit");
        cb.setPrefWidth(300);

        for (Produit p : produits) {
            if (p.getQuantite() > 0) {
                String affichage = String.format("%s - %s", p.getCode(), p.getNom());

                String infosQuantiteGros = "";
                if (p.getModeVente().equals("Gros et Détail") && p.getQuantiteGros() > 1) {
                    infosQuantiteGros = String.format(" (Qté gros: %d unités)", p.getQuantiteGros());
                }

                String infos = String.format(" (Stock: %d u | %s)%s",
                        p.getQuantite(), p.getModeVente(), infosQuantiteGros);

                String icone = "";
                switch (p.getModeVente()) {
                    case "Détail":
                        icone = "🛒 ";
                        break;
                    case "Gros":
                        icone = "📦 ";
                        break;
                    case "Gros et Détail":
                        icone = "🛒📦 ";
                        break;
                }

                cb.getItems().add(icone + affichage + infos);
            }
        }

        cb.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Détail")) {
                        setStyle("-fx-text-fill: #2980b9;");
                    } else if (item.contains("Gros") && !item.contains("Gros et Détail")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if (item.contains("Gros et Détail")) {
                        setStyle("-fx-text-fill: #8e44ad; -fx-font-weight: bold;");
                    }
                }
            }
        });

        return cb;
    }

    private VBox creerStatsVente(ObservableList<Vente.ProduitVente> lignes) {
        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10; -fx-border-color: #dee2e6;");

        Label lblTitreStats = new Label("📊 STATISTIQUES DE LA VENTE");
        lblTitreStats.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c5aa0;");

        Label lblNbProduits = new Label("Nombre de produits: 0");
        Label lblNbArticles = new Label("Nombre d'articles: 0 unités");
        Label lblMoyenne = new Label("Prix moyen par produit: 0.00 HTG");
        Label lblMode = new Label("Mode principal: Non défini");

        lignes.addListener((ListChangeListener<Vente.ProduitVente>) change -> {
            int nbProduits = lignes.size();
            int nbArticles = lignes.stream().mapToInt(Vente.ProduitVente::getQuantite).sum();
            double total = calculerTotal(lignes);
            double moyenne = nbProduits > 0 ? total / nbProduits : 0;

            long nbDetail = lignes.stream()
                    .filter(pv -> !pv.getNomProduit().contains("[Gros]") && !pv.getNomProduit().contains("paquets"))
                    .count();
            long nbGros = lignes.stream()
                    .filter(pv -> pv.getNomProduit().contains("[Gros]") || pv.getNomProduit().contains("paquets"))
                    .count();

            String modePrincipal;
            if (nbDetail > nbGros) {
                modePrincipal = "DÉTAIL";
            } else if (nbGros > nbDetail) {
                modePrincipal = "GROS";
            } else if (nbDetail == 0 && nbGros == 0) {
                modePrincipal = "Non défini";
            } else {
                modePrincipal = "MIXTE";
            }

            lblNbProduits.setText(String.format("Nombre de produits: %d", nbProduits));
            lblNbArticles.setText(String.format("Nombre d'articles: %d unités", nbArticles));
            lblMoyenne.setText(String.format("Prix moyen par produit: %.2f HTG", moyenne));
            lblMode.setText(String.format("Mode principal: %s", modePrincipal));
        });

        statsBox.getChildren().addAll(lblTitreStats, lblNbProduits, lblNbArticles, lblMoyenne, lblMode);
        return statsBox;
    }

    private VBox creerSection(String titre, Control... controls) {
        VBox box = new VBox(10);
        Label lbl = new Label(titre);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c5aa0;");
        box.getChildren().add(lbl);
        box.getChildren().addAll(controls);
        return box;
    }

    private void reinitialiserVente(ComboBox<String> cbClient, ComboBox<String> cbPaiement,
            ComboBox<String> cbProduit, ComboBox<String> cbModeProduit,
            TextField txtQte, ObservableList<Vente.ProduitVente> lignes,
            Label lblTotal, Label lblIdVente, Label lblInfoProduit) {

        lignes.clear();
        lblTotal.setText("Total: 0.00 HTG");
        cbClient.setValue(null);
        cbPaiement.setValue("Cash");

        if (cbProduit != null) {
            cbProduit.setValue(null);
        }

        if (cbModeProduit != null) {
            cbModeProduit.getItems().clear();
            cbModeProduit.setDisable(true);
            cbModeProduit.setValue(null);
        }

        if (txtQte != null) {
            txtQte.clear();
            txtQte.setDisable(true);
        }

        if (lblInfoProduit != null) {
            lblInfoProduit.setText("");
        }

        lblIdVente.setText("ID Vente: " + GenerateurID.genererProchainIdVente());
    }

    private boolean estVenteDetail(Vente v) {
        int countDetail = 0;
        int countGros = 0;

        for (Vente.ProduitVente pv : v.getProduits()) {
            if (pv.getNomProduit().contains("[Gros]") || pv.getNomProduit().contains("paquets")) {
                countGros++;
            } else {
                countDetail++;
            }
        }

        return countDetail > 0 && countGros == 0;
    }

    private boolean estVenteGros(Vente v) {
        int countDetail = 0;
        int countGros = 0;

        for (Vente.ProduitVente pv : v.getProduits()) {
            if (pv.getNomProduit().contains("[Gros]") || pv.getNomProduit().contains("paquets")) {
                countGros++;
            } else {
                countDetail++;
            }
        }

        return countGros > 0 && countDetail == 0;
    }

    private boolean estVenteMixte(Vente v) {
        boolean hasDetail = false;
        boolean hasGros = false;

        for (Vente.ProduitVente pv : v.getProduits()) {
            if (pv.getNomProduit().contains("[Gros]") || pv.getNomProduit().contains("paquets")) {
                hasGros = true;
            } else {
                hasDetail = true;
            }

            if (hasDetail && hasGros) {
                return true;
            }
        }

        return hasDetail && hasGros;
    }

    private double calculerTotal(ObservableList<Vente.ProduitVente> lignes) {
        double total = 0;
        for (Vente.ProduitVente l : lignes) {
            total += l.getSousTotal();
        }
        return total;
    }

    private ComboBox<String> creerComboClient() {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPromptText("Sélectionner un client");
        cb.getItems().add("👤 ANONYME - Client non enregistré");
        for (Client c : clients) {
            cb.getItems().add("👤 " + c.getId() + " - " + c.getNom() + " " + c.getPrenom());
        }
        return cb;
    }

    private ComboBox<String> creerComboPaiement() {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll("💵 Cash", "📝 Chèque", "💳 Carte crédit", "💳 Carte débit", "📋 Crédit");
        cb.setValue("💵 Cash");
        return cb;
    }

    private Button creerBoutonNouveauClient() {
        Button btn = new Button("➕ Nouveau Client");
        btn.setOnAction(e -> new ClientController(stage, clients, menuCtrl).afficherGestionClients());
        return btn;
    }

    private TableColumn creerCol(String titre, String prop, int width) {
        TableColumn col = new TableColumn(titre);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(width);
        return col;
    }

    private TableColumn creerColClient() {
        TableColumn<Vente, String> col = new TableColumn<>("Client");
        col.setCellValueFactory(cellData -> {
            String id = cellData.getValue().getIdClient();
            if (id == null || id.isEmpty()) {
                return new javafx.beans.property.SimpleStringProperty("ANONYME");
            }
            Client c = trouverClient(id);
            return new javafx.beans.property.SimpleStringProperty(
                    c != null ? c.getNom() + " " + c.getPrenom() : "");
        });
        col.setPrefWidth(150);
        return col;
    }

    private TableColumn creerColNbProduits() {
        TableColumn<Vente, Integer> col = new TableColumn<>("Nb Produits");
        col.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleIntegerProperty(
                        cellData.getValue().getProduits().size()).asObject());
        col.setPrefWidth(100);
        return col;
    }

    private TableColumn<Vente, Double> creerColTotal() {
        TableColumn<Vente, Double> col = new TableColumn<>("Total (HTG)");
        col.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        col.setPrefWidth(120);
        col.setCellFactory(c -> new TableCell<Vente, Double>() {
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });
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

    private Button creerBouton(String texte, javafx.event.EventHandler e, String couleur) {
        Button btn = new Button(texte);
        btn.setOnAction(e);
        btn.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; -fx-font-weight: bold;");
        return btn;
    }

    private Produit trouverProduit(String code) {
        for (Produit p : produits) {
            if (p.getCode().equals(code)) {
                return p;
            }
        }
        return null;
    }

    private Client trouverClient(String id) {
        for (Client c : clients) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    private void msg(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
