package com.pfa.tracabilite_ia.dto.response;

import com.pfa.tracabilite_ia.enumeration.SupportMessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportMessageResponse {
    private UUID id;
    private String name;
    private String email;
    private String subject;
    private String message;
    private SupportMessageStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private UUID processedById;
    private String processedByName;
}
