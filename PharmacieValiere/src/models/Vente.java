package models;

import java.io.Serializable;
import java.util.ArrayList;

public class Vente implements Serializable {

    private String id;
    private String idClient;
    private ArrayList<ProduitVente> produits;
    private String modePaiement;
    private String dateVente;
    private double montantTotal;

    // CONSTRUCTEUR
    public Vente(String id, String idClient, ArrayList<ProduitVente> produits,
            String modePaiement, String dateVente) {
        this.id = id;
        this.idClient = idClient;
        this.produits = (produits != null) ? produits : new ArrayList<>();
        this.modePaiement = modePaiement;
        this.dateVente = dateVente;
        this.montantTotal = calculerTotal();
    }

    // GETTERS ET SETTERS
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public ArrayList<ProduitVente> getProduits() {
        return produits;
    }

    public void setProduits(ArrayList<ProduitVente> produits) {
        this.produits = produits;
        this.montantTotal = calculerTotal();
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getDateVente() {
        return dateVente;
    }

    public void setDateVente(String dateVente) {
        this.dateVente = dateVente;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    // MÉTHODES UTILITAIRES
    private double calculerTotal() {
        double total = 0;
        if (produits != null) {
            for (ProduitVente pv : produits) {
                total += pv.getSousTotal();
            }
        }
        return total;
    }

    public int getNombreProduits() {
        return (produits != null) ? produits.size() : 0;
    }

    public int getNombreArticles() {
        int total = 0;
        if (produits != null) {
            for (ProduitVente pv : produits) {
                total += pv.getQuantite();
            }
        }
        return total;
    }

    // CLASSE INTERNE ProduitVente
    public static class ProduitVente implements Serializable {

        private String codeProduit;
        private String nomProduit;
        private int quantite;
        private double prixUnitaire;
        private double sousTotal;

        // CONSTRUCTEUR
        public ProduitVente(String codeProduit, String nomProduit, int quantite, double prixUnitaire) {
            this.codeProduit = codeProduit;
            this.nomProduit = nomProduit;
            this.quantite = quantite;
            this.prixUnitaire = prixUnitaire;
            this.sousTotal = quantite * prixUnitaire;
        }

        // GETTERS ET SETTERS
        public String getCodeProduit() {
            return codeProduit;
        }

        public void setCodeProduit(String codeProduit) {
            this.codeProduit = codeProduit;
        }

        public String getNomProduit() {
            return nomProduit;
        }

        public void setNomProduit(String nomProduit) {
            this.nomProduit = nomProduit;
        }

        public int getQuantite() {
            return quantite;
        }

        public void setQuantite(int quantite) {
            this.quantite = quantite;
            this.sousTotal = this.quantite * this.prixUnitaire;
        }

        public double getPrixUnitaire() {
            return prixUnitaire;
        }

        public void setPrixUnitaire(double prixUnitaire) {
            this.prixUnitaire = prixUnitaire;
            this.sousTotal = this.quantite * this.prixUnitaire;
        }

        public double getSousTotal() {
            return sousTotal;
        }

        public void setSousTotal(double sousTotal) {
            this.sousTotal = sousTotal;
        }

        // SÉRIALISATION
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(codeProduit).append("|")
                    .append(nomProduit).append("|")
                    .append(quantite).append("|")
                    .append(String.format("%.2f", prixUnitaire)).append("|")
                    .append(String.format("%.2f", sousTotal));
            return sb.toString();
        }

        public static ProduitVente fromString(String ligne) {
            if (ligne == null || ligne.trim().isEmpty()) {
                return null;
            }

            String[] parts = ligne.split("\\|");

            if (parts.length < 4) {
                System.err.println("Format invalide ProduitVente (moins de 4 parties): " + ligne);
                return null;
            }

            try {
                String codeProduit = parts[0];
                String nomProduit = parts[1];
                int quantite = Integer.parseInt(parts[2]);
                double prixUnitaire = Double.parseDouble(parts[3]);

                ProduitVente pv = new ProduitVente(codeProduit, nomProduit, quantite, prixUnitaire);

                if (parts.length > 4 && !parts[4].isEmpty()) {
                    try {
                        pv.setSousTotal(Double.parseDouble(parts[4]));
                    } catch (NumberFormatException e) {
                        pv.setSousTotal(quantite * prixUnitaire);
                    }
                }

                return pv;

            } catch (NumberFormatException e) {
                System.err.println("Erreur de parsing ProduitVente: " + ligne);
                System.err.println("Message: " + e.getMessage());
                return null;
            }
        }
    }

    // SÉRIALISATION DE VENTE
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(id != null ? id : "").append("|")
                .append(idClient != null ? idClient : "").append("|")
                .append(modePaiement != null ? modePaiement : "").append("|")
                .append(dateVente != null ? dateVente : "").append("|")
                .append(String.format("%.2f", montantTotal));

        if (produits != null && !produits.isEmpty()) {
            sb.append("|");
            for (int i = 0; i < produits.size(); i++) {
                sb.append(produits.get(i).toString());
                if (i < produits.size() - 1) {
                    sb.append(";");
                }
            }
        }

        return sb.toString();
    }

    public static Vente fromString(String ligne) {
        if (ligne == null || ligne.trim().isEmpty()) {
            return null;
        }

        try {
            String[] parts = ligne.split("\\|", -1);

            if (parts.length < 5) {
                System.err.println("Format invalide Vente (moins de 5 parties): " + ligne);
                return null;
            }

            String id = parts[0].isEmpty() ? "VENTE-" + System.currentTimeMillis() : parts[0];
            String idClient = parts[1].isEmpty() ? null : parts[1];
            String modePaiement = parts[2].isEmpty() ? "Cash" : parts[2];
            String dateVente = parts[3].isEmpty()
                    ? java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : parts[3];

            double montantTotal = 0.0;
            if (!parts[4].isEmpty()) {
                try {
                    montantTotal = Double.parseDouble(parts[4]);
                } catch (NumberFormatException e) {
                    System.err.println("Montant total invalide, utilisation de 0.0");
                    montantTotal = 0.0;
                }
            }

            ArrayList<ProduitVente> produitsVente = new ArrayList<>();
            if (parts.length > 5 && !parts[5].isEmpty()) {
                String[] produitsStr = parts[5].split(";");
                for (String produitStr : produitsStr) {
                    if (!produitStr.trim().isEmpty()) {
                        ProduitVente pv = ProduitVente.fromString(produitStr);
                        if (pv != null) {
                            produitsVente.add(pv);
                        }
                    }
                }
            }

            Vente vente = new Vente(id, idClient, produitsVente, modePaiement, dateVente);
            vente.setMontantTotal(montantTotal);

            return vente;

        } catch (Exception e) {
            System.err.println("Erreur de parsing Vente: " + ligne);
            System.err.println("Exception: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
