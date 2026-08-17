package com.pfa.tracabilite_ia.dto.request;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestoreBackupRequest {

    @AssertTrue(message = "La restauration exige une confirmation explicite.")
    private Boolean confirm;
}
