package com.pfa.tracabilite_ia.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseUrlEnvironmentPostProcessorTest {

    @Test
    void convertsPostgresUrlAndAddsNeonSsl() {
        var parsed = DatabaseUrlEnvironmentPostProcessor.parseDatabaseUrl(
                "postgresql://neondb_owner:secret%21@ep-demo.eu-central-1.aws.neon.tech/neondb"
        );

        assertThat(parsed.jdbcUrl()).startsWith("jdbc:postgresql://ep-demo.eu-central-1.aws.neon.tech:5432/neondb");
        assertThat(parsed.jdbcUrl()).contains("sslmode=require");
        assertThat(parsed.username()).isEqualTo("neondb_owner");
        assertThat(parsed.password()).isEqualTo("secret!");
    }

    @Test
    void keepsExistingJdbcSslMode() {
        var parsed = DatabaseUrlEnvironmentPostProcessor.parseDatabaseUrl(
                "jdbc:postgresql://db.example.com:5432/app?sslmode=verify-full"
        );

        assertThat(parsed.jdbcUrl()).isEqualTo("jdbc:postgresql://db.example.com:5432/app?sslmode=verify-full");
        assertThat(parsed.username()).isNull();
    }
}
