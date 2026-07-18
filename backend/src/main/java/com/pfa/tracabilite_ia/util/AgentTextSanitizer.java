package com.pfa.tracabilite_ia.util;

/**
 * Nettoie le texte des réponses agent sans altérer le sens ni les accents français.
 * Supprime uniquement les caractères de contrôle invalides (hors tabulation et sauts de ligne).
 */
public final class AgentTextSanitizer {

    private AgentTextSanitizer() {
    }

    public static String sanitize(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '\n' || character == '\r' || character == '\t') {
                builder.append(character);
            } else if (!Character.isISOControl(character)) {
                builder.append(character);
            }
        }
        return builder.toString().trim();
    }

    public static boolean containsInvalidControlCharacters(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character != '\n' && character != '\r' && character != '\t' && Character.isISOControl(character)) {
                return true;
            }
        }
        return false;
    }
}
