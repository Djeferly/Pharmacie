package controller;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.*;
import java.util.*;

public class RapportController {

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
     * CONSTRUCTEUR - Initialise le contrôleur des rapports
     *
     * @param stage Fenêtre principale de l'application
     * @param produits Liste des produits disponibles
     * @param clients Liste des clients enregistrés
     * @param ventes Historique des ventes
     * @param mesures Liste des unités de mesure
     * @param menuCtrl Contrôleur du menu pour la navigation
     */
    public RapportController(Stage stage, ArrayList<Produit> produits, ArrayList<Client> clients,
            ArrayList<Vente> ventes, ArrayList<Mesure> mesures, MenuController menuCtrl) {
        this.stage = stage;
        this.produits = produits;
        this.clients = clients;
        this.ventes = ventes;
        this.mesures = mesures;
        this.menuCtrl = menuCtrl;
    }

    // ============================================================
    // MÉTHODES D'AFFICHAGE PRINCIPALES
    // ============================================================
    /**
     * Affiche l'interface principale des rapports et statistiques Organisation
     * en 4 onglets : Statistiques, Top produits, Alertes, Ventes période
     */
    public void afficherRapports() {
        // Conteneur principal avec espacement
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Titre principal avec icône graphique
        Label titre = creerTitre("RAPPORTS ET STATISTIQUES");

        // Création des onglets de navigation (non fermables)
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // ──────────────────────────────────────────────────────────────
        // CRÉATION DES 4 ONGLETS PRINCIPAUX :
        // 1. Statistiques : Vue d'ensemble des KPI
        // 2. Top Produits : Produits les plus vendus
        // 3. Alertes Stock : Produits en faible stock
        // 4. Ventes par période : Analyse temporelle
        // ──────────────────────────────────────────────────────────────
        tabs.getTabs().addAll(
                creerTab("Statistiques", creerStatsGenerales()),
                creerTab("Top Produits", creerTopProduits()),
                creerTab("Alertes Stock", creerAlertesStock()),
                creerTab("Ventes par période", creerVentesParPeriode())
        );

        // Bouton de retour au menu principal
        Button btnRetour = creerBouton("⬅️ Retour",
                e -> menuCtrl.afficherMenuPrincipal(), "#6c757d");

        // Assemblage final
        root.getChildren().addAll(titre, tabs, btnRetour);
        stage.setScene(new Scene(root, 900, 600));
    }

    // ============================================================
    // MÉTHODES DE STATISTIQUES GÉNÉRALES
    // ============================================================
    /**
     * Crée le panneau de statistiques générales (KPI)
     *
     * @return VBox contenant les indicateurs clés de performance
     */
    private VBox creerStatsGenerales() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.getChildren().add(creerLabel("STATISTIQUES GÉNÉRALES", 18, true));

        // ──────────────────────────────────────────────────────────────
        // CALCUL DES INDICATEURS CLÉS
        // Ces calculs sont effectués en temps réel sur les données
        // ──────────────────────────────────────────────────────────────
        double totalVentes = 0, totalDettes = 0, valeurStock = 0;
        int faibleStock = 0;

        // 1. Total des ventes
        for (Vente v : ventes) {
            totalVentes += v.getMontantTotal();
        }

        // 2. Total des dettes clients
        for (Client c : clients) {
            totalDettes += c.getMontantDette();
        }

        // 3. Valeur du stock et comptage des produits en faible stock
        for (Produit p : produits) {
            if (p.getQuantite() < 10) {
                faibleStock++;
            }
            valeurStock += p.getQuantite() * p.getPrixAchat();
        }

        // Grille pour afficher les cartes d'indicateurs
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10;");

