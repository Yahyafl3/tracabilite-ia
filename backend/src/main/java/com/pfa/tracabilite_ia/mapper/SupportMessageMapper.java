package com.pfa.tracabilite_ia.mapper;

import com.pfa.tracabilite_ia.dto.response.SupportMessagePageResponse;
import com.pfa.tracabilite_ia.dto.response.SupportMessageResponse;
import com.pfa.tracabilite_ia.entities.SupportMessage;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class SupportMessageMapper {

    public SupportMessageResponse toResponse(SupportMessage entity) {
        return SupportMessageResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .processedAt(entity.getProcessedAt())
                .processedById(entity.getProcessedBy() != null ? entity.getProcessedBy().getId() : null)
                .processedByName(entity.getProcessedBy() != null ? entity.getProcessedBy().getNom() : null)
                .build();
    }

    public SupportMessagePageResponse toPageResponse(Page<SupportMessage> page) {
        return SupportMessagePageResponse.builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
