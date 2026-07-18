package models;

import java.io.Serializable;

public class Mesure implements Serializable {

    private String code, nom, description;

    public Mesure(String code, String nom, String description) {
        this.code = code;
        this.nom = nom;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return code + "|" + nom + "|" + description;
    }

    public static Mesure fromString(String ligne) {
        String[] p = ligne.split("\\|");
        return p.length >= 3 ? new Mesure(p[0], p[1], p[2]) : null;
    }
}
