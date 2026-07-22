package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.CreateSupportMessageRequest;
import com.pfa.tracabilite_ia.dto.request.UpdateSupportMessageStatusRequest;
import com.pfa.tracabilite_ia.dto.response.MessageResponse;
import com.pfa.tracabilite_ia.dto.response.SupportMessagePageResponse;
import com.pfa.tracabilite_ia.dto.response.SupportMessageResponse;
import com.pfa.tracabilite_ia.enumeration.SupportMessageStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SupportService {

    MessageResponse createMessage(CreateSupportMessageRequest request, String clientIp);

    SupportMessagePageResponse listMessages(SupportMessageStatus status, String query, Pageable pageable);

    SupportMessageResponse getMessage(UUID id);

    SupportMessageResponse updateStatus(UUID id, UpdateSupportMessageStatusRequest request);
}
