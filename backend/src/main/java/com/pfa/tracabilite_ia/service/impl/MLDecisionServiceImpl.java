package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.CreditFeaturesRequest;
import com.pfa.tracabilite_ia.dto.response.MLPredictionResponse;
import com.pfa.tracabilite_ia.exception.MLServiceValidationException;
import com.pfa.tracabilite_ia.service.MLDecisionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class MLDecisionServiceImpl implements MLDecisionService {

    private final RestClient restClient;

    public MLDecisionServiceImpl(@Value("${ml.service.url:http://ml-service:5000}") String mlServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(mlServiceUrl)
                .build();
    }

    @Override
    public MLPredictionResponse predict(CreditFeaturesRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", request.getAmount());
        body.put("monthlyIncome", request.getMonthlyIncome());
        body.put("companyAgeYears", request.getCompanyAgeYears());
        body.put("paymentIncidents", request.getPaymentIncidents());
        body.put("debtRatio", request.getDebtRatio());
        body.put("sector", request.getSector().name());
        body.put("includeExplanation", true);

        try {
            return restClient.post()
                    .uri("/predict")
                    .body(body)
                    .retrieve()
                    .body(MLPredictionResponse.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(400))) {
                String message = extractMlError(ex);
                throw new MLServiceValidationException(message);
            }
            throw ex;
        }
    }

    @Override
    public boolean isMLServiceAvailable() {
        try {
            Map<?, ?> response = restClient.get()
                    .uri("/ready")
                    .retrieve()
                    .body(Map.class);
            return response != null && Boolean.TRUE.equals(response.get("ready"));
        } catch (Exception ex) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractMlError(RestClientResponseException ex) {
        try {
            Map<String, Object> payload = ex.getResponseBodyAs(Map.class);
            if (payload != null && payload.get("error") != null) {
                return payload.get("error").toString();
            }
        } catch (Exception ignored) {
            // fallback below
        }
        return "Requête ML invalide: " + ex.getStatusText();
    }
}
