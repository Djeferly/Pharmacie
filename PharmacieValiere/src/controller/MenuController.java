package controller;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.*;
import data.FileManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MenuController {

    private Stage stage;
    private String user;
    private String role;
    private ArrayList<Mesure> mesures;
    private ArrayList<Produit> produits;
    private ArrayList<Client> clients;
    private ArrayList<Vente> ventes;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MenuController(Stage stage, String user, ArrayList<Mesure> mesures,
            ArrayList<Produit> produits, ArrayList<Client> clients, ArrayList<Vente> ventes) {
        this.stage = stage;
        this.user = user;
        this.role = FileManager.obtenirRoleUtilisateur(user);
        this.mesures = mesures;
        this.produits = produits;
        this.clients = clients;
        this.ventes = ventes;
    }

    public void afficherMenuPrincipal() {
        BorderPane root = new BorderPane();
        root.setTop(creerHeader());
        root.setCenter(creerMenu());
        stage.setScene(new Scene(root, 900, 700));
    }

    private VBox creerHeader() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: linear-gradient(to right, #2c5aa0, #3a7bd5);");

        // Ligne supérieure : Titre + bouton d'actualisation + date
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label titre = new Label("SYSTÈME DE GESTION - PHARMACIE VALLIÈRE");
        titre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bouton d'actualisation global
        Button btnActualiserGlobal = new Button("🔄");
        btnActualiserGlobal.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; "
                + "-fx-font-size: 14px; -fx-background-radius: 50%; -fx-min-width: 30; -fx-min-height: 30; "
                + "-fx-cursor: hand;");
        btnActualiserGlobal.setTooltip(new Tooltip("Actualiser toutes les données"));
        btnActualiserGlobal.setOnAction(e -> {
            chargerToutesDonnees();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("✅ Actualisation complète");
            alert.setHeaderText(null);
            alert.setContentText("Toutes les données ont été actualisées depuis les fichiers!");
            alert.showAndWait();
        });

        Label date = new Label("Date: " + LocalDate.now().format(DATE_FMT));
        date.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        top.getChildren().addAll(titre, spacer, btnActualiserGlobal, date);

        // Ligne inférieure : Informations utilisateur
        HBox bottom = new HBox(20);
        bottom.setAlignment(Pos.CENTER_LEFT);

        Label lblUser = new Label(user + " (" + role + ")");
        lblUser.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Label lblDev = new Label("Développé par: 3D TECH (Djeferly FELIX , Di-Enilson ETIENNE , Djedly Fitz GEROME)");
        lblDev.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");

        bottom.getChildren().addAll(lblUser, lblDev);

        header.getChildren().addAll(top, bottom);
        return header;
    }

    private GridPane creerMenu() {
        GridPane menu = new GridPane();
        menu.setPadding(new Insets(30));
        menu.setHgap(20);
        menu.setVgap(20);
        menu.setAlignment(Pos.CENTER);

        Button[] btns = {
            creerBoutonMenu("MESURES", "#3498db", "Gérer les unités de mesure"),
            creerBoutonMenu("PRODUITS", "#2ecc71", "Gérer les produits et stocks"),
            creerBoutonMenu("CLIENTS", "#e74c3c", "Gérer les clients et dettes"),
            creerBoutonMenu("VENTES", "#f39c12", "Gérer les ventes et transactions"),
            creerBoutonMenu("RAPPORTS", "#9b59b6", "Consulter les statistiques et rapports"),
            creerBoutonMenu("DÉCONNEXION", "#95a5a6", "Se déconnecter du système")
        };

        btns[0].setOnAction(e -> new MesureController(stage, mesures, produits, this).afficherGestionMesures());
        btns[1].setOnAction(e -> new ProduitController(stage, mesures, produits, this).afficherGestionProduits());
        btns[2].setOnAction(e -> new ClientController(stage, clients, this).afficherGestionClients());
        btns[3].setOnAction(e -> new VenteController(stage, produits, clients, ventes, this).afficherGestionVentes());
        btns[4].setOnAction(e -> new RapportController(stage, produits, clients, ventes, mesures, this).afficherRapports());
        btns[5].setOnAction(e -> {
            sauvegarder();
            new Main().redemarrer(stage);
        });

        for (int i = 0; i < btns.length; i++) {
            menu.add(btns[i], i % 2, i / 2);
        }

        return menu;
    }

    private Button creerBoutonMenu(String texte, String couleur, String tooltip) {
        Button btn = new Button(texte);
        btn.setPrefSize(300, 80);
        String style = "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 18px; "
                + "-fx-font-weight: bold; -fx-background-radius: 10;";
        btn.setStyle(String.format(style, couleur));
        String couleurFonce = assombrir(couleur);
        btn.setOnMouseEntered(e -> btn.setStyle(String.format(style, couleurFonce)));
        btn.setOnMouseExited(e -> btn.setStyle(String.format(style, couleur)));
        Tooltip.install(btn, new Tooltip(tooltip));
        return btn;
    }

    private String assombrir(String couleur) {
        switch (couleur) {
            case "#3498db":
                return "#2980b9";
            case "#2ecc71":
                return "#27ae60";
            case "#e74c3c":
                return "#c0392b";
            case "#f39c12":
                return "#d35400";
            case "#9b59b6":
                return "#8e44ad";
            default:
                return "#7f8c8d";
        }
    }

    private void sauvegarder() {
        FileManager.sauvegarderMesures(mesures);
        FileManager.sauvegarderProduits(produits);
        FileManager.sauvegarderClients(clients);
        FileManager.sauvegarderVentes(ventes);
        System.out.println("Données sauvegardées avant déconnexion");
    }

    // ============================================================
    // MÉTHODES D'ACTUALISATION
    // ============================================================
    /**
     * Recharge les données depuis les fichiers
     *
     * @param type Type de données à recharger
     */
    public void rechargerDonnees(String type) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🔄 Actualisation");
        alert.setHeaderText(null);

        switch (type) {
            case "produits":
                produits = FileManager.chargerProduits();
                alert.setContentText("Liste des produits actualisée avec succès!");
                break;
            case "clients":
                clients = FileManager.chargerClients();
                alert.setContentText("Liste des clients actualisée avec succès!");
                break;
            case "ventes":
                ventes = FileManager.chargerVentes();
                alert.setContentText("Liste des ventes actualisée avec succès!");
                break;
            case "mesures":
                mesures = FileManager.chargerMesures();
                alert.setContentText("Liste des mesures actualisée avec succès!");
                break;
            case "tous":
                chargerToutesDonnees();
                alert.setContentText("Toutes les données ont été actualisées!");
                break;
        }

        alert.showAndWait();
    }

    /**
     * Recharge toutes les données depuis les fichiers
     */
    public void chargerToutesDonnees() {
        produits = FileManager.chargerProduits();
        clients = FileManager.chargerClients();
        ventes = FileManager.chargerVentes();
        mesures = FileManager.chargerMesures();
    }

    // ============================================================
    // GETTERS POUR ACCÉDER AUX DONNÉES
    // ============================================================
    public ArrayList<Produit> getProduits() {
        return produits;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public ArrayList<Vente> getVentes() {
        return ventes;
    }

    public ArrayList<Mesure> getMesures() {
        return mesures;
    }
}