        // ──────────────────────────────────────────────────────────────
        // CRÉATION DES CARTES D'INDICATEURS
        // Chaque carte a une couleur thématique et affiche un KPI
        // ──────────────────────────────────────────────────────────────
        grid.add(creerCarte("📦 Produits", String.valueOf(produits.size()), "#3498db"), 0, 0);
        grid.add(creerCarte("👥 Clients", String.valueOf(clients.size()), "#2ecc71"), 1, 0);
        grid.add(creerCarte("💰 Ventes", String.valueOf(ventes.size()), "#f39c12"), 0, 1);
        grid.add(creerCarte("💵 Total Ventes", String.format("%.2f HTG", totalVentes), "#9b59b6"), 1, 1);
        grid.add(creerCarte("🏦 Dettes", String.format("%.2f HTG", totalDettes), "#e74c3c"), 0, 2);
        grid.add(creerCarte("⚠️ Faible Stock", String.valueOf(faibleStock), "#e67e22"), 1, 2);
        grid.add(creerCarte("📊 Valeur Stock", String.format("%.2f HTG", valeurStock), "#1abc9c"), 0, 3);

        box.getChildren().add(grid);
        return box;
    }

    /**
     * Crée une carte d'indicateur visuelle
     *
     * @param titre Titre de l'indicateur (avec emoji)
     * @param valeur Valeur numérique ou textuelle
     * @param couleur Code couleur hexadécimal pour le fond
     * @return VBox stylisée représentant la carte
     */
    private VBox creerCarte(String titre, String valeur, String couleur) {
        VBox carte = new VBox(5);
        carte.setPadding(new Insets(15));
        carte.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 10;");
        carte.setPrefSize(200, 100);

        // Titre de la carte
        Label lblTitre = new Label(titre);
        lblTitre.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Valeur de l'indicateur (plus grande et en gras)
        Label lblValeur = new Label(valeur);
        lblValeur.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        carte.getChildren().addAll(lblTitre, lblValeur);
        return carte;
    }

    // ============================================================
    // MÉTHODES D'ANALYSE DES VENTES
    // ============================================================
    /**
     * Crée le tableau des 10 produits les plus vendus
     *
     * @return VBox contenant le classement des produits
     */
    private VBox creerTopProduits() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.getChildren().add(creerLabel("TOP 10 DES PRODUITS LES PLUS VENDUS", 18, true));

        // ──────────────────────────────────────────────────────────────
        // ANALYSE DES VENTES PAR PRODUIT
        // Utilisation de Maps pour agréger les données
        // ──────────────────────────────────────────────────────────────
        Map<String, Integer> ventesParProduit = new HashMap<>();
        Map<String, Double> revenusParProduit = new HashMap<>();

        // Parcours de toutes les ventes pour agréger les données
        for (Vente v : ventes) {
            for (Vente.ProduitVente pv : v.getProduits()) {
                String code = pv.getCodeProduit();

                // Cumul des quantités vendues
                ventesParProduit.put(code, ventesParProduit.getOrDefault(code, 0) + pv.getQuantite());

                // Cumul des revenus générés
                revenusParProduit.put(code, revenusParProduit.getOrDefault(code, 0.0) + pv.getSousTotal());
            }
        }

        // Conversion en liste pour tri
        List<Map.Entry<String, Integer>> liste = new ArrayList<>(ventesParProduit.entrySet());

        // Tri décroissant par quantité vendue
        liste.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Création de la table de classement
        TableView<Map.Entry<String, Integer>> table = new TableView<>();
        table.setPrefHeight(400);

        // Colonne Produit : Code + Nom
        TableColumn<Map.Entry<String, Integer>, String> colProduit = new TableColumn<>("Produit");
        colProduit.setCellValueFactory(cellData -> {
            String code = cellData.getValue().getKey();
            Produit p = trouverProduit(code);
            return new javafx.beans.property.SimpleStringProperty(
                    code + " - " + (p != null ? p.getNom() : ""));
        });

        // Colonne Quantité Vendue
        TableColumn<Map.Entry<String, Integer>, Integer> colQte = new TableColumn<>("Quantité Vendue");
        colQte.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getValue()).asObject());

        // Colonne Revenu Total avec formatage devise
        TableColumn<Map.Entry<String, Integer>, Double> colRevenu = new TableColumn<>("Revenu Total");
        colRevenu.setCellValueFactory(cellData -> {
            String code = cellData.getValue().getKey();
            return new javafx.beans.property.SimpleDoubleProperty(
                    revenusParProduit.getOrDefault(code, 0.0)).asObject();
        });
        colRevenu.setCellFactory(col -> new TableCell<Map.Entry<String, Integer>, Double>() {
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f HTG", item));
            }
        });

        // Ajout des colonnes à la table
        table.getColumns().addAll(colProduit, colQte, colRevenu);

        // Limitation aux 10 premiers produits
        int limit = Math.min(10, liste.size());
        for (int i = 0; i < limit; i++) {
            table.getItems().add(liste.get(i));
        }

        box.getChildren().add(table);
        return box;
    }

    // ============================================================
    // MÉTHODES DE GESTION DES ALERTES
    // ============================================================
    /**
     * Crée la liste des produits en faible stock (< 10 unités) @ret
     *
     *
     * urn VBox contenant la table d'alertes
     */
    private VBox creerAlertesStock() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        // Titre avec couleur d'alerte
        Label titre = creerLabel("⚠️ PRODUITS EN FAIBLE STOCK (moins de 10 unités)", 18, true);
        titre.setStyle(titre.getStyle() + " -fx-text-fill: #e74c3c;");

        // Création de la table d'alertes
        TableView<Produit> table = new TableView<>();
        table.setPrefHeight(400);

        // Configuration des colonnes
        table.getColumns().addAll(
                creerCol("Code", "code", 80),
                creerCol("Nom", "nom", 150),
                creerCol("Stock", "quantite", 80),
                creerColMesure(),
                creerColStatut()
        );

        // Filtrage des produits en faible stock
        for (Produit p : produits) {
            if (p.getQuantite() < 10) {
                table.getItems().add(p);
            }
        }

        // Message spécial si aucun produit en alerte
        if (table.getItems().isEmpty()) {
            Label lblOk = new Label("🎉 Aucun produit en faible stock!");
            lblOk.setStyle("-fx-font-size: 16px; -fx-text-fill: #2ecc71;");
            box.getChildren().addAll(titre, lblOk);
        } else {
            box.getChildren().addAll(titre, table);
        }

        return box;
    }

    /**
     * Crée une colonne pour afficher l'unité de mesure
     *
     * @return TableColumn<Produit, String> configurée
     */
    private TableColumn<Produit, String> creerColMesure() {
        TableColumn<Produit, String> col = new TableColumn<>("Mesure");
        col.setCellValueFactory(cellData -> {
            String codeMesure = cellData.getValue().getCodeMesure();
            for (Mesure m : mesures) {
                if (m.getCode().equals(codeMesure)) {
                    return new javafx.beans.property.SimpleStringProperty(m.getNom());
                }
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        return col;
    }

    /**
     * Crée une colonne pour afficher le statut de stock avec codes couleur
     *
     * @return TableColumn<Produit, String> configurée
     */
    private TableColumn<Produit, String> creerColStatut() {
        TableColumn<Produit, String> col = new TableColumn<>("Statut");
        col.setCellValueFactory(cellData -> {
            int qte = cellData.getValue().getQuantite();
            String statut = qte == 0 ? "RUPTURE" : qte < 5 ? "CRITIQUE" : "FAIBLE";
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
                    // Code couleur selon le niveau d'alerte
                    String style = item.equals("RUPTURE")
                            ? "-fx-text-fill: #dc3545; -fx-font-weight: bold;"
                            : item.equals("CRITIQUE")
                            ? "-fx-text-fill: #ff9800; -fx-font-weight: bold;"
                            : "-fx-text-fill: #f39c12;";
                    setStyle(style);
                }
            }
        });
        return col;
    }

    // ============================================================
    // MÉTHODES D'ANALYSE TEMPORELLE
    // ============================================================
    /**
     * Crée l'analyse des ventes par période (mensuelle)
     *
     * @return VBox contenant le tableau d'analyse temporelle
     */
    private VBox creerVentesParPeriode() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.getChildren().add(creerLabel("📅 VENTES PAR PÉRIODE", 18, true));

        // ──────────────────────────────────────────────────────────────
        // AGRÉGATION DES VENTES PAR MOIS
        // Format de date attendu: "JJ/MM/AAAA"
        // ──────────────────────────────────────────────────────────────
        Map<String, Double> ventesParMois = new HashMap<>();
        Map<String, Integer> countParMois = new HashMap<>();

        for (Vente v : ventes) {
            String date = v.getDateVente();
            if (date != null && date.length() >= 7) {
                // Extraction du mois (format: "MM/AAAA")
                String mois = date.substring(3);

                // Cumul du montant des ventes
                ventesParMois.put(mois, ventesParMois.getOrDefault(mois, 0.0) + v.getMontantTotal());

                // Comptage du nombre de ventes
                countParMois.put(mois, countParMois.getOrDefault(mois, 0) + 1);
            }
        }

        // Création de la table d'analyse
        TableView<Map.Entry<String, Double>> table = new TableView<>();
        table.setPrefHeight(300);

        // Colonne Mois
        TableColumn<Map.Entry<String, Double>, String> colMois = new TableColumn<>("Mois");
        colMois.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));

        // Colonne Nombre de ventes
        TableColumn<Map.Entry<String, Double>, Integer> colNb = new TableColumn<>("Nb Ventes");
        colNb.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleIntegerProperty(
                        countParMois.getOrDefault(cellData.getValue().getKey(), 0)).asObject());

        // Colonne Total des ventes avec formatage
        TableColumn<Map.Entry<String, Double>, Double> colTotal = new TableColumn<>("Total Ventes");
        colTotal.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getValue()).asObject());
        colTotal.setCellFactory(col -> new TableCell<Map.Entry<String, Double>, Double>() {
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f HTG", item));
            }
        });

        // Ajout des colonnes
        table.getColumns().addAll(colMois, colNb, colTotal);

        // Tri décroissant par mois (du plus récent au plus ancien)
        List<Map.Entry<String, Double>> liste = new ArrayList<>(ventesParMois.entrySet());
        liste.sort((a, b) -> b.getKey().compareTo(a.getKey()));

        // Ajout des données à la table
        for (Map.Entry<String, Double> entry : liste) {
            table.getItems().add(entry);
        }

        // Calcul des totaux généraux
        double totalGeneral = ventesParMois.values().stream().mapToDouble(Double::doubleValue).sum();
        int totalCount = countParMois.values().stream().mapToInt(Integer::intValue).sum();

        // Affichage des totaux
        Label lblTotal = creerLabel(String.format("Total général: %d ventes pour %.2f HTG",
                totalCount, totalGeneral), 14, true);

        box.getChildren().addAll(table, lblTotal);
        return box;
    }

    // ============================================================
    // MÉTHODES UTILITAIRES - CRÉATION DE COLONNES
    // ============================================================
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

    // ============================================================
    // MÉTHODES UTILITAIRES - MISE EN PAGE
    // ============================================================
    /**
     * Crée un onglet avec titre et contenu
     *
     * @param titre Titre de l'onglet (avec emoji)
     * @param content Contenu de l'onglet
     * @return Tab configuré
     */
    private Tab creerTab(String titre, Region content) {
        return new Tab(titre, content);
    }

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
     * @param e Gestionnaire d'événement
     * @param couleur Code couleur hexadécimal
     * @return Button configuré
     */
    private Button creerBouton(String texte, javafx.event.EventHandler e, String couleur) {
        Button btn = new Button(texte);
        btn.setOnAction(e);
        btn.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white;");
        return btn;
    }

    // ============================================================
    // MÉTHODES UTILITAIRES - RECHERCHE
    // ============================================================
    /**
     * Recherche un produit par son code
     *
     * @param code Code du produit recherché
     * @return Produit ou null si non trouvé
     */
    private Produit trouverProduit(String code) {
        for (Produit p : produits) {
            if (p.getCode().equals(code)) {
                return p;
            }
        }
        return null;
    }
}
