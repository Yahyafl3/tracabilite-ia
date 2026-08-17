package com.pfa.tracabilite_ia.util;

import java.text.Normalizer;
import java.util.Locale;

public class StringUtils {

    private StringUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Removes diacritical marks (accents) from a string.
     * Example: "Modéré" -> "Modere", "ÉLEVÉ" -> "ELEVE"
     *
     * @param input the input string, may be null
     * @return the string without accents, or null if input was null
     */
    public static String stripAccents(String input) {
        if (input == null) {
            return null;
        }
        // Normalize to NFD (decomposes accented characters into base + accent)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Remove all non-ASCII characters (the accents)
        return normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    /**
     * Normalizes a string for comparison: trims, converts to uppercase, and strips accents.
     * Example: "  Modéré  " -> "MODERE"
     *
     * @param input the input string, may be null
     * @return the normalized string, or null if input was null
     */
    public static String normalizeForComparison(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return stripAccents(input.trim()).toUpperCase(Locale.ROOT);
    }
}
