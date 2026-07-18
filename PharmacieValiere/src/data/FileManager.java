package data;

import models.*;
import java.io.*;
import java.util.ArrayList;

public class FileManager {

    // Répertoire de stockage des fichiers de données
    private static final String DATA = "data/";

    // Extension des fichiers de données
    private static final String EXTENSION = ".txt";

    // Bloc d'initialisation statique : création du répertoire data au démarrage
    static {
        new File(DATA).mkdirs();
        System.out.println("✅ Répertoire de données initialisé: " + DATA);
    }

    // MÉTHODES DE GESTION DES MESURES
    /**
     * Sauvegarde la liste des unités de mesure dans le fichier dédié
     */
    public static void sauvegarderMesures(ArrayList<Mesure> mesures) {
        sauvegarder(DATA + "mesures.txt", mesures);
    }

    /**
     * Charge la liste des unités de mesure depuis le fichier
     */
    public static ArrayList<Mesure> chargerMesures() {
        return charger(DATA + "mesures.txt", Mesure::fromString);
    }

    // MÉTHODES DE GESTION DES PRODUITS
    public static void sauvegarderProduits(ArrayList<Produit> produits) {
        sauvegarder(DATA + "produits.txt", produits);
    }

    public static ArrayList<Produit> chargerProduits() {
        return charger(DATA + "produits.txt", Produit::fromString);
    }

    // MÉTHODES DE GESTION DES CLIENTS
    public static void sauvegarderClients(ArrayList<Client> clients) {
        sauvegarder(DATA + "clients.txt", clients);
    }

    public static ArrayList<Client> chargerClients() {
        return charger(DATA + "clients.txt", Client::fromString);
    }

    // MÉTHODES DE GESTION DES VENTES
    public static void sauvegarderVentes(ArrayList<Vente> ventes) {
        sauvegarder(DATA + "ventes.txt", ventes);
    }

    public static ArrayList<Vente> chargerVentes() {
        return charger(DATA + "ventes.txt", Vente::fromString);
    }

