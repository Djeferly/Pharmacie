package data;

import java.io.*;
import java.util.regex.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GenerateurID {

    //Formateur de date pour les transactions
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("ddMMyyyy");

    //Longueur par défaut des IDs numériques
    private static final int LONGUEUR_ID = 4; // Format: 0001, 0002, etc.

    // MÉTHODES DE GÉNÉRATION D'IDs SÉQUENTIELS
    /**
     * Génère le prochain ID de vente (format: V0001, V0002, etc.)
     *
     * @return String ID unique pour une nouvelle vente
     */
    public static String genererProchainIdVente() {
        return genererID("data/ventes.txt", "V(\\d+)", "V%04d");
    }

    /**
     * Génère le prochain ID client (format: CLI0001, CLI0002, etc.)
     *
     * @return String ID unique pour un nouveau client
     */
    public static String genererProchainIdClient() {
        return genererID("data/clients.txt", "CLI(\\d+)", "CLI%04d");
    }

    /**
     * Génère le prochain ID produit (format: PROD0001, PROD0002, etc.)
     *
     * @return String ID unique pour un nouveau produit
     */
    public static String genererProchainIdProduit() {
        return genererID("data/produits.txt", "PROD(\\d+)", "PROD%04d");
    }

    /**
     * Génère le prochain ID mesure (format: MES001, MES002, etc.)
     *
     * @return String ID unique pour une nouvelle mesure
     */
    public static String genererProchainIdMesure() {
        return genererID("data/mesures.txt", "MES(\\d+)", "MES%03d");
    }

    // MÉTHODES DE GÉNÉRATION DE CODES THÉMATIQUES
    /**
     * Génère un code produit basé sur sa catégorie Format: PREFIXE-001 (ex:
     * PHAR-001, COSM-002)
     *
     * @param categorie Catégorie du produit
     * @return String Code produit unique
     */
    public static String genererCodeProduit(String categorie) {
        String prefix = getPrefixCategorie(categorie);
        return genererID("data/produits.txt", prefix + "-(\\d+)", prefix + "-%03d");
    }

    /**
     * Génère un code d'unité de mesure (format: M001, M002, etc.)
     *
     * @return String Code mesure unique
     */
    public static String genererCodeMesure() {
        int maxCode = getMaxCode("data/mesures.txt", "M(\\d+)");
        return String.format("M%03d", maxCode + 1);
    }

    /**
     * Génère un code de transaction avec date (format: TRX-31122023-001)
     *
     * @return String Code transaction unique
     */
    public static String genererCodeTransaction() {
        String date = LocalDate.now().format(DATE_FMT);
        String pattern = "TRX-" + date + "-(\\d+)";
        String fichier = "data/ventes.txt";

        int maxSeq = getMaxCode(fichier, pattern);
        return String.format("TRX-%s-%03d", date, maxSeq + 1);
    }

    // MÉTHODES UTILITAIRES DE GÉNÉRATION
    /**
     * Méthode générique pour générer un ID séquentiel
     *
     * @param fichier Fichier contenant les données existantes
     * @param regex Expression régulière pour extraire les IDs
     * @param format Format de l'ID (ex: "V%04d", "CLI%04d")
     * @return String Nouvel ID généré
     */
    private static String genererID(String fichier, String regex, String format) {
        int maxId = getMaxCode(fichier, regex);
        return String.format(format, maxId + 1);
    }

    /**
     * Trouve le code numérique maximum dans un fichier selon un motif
     *
     * @param fichier Fichier à analyser
     * @param regex Expression régulière pour extraire les codes
     * @return int Code maximum trouvé (0 si aucun)
     */
    private static int getMaxCode(String fichier, String regex) {
        int maxCode = 0;
        File f = new File(fichier);

        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String ligne;
                Pattern pattern = Pattern.compile(regex);

                while ((ligne = br.readLine()) != null) {
                    Matcher matcher = pattern.matcher(ligne);
                    if (matcher.find()) {
                        try {
                            int code = Integer.parseInt(matcher.group(1));
                            if (code > maxCode) {
                                maxCode = code;
                            }
                        } catch (NumberFormatException e) {
                            // Ignorer les lignes mal formatées
                            System.err.println("Ligne ignorée dans " + fichier + ": " + ligne);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Erreur de lecture du fichier " + fichier + ": " + e.getMessage());
            }
        }
        return maxCode;
    }

    /**
     * Convertit une catégorie en préfixe de code
     *
     * @param categorie Catégorie du produit
     * @return String Préfixe correspondant
     */
    private static String getPrefixCategorie(String categorie) {
        if (categorie == null) {
            return "PROD";
        }

        switch (categorie.toUpperCase()) {
            case "PHARMACEUTIQUE":
                return "PHAR";
            case "COSMÉTIQUE":
            case "COSMETIQUE":
                return "COSM";
            case "MÉDICAL":
            case "MEDICAL":
                return "MED";
            case "HYGIÈNE":
            case "HYGIENE":
                return "HYG";
            default:
                return "PROD";
        }
    }

    /**
     * Vérifie si un code existe déjà dans un fichier
     *
     * @param fichier Fichier à vérifier
     * @param code Code à rechercher
     * @return true si le code existe, false sinon
     */
    public static boolean codeExiste(String fichier, String code) {
        File f = new File(fichier);
        if (!f.exists()) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                // Vérifie si la ligne commence par le code suivi d'un séparateur
                if (ligne.startsWith(code + "|")) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur de vérification du code dans " + fichier + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Génère un ID basé sur l'horodatage actuel (pour haute fréquence) Format:
     * TIMESTAMP + compteur aléatoire
     *
     * @return String ID basé sur le timestamp
     */
    public static String genererIdTimestamp() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return String.format("T%d%03d", timestamp, random);
    }

    /**
     * Vérifie la validité d'un ID généré
     *
     * @param id ID à vérifier
     * @param regex Motif de validation
     * @return true si l'ID est valide, false sinon
     */
    public static boolean validerId(String id, String regex) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return id.matches(regex);
    }
}
