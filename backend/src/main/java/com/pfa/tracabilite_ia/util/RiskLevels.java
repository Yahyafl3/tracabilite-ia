package com.pfa.tracabilite_ia.util;

/**
 * Vocabulaire de risque unifié.
 *
 * <p>Le service ML produit deux jeux de libellés : anglais
 * ({@code HIGH}/{@code MEDIUM}/{@code LOW}) pour le parcours crédit historique,
 * français ({@code ELEVE}/{@code MOYEN}/{@code MODERE}/{@code FAIBLE}) pour le
 * parcours multidomaine. Toute comparaison sur {@code Decision.riskLevel} doit
 * passer par cette classe.
 */
public final class RiskLevels {

    public static final String HIGH = "ELEVE";
    public static final String MEDIUM = "MOYEN";
    public static final String LOW = "FAIBLE";

    private RiskLevels() {
    }

    /** Retourne le libellé canonique, ou {@code null} si la valeur est inconnue. */
    public static String normalize(String riskLevel) {
        String normalized = StringUtils.normalizeForComparison(riskLevel);
        if (normalized == null) {
            return null;
        }
        return switch (normalized) {
            case "HIGH", "ELEVE" -> HIGH;
            case "MEDIUM", "MOYEN", "MODERE" -> MEDIUM;
            case "LOW", "FAIBLE" -> LOW;
            default -> null;
        };
    }

    public static boolean isHigh(String riskLevel) {
        return HIGH.equals(normalize(riskLevel));
    }

    public static boolean isMedium(String riskLevel) {
        return MEDIUM.equals(normalize(riskLevel));
    }

    public static boolean isLow(String riskLevel) {
        return LOW.equals(normalize(riskLevel));
    }
}
