package com.pfa.tracabilite_ia.controller;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class AIControllerSecurityTest {

    @Test
    void analyzeDecision_requiresUserOrAdminRole() throws Exception {
        Method method = AIController.class.getMethod("analyzeDecision",
                com.pfa.tracabilite_ia.dto.request.DecisionAnalysisRequest.class);

        assertThat(method.isAnnotationPresent(
                org.springframework.security.access.prepost.PreAuthorize.class)).isTrue();
        assertThat(method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class).value())
                .isEqualTo("hasAnyRole('USER', 'ADMIN')");
    }

    @Test
    void testPost_hasNoPreAuthorize() throws Exception {
        Method method = AIController.class.getMethod("testPost", java.util.Map.class);

        assertThat(method.isAnnotationPresent(
                org.springframework.security.access.prepost.PreAuthorize.class)).isFalse();
    }
}
