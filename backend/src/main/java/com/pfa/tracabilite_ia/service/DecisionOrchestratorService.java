package com.pfa.tracabilite_ia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.request.CreateCreditDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.CreateEducationDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.CreateMedicalDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.DomainValidationRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.dto.response.DomainPredictionResponse;
import com.pfa.tracabilite_ia.entities.*;
import com.pfa.tracabilite_ia.enumeration.*;
import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;
import com.pfa.tracabilite_ia.mapper.DecisionMapper;
import com.pfa.tracabilite_ia.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DecisionOrchestratorService {

    private static final Set<String> CREDIT_DECISIONS = Set.of("ACCEPTEE", "REFUSEE", "A_REVOIR");
    private static final Set<String> MEDICAL_DECISIONS = Set.of(
            "SUIVI_STANDARD", "EXAMEN_COMPLEMENTAIRE", "ORIENTATION_SPECIALISTE", "A_REVOIR");
    private static final Set<String> EDUCATION_DECISIONS = Set.of(
            "AUCUNE_INTERVENTION", "ACCOMPAGNEMENT", "ENTRETIEN_PEDAGOGIQUE",
            "TUTORAT", "ORIENTATION", "A_REVOIR");

    private final DecisionRepository decisionRepository;
    private final MLDecisionService mlDecisionService;
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final DecisionMapper decisionMapper;
    private final ObjectMapper objectMapper;
    private final DomainAgentConsultationService domainAgentConsultationService;
    private final DecisionHashService decisionHashService;

    public DecisionOrchestratorService(
            DecisionRepository decisionRepository,
            MLDecisionService mlDecisionService,
            AuthService authService,
            AuditLogService auditLogService,
            DecisionMapper decisionMapper,
            ObjectMapper objectMapper,
            DomainAgentConsultationService domainAgentConsultationService,
            DecisionHashService decisionHashService
    ) {
        this.decisionRepository = decisionRepository;
        this.mlDecisionService = mlDecisionService;
        this.authService = authService;
        this.auditLogService = auditLogService;
        this.decisionMapper = decisionMapper;
        this.objectMapper = objectMapper;
        this.domainAgentConsultationService = domainAgentConsultationService;
        this.decisionHashService = decisionHashService;
    }

    @Transactional
    public DecisionResponse createAndAnalyzeCredit(CreateCreditDecisionRequest request) {
        Utilisateur user = authService.getCurrentUser();
        assertCanCreate(user, DecisionDomain.CREDIT);
        DomainPredictionResponse prediction = mlDecisionService.predictCredit(request);

        Decision decision = baseDecision(DecisionDomain.CREDIT, user, request.getDescription(), prediction);
        decision.setPrompt("Analyse risque défaut crédit (synthétique Maroc)");
        decision.setContexte("Domaine CREDIT — validation humaine RESPONSABLE_CREDIT");
        decision.setFeaturesJson(writeJson(request));
        decision.setSourceDonnees("credit-maroc-synthetic");

        CreditDecisionData data = new CreditDecisionData();
        data.setDecision(decision);
        data.setAge(request.getAge());
        data.setDureeMois(request.getDureeMois());
        data.setTypeContrat(request.getTypeContrat());
        data.setStatutLogement(request.getStatutLogement());
        data.setIncidentPaiementBam(request.getIncidentPaiementBam());
        data.setMontantDemandeMad(request.getMontantDemandeMad());
        data.setNouvelleEcheanceMad(request.getNouvelleEcheanceMad());
        data.setRevenuMensuelMad(request.getRevenuMensuelMad());
        data.setTauxEndettement(request.getTauxEndettement());
        decision.setCreditData(data);

        applyPrediction(decision, prediction);
        return persistAnalyzeAndAudit(
                decision, user, request.getIncludeAgents(), DecisionDomain.CREDIT,
                "Analyse ML crédit", prediction.getAnalysisId());
    }

    @Transactional
    public DecisionResponse createAndAnalyzeMedical(CreateMedicalDecisionRequest request) {
        Utilisateur user = authService.getCurrentUser();
        assertCanCreate(user, DecisionDomain.MEDICAL);
        DomainPredictionResponse prediction = mlDecisionService.predictMedical(request);

        Decision decision = baseDecision(DecisionDomain.MEDICAL, user, request.getDescription(), prediction);
        decision.setPrompt("Évaluation indicative risque diabète (non diagnostique)");
        decision.setContexte(
                "Ce module fournit une estimation de risque à titre d'aide à la décision. "
                        + "Il ne remplace pas un diagnostic médical ni l'avis d'un professionnel de santé.");
        decision.setFeaturesJson(writeJson(request));
        decision.setSourceDonnees("medical-diabetes-maroc-synthetic");

        MedicalDecisionData data = new MedicalDecisionData();
        data.setDecision(decision);
        data.setAge(request.getAge());
        data.setGrossesses(request.getGrossesses());
        data.setGlycemieMgDl(request.getGlycemieMgDl());
        data.setPressionArterielleMmhg(request.getPressionArterielleMmhg());
        data.setEpaisseurPliCutaneMm(request.getEpaisseurPliCutaneMm());
        data.setInsulineMicroUMl(request.getInsulineMicroUMl());
        data.setImcKgM2(request.getImcKgM2());
        decision.setMedicalData(data);

        applyPrediction(decision, prediction);
        return persistAnalyzeAndAudit(
                decision, user, request.getIncludeAgents(), DecisionDomain.MEDICAL,
                "Analyse ML médicale (indicative)", prediction.getAnalysisId());
    }

    @Transactional
    public DecisionResponse createAndAnalyzeEducation(CreateEducationDecisionRequest request) {
        Utilisateur user = authService.getCurrentUser();
        assertCanCreate(user, DecisionDomain.EDUCATION);
        DomainPredictionResponse prediction = mlDecisionService.predictEducation(request);

        Decision decision = baseDecision(DecisionDomain.EDUCATION, user, request.getDescription(), prediction);
        decision.setPrompt("Évaluation risque décrochage — accompagnement pédagogique");
        decision.setContexte(
                "Aide à l'accompagnement. Ne constitue pas une sanction automatique contre l'étudiant.");
        decision.setFeaturesJson(writeJson(request));
        decision.setSourceDonnees("students-maroc-dropout-synthetic");

        EducationDecisionData data = new EducationDecisionData();
        data.setDecision(decision);
        data.setAgeInscription(request.getAgeInscription());
        data.setNoteAdmission(request.getNoteAdmission());
        data.setNoteQualificationPrecedente(request.getNoteQualificationPrecedente());
        data.setUnitesValideesS1(request.getUnitesValideesS1());
        data.setMoyenneS1(request.getMoyenneS1());
        data.setUnitesValideesS2(request.getUnitesValideesS2());
        data.setMoyenneS2(request.getMoyenneS2());
        data.setTauxChomage(request.getTauxChomage());
        data.setTauxInflation(request.getTauxInflation());
        data.setPib(request.getPib());
        data.setSexe(request.getSexe());
        data.setBoursier(request.getBoursier());
        data.setFraisAJour(request.getFraisAJour());
        data.setDebiteur(request.getDebiteur());
        data.setDeplace(request.getDeplace());
        data.setInternational(request.getInternational());
        decision.setEducationData(data);

        applyPrediction(decision, prediction);
        return persistAnalyzeAndAudit(
                decision, user, request.getIncludeAgents(), DecisionDomain.EDUCATION,
                "Analyse ML éducation", prediction.getAnalysisId());
    }

    /**
     * Persiste l'agrégat {@link Decision} (cascade one-to-one + facteurs) une seule fois,
     * puis consulte les agents (nécessite un {@code decisionId} non transient),
     * puis enregistre l'{@link AuditLog}.
     */
    private DecisionResponse persistAnalyzeAndAudit(
            Decision decision,
            Utilisateur user,
            Boolean includeAgents,
            DecisionDomain domain,
            String auditDetails,
            String correlationId
    ) {
        // 1) Graphe cohérent → un seul save racine (cascade ALL sur données spécialisées / facteurs)
        Decision saved = decisionRepository.save(decision);

        // 2) Agents après persist : DecisionHistory / ReponseAgentIA référencent un Decision géré
        if (Boolean.TRUE.equals(includeAgents)) {
            domainAgentConsultationService.consultAgents(
                    saved, domain, saved.getFeaturesJson(), user);
        }

        // 3) Signature après persist : decisionId n'est généré qu'à ce moment, et le hacher
        // avant produirait une empreinte que la relecture ne pourra jamais reproduire.
        decisionHashService.refreshHashComponents(saved, saved.getReponsesAgents());
        saved = decisionRepository.save(saved);

        // 3) Audit après sauvegarde cohérente (pas dans @PrePersist)
        auditLogService.record(saved.getDecisionId(), user, "ANALYSE",
                StatutDecisionEnum.BROUILLON, StatutDecisionEnum.EN_ATTENTE_VALIDATION,
                auditDetails, correlationId);

        return decisionMapper.toResponse(saved);
    }

    @Transactional
    public DecisionResponse submit(UUID id) {
        Utilisateur user = authService.getCurrentUser();
        Decision decision = find(id);
        StatutDecisionEnum old = decision.getStatutValidation();
        decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE_VALIDATION);
        decision.setSubmittedAt(LocalDateTime.now());
        decision.setUpdatedAt(LocalDateTime.now());
        decision.setCurrentHash(decision.calculerHash());
        Decision saved = decisionRepository.save(decision);
        auditLogService.record(id, user, "SOUMISSION", old, StatutDecisionEnum.EN_ATTENTE_VALIDATION,
                "Soumission à validation humaine", null);
        return decisionMapper.toResponse(saved);
    }

    @Transactional
    public DecisionResponse validate(UUID id, DomainValidationRequest request) {
        Utilisateur user = authService.getCurrentUser();
        Decision decision = find(id);
        assertCanValidate(user, decision);
        assertValidFinalDecision(decision.getDomaine(), request.getDecisionFinale());

        if (request.getJustificationHumaine() == null || request.getJustificationHumaine().isBlank()
                || request.getJustificationHumaine().trim().length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Justification humaine obligatoire (minimum 10 caractères)");
        }

        boolean agreement = computeAgreement(decision, request);
        if (!agreement && request.getJustificationHumaine().trim().length() < 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Justification détaillée obligatoire en cas de désaccord avec l'IA (minimum 30 caractères)");
        }

        StatutDecisionEnum old = decision.getStatutValidation();
        decision.setHumanDecision(request.getDecisionFinale());
        decision.setJustificationHumaine(request.getJustificationHumaine());
        decision.setAccordAvecIa(agreement);
        decision.setValidateurId(user.getId());
        decision.setValidateurRole(user.getRole().name());
        decision.setValidatorEmail(user.getEmail());
        // Truncature à la seconde : évite un drift nanos vs TIMESTAMP PostgreSQL
        decision.setValidatedAt(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        decision.setUpdatedAt(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));

        StatutDecisionEnum nouveau = "A_REVOIR".equals(request.getDecisionFinale())
                ? StatutDecisionEnum.A_REVOIR
                : StatutDecisionEnum.VALIDEE;
        decision.setStatutValidation(nouveau);
        Decision saved = decisionRepository.save(decision);
        // Recalcule après flush pour utiliser les valeurs persistées
        saved.setCurrentHash(saved.calculerHash());
        saved = decisionRepository.save(saved);
        auditLogService.record(id, user, agreement ? "VALIDATION" : "VALIDATION_DESACCORD_IA",
                old, nouveau,
                "Décision finale=" + request.getDecisionFinale() + "; accordIA=" + agreement,
                null);
        return decisionMapper.toResponse(saved);
    }

    @Transactional
    public DecisionResponse requestReview(UUID id, DomainValidationRequest request) {
        Utilisateur user = authService.getCurrentUser();
        Decision decision = find(id);
        assertCanValidate(user, decision);
        StatutDecisionEnum old = decision.getStatutValidation();
        decision.setStatutValidation(StatutDecisionEnum.A_REVOIR);
        decision.setJustificationHumaine(request.getJustificationHumaine());
        decision.setValidateurId(user.getId());
        decision.setValidateurRole(user.getRole().name());
        decision.setValidatorEmail(user.getEmail());
        decision.setUpdatedAt(LocalDateTime.now());
        decision.setCurrentHash(decision.calculerHash());
        Decision saved = decisionRepository.save(decision);
        auditLogService.record(id, user, "DEMANDE_REVISION", old, StatutDecisionEnum.A_REVOIR,
                request.getJustificationHumaine(), null);
        return decisionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> byDomain(DecisionDomain domain) {
        Utilisateur user = authService.getCurrentUser();
        RoleEnum role = user != null ? user.getRole() : null;

        if (role != RoleEnum.ADMINISTRATEUR && role != RoleEnum.AUDITEUR) {
             DecisionDomain checkDomain = domain != null ? domain : DecisionDomain.CREDIT;
             boolean allowed = switch (checkDomain) {
                case CREDIT -> role == RoleEnum.RESPONSABLE_CREDIT;
                case MEDICAL -> role == RoleEnum.PROFESSIONNEL_SANTE;
                case EDUCATION -> role == RoleEnum.RESPONSABLE_PEDAGOGIQUE;
             };
             if (!allowed) {
                 throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé au domaine " + checkDomain);
             }
        }

        return decisionRepository.findAll().stream()
                .filter(d -> d.getDomaine() == domain
                        || (d.getDomaine() == null && domain == DecisionDomain.CREDIT))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .map(decisionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> pendingValidation() {
        Utilisateur user = authService.getCurrentUser();
        RoleEnum role = user != null ? user.getRole() : null;

        return decisionRepository.findByStatutValidationInOrderByTimestampDesc(
                        List.of(StatutDecisionEnum.EN_ATTENTE_VALIDATION, StatutDecisionEnum.EN_ATTENTE))
                .stream()
                .filter(d -> {
                    if (role == RoleEnum.ADMINISTRATEUR || role == RoleEnum.AUDITEUR) return true;
                    DecisionDomain domain = d.getDomaine() != null ? d.getDomaine() : DecisionDomain.CREDIT;
                    if (role == RoleEnum.RESPONSABLE_CREDIT) return domain == DecisionDomain.CREDIT;
                    if (role == RoleEnum.PROFESSIONNEL_SANTE) return domain == DecisionDomain.MEDICAL;
                    if (role == RoleEnum.RESPONSABLE_PEDAGOGIQUE) return domain == DecisionDomain.EDUCATION;
                    return false;
                })
                .map(decisionMapper::toResponse)
                .toList();
    }

    private Decision baseDecision(
            DecisionDomain domain,
            Utilisateur user,
            String description,
            DomainPredictionResponse prediction
    ) {
        Decision decision = new Decision();
        decision.setDomaine(domain);
        decision.setDossierReference(domain.name() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        decision.setDescription(description);
        decision.setCreatedBy(user.getEmail());
        decision.setModelName(prediction.getModelType() != null ? prediction.getModelType() : "sklearn");
        decision.setModelVersion(prediction.getModelVersion());
        decision.setDatasetVersion(prediction.getDatasetVersion());
        decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE_VALIDATION);
        decision.setSubmittedAt(LocalDateTime.now());
        decision.setUpdatedAt(LocalDateTime.now());
        return decision;
    }

    private void applyPrediction(Decision decision, DomainPredictionResponse prediction) {
        decision.setSuggestedDecision(prediction.getPrediction());
        decision.setConfidenceScore(prediction.getConfidence());
        decision.setRiskLevel(prediction.getRiskLevel());
        decision.setReponse(prediction.getRecommendation() != null
                ? prediction.getRecommendation()
                : prediction.getPrediction());
        decision.setExplanationSource(prediction.getExplanationMethod());
        if (prediction.getFactors() != null) {
            List<ExplanationFactor> factors = new ArrayList<>();
            int rank = 1;
            for (DomainPredictionResponse.Factor f : prediction.getFactors()) {
                ExplanationFactor ef = new ExplanationFactor();
                ef.setDecision(decision);
                ef.setName(f.getFeature());
                ef.setValue(f.getFeature());
                ef.setShapValue(f.getImportance() != null ? f.getImportance() : 0.0);
                ef.setImpact(f.getImpact() != null ? f.getImpact() : "NEUTRAL");
                ef.setContributionPercent(f.getImportance() != null ? f.getImportance() * 100 : 0.0);
                ef.setRank(rank++);
                ef.setSource(prediction.getExplanationMethod() != null
                        ? prediction.getExplanationMethod() : "feature_importance");
                factors.add(ef);
            }
            decision.setExplanationFactors(factors);
        }
    }

    /**
     * Vérifie que l'utilisateur courant est autorisé à créer une décision pour ce domaine.
     * ADMINISTRATEUR : tous les domaines.
     * AGENT_CREDIT : CREDIT uniquement.
     * AGENT_SANTE : MEDICAL uniquement.
     * AGENT_PEDAGOGIQUE : EDUCATION uniquement.
     * Tout autre rôle (validateurs, auditeur) : interdit.
     */
    private void assertCanCreate(Utilisateur user, DecisionDomain domain) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }
        RoleEnum role = user.getRole();
        if (role == RoleEnum.ADMINISTRATEUR) return; // all domains
        boolean allowed = switch (domain) {
            case CREDIT    -> role == RoleEnum.AGENT_CREDIT;
            case MEDICAL   -> role == RoleEnum.AGENT_SANTE;
            case EDUCATION -> role == RoleEnum.AGENT_PEDAGOGIQUE;
        };
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Votre rôle " + role + " ne permet pas de créer une décision du domaine " + domain);
        }
    }

    private void assertCanValidate(Utilisateur user, Decision decision) {
        if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(decision.getCreatedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "L'auteur ne peut pas valider son propre dossier");
        }
        RoleEnum role = user.getRole();
        DecisionDomain domain = decision.getDomaine() != null ? decision.getDomaine() : DecisionDomain.CREDIT;

        boolean allowed = switch (domain) {
            case CREDIT -> role == RoleEnum.RESPONSABLE_CREDIT;
            case MEDICAL -> role == RoleEnum.PROFESSIONNEL_SANTE;
            case EDUCATION -> role == RoleEnum.RESPONSABLE_PEDAGOGIQUE;
        };
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Rôle insuffisant pour valider le domaine " + domain
                            + ". ADMINISTRATEUR n'est pas validateur métier par défaut.");
        }
    }

    private void assertValidFinalDecision(DecisionDomain domain, String finale) {
        if (finale == null || finale.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "decisionFinale requise");
        }
        Set<String> allowed = switch (domain != null ? domain : DecisionDomain.CREDIT) {
            case CREDIT -> CREDIT_DECISIONS;
            case MEDICAL -> MEDICAL_DECISIONS;
            case EDUCATION -> EDUCATION_DECISIONS;
        };
        if (!allowed.contains(finale)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Décision humaine invalide pour " + domain + ": " + finale);
        }
    }

    private boolean computeAgreement(Decision decision, DomainValidationRequest request) {
        if (request.getAccordAvecIa() != null) {
            return request.getAccordAvecIa();
        }
        String ia = decision.getSuggestedDecision();
        String human = request.getDecisionFinale();
        if (ia == null || human == null) {
            return true;
        }
        // Heuristique : A_REVOIR / REFUSEE vs RISQUE_ELEVE = accord approximatif
        if (human.equals("A_REVOIR") || human.equals("REFUSEE") || human.equals("ORIENTATION_SPECIALISTE")) {
            return ia.contains("ELEVE") || ia.contains("MOYEN") || ia.contains("MODERE");
        }
        if (human.equals("ACCEPTEE") || human.equals("SUIVI_STANDARD") || human.equals("AUCUNE_INTERVENTION")) {
            return ia.contains("FAIBLE");
        }
        return true;
    }

    private Decision find(UUID id) {
        return decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Décision introuvable: " + id));
    }

    private String writeJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }
}
