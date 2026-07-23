package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.CreateSupportMessageRequest;
import com.pfa.tracabilite_ia.dto.request.UpdateSupportMessageStatusRequest;
import com.pfa.tracabilite_ia.dto.response.MessageResponse;
import com.pfa.tracabilite_ia.dto.response.SupportMessageResponse;
import com.pfa.tracabilite_ia.entities.SupportMessage;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.enumeration.SupportMessageStatus;
import com.pfa.tracabilite_ia.mapper.SupportMessageMapper;
import com.pfa.tracabilite_ia.repository.SupportMessageRepository;
import com.pfa.tracabilite_ia.service.AuthService;
import com.pfa.tracabilite_ia.service.MailService;
import com.pfa.tracabilite_ia.service.SupportRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportServiceImplTest {

    @Mock
    private SupportMessageRepository supportMessageRepository;
    @Mock
    private MailService mailService;
    @Mock
    private AuthService authService;
    @Mock
    private SupportRateLimiter rateLimiter;

    private SupportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SupportServiceImpl(
                supportMessageRepository,
                new SupportMessageMapper(),
                mailService,
                authService,
                rateLimiter
        );
    }

    @Test
    void createMessage_valid_savesWithNewStatus_andSendsMail() {
        CreateSupportMessageRequest request = validRequest();
        doNothing().when(rateLimiter).checkAllowed(any(), any());
        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(inv -> {
            SupportMessage msg = inv.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });

        MessageResponse response = service.createMessage(request, "127.0.0.1");

        assertThat(response.getMessage())
                .isEqualTo("Votre demande a été envoyée. Notre équipe vous répondra dès que possible.");

        ArgumentCaptor<SupportMessage> captor = ArgumentCaptor.forClass(SupportMessage.class);
        verify(supportMessageRepository).save(captor.capture());
        SupportMessage saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(SupportMessageStatus.NEW);
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(saved.getName()).isEqualTo("Jane Doe");
        verify(mailService).sendSupportNotification(any(SupportMessage.class));
    }

    @Test
    void createMessage_savesEvenWhenSmtpFails() {
        CreateSupportMessageRequest request = validRequest();
        doNothing().when(rateLimiter).checkAllowed(any(), any());
        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(inv -> {
            SupportMessage msg = inv.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });
        doThrow(new IllegalStateException("smtp down"))
                .when(mailService).sendSupportNotification(any(SupportMessage.class));

        MessageResponse response = service.createMessage(request, "10.0.0.1");

        assertThat(response.getMessage()).contains("Votre demande a été envoyée");
        verify(supportMessageRepository).save(any(SupportMessage.class));
    }

    @Test
    void updateStatus_setsProcessedByFromAuthenticatedAdmin() {
        UUID id = UUID.randomUUID();
        SupportMessage existing = new SupportMessage();
        existing.setId(id);
        existing.setName("Jane");
        existing.setEmail("user@example.com");
        existing.setSubject("Sujet long");
        existing.setMessage("Message suffisamment long");
        existing.setStatus(SupportMessageStatus.NEW);

        Utilisateur admin = new Utilisateur();
        admin.setId(UUID.randomUUID());
        admin.setNom("Admin User");
        admin.setEmail("admin@tracabilite.ia");
        admin.setRole(RoleEnum.ADMINISTRATEUR);

        when(supportMessageRepository.findById(id)).thenReturn(Optional.of(existing));
        when(authService.getCurrentUser()).thenReturn(admin);
        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateSupportMessageStatusRequest request = new UpdateSupportMessageStatusRequest();
        request.setStatus(SupportMessageStatus.RESOLVED);

        SupportMessageResponse response = service.updateStatus(id, request);

        assertThat(response.getStatus()).isEqualTo(SupportMessageStatus.RESOLVED);
        assertThat(response.getProcessedById()).isEqualTo(admin.getId());
        assertThat(response.getProcessedByName()).isEqualTo("Admin User");
        assertThat(response.getProcessedAt()).isNotNull();
        verify(authService).getCurrentUser();
    }

    @Test
    void updateStatus_inProgress_setsProcessedByWithoutProcessedAtRequirement() {
        UUID id = UUID.randomUUID();
        SupportMessage existing = new SupportMessage();
        existing.setId(id);
        existing.setName("Jane");
        existing.setEmail("user@example.com");
        existing.setSubject("Sujet long");
        existing.setMessage("Message suffisamment long");
        existing.setStatus(SupportMessageStatus.NEW);

        Utilisateur admin = new Utilisateur();
        admin.setId(UUID.randomUUID());
        admin.setNom("Admin");
        admin.setRole(RoleEnum.ADMINISTRATEUR);

        when(supportMessageRepository.findById(id)).thenReturn(Optional.of(existing));
        when(authService.getCurrentUser()).thenReturn(admin);
        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateSupportMessageStatusRequest request = new UpdateSupportMessageStatusRequest();
        request.setStatus(SupportMessageStatus.IN_PROGRESS);

        SupportMessageResponse response = service.updateStatus(id, request);

        assertThat(response.getStatus()).isEqualTo(SupportMessageStatus.IN_PROGRESS);
        assertThat(response.getProcessedById()).isEqualTo(admin.getId());
    }

    @Test
    void createMessage_rateLimited_doesNotSave() {
        CreateSupportMessageRequest request = validRequest();
        doThrow(new IllegalArgumentException("Trop de demandes récentes. Veuillez réessayer dans quelques minutes."))
                .when(rateLimiter).checkAllowed(any(), any());

        assertThatThrownBy(() -> service.createMessage(request, "1.2.3.4"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trop de demandes");

        verify(supportMessageRepository, never()).save(any());
        verify(mailService, never()).sendSupportNotification(any());
    }

    private static CreateSupportMessageRequest validRequest() {
        CreateSupportMessageRequest request = new CreateSupportMessageRequest();
        request.setName("Jane Doe");
        request.setEmail("user@example.com");
        request.setSubject("Problème de connexion");
        request.setMessage("Je n'arrive pas à me connecter depuis ce matin.");
        return request;
    }
}
