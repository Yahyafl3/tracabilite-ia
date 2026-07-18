package com.pfa.tracabilite_ia.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentTextSanitizerTest {

    @Test
    void sanitize_preservesFrenchAccents() {
        String input = "éligibilité, créance, réévaluation, décision";

        assertThat(AgentTextSanitizer.sanitize(input)).isEqualTo(input);
    }

    @Test
    void sanitize_removesInvalidControlCharacters() {
        assertThat(AgentTextSanitizer.sanitize("texte\u0001\u0007valide"))
                .isEqualTo("textevalide");
    }

    @Test
    void sanitize_keepsNewlinesAndTabs() {
        assertThat(AgentTextSanitizer.sanitize("ligne1\nligne2\tindent"))
                .isEqualTo("ligne1\nligne2\tindent");
    }
}
