package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.mail.ResendEmailClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private ResendEmailClient resendEmailClient;

    @Test
    void sendPasswordResetEmail_usesResendWhenProviderIsResend() {
        MailServiceImpl service = new MailServiceImpl(
                mailSender,
                resendEmailClient,
                "resend",
                "Traçabilité IA <noreply@example.com>",
                "",
                ""
        );

        String link = "https://tracabilite-ia.vercel.app/auth/reset-password?token=abc123token";
        service.sendPasswordResetEmail("user@test.fr", link);

        verify(resendEmailClient).sendHtml(
                eq("Traçabilité IA <noreply@example.com>"),
                eq("user@test.fr"),
                eq("Réinitialisation de votre mot de passe — Traçabilité IA"),
                org.mockito.ArgumentMatchers.argThat(html ->
                        html.contains("Réinitialiser mon mot de passe")
                                && html.contains(link)
                                && !html.contains("copiez ce lien")
                )
        );
        verifyNoInteractions(mailSender);
    }

    @Test
    void buildPasswordResetHtml_containsButtonOnly_noRawLinkParagraph() {
        String html = MailServiceImpl.buildPasswordResetHtml(
                "https://tracabilite-ia.vercel.app/auth/reset-password?token=tok"
        );
        assertThat(html).contains("href=\"https://tracabilite-ia.vercel.app/auth/reset-password?token=tok\"");
        assertThat(html).contains("Réinitialiser mon mot de passe");
        assertThat(html).doesNotContain("Si le bouton ne fonctionne pas");
        assertThat(html).doesNotContain("copiez ce lien");
    }
}
