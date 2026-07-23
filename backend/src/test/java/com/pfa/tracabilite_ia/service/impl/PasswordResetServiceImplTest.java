package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.ForgotPasswordRequest;
import com.pfa.tracabilite_ia.dto.request.ResetPasswordRequest;
import com.pfa.tracabilite_ia.dto.response.MessageResponse;
import com.pfa.tracabilite_ia.entities.PasswordResetToken;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.repository.PasswordResetTokenRepository;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import com.pfa.tracabilite_ia.service.MailService;
import com.pfa.tracabilite_ia.util.HashUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MailService mailService;

    private PasswordResetServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetServiceImpl(
                utilisateurRepository,
                tokenRepository,
                passwordEncoder,
                mailService,
                "http://localhost/auth/reset-password",
                20
        );
    }

    @Test
    void forgotPassword_unknownEmail_returnsSameGenericMessage_andDoesNotSendMail() {
        when(utilisateurRepository.findByEmail("inconnu@test.fr")).thenReturn(Optional.empty());

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("inconnu@test.fr");

        MessageResponse response = service.forgotPassword(request);

        assertThat(response.getMessage())
                .isEqualTo("Si un compte correspond à cette adresse, un lien de réinitialisation a été envoyé.");
        verify(mailService, never()).sendPasswordResetEmail(anyString(), anyString());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void forgotPassword_knownEmail_sendsMailWithToken_andStoresOnlyHash() {
        Utilisateur user = sampleUser();
        when(utilisateurRepository.findByEmail("user@test.fr")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user@test.fr");

        MessageResponse response = service.forgotPassword(request);

        assertThat(response.getMessage())
                .isEqualTo("Si un compte correspond à cette adresse, un lien de réinitialisation a été envoyé.");
        verify(tokenRepository).invalidateActiveTokens(eq(user), any(LocalDateTime.class));
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(mailService).sendPasswordResetEmail(eq("user@test.fr"), linkCaptor.capture());

        String link = linkCaptor.getValue();
        assertThat(link).startsWith("http://localhost/auth/reset-password?token=");
        String rawToken = link.substring(link.indexOf("token=") + 6);
        assertThat(tokenCaptor.getValue().getTokenHash()).isEqualTo(HashUtils.sha256(rawToken));
        assertThat(tokenCaptor.getValue().getTokenHash()).isNotEqualTo(rawToken);
        assertThat(response.getMessage()).doesNotContain(rawToken);
    }

    @Test
    void resetPassword_validToken_encodesNewPassword_andMarksTokenUsed() {
        Utilisateur user = sampleUser();
        String rawToken = "raw-token-value";
        PasswordResetToken token = activeToken(user, HashUtils.sha256(rawToken));

        when(tokenRepository.findByTokenHash(HashUtils.sha256(rawToken))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("nouveau123")).thenReturn("encoded-nouveau123");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(rawToken);
        request.setNewPassword("nouveau123");
        request.setConfirmPassword("nouveau123");

        MessageResponse response = service.resetPassword(request);

        assertThat(response.getMessage()).contains("réinitialisé");
        assertThat(user.getMotDePasseHash()).isEqualTo("encoded-nouveau123");
        assertThat(token.isUsed()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
        verify(passwordEncoder).encode("nouveau123");
        verify(tokenRepository).invalidateActiveTokens(eq(user), any(LocalDateTime.class));
        assertThat(response.getMessage()).doesNotContain(rawToken);
        assertThat(response.getMessage()).doesNotContain("encoded-nouveau123");
    }

    @Test
    void resetPassword_expiredToken_throws() {
        Utilisateur user = sampleUser();
        String rawToken = "expired-token";
        PasswordResetToken token = activeToken(user, HashUtils.sha256(rawToken));
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenHash(HashUtils.sha256(rawToken))).thenReturn(Optional.of(token));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(rawToken);
        request.setNewPassword("nouveau123");
        request.setConfirmPassword("nouveau123");

        assertThatThrownBy(() -> service.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiré");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void resetPassword_usedToken_throws() {
        Utilisateur user = sampleUser();
        String rawToken = "used-token";
        PasswordResetToken token = activeToken(user, HashUtils.sha256(rawToken));
        token.setUsed(true);
        when(tokenRepository.findByTokenHash(HashUtils.sha256(rawToken))).thenReturn(Optional.of(token));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(rawToken);
        request.setNewPassword("nouveau123");
        request.setConfirmPassword("nouveau123");

        assertThatThrownBy(() -> service.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("déjà été utilisé");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void resetPassword_passwordMismatch_throws() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("any");
        request.setNewPassword("nouveau123");
        request.setConfirmPassword("autre");

        assertThatThrownBy(() -> service.resetPassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ne correspondent pas");
    }

    private Utilisateur sampleUser() {
        Utilisateur user = new Utilisateur();
        user.setId(UUID.randomUUID());
        user.setEmail("user@test.fr");
        user.setNom("User");
        user.setRole(RoleEnum.UTILISATEUR);
        user.setMotDePasseHash("old-hash");
        return user;
    }

    private PasswordResetToken activeToken(Utilisateur user, String hash) {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(UUID.randomUUID());
        token.setUtilisateur(user);
        token.setTokenHash(hash);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(20));
        token.setUsed(false);
        token.setCreatedAt(LocalDateTime.now());
        return token;
    }
}
