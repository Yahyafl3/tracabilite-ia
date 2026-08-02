package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.CreateCreditDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.CreateEducationDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.CreateMedicalDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.CreditFeaturesRequest;
import com.pfa.tracabilite_ia.dto.response.DomainPredictionResponse;
import com.pfa.tracabilite_ia.dto.response.MLPredictionResponse;
import com.pfa.tracabilite_ia.exception.MLServiceValidationException;
import com.pfa.tracabilite_ia.service.MLDecisionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class MLDecisionServiceImpl implements MLDecisionService {

    private final RestClient restClient;

    public MLDecisionServiceImpl(
            @Value("${ml.service.url:http://ml-service:5000}") String mlServiceUrl,
            @Value("${ml.service.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${ml.service.read-timeout-ms:15000}") int readTimeoutMs,
            @Value("${ml.service.token:}") String mlServiceToken
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(mlServiceUrl)
                .requestFactory(requestFactory);
        if (mlServiceToken != null && !mlServiceToken.isBlank()) {
            builder.defaultHeader("X-Internal-Token", mlServiceToken);
        }
        this.restClient = builder.build();
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
                throw new MLServiceValidationException(extractMlError(ex));
            }
            if (ex.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(401))
                    || ex.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(403))) {
                throw new IllegalStateException("Service ML : authentification interne refusée");
            }
            throw ex;
        }
    }

    @Override
    public DomainPredictionResponse predictCredit(CreateCreditDecisionRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("age", request.getAge());
        body.put("dureeMois", request.getDureeMois());
        body.put("typeContrat", request.getTypeContrat());
        body.put("statutLogement", request.getStatutLogement());
        body.put("incidentPaiementBam", request.getIncidentPaiementBam());
        body.put("montantDemandeMad", request.getMontantDemandeMad());
        body.put("nouvelleEcheanceMad", request.getNouvelleEcheanceMad());
        body.put("revenuMensuelMad", request.getRevenuMensuelMad());
        body.put("tauxEndettement", request.getTauxEndettement());
        return postDomainPredict("/predict/credit", body);
    }

    @Override
    public DomainPredictionResponse predictMedical(CreateMedicalDecisionRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("age", request.getAge());
        body.put("grossesses", request.getGrossesses());
        body.put("glycemieMgDl", request.getGlycemieMgDl());
        body.put("pressionArterielleMmhg", request.getPressionArterielleMmhg());
        body.put("epaisseurPliCutaneMm", request.getEpaisseurPliCutaneMm());
        body.put("insulineMicroUMl", request.getInsulineMicroUMl());
        body.put("imcKgM2", request.getImcKgM2());
        return postDomainPredict("/predict/medical", body);
    }

    @Override
    public DomainPredictionResponse predictEducation(CreateEducationDecisionRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("ageInscription", request.getAgeInscription());
        body.put("noteAdmission", request.getNoteAdmission());
        body.put("noteQualificationPrecedente", request.getNoteQualificationPrecedente());
        body.put("unitesValideesS1", request.getUnitesValideesS1());
        body.put("moyenneS1", request.getMoyenneS1());
        body.put("unitesValideesS2", request.getUnitesValideesS2());
        body.put("moyenneS2", request.getMoyenneS2());
        body.put("tauxChomage", request.getTauxChomage());
        body.put("tauxInflation", request.getTauxInflation());
        body.put("pib", request.getPib());
        body.put("sexe", request.getSexe());
        body.put("boursier", request.getBoursier());
        body.put("fraisAJour", request.getFraisAJour());
        body.put("debiteur", request.getDebiteur());
        body.put("deplace", request.getDeplace());
        body.put("international", request.getInternational());
        return postDomainPredict("/predict/education", body);
    }

    @Override
    public DomainPredictionResponse predictDomain(String domain, Map<String, Object> payload) {
        String path = switch (domain.toUpperCase()) {
            case "CREDIT" -> "/predict/credit";
            case "MEDICAL" -> "/predict/medical";
            case "EDUCATION" -> "/predict/education";
            default -> throw new IllegalArgumentException("Domaine inconnu: " + domain);
        };
        return postDomainPredict(path, payload);
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

    private DomainPredictionResponse postDomainPredict(String uri, Map<String, Object> body) {
        try {
            return restClient.post()
                    .uri(uri)
                    .body(body)
                    .retrieve()
                    .body(DomainPredictionResponse.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(400))) {
                throw new MLServiceValidationException(extractMlError(ex));
            }
            if (ex.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(401))
                    || ex.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(403))) {
                throw new IllegalStateException("Service ML : authentification interne refusée");
            }
            throw ex;
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
