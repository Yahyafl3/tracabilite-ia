package com.pfa.tracabilite_ia.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashUtilsTest {

    @Test
    void sha256_stringAndUtf8BytesMatch() {
        assertThat(HashUtils.sha256("abc")).isEqualTo(HashUtils.sha256("abc".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test
    void sha256_detectsByteChange() {
        byte[] original = "{\"ok\":true}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] tampered = "{\"ok\":false}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(HashUtils.sha256(original)).isNotEqualTo(HashUtils.sha256(tampered));
    }
}
