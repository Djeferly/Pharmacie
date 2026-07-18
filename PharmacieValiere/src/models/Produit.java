package models;

import java.io.Serializable;

public class Produit implements Serializable {

    private String code;
    private String categorie;
    private String codeMesure;
    private String nom;
    private double prixAchat;
    private double prixVente;
    private int quantite;
    private String modeVente; // "Détail", "Gros", "Gros et Détail"
    private String dateAjout;
    private double prixVenteGros;
    private int quantiteGros; // Quantité associée au prix gros
    private int stockDetail;
    private int stockGros;
    private boolean gestionSepareeStock;

    // CONSTRUCTEUR
    public Produit(String code, String categorie, String codeMesure, String nom,
            double prixAchat, double prixVente, int quantite,
            String modeVente, String dateAjout) {
        this.code = code;
        this.categorie = categorie;
        this.codeMesure = codeMesure;
        this.nom = nom;
        this.prixAchat = prixAchat;
        this.prixVente = prixVente;
        this.quantite = quantite;
        this.modeVente = modeVente;
        this.dateAjout = dateAjout;

        // Valeurs par défaut
        this.prixVenteGros = 0.0;
        this.quantiteGros = 1;
        this.gestionSepareeStock = false;
        initialiserStock();
    }

    // Constructeur simplifié
    public Produit(String code, String categorie, String nom, double prixAchat,
            double prixVente, int quantite, String modeVente) {
        this(code, categorie, "UN", nom, prixAchat, prixVente, quantite, modeVente,
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void initialiserStock() {
        if ("Détail".equals(modeVente)) {
            this.stockDetail = this.quantite;
            this.stockGros = 0;
        } else if ("Gros".equals(modeVente)) {
            this.stockDetail = 0;
            this.stockGros = this.quantite;
        } else if ("Gros et Détail".equals(modeVente)) {
            if (gestionSepareeStock) {
                this.stockDetail = this.quantite / 2;
                this.stockGros = this.quantite - this.stockDetail;
            } else {
                this.stockDetail = this.quantite;
                this.stockGros = this.quantite;
            }
        }
    }

    // GETTERS ET SETTERS
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
        initialiserStock();
    }

    public String getCodeMesure() {
        return codeMesure;
    }

    public void setCodeMesure(String codeMesure) {
        this.codeMesure = codeMesure;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(double prixAchat) {
        this.prixAchat = prixAchat;
    }

    public double getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(double prixVente) {
        this.prixVente = prixVente;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
        recalculerStockTotal();
    }

    public String getModeVente() {
        return modeVente;
    }

    public void setModeVente(String modeVente) {
        this.modeVente = modeVente;
        initialiserStock();
    }

    public String getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(String dateAjout) {
        this.dateAjout = dateAjout;
    }

    public double getPrixVenteGros() {
        return prixVenteGros;
    }

    public void setPrixVenteGros(double prixVenteGros) {
        this.prixVenteGros = prixVenteGros;
    }

    public int getQuantiteGros() {
        return quantiteGros;
    }

    public void setQuantiteGros(int quantiteGros) {
        this.quantiteGros = quantiteGros;
    }

    public int getStockDetail() {
        return stockDetail;
    }

    public void setStockDetail(int stockDetail) {
        this.stockDetail = stockDetail;
        recalculerStockTotal();
    }

    public int getStockGros() {
        return stockGros;
    }

    public void setStockGros(int stockGros) {
        this.stockGros = stockGros;
        recalculerStockTotal();
    }

    public boolean isGestionSepareeStock() {
        return gestionSepareeStock;
    }

    public void setGestionSepareeStock(boolean gestionSepareeStock) {
        this.gestionSepareeStock = gestionSepareeStock;
        initialiserStock();
    }

    // MÉTHODES UTILITAIRES
    public double getPrixSelonMode(String mode) {
        if ("Gros".equals(mode)) {
            return prixVenteGros;
        }
        return prixVente;
    }

    public boolean peutVendre(String mode, int quantite) {
    if ("Détail".equals(modeVente) && !"Détail".equals(mode)) {
        return false;
    }
    if ("Gros".equals(modeVente) && !"Gros".equals(mode)) {
        return false;
    }
    if ("Gros".equals(mode) && "Gros et Détail".equals(modeVente) && quantite % quantiteGros != 0) {
        return false; // Pour vendre en gros dans le mode mixte, la quantité doit être un multiple de quantiteGros
    }
    if (gestionSepareeStock) {
        if ("Détail".equals(mode)) {
            return quantite <= stockDetail;
        } else if ("Gros".equals(mode)) {
            return quantite <= stockGros;
        }
    }
    return quantite <= this.quantite;
}

    public boolean deduireStock(String mode, int quantite) {
        if (!peutVendre(mode, quantite)) {
            return false;
        }
        if (gestionSepareeStock) {
            if ("Détail".equals(mode)) {
                stockDetail -= quantite;
            } else if ("Gros".equals(mode)) {
                stockGros -= quantite;
            }
        }
        this.quantite -= quantite;
        return true;
    }

    public void recalculerStockTotal() {
        if (gestionSepareeStock) {
            this.quantite = stockDetail + stockGros;
        }
    }

    // MÉTHODES DE SÉRIALISATION
    @Override
    public String toString() {
        return String.join("|",
                code, categorie, codeMesure, nom,
                String.valueOf(prixAchat),
                String.valueOf(prixVente),
                String.valueOf(quantite),
                modeVente,
                dateAjout,
                String.valueOf(prixVenteGros),
                String.valueOf(quantiteGros),
                String.valueOf(stockDetail),
                String.valueOf(stockGros),
                String.valueOf(gestionSepareeStock)
        );
    }

    public static Produit fromString(String ligne) {
        if (ligne == null || ligne.trim().isEmpty()) {
            return null;
        }

        String[] parts = ligne.split("\\|");
        if (parts.length < 9) {
            return null;
        }

        try {
            Produit p = new Produit(
                    parts[0], parts[1], parts[2], parts[3],
                    Double.parseDouble(parts[4]),
                    Double.parseDouble(parts[5]),
                    Integer.parseInt(parts[6]),
                    parts[7],
                    parts[8]
            );

            if (parts.length > 9 && !parts[9].isEmpty()) {
                p.setPrixVenteGros(Double.parseDouble(parts[9]));
            }
            if (parts.length > 10 && !parts[10].isEmpty()) {
                p.setQuantiteGros(Integer.parseInt(parts[10]));
            }
            if (parts.length > 11 && !parts[11].isEmpty()) {
                p.setStockDetail(Integer.parseInt(parts[11]));
            }
            if (parts.length > 12 && !parts[12].isEmpty()) {
                p.setStockGros(Integer.parseInt(parts[12]));
            }
            if (parts.length > 13 && !parts[13].isEmpty()) {
                p.setGestionSepareeStock(Boolean.parseBoolean(parts[13]));
            }

            return p;
        } catch (NumberFormatException e) {
            System.err.println("Erreur de parsing Produit: " + ligne);
            return null;
        }
    }
}
