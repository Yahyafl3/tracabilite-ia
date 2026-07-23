package com.pfa.tracabilite_ia.dto.request;

import com.pfa.tracabilite_ia.enumeration.SupportMessageStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSupportMessageStatusRequest {

    @NotNull(message = "Le statut est requis")
    private SupportMessageStatus status;
}