    // MÉTHODES DE GESTION DES UTILISATEURS
    /**
     * Initialise le fichier des utilisateurs avec des comptes par défaut
     * Format: username:password:role:nomComplet
     */
    public static void initialiserUtilisateurs() {
        File f = new File(DATA + "utilisateurs.txt");
        
        if (!f.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                // Comptes par défaut avec noms complets
                pw.println("admin:admin123:Administrateur:Dr. Jean Vallière");
                pw.println("pharmacien:pharma456:Pharmacien:Marie Deschamps");
                pw.println("vendeur:vendeur789:Vendeur:Pierre Laurent");
                pw.println("assistant:assist012:Assistant:Sophie Martin");
                
                System.out.println("✅ Fichier utilisateurs initialisé avec 4 comptes par défaut");
            } catch (IOException e) {
                System.err.println("❌ Erreur lors de l'initialisation des utilisateurs: " + e.getMessage());
            }
        }
    }

    /**
     * Vérifie les identifiants d'un utilisateur
     */
    public static boolean verifierUtilisateur(String username, String password) {
        File f = new File(DATA + "utilisateurs.txt");
        
        if (!f.exists()) {
            initialiserUtilisateurs();
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] parties = ligne.split(":");
                
                if (parties.length >= 2 && parties[0].equals(username) && parties[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur de lecture du fichier utilisateurs: " + e.getMessage());
        }
        return false;
    }

    /**
     * Vérifie si un utilisateur existe déjà
     */
    public static boolean verifierUtilisateurExiste(String username) {
        File f = new File(DATA + "utilisateurs.txt");
        
        if (!f.exists()) {
            return false;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] parties = ligne.split(":");
                if (parties.length >= 1 && parties[0].equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la vérification de l'utilisateur: " + e.getMessage());
        }
        return false;
    }

    /**
     * Récupère le rôle d'un utilisateur
     */
    public static String obtenirRoleUtilisateur(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(DATA + "utilisateurs.txt"))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] parties = ligne.split(":");
                
                if (parties.length >= 3 && parties[0].equals(username)) {
                    return parties[2];
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du rôle: " + e.getMessage());
        }
        return "Utilisateur";
    }

    /**
     * Ajoute un nouvel utilisateur au fichier des utilisateurs
     */
    public static boolean ajouterUtilisateur(String username, String password, String role) {
        File f = new File(DATA + "utilisateurs.txt");
        
        // Vérifier d'abord si l'utilisateur existe déjà
        if (verifierUtilisateurExiste(username)) {
            System.out.println("⚠ L'utilisateur existe déjà: " + username);
            return false;
        }
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(f, true))) {
            // Format: username:password:role:nomComplet (nomComplet vide pour nouveaux)
            pw.println(username + ":" + password + ":" + role + ":" + username);
            System.out.println("✅ Nouvel utilisateur ajouté: " + username + " avec rôle: " + role);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère la liste de tous les utilisateurs
     */
    public static ArrayList<String[]> obtenirTousUtilisateurs() {
        ArrayList<String[]> utilisateurs = new ArrayList<>();
        File f = new File(DATA + "utilisateurs.txt");
        
        if (!f.exists()) {
            return utilisateurs;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] parties = ligne.split(":");
                if (parties.length >= 3) {
                    utilisateurs.add(parties);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }
        return utilisateurs;
    }

    /**
     * Supprime un utilisateur
     */
    public static boolean supprimerUtilisateur(String username) {
        ArrayList<String[]> utilisateurs = obtenirTousUtilisateurs();
        boolean trouve = false;
        
        // Filtrer l'utilisateur à supprimer
        ArrayList<String[]> nouveauxUtilisateurs = new ArrayList<>();
        for (String[] user : utilisateurs) {
            if (!user[0].equals(username)) {
                nouveauxUtilisateurs.add(user);
            } else {
                trouve = true;
            }
        }
        
        if (!trouve) {
            System.out.println("⚠ Utilisateur non trouvé: " + username);
            return false;
        }
        
        // Réécrire le fichier
        File f = new File(DATA + "utilisateurs.txt");
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            for (String[] user : nouveauxUtilisateurs) {
                pw.println(String.join(":", user));
            }
            System.out.println("✅ Utilisateur supprimé: " + username);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    // MÉTHODES GÉNÉRIQUES DE PERSISTANCE
    /**
     * Méthode générique pour sauvegarder une liste d'objets
     */
    private static <T> void sauvegarder(String fichier, ArrayList<T> liste) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fichier))) {
            for (T item : liste) {
                pw.println(item.toString());
            }
            System.out.println("💾 Données sauvegardées dans: " + fichier + " (" + liste.size() + " éléments)");
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la sauvegarde dans " + fichier + ": " + e.getMessage());
        }
    }

    /**
     * Méthode générique pour charger une liste d'objets
     */
    private static <T> ArrayList<T> charger(String fichier, Converter<T> converter) {
        ArrayList<T> liste = new ArrayList<>();
        File f = new File(fichier);
        
        if (!f.exists()) {
            System.out.println("📁 Fichier non trouvé (sera créé à la première sauvegarde): " + fichier);
            return liste;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String ligne;
            int count = 0;
            
            while ((ligne = br.readLine()) != null) {
                if (!ligne.trim().isEmpty()) {
                    T item = converter.convert(ligne);
                    if (item != null) {
                        liste.add(item);
                        count++;
                    }
                }
            }
            System.out.println("📂 " + count + " objets chargés depuis: " + fichier);
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de " + fichier + ": " + e.getMessage());
        }
        return liste;
    }

    // INTERFACE FONCTIONNELLE POUR LA CONVERSION
    /**
     * Interface fonctionnelle pour convertir une String en objet T
     */
    @FunctionalInterface
    interface Converter<T> {
        T convert(String ligne);
    }
}