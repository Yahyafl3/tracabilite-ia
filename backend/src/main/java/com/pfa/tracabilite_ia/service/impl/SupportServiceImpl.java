package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.CreateSupportMessageRequest;
import com.pfa.tracabilite_ia.dto.request.UpdateSupportMessageStatusRequest;
import com.pfa.tracabilite_ia.dto.response.MessageResponse;
import com.pfa.tracabilite_ia.dto.response.SupportMessagePageResponse;
import com.pfa.tracabilite_ia.dto.response.SupportMessageResponse;
import com.pfa.tracabilite_ia.entities.SupportMessage;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.SupportMessageStatus;
import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;
import com.pfa.tracabilite_ia.mapper.SupportMessageMapper;
import com.pfa.tracabilite_ia.repository.SupportMessageRepository;
import com.pfa.tracabilite_ia.service.AuthService;
import com.pfa.tracabilite_ia.service.MailService;
import com.pfa.tracabilite_ia.service.SupportRateLimiter;
import com.pfa.tracabilite_ia.service.SupportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SupportServiceImpl implements SupportService {

    private static final Logger log = LoggerFactory.getLogger(SupportServiceImpl.class);
    private static final String SUCCESS_MESSAGE =
            "Votre demande a été envoyée. Notre équipe vous répondra dès que possible.";

    private final SupportMessageRepository supportMessageRepository;
    private final SupportMessageMapper supportMessageMapper;
    private final MailService mailService;
    private final AuthService authService;
    private final SupportRateLimiter rateLimiter;

    public SupportServiceImpl(
            SupportMessageRepository supportMessageRepository,
            SupportMessageMapper supportMessageMapper,
            MailService mailService,
            AuthService authService,
            SupportRateLimiter rateLimiter
    ) {
        this.supportMessageRepository = supportMessageRepository;
        this.supportMessageMapper = supportMessageMapper;
        this.mailService = mailService;
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @Override
    @Transactional
    public MessageResponse createMessage(CreateSupportMessageRequest request, String clientIp) {
        String name = sanitize(request.getName());
        String email = sanitize(request.getEmail()).toLowerCase();
        String subject = sanitize(request.getSubject());
        String message = sanitize(request.getMessage());

        rateLimiter.checkAllowed(email, clientIp);

        SupportMessage entity = new SupportMessage();
        entity.setName(name);
        entity.setEmail(email);
        entity.setSubject(subject);
        entity.setMessage(message);
        entity.setStatus(SupportMessageStatus.NEW);

        SupportMessage saved = supportMessageRepository.save(entity);

        try {
            mailService.sendSupportNotification(saved);
        } catch (RuntimeException ex) {
            log.error("Support notification email failed for messageId={}", saved.getId());
        }

        return new MessageResponse(SUCCESS_MESSAGE);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportMessagePageResponse listMessages(
            SupportMessageStatus status,
            String query,
            Pageable pageable
    ) {
        String normalizedQuery = query == null ? null : query.trim();
        Page<SupportMessage> page = supportMessageRepository.search(status, normalizedQuery, pageable);
        return supportMessageMapper.toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportMessageResponse getMessage(UUID id) {
        return supportMessageMapper.toResponse(findById(id));
    }

    @Override
    @Transactional
    public SupportMessageResponse updateStatus(UUID id, UpdateSupportMessageStatusRequest request) {
        SupportMessage entity = findById(id);
        SupportMessageStatus newStatus = request.getStatus();
        entity.setStatus(newStatus);
        entity.setUpdatedAt(LocalDateTime.now());

        Utilisateur admin = authService.getCurrentUser();
        entity.setProcessedBy(admin);

        if (newStatus == SupportMessageStatus.RESOLVED || newStatus == SupportMessageStatus.CLOSED) {
            entity.setProcessedAt(LocalDateTime.now());
        }

        return supportMessageMapper.toResponse(supportMessageRepository.save(entity));
    }

    private SupportMessage findById(UUID id) {
        return supportMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de support introuvable."));
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace("\0", "");
    }
}
