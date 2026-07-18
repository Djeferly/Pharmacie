package models;

import java.io.Serializable;

public class Client implements Serializable {

    private String id, nom, prenom, adresse, telephone, email, type;
    private double montantDette;

    public Client(String id, String nom, String prenom, String adresse,
            String telephone, String email, String type, double montantDette) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.type = type;
        this.montantDette = montantDette;
    }

    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    public String getType() {
        return type;
    }

    public double getMontantDette() {
        return montantDette;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMontantDette(double montantDette) {
        this.montantDette = montantDette;
    }

    public void ajouterDette(double montant) {
        this.montantDette += montant;
    }

    public void payerDette(double montant) {
        if (montant <= this.montantDette) {
            this.montantDette -= montant;
        }
    }

    @Override
    public String toString() {
        return id + "|" + nom + "|" + prenom + "|" + adresse + "|"
                + telephone + "|" + email + "|" + type + "|" + montantDette;
    }

    public static Client fromString(String ligne) {
        String[] p = ligne.split("\\|");
        return p.length >= 8 ? new Client(p[0], p[1], p[2], p[3], p[4], p[5], p[6],
                Double.parseDouble(p[7])) : null;
    }
}
