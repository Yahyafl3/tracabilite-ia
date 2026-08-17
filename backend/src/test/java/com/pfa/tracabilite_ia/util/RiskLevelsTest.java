package com.pfa.tracabilite_ia.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RiskLevelsTest {

    @Test
    void isHigh_acceptsEnglishAndFrenchLabels() {
        assertThat(RiskLevels.isHigh("HIGH")).isTrue();
        assertThat(RiskLevels.isHigh("ELEVE")).isTrue();
        assertThat(RiskLevels.isHigh("ÉLEVÉ")).isTrue();
        assertThat(RiskLevels.isHigh("Élevé")).isTrue();
        assertThat(RiskLevels.isHigh(" eleve ")).isTrue();
    }

    @Test
    void isMedium_acceptsBothMlVocabularies() {
        assertThat(RiskLevels.isMedium("MEDIUM")).isTrue();
        assertThat(RiskLevels.isMedium("MOYEN")).isTrue();
        assertThat(RiskLevels.isMedium("MODERE")).isTrue();
        assertThat(RiskLevels.isMedium("Modéré")).isTrue();
    }

    @Test
    void isLow_acceptsBothMlVocabularies() {
        assertThat(RiskLevels.isLow("LOW")).isTrue();
        assertThat(RiskLevels.isLow("FAIBLE")).isTrue();
    }

    @Test
    void levelsAreMutuallyExclusive() {
        assertThat(RiskLevels.isMedium("ELEVE")).isFalse();
        assertThat(RiskLevels.isLow("HIGH")).isFalse();
        assertThat(RiskLevels.isHigh("MOYEN")).isFalse();
    }

    @Test
    void unknownOrNullValuesMatchNothing() {
        assertThat(RiskLevels.normalize(null)).isNull();
        assertThat(RiskLevels.normalize("")).isNull();
        assertThat(RiskLevels.normalize("RISQUE_ELEVE")).isNull();
        assertThat(RiskLevels.isHigh(null)).isFalse();
    }
}
