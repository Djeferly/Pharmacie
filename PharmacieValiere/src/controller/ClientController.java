package controller;

import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Client;
import data.*;
import java.util.ArrayList;

public class ClientController {

    private Stage stage;
    private ArrayList<Client> clients;
    private MenuController menuCtrl;

    public ClientController(Stage stage, ArrayList<Client> clients, MenuController menuCtrl) {
        this.stage = stage;
        this.clients = clients;
        this.menuCtrl = menuCtrl;
    }

    public void afficherGestionClients() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // ====================================================
        // EN-TÊTE AVEC TITRE ET BOUTON D'ACTUALISATION
        // ====================================================
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titre = creerTitre("👥 GESTION DES CLIENTS");

        // Bouton d'actualisation
        Button btnActualiser = new Button("🔄 Actualiser");
        btnActualiser.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand;");
        btnActualiser.setTooltip(new Tooltip("Recharger les dernières données des clients"));

        btnActualiser.setOnAction(e -> {
            // Animation de désactivation temporaire
            btnActualiser.setDisable(true);
            String texteOriginal = btnActualiser.getText();
            btnActualiser.setText("🔄 Actualisation...");

            // Recharger les données
            menuCtrl.rechargerDonnees("clients");
            clients = menuCtrl.getClients();

            // Rafraîchir l'écran
            afficherGestionClients();

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

        // Création des onglets de navigation
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabs.getTabs().addAll(
                creerTab("➕ Ajouter Client", creerFormAjout()),
                creerTab("📋 Tous les clients", creerListe(false)),
                creerTab("💰 Clients avec dettes", creerListe(true)),
                creerTab("💳 Payer dette", creerFormPayer()),
                creerTab("🗑️ Supprimer Client", creerFormSupprimer())
        );

        // Rafraîchir les onglets de liste quand on y accède
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab.getText().equals("📋 Tous les clients") || newTab.getText().equals("💰 Clients avec dettes")) {
                menuCtrl.rechargerDonnees("clients");
                clients = menuCtrl.getClients();
                boolean filtreDettes = newTab.getText().equals("💰 Clients avec dettes");
                rafraichirListeClients((ScrollPane) newTab.getContent(), filtreDettes);
            }
        });

        // Bouton de retour au menu principal
        Button btnRetour = creerBouton("⬅️ Retour",
                e -> menuCtrl.afficherMenuPrincipal(), "#6c757d");

        // Assemblage final
        root.getChildren().addAll(headerBox, tabs, btnRetour);
        stage.setScene(new Scene(root, 1100, 700));
    }

    /**
     * Rafraîchit une liste de clients
     */
    private void rafraichirListeClients(ScrollPane scrollPane, boolean filtreDettes) {
        VBox content = (VBox) scrollPane.getContent();
        content.getChildren().clear();

        // Titre dynamique selon le filtre
        String titre = filtreDettes ? "💰 CLIENTS AVEC DETTES" : "📋 TOUS LES CLIENTS";
        int nb = filtreDettes ? (int) clients.stream().filter(c -> c.getMontantDette() > 0).count() : clients.size();
        Label titreLabel = creerLabel(titre + " (" + nb + ")", 18, true);
        content.getChildren().add(titreLabel);

        // Recréation de la table
        TableView<Client> table = new TableView<>();
        table.setPrefHeight(400);

        table.getColumns().addAll(
                creerCol("ID", "id", 80),
                creerCol("Nom", "nom", 120),
                creerCol("Prénom", "prenom", 120),
                creerCol("Téléphone", "telephone", 120),
                creerCol("Type", "type", 100),
                creerColDette()
        );

        // Filtrage des données
        ObservableList<Client> filtered = FXCollections.observableArrayList();
        for (Client c : clients) {
            if (!filtreDettes || c.getMontantDette() > 0) {
                filtered.add(c);
            }
        }
        table.setItems(filtered);

        // Barre de filtrage
        TextField txtFilter = new TextField();
        txtFilter.setPromptText("Filtrer par nom, prénom, ID...");
        txtFilter.setPrefWidth(200);

        Button btnFilter = new Button("🔍 Filtrer");
        Button btnReset = new Button("🗑️ Réinitialiser");

        btnFilter.setOnAction(e -> filtrer(txtFilter, table, filtered, filtreDettes));
        btnReset.setOnAction(e -> {
            txtFilter.clear();
            table.setItems(filtered);
        });

        HBox filterBox = new HBox(10, new Label("Filtre:"), txtFilter, btnFilter, btnReset);
        filterBox.setPadding(new Insets(10));

        content.getChildren().addAll(filterBox, table);
    }

    private VBox creerFormAjout() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.getChildren().add(creerLabel("➕ NOUVEAU CLIENT", 18, true));

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(10));

        TextField txtId = new TextField();
        txtId.setDisable(true);
        txtId.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #495057;");
        txtId.setPrefHeight(35);
        txtId.setText(GenerateurID.genererProchainIdClient());

        TextField txtNom = creerTextField("Nom du client");
        TextField txtPrenom = creerTextField("Prénom du client");
        TextField txtAdresse = creerTextField("Adresse complète");
        TextField txtTel = creerTextField("Format: 509XXXXXXXX");
        TextField txtEmail = creerTextField("email@exemple.com");
        ComboBox<String> cbType = creerCombo(new String[]{"Aucun", "À crédit"}, "Aucun");

        int r = 0;
        form.add(new Label("ID:"), 0, r);
        form.add(txtId, 1, r++);
        form.add(new Label("Nom*:"), 0, r);
        form.add(txtNom, 1, r++);
        form.add(new Label("Prénom*:"), 0, r);
        form.add(txtPrenom, 1, r++);
        form.add(new Label("Adresse:"), 0, r);
        form.add(txtAdresse, 1, r++);
        form.add(new Label("Téléphone:"), 0, r);
        form.add(txtTel, 1, r++);
        form.add(new Label("Email:"), 0, r);
        form.add(txtEmail, 1, r++);
        form.add(new Label("Type:"), 0, r);
        form.add(cbType, 1, r++);

        Label lblMsg = new Label();

        Button btnAjouter = creerBouton("✅ Enregistrer Client",
                e -> ajouterClient(txtId, txtNom, txtPrenom, txtAdresse, txtTel, txtEmail, cbType, lblMsg), "#4CAF50");
        btnAjouter.setPrefHeight(40);

        box.getChildren().addAll(form, lblMsg, btnAjouter);
        return box;
    }

    private void ajouterClient(TextField txtId, TextField txtNom, TextField txtPrenom,
            TextField txtAdresse, TextField txtTel, TextField txtEmail,
            ComboBox<String> cbType, Label lblMsg) {

        String id = txtId.getText().trim();
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty()) {
            lblMsg.setText("❌ Nom et Prénom obligatoires!");
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
            return;
        }

        for (Client c : clients) {
            if (c.getId().equals(id)) {
                id = GenerateurID.genererProchainIdClient();
                txtId.setText(id);
            }
        }

        clients.add(new Client(id, nom, prenom, txtAdresse.getText(),
                txtTel.getText(), txtEmail.getText(), cbType.getValue(), 0.0));

        FileManager.sauvegarderClients(clients);

        lblMsg.setText("✅ Client enregistré! ID: " + id);
        lblMsg.setStyle("-fx-text-fill: #28a745;");

        txtId.setText(GenerateurID.genererProchainIdClient());
        txtNom.clear();
        txtPrenom.clear();
        txtAdresse.clear();
        txtTel.clear();
        txtEmail.clear();

        afficherGestionClients();
    }

    private ScrollPane creerListe(boolean dettes) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        String titre = dettes ? "💰 CLIENTS AVEC DETTES" : "📋 TOUS LES CLIENTS";
        int nb = dettes ? (int) clients.stream().filter(c -> c.getMontantDette() > 0).count() : clients.size();
        box.getChildren().add(creerLabel(titre + " (" + nb + ")", 18, true));

        TableView<Client> table = new TableView<>();
        table.setPrefHeight(400);

        table.getColumns().addAll(
                creerCol("ID", "id", 80),
                creerCol("Nom", "nom", 120),
                creerCol("Prénom", "prenom", 120),
                creerCol("Téléphone", "telephone", 120),
                creerCol("Type", "type", 100),
                creerColDette()
        );

        ObservableList<Client> filtered = FXCollections.observableArrayList();
        for (Client c : clients) {
            if (!dettes || c.getMontantDette() > 0) {
                filtered.add(c);
            }
        }
        table.setItems(filtered);

        TextField txtFilter = new TextField();
        txtFilter.setPromptText("Filtrer par nom, prénom, ID...");
        txtFilter.setPrefWidth(200);

        Button btnFilter = new Button("🔍 Filtrer");
        Button btnReset = new Button("🗑️ Réinitialiser");

        btnFilter.setOnAction(e -> filtrer(txtFilter, table, filtered, dettes));
        btnReset.setOnAction(e -> {
            txtFilter.clear();
            table.setItems(filtered);
        });

        HBox filterBox = new HBox(10, new Label("Filtre:"), txtFilter, btnFilter, btnReset);
        filterBox.setPadding(new Insets(10));

        box.getChildren().addAll(filterBox, table);
        return new ScrollPane(box);
    }

    private void filtrer(TextField txtFilter, TableView<Client> table,
            ObservableList<Client> filtered, boolean dettes) {
        String filter = txtFilter.getText().toLowerCase();
        ObservableList<Client> result = FXCollections.observableArrayList();

        for (Client c : clients) {
            if (!dettes || c.getMontantDette() > 0) {
                boolean match = filter.isEmpty()
                        || c.getId().toLowerCase().contains(filter)
                        || c.getNom().toLowerCase().contains(filter)
                        || c.getPrenom().toLowerCase().contains(filter);
                if (match) {
                    result.add(c);
                }
            }
        }
        table.setItems(result);
    }

    private VBox creerFormPayer() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.getChildren().add(creerLabel("💳 PAYER UNE DETTE CLIENT", 18, true));

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);

        TextField txtId = creerTextField("ID du client");
        Label lblInfo = new Label();
        lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        TextField txtMontant = creerTextField("Montant à payer");

        form.add(new Label("ID Client*:"), 0, 0);
        form.add(txtId, 1, 0);
        form.add(lblInfo, 1, 1);
        form.add(new Label("Montant*:"), 0, 2);
        form.add(txtMontant, 1, 2);

        txtId.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.trim().isEmpty()) {
                Client c = trouverClient(val.trim().toUpperCase());
                if (c != null) {
                    lblInfo.setText(String.format("👤 Client: %s %s | 💰 Dette: %.2f HTG",
                            c.getNom(), c.getPrenom(), c.getMontantDette()));
                    lblInfo.setStyle("-fx-text-fill: #666;");
                } else {
                    lblInfo.setText("❌ Client introuvable");
                    lblInfo.setStyle("-fx-text-fill: #dc3545;");
                }
            } else {
                lblInfo.setText("");
            }
        });

        Label lblMsg = new Label();

        Button btnPayer = creerBouton("💳 Payer Dette",
                e -> payerDette(txtId, txtMontant, lblInfo, lblMsg), "#4CAF50");
        btnPayer.setPrefHeight(40);

        box.getChildren().addAll(form, lblMsg, btnPayer);
        return box;
    }

    private void payerDette(TextField txtId, TextField txtMontant, Label lblInfo, Label lblMsg) {
        Client c = trouverClient(txtId.getText().trim().toUpperCase());
        if (c == null) {
            lblMsg.setText("❌ Client introuvable!");
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
            return;
        }

        try {
            double montant = Double.parseDouble(txtMontant.getText());

            if (montant <= 0) {
                lblMsg.setText("❌ Montant doit être positif!");
                lblMsg.setStyle("-fx-text-fill: #dc3545;");
                return;
            }

            if (montant > c.getMontantDette()) {
                lblMsg.setText(String.format("❌ Montant supérieur à la dette! Dette: %.2f HTG",
                        c.getMontantDette()));
                lblMsg.setStyle("-fx-text-fill: #dc3545;");
                return;
            }

            c.setMontantDette(c.getMontantDette() - montant);

            FileManager.sauvegarderClients(clients);

            lblMsg.setText(String.format("✅ Paiement effectué! 💰 Reste: %.2f HTG", c.getMontantDette()));
            lblMsg.setStyle("-fx-text-fill: #28a745;");

            txtId.clear();
            txtMontant.clear();
            lblInfo.setText("");

        } catch (NumberFormatException e) {
            lblMsg.setText("❌ Montant invalide!");
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
        }
    }

    private VBox creerFormSupprimer() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.getChildren().add(creerLabel("🗑️ SUPPRIMER UN CLIENT", 18, true));

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(10));

        TextField txtId = creerTextField("ID du client à supprimer");
        Label lblInfo = new Label();
        lblInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        CheckBox cbConfirmation = new CheckBox("Je confirme vouloir supprimer ce client");
        cbConfirmation.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");

        form.add(new Label("ID Client*:"), 0, 0);
        form.add(txtId, 1, 0);
        form.add(lblInfo, 1, 1);
        form.add(cbConfirmation, 0, 2, 2, 1);

        txtId.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.trim().isEmpty()) {
                Client c = trouverClient(val.trim().toUpperCase());
                if (c != null) {
                    String info = String.format("👤 Client: %s %s\n"
                            + "💰 Dette: %.2f HTG | 📱 Tél: %s",
                            c.getNom(), c.getPrenom(), c.getMontantDette(), c.getTelephone());

                    if (c.getMontantDette() > 0) {
                        info += "\n⚠️ ATTENTION: Ce client a une dette impayée!";
                        lblInfo.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        lblInfo.setStyle("-fx-text-fill: #666; -fx-font-weight: normal;");
                    }

                    lblInfo.setText(info);
                } else {
                    lblInfo.setText("❌ Client introuvable");
                    lblInfo.setStyle("-fx-text-fill: #dc3545;");
                }
            } else {
                lblInfo.setText("");
                lblInfo.setStyle("-fx-text-fill: #666;");
            }
        });

        Label lblMsg = new Label();

        Button btnSupprimer = creerBouton("🗑️ Supprimer Définitivement",
                e -> supprimerClient(txtId, cbConfirmation, lblInfo, lblMsg), "#dc3545");
        btnSupprimer.setPrefHeight(40);
        btnSupprimer.setDisable(true);

        cbConfirmation.selectedProperty().addListener((obs, old, selected) -> {
            btnSupprimer.setDisable(!selected);
        });

        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffeaa7; -fx-border-radius: 5;");

        Label lblAvertissement = new Label("⚠️ MESURES DE SÉCURITÉ");
        lblAvertissement.setStyle("-fx-font-weight: bold; -fx-text-fill: #856404;");

        TextArea txtAvertissement = new TextArea();
        txtAvertissement.setText("""
            AVANT DE SUPPRIMER UN CLIENT, VEUILLEZ VÉRIFIER:
            
            1. Le client n'a AUCUNE dette impayée
            2. Aucune vente en cours n'est associée à ce client
            
            ATTENTION: Cette action est IRREVERSIBLE!
            Toutes les données du client seront définitivement effacées.
            """);
        txtAvertissement.setEditable(false);
        txtAvertissement.setWrapText(true);
        txtAvertissement.setPrefHeight(150);
        txtAvertissement.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        infoBox.getChildren().addAll(lblAvertissement, txtAvertissement);

        box.getChildren().addAll(form, infoBox, lblMsg, btnSupprimer);
        return box;
    }

    private void supprimerClient(TextField txtId, CheckBox cbConfirmation,
            Label lblInfo, Label lblMsg) {

        String idClient = txtId.getText().trim().toUpperCase();

        if (!cbConfirmation.isSelected()) {
            lblMsg.setText("❌ Veuillez confirmer la suppression en cochant la case!");
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
            return;
        }

        Client client = trouverClient(idClient);
        if (client == null) {
            lblMsg.setText("❌ Client introuvable!");
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
            return;
        }

        if (client.getMontantDette() > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("⚠️ DETTE IMPAYÉE DÉTECTÉE");
            alert.setHeaderText("Ce client a une dette impayée!");
            alert.setContentText(String.format(
                    "Le client %s %s a une dette de %.2f HTG.\n"
                    + "Êtes-vous ABSOLUMENT SURE de vouloir le supprimer?\n\n"
                    + "Recommandation: Faites d'abord payer la dette.",
                    client.getNom(), client.getPrenom(), client.getMontantDette()));

            ButtonType btnOui = new ButtonType("Supprimer quand même", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnNon = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnOui, btnNon);

            alert.showAndWait().ifPresent(response -> {
                if (response == btnOui) {
                    procederSuppression(client, idClient, txtId, cbConfirmation, lblInfo, lblMsg);
                } else {
                    lblMsg.setText("❌ Suppression annulée à cause de la dette impayée.");
                    lblMsg.setStyle("-fx-text-fill: #dc3545;");
                }
            });
        } else {
            procederSuppression(client, idClient, txtId, cbConfirmation, lblInfo, lblMsg);
        }
    }

    private void procederSuppression(Client client, String idClient, TextField txtId,
            CheckBox cbConfirmation, Label lblInfo, Label lblMsg) {

        try {
            boolean supprime = clients.removeIf(c -> c.getId().equalsIgnoreCase(idClient));

            if (supprime) {
                FileManager.sauvegarderClients(clients);

                lblMsg.setText(String.format("✅ Client %s %s supprimé avec succès!",
                        client.getNom(), client.getPrenom()));
                lblMsg.setStyle("-fx-text-fill: #28a745;");

                txtId.clear();
                cbConfirmation.setSelected(false);
                lblInfo.setText("");

                System.out.println(String.format("CLIENT SUPPRIMÉ: ID=%s, Nom=%s %s, Dette=%.2f HTG",
                        client.getId(), client.getNom(), client.getPrenom(), client.getMontantDette()));

                afficherGestionClients();
            } else {
                lblMsg.setText("❌ Erreur lors de la suppression!");
                lblMsg.setStyle("-fx-text-fill: #dc3545;");
            }

        } catch (Exception e) {
            lblMsg.setText("❌ Erreur système: " + e.getMessage());
            lblMsg.setStyle("-fx-text-fill: #dc3545;");
            System.err.println("ERREUR suppression client: " + e.getMessage());
        }
    }

    private TableColumn<Client, Double> creerColDette() {
        TableColumn<Client, Double> col = new TableColumn<>("Dette (HTG)");
        col.setCellValueFactory(new PropertyValueFactory<>("montantDette"));
        col.setPrefWidth(120);

        col.setCellFactory(c -> new TableCell<Client, Double>() {
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", item));
                    setStyle(item > 0 ? "-fx-text-fill: #dc3545; -fx-font-weight: bold;"
                            : "-fx-text-fill: #28a745;");
                }
            }
        });
        return col;
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

    private TableColumn creerCol(String titre, String prop, int width) {
        TableColumn col = new TableColumn(titre);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(width);
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
        btn.setOnAction(e);
        btn.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; -fx-font-weight: bold;");
        return btn;
    }

    private Client trouverClient(String id) {
        for (Client c : clients) {
            if (c.getId().equalsIgnoreCase(id)) {
                return c;
            }
        }
        return null;
    }
}
