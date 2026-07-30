package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.CreateCreditDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.CreateEducationDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.CreateMedicalDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.CreditFeaturesRequest;
import com.pfa.tracabilite_ia.dto.response.DomainPredictionResponse;
import com.pfa.tracabilite_ia.dto.response.MLPredictionResponse;

import java.util.Map;

public interface MLDecisionService {

    /** Legacy crédit (features simplifiées + SHAP). */
    MLPredictionResponse predict(CreditFeaturesRequest request);

    DomainPredictionResponse predictCredit(CreateCreditDecisionRequest request);

    DomainPredictionResponse predictMedical(CreateMedicalDecisionRequest request);

    DomainPredictionResponse predictEducation(CreateEducationDecisionRequest request);

    DomainPredictionResponse predictDomain(String domain, Map<String, Object> payload);

    boolean isMLServiceAvailable();
}
