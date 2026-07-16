package com.pfa.tracabilite_ia.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pfa.tracabilite_ia.config.CreditSchemaConfig;
import com.pfa.tracabilite_ia.dto.request.CreditFeaturesRequest;
import com.pfa.tracabilite_ia.enumeration.CreditSectorEnum;
import com.pfa.tracabilite_ia.exception.MLServiceValidationException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CreditFeaturesValidator {

    private static final Set<String> ALLOWED_REQUEST_FIELDS = Set.of(
            "amount",
            "monthlyIncome",
            "companyAgeYears",
            "paymentIncidents",
            "debtRatio",
            "sector",
            "description",
            "includeOllama"
    );

    public void validate(CreditFeaturesRequest request) {
        if (request.getSector() == null) {
            throw new MLServiceValidationException("Le secteur est requis.");
        }

        CreditSectorEnum sector = request.getSector();
        Set<String> allowedMlKeys = CreditSchemaConfig.allowedMlKeys(sector);

        if (!allowedMlKeys.contains("sector")) {
            throw new MLServiceValidationException("Configuration ML invalide pour le secteur " + sector);
        }

        var configuredKeys = CreditSchemaConfig.fieldsFor(sector).stream()
                .map(CreditSchemaConfig.CreditFieldDefinition::key)
                .toList();

        for (String key : configuredKeys) {
            if (!allowedMlKeys.contains(key)) {
                throw new MLServiceValidationException(
                        "Champ '" + key + "' configuré pour " + sector + " mais absent du schéma ML."
                );
            }
        }
    }

    public static Set<String> allowedRequestFields() {
        return ALLOWED_REQUEST_FIELDS;
    }
}
