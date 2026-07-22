package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.entities.SupportMessage;
import com.pfa.tracabilite_ia.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.format.DateTimeFormatter;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String supportEmail;

    public MailServiceImpl(
            JavaMailSender mailSender,
            @Value("${app.mail.from:}") String fromAddress,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${app.support.email:}") String supportEmail
    ) {
        this.mailSender = mailSender;
        this.fromAddress = (fromAddress == null || fromAddress.isBlank()) ? mailUsername : fromAddress;
        this.supportEmail = (supportEmail == null || supportEmail.isBlank()) ? this.fromAddress : supportEmail;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        String maskedTo = maskEmail(toEmail);
        log.info("Password reset email send attempt to={}", maskedTo);
        if (fromAddress == null || fromAddress.isBlank()) {
            log.error("Password reset email aborted: sender not configured");
            throw new IllegalStateException("Impossible d'envoyer l'email de réinitialisation.");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Réinitialisation de votre mot de passe — Traçabilité IA");
            helper.setText(buildPasswordResetHtml(resetLink), true);
            mailSender.send(message);
            log.info("Password reset email sent successfully to={}", maskedTo);
        } catch (MessagingException | MailException ex) {
            log.error("Failed to send password reset email to={} errorType={} detail={}",
                    maskedTo,
                    ex.getClass().getSimpleName(),
                    safeMailError(ex));
            throw new IllegalStateException("Impossible d'envoyer l'email de réinitialisation.");
        }
    }

    @Override
    public void sendSupportNotification(SupportMessage supportMessage) {
        if (supportEmail == null || supportEmail.isBlank()) {
            log.error("Support notification skipped: destination not configured");
            throw new IllegalStateException("Destinataire support non configuré.");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(supportEmail);
            helper.setSubject("Nouvelle demande de support — Traçabilité IA");
            helper.setText(buildSupportText(supportMessage), buildSupportHtml(supportMessage));
            mailSender.send(message);
            log.info("Support notification email dispatched for messageId={}", supportMessage.getId());
        } catch (MessagingException | MailException ex) {
            log.error("Failed to send support notification email: {} - {}",
                    ex.getClass().getSimpleName(),
                    safeMailError(ex));
            throw new IllegalStateException("Impossible d'envoyer la notification de support.");
        }
    }

    private static String safeMailError(Throwable ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            Throwable cause = ex.getCause();
            msg = cause != null ? cause.getClass().getSimpleName() : "unknown";
        }
        return msg.replaceAll("(?i)(password|passwd|secret|token)\\s*[=:].*", "$1=***")
                .replaceAll("[\\w.+-]+@[\\w.-]+", "[redacted]");
    }

    /** Mask email for logs: y***@gmail.com */
    static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "[empty]";
        }
        String trimmed = email.trim();
        int at = trimmed.indexOf('@');
        if (at <= 0 || at == trimmed.length() - 1) {
            return "***";
        }
        char first = trimmed.charAt(0);
        return first + "***@" + trimmed.substring(at + 1);
    }

    private String buildPasswordResetHtml(String resetLink) {
        String safeLink = HtmlUtils.htmlEscape(resetLink == null ? "" : resetLink);
        return """
                <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #0f172a;">
                  <h2 style="color: #4f46e5;">Traçabilité IA</h2>
                  <p>Vous avez demandé la réinitialisation de votre mot de passe.</p>
                  <p>Ce lien est valable pendant <strong>20 minutes</strong> et ne peut être utilisé qu'une seule fois.</p>
                  <p style="margin: 28px 0;">
                    <a href="%s"
                       style="display: inline-block; padding: 12px 22px; background: #4f46e5; color: #ffffff;
                              text-decoration: none; border-radius: 8px; font-weight: 600;">
                      Réinitialiser mon mot de passe
                    </a>
                  </p>
                  <p>Si le bouton ne fonctionne pas, copiez ce lien dans votre navigateur :</p>
                  <p style="word-break: break-all; color: #334155;">%s</p>
                  <p style="color: #64748b; font-size: 0.9rem;">
                    Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.
                  </p>
                </div>
                """.formatted(safeLink, safeLink);
    }

    private String buildSupportHtml(SupportMessage msg) {
        String created = msg.getCreatedAt() != null ? DATE_FORMAT.format(msg.getCreatedAt()) : "";
        return """
                <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #0f172a;">
                  <h2 style="color: #4f46e5;">Nouvelle demande de support</h2>
                  <p><strong>ID :</strong> %s</p>
                  <p><strong>Nom :</strong> %s</p>
                  <p><strong>Email :</strong> %s</p>
                  <p><strong>Sujet :</strong> %s</p>
                  <p><strong>Date :</strong> %s</p>
                  <p><strong>Message :</strong></p>
                  <pre style="white-space: pre-wrap; background: #f8fafc; padding: 12px; border-radius: 8px;">%s</pre>
                </div>
                """.formatted(
                HtmlUtils.htmlEscape(String.valueOf(msg.getId())),
                HtmlUtils.htmlEscape(nullToEmpty(msg.getName())),
                HtmlUtils.htmlEscape(nullToEmpty(msg.getEmail())),
                HtmlUtils.htmlEscape(nullToEmpty(msg.getSubject())),
                HtmlUtils.htmlEscape(created),
                HtmlUtils.htmlEscape(nullToEmpty(msg.getMessage()))
        );
    }

    private String buildSupportText(SupportMessage msg) {
        String created = msg.getCreatedAt() != null ? DATE_FORMAT.format(msg.getCreatedAt()) : "";
        return """
                Nouvelle demande de support — Traçabilité IA

                ID: %s
                Nom: %s
                Email: %s
                Sujet: %s
                Date: %s

                Message:
                %s
                """.formatted(
                msg.getId(),
                nullToEmpty(msg.getName()),
                nullToEmpty(msg.getEmail()),
                nullToEmpty(msg.getSubject()),
                created,
                nullToEmpty(msg.getMessage())
        );
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
