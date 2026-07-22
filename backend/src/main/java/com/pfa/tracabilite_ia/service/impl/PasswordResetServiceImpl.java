package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.ForgotPasswordRequest;
import com.pfa.tracabilite_ia.dto.request.ResetPasswordRequest;
import com.pfa.tracabilite_ia.dto.response.MessageResponse;
import com.pfa.tracabilite_ia.entities.PasswordResetToken;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.repository.PasswordResetTokenRepository;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import com.pfa.tracabilite_ia.service.MailService;
import com.pfa.tracabilite_ia.service.PasswordResetService;
import com.pfa.tracabilite_ia.util.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final String GENERIC_FORGOT_MESSAGE =
            "Si un compte correspond à cette adresse, un lien de réinitialisation a été envoyé.";
    private static final String GENERIC_RESET_SUCCESS =
            "Votre mot de passe a été réinitialisé. Vous pouvez maintenant vous connecter.";

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String resetPasswordUrl;
    private final long expirationMinutes;

    public PasswordResetServiceImpl(
            UtilisateurRepository utilisateurRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            @Value("${app.frontend.reset-password-url}") String resetPasswordUrl,
            @Value("${app.password-reset.expiration-minutes:20}") long expirationMinutes
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.resetPasswordUrl = resetPasswordUrl;
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            Utilisateur user = userOpt.get();
            tokenRepository.invalidateActiveTokens(user, LocalDateTime.now());

            String rawToken = generateRawToken();
            PasswordResetToken entity = new PasswordResetToken();
            entity.setUtilisateur(user);
            entity.setTokenHash(HashUtils.sha256(rawToken));
            entity.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
            entity.setUsed(false);
            tokenRepository.save(entity);

            String link = buildResetLink(rawToken);
            try {
                mailService.sendPasswordResetEmail(user.getEmail(), link);
            } catch (RuntimeException ex) {
                log.error("Password reset email failed for userId={}", user.getId());
            }
        } else {
            log.info("Password reset requested for unknown account");
        }

        return new MessageResponse(GENERIC_FORGOT_MESSAGE);
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        String rawToken = request.getToken() == null ? "" : request.getToken().trim();
        if (rawToken.isBlank()) {
            throw new IllegalArgumentException("Lien de réinitialisation invalide ou expiré.");
        }

        PasswordResetToken token = tokenRepository.findByTokenHash(HashUtils.sha256(rawToken))
                .orElseThrow(() -> new IllegalArgumentException("Lien de réinitialisation invalide ou expiré."));

        if (token.isUsed()) {
            throw new IllegalArgumentException("Ce lien de réinitialisation a déjà été utilisé.");
        }
        if (token.isExpired()) {
            throw new IllegalArgumentException("Ce lien de réinitialisation a expiré.");
        }

        Utilisateur user = token.getUtilisateur();
        user.setMotDePasseHash(passwordEncoder.encode(request.getNewPassword()));
        utilisateurRepository.save(user);

        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);
        tokenRepository.invalidateActiveTokens(user, LocalDateTime.now());

        log.info("Password reset completed for userId={}", user.getId());
        return new MessageResponse(GENERIC_RESET_SUCCESS);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildResetLink(String rawToken) {
        String base = resetPasswordUrl.endsWith("/")
                ? resetPasswordUrl.substring(0, resetPasswordUrl.length() - 1)
                : resetPasswordUrl;
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "token=" + rawToken;
    }
}
