package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.CreditFeaturesRequest;
import com.pfa.tracabilite_ia.dto.response.MLPredictionResponse;

public interface MLDecisionService {

    MLPredictionResponse predict(CreditFeaturesRequest request);

    boolean isMLServiceAvailable();
}
