package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.backup.BackupDecisionSnapshot;
import com.pfa.tracabilite_ia.dto.backup.BackupDecisionSnapshot.BackupAgentSnapshot;
import com.pfa.tracabilite_ia.dto.backup.BackupDecisionSnapshot.BackupCreditSnapshot;
import com.pfa.tracabilite_ia.dto.backup.BackupDecisionSnapshot.BackupEducationSnapshot;
import com.pfa.tracabilite_ia.dto.backup.BackupDecisionSnapshot.BackupFactorSnapshot;
import com.pfa.tracabilite_ia.dto.backup.BackupDecisionSnapshot.BackupHistorySnapshot;
import com.pfa.tracabilite_ia.dto.backup.BackupDecisionSnapshot.BackupMedicalSnapshot;
import com.pfa.tracabilite_ia.dto.backup.BackupDecisionSnapshot.BackupSourceSnapshot;
import com.pfa.tracabilite_ia.dto.backup.BackupPack;
import com.pfa.tracabilite_ia.dto.backup.BackupUserSnapshot;
import com.pfa.tracabilite_ia.dto.response.BackupJobResponse;
import com.pfa.tracabilite_ia.dto.response.BackupRestoreResponse;
import com.pfa.tracabilite_ia.dto.response.BackupVerifyResponse;
import com.pfa.tracabilite_ia.entities.BackupJob;
import com.pfa.tracabilite_ia.entities.CreditDecisionData;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.DecisionHistory;
import com.pfa.tracabilite_ia.entities.DecisionSource;
import com.pfa.tracabilite_ia.entities.EducationDecisionData;
import com.pfa.tracabilite_ia.entities.ExplanationFactor;
import com.pfa.tracabilite_ia.entities.MedicalDecisionData;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.BackupJobStatus;
import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;
import com.pfa.tracabilite_ia.repository.BackupJobRepository;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import com.pfa.tracabilite_ia.util.HashUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class BackupRestoreService {

    private final BackupJobRepository backupJobRepository;
    private final DecisionRepository decisionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final Path backupDir;

    public BackupRestoreService(
            BackupJobRepository backupJobRepository,
            DecisionRepository decisionRepository,
            UtilisateurRepository utilisateurRepository,
            AuditLogService auditLogService,
            ObjectMapper objectMapper,
            PasswordEncoder passwordEncoder,
            @Value("${app.backup.dir:./data/backups}") String backupDir
    ) {
        this.backupJobRepository = backupJobRepository;
        this.decisionRepository = decisionRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.backupDir = Path.of(backupDir).toAbsolutePath().normalize();
    }

    @Transactional
    public BackupJobResponse create(Utilisateur admin) {
        try {
            Files.createDirectories(backupDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de créer le répertoire de sauvegarde.");
        }

        List<Utilisateur> users = utilisateurRepository.findAll();
        List<Decision> decisions = decisionRepository.findAllByOrderByTimestampAsc();
        decisions.forEach(this::touchAssociations);

        BackupPack pack = BackupPack.builder()
                .version(BackupPack.SCHEMA_VERSION)
                .createdAt(LocalDateTime.now())
                .createdByEmail(admin != null ? admin.getEmail() : null)
                .users(users.stream().map(this::toUserSnapshot).toList())
                .decisions(decisions.stream().map(this::toDecisionSnapshot).toList())
                .build();

        BackupJob job = backupJobRepository.save(BackupJob.builder()
                .createdByUserId(admin != null ? admin.getId() : null)
                .createdByEmail(admin != null ? admin.getEmail() : null)
                .filename("pending.json")
                .sizeBytes(0)
                .packSha256("pending")
                .decisionCount(pack.getDecisions().size())
                .userCount(pack.getUsers().size())
                .status(BackupJobStatus.CREATED)
                .build());

        String filename = "backup-" + job.getId() + ".json";
        Path file = jobFile(job.getId());
        try {
            byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(pack);
            String sha = HashUtils.sha256(bytes);
            pack.setPackSha256(sha);
            bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(pack);
            sha = HashUtils.sha256(bytes);
            Files.write(file, bytes);
            job.setFilename(filename);
            job.setSizeBytes(bytes.length);
            job.setPackSha256(sha);
            backupJobRepository.save(job);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'écrire le fichier de sauvegarde.");
        }

        auditLogService.record(null, admin, "BACKUP", null, null,
                "Sauvegarde " + job.getId() + " — SHA-256 " + job.getPackSha256(), null);
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public List<BackupJobResponse> list() {
        return backupJobRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BackupVerifyResponse verify(UUID id, Utilisateur admin) {
        BackupJob job = requireJob(id);
        Path file = jobFile(job.getId());
        if (!Files.isRegularFile(file)) {
            job.setStatus(BackupJobStatus.MISSING_FILE);
            job.setLastVerifiedAt(LocalDateTime.now());
            backupJobRepository.save(job);
            return BackupVerifyResponse.builder()
                    .id(job.getId())
                    .valid(false)
                    .expectedSha256(job.getPackSha256())
                    .actualSha256(null)
                    .status(job.getStatus())
                    .build();
        }

        String actual;
        try {
            actual = HashUtils.sha256(Files.readAllBytes(file));
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de lire le fichier de sauvegarde.");
        }

        boolean valid = actual.equalsIgnoreCase(job.getPackSha256());
        job.setStatus(valid ? BackupJobStatus.VERIFIED_OK : BackupJobStatus.VERIFIED_TAMPERED);
        job.setLastVerifiedAt(LocalDateTime.now());
        backupJobRepository.save(job);

        auditLogService.record(null, admin, "BACKUP_VERIFY", null, null,
                "Vérification " + job.getId() + " — " + (valid ? "OK" : "ALTEREE"), null);

        return BackupVerifyResponse.builder()
                .id(job.getId())
                .valid(valid)
                .expectedSha256(job.getPackSha256())
                .actualSha256(actual)
                .status(job.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] readFile(UUID id) {
        BackupJob job = requireJob(id);
        Path file = jobFile(job.getId());
        if (!Files.isRegularFile(file)) {
            throw new ResourceNotFoundException("Fichier de sauvegarde introuvable.");
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de lire le fichier de sauvegarde.");
        }
    }

    public String downloadFilename(UUID id) {
        return requireJob(id).getFilename();
    }

    @Transactional
    public BackupRestoreResponse restore(UUID id, boolean confirm, Utilisateur admin) {
        if (!confirm) {
            throw new IllegalArgumentException("La restauration exige une confirmation explicite.");
        }

        BackupVerifyResponse verify = verify(id, admin);
        if (!verify.isValid()) {
            throw new IllegalStateException("Sauvegarde altérée ou fichier manquant — restauration refusée.");
        }

        BackupPack pack;
        try {
            pack = objectMapper.readValue(readFile(id), BackupPack.class);
        } catch (IOException ex) {
            throw new IllegalStateException("Pack de sauvegarde illisible.");
        }

        int usersCreated = 0;
        int usersSkipped = 0;
        if (pack.getUsers() != null) {
            for (BackupUserSnapshot snapshot : pack.getUsers()) {
                if (snapshot.getEmail() == null) {
                    continue;
                }
                if (utilisateurRepository.existsByEmailIgnoreCase(snapshot.getEmail())) {
                    usersSkipped++;
                    continue;
                }
                Utilisateur user = new Utilisateur();
                user.setId(snapshot.getId());
                user.setNom(snapshot.getNom());
                user.setEmail(snapshot.getEmail());
                user.setRole(snapshot.getRole());
                user.setActif(false);
                user.setMotDePasseHash(passwordEncoder.encode(randomSecret()));
                utilisateurRepository.save(user);
                usersCreated++;
            }
        }

        int decisionsCreated = 0;
        int decisionsSkipped = 0;
        if (pack.getDecisions() != null) {
            for (BackupDecisionSnapshot snapshot : pack.getDecisions()) {
                if (snapshot.getDecisionId() != null && decisionRepository.existsById(snapshot.getDecisionId())) {
                    decisionsSkipped++;
                    continue;
                }
                persistDecision(snapshot);
                decisionsCreated++;
            }
        }

        BackupJob job = requireJob(id);
        job.setStatus(BackupJobStatus.RESTORED);
        job.setLastRestoredAt(LocalDateTime.now());
        job.setRestoreUsersCreated(usersCreated);
        job.setRestoreUsersSkipped(usersSkipped);
        job.setRestoreDecisionsCreated(decisionsCreated);
        job.setRestoreDecisionsSkipped(decisionsSkipped);
        backupJobRepository.save(job);

        auditLogService.record(null, admin, "RESTORE", null, null,
                "Restauration " + job.getId() + " — décisions +" + decisionsCreated + " (skip " + decisionsSkipped + ")",
                null);

        return BackupRestoreResponse.builder()
                .id(job.getId())
                .usersCreated(usersCreated)
                .usersSkipped(usersSkipped)
                .decisionsCreated(decisionsCreated)
                .decisionsSkipped(decisionsSkipped)
                .packSha256(job.getPackSha256())
                .build();
    }

    private void persistDecision(BackupDecisionSnapshot snapshot) {
        Decision decision = new Decision();
        decision.setDecisionId(snapshot.getDecisionId());
        decision.setTimestamp(snapshot.getTimestamp());
        decision.setPrompt(snapshot.getPrompt() != null ? snapshot.getPrompt() : "");
        decision.setContexte(snapshot.getContexte());
        decision.setModelName(snapshot.getModelName() != null ? snapshot.getModelName() : "restored");
        decision.setModelVersion(snapshot.getModelVersion());
        decision.setReponse(snapshot.getReponse() != null ? snapshot.getReponse() : "");
        decision.setStatutValidation(snapshot.getStatutValidation() != null
                ? snapshot.getStatutValidation()
                : com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum.EN_ATTENTE);
        decision.setPreviousHash(snapshot.getPreviousHash());
        decision.setCurrentHash(snapshot.getCurrentHash());
        decision.setFeaturesJson(snapshot.getFeaturesJson());
        decision.setSuggestedDecision(snapshot.getSuggestedDecision());
        decision.setConfidenceScore(snapshot.getConfidenceScore());
        decision.setRiskLevel(snapshot.getRiskLevel());
        decision.setProbabilitiesJson(snapshot.getProbabilitiesJson());
        decision.setResumeConsensus(snapshot.getResumeConsensus());
        decision.setConsensusJson(snapshot.getConsensusJson());
        decision.setExplanationSource(snapshot.getExplanationSource());
        decision.setBusinessDataHash(snapshot.getBusinessDataHash());
        decision.setSourcesHash(snapshot.getSourcesHash());
        decision.setAgentResponsesHash(snapshot.getAgentResponsesHash());
        decision.setHumanDecision(snapshot.getHumanDecision());
        decision.setValidatorEmail(snapshot.getValidatorEmail());
        decision.setDomaine(snapshot.getDomaine());
        decision.setDossierReference(snapshot.getDossierReference());
        decision.setDescription(snapshot.getDescription());
        decision.setDatasetVersion(snapshot.getDatasetVersion());
        decision.setSourceDonnees(snapshot.getSourceDonnees());
        decision.setAccordAvecIa(snapshot.getAccordAvecIa());
        decision.setJustificationHumaine(snapshot.getJustificationHumaine());
        decision.setValidateurId(snapshot.getValidateurId());
        decision.setValidateurRole(snapshot.getValidateurRole());
        decision.setSubmittedAt(snapshot.getSubmittedAt());
        decision.setValidatedAt(snapshot.getValidatedAt());
        decision.setCreatedBy(snapshot.getCreatedBy());
        decision.setUpdatedAt(snapshot.getUpdatedAt());

        if (snapshot.getCreditData() != null) {
            BackupCreditSnapshot src = snapshot.getCreditData();
            CreditDecisionData data = new CreditDecisionData();
            data.setDecision(decision);
            data.setAge(src.getAge());
            data.setDureeMois(src.getDureeMois());
            data.setTypeContrat(src.getTypeContrat());
            data.setStatutLogement(src.getStatutLogement());
            data.setIncidentPaiementBam(src.getIncidentPaiementBam());
            data.setMontantDemandeMad(src.getMontantDemandeMad());
            data.setNouvelleEcheanceMad(src.getNouvelleEcheanceMad());
            data.setRevenuMensuelMad(src.getRevenuMensuelMad());
            data.setTauxEndettement(src.getTauxEndettement());
            decision.setCreditData(data);
        }
        if (snapshot.getMedicalData() != null) {
            BackupMedicalSnapshot src = snapshot.getMedicalData();
            MedicalDecisionData data = new MedicalDecisionData();
            data.setDecision(decision);
            data.setAge(src.getAge());
            data.setGrossesses(src.getGrossesses());
            data.setGlycemieMgDl(src.getGlycemieMgDl());
            data.setPressionArterielleMmhg(src.getPressionArterielleMmhg());
            data.setEpaisseurPliCutaneMm(src.getEpaisseurPliCutaneMm());
            data.setInsulineMicroUMl(src.getInsulineMicroUMl());
            data.setImcKgM2(src.getImcKgM2());
            decision.setMedicalData(data);
        }
        if (snapshot.getEducationData() != null) {
            BackupEducationSnapshot src = snapshot.getEducationData();
            EducationDecisionData data = new EducationDecisionData();
            data.setDecision(decision);
            data.setAgeInscription(src.getAgeInscription());
            data.setNoteAdmission(src.getNoteAdmission());
            data.setNoteQualificationPrecedente(src.getNoteQualificationPrecedente());
            data.setUnitesValideesS1(src.getUnitesValideesS1());
            data.setMoyenneS1(src.getMoyenneS1());
            data.setUnitesValideesS2(src.getUnitesValideesS2());
            data.setMoyenneS2(src.getMoyenneS2());
            data.setTauxChomage(src.getTauxChomage());
            data.setTauxInflation(src.getTauxInflation());
            data.setPib(src.getPib());
            data.setSexe(src.getSexe());
            data.setBoursier(src.getBoursier());
            data.setFraisAJour(src.getFraisAJour());
            data.setDebiteur(src.getDebiteur());
            data.setDeplace(src.getDeplace());
            data.setInternational(src.getInternational());
            decision.setEducationData(data);
        }

        if (snapshot.getSources() != null) {
            for (BackupSourceSnapshot src : snapshot.getSources()) {
                DecisionSource source = new DecisionSource();
                source.setDecision(decision);
                source.setSourceType(src.getSourceType());
                source.setName(src.getName() != null ? src.getName() : "source");
                source.setDescription(src.getDescription());
                source.setUrl(src.getUrl());
                source.setDocumentReference(src.getDocumentReference());
                source.setContentHash(src.getContentHash());
                source.setMetadataJson(src.getMetadataJson());
                source.setCreatedById(src.getCreatedById());
                source.setCreatedByEmail(src.getCreatedByEmail());
                source.setCreatedAt(src.getCreatedAt());
                decision.getSources().add(source);
            }
        }
        if (snapshot.getAgentResponses() != null) {
            for (BackupAgentSnapshot src : snapshot.getAgentResponses()) {
                ReponseAgentIA agent = new ReponseAgentIA();
                agent.setDecision(decision);
                agent.setAgentKey(src.getAgentKey() != null ? src.getAgentKey() : "AGENT");
                agent.setModelId(src.getModelId() != null ? src.getModelId() : "unknown");
                agent.setModelName(src.getModelName() != null ? src.getModelName() : "unknown");
                agent.setProvider(src.getProvider() != null ? src.getProvider() : "unknown");
                agent.setRequestedModelId(src.getRequestedModelId());
                agent.setActualModelId(src.getActualModelId());
                agent.setFallbackUsed(src.getFallbackUsed());
                agent.setFallbackReason(src.getFallbackReason());
                agent.setResponseHash(src.getResponseHash());
                agent.setRetryCount(src.getRetryCount());
                agent.setReponseBrute(src.getReponseBrute());
                agent.setReponseNormalisee(src.getReponseNormalisee());
                agent.setDecisionProposee(src.getDecisionProposee());
                agent.setConfianceDeclaree(src.getConfianceDeclaree());
                agent.setNiveauRisque(src.getNiveauRisque());
                agent.setResume(src.getResume());
                agent.setExplication(src.getExplication());
                agent.setRecommandationsJson(src.getRecommandationsJson());
                agent.setDureeMs(src.getDureeMs());
                agent.setNombreTokens(src.getNombreTokens());
                agent.setStatut(src.getStatut() != null ? src.getStatut() : com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum.FAILURE);
                agent.setCodeErreur(src.getCodeErreur());
                agent.setTimestamp(src.getTimestamp());
                decision.getReponsesAgents().add(agent);
            }
        }
        if (snapshot.getExplanationFactors() != null) {
            for (BackupFactorSnapshot src : snapshot.getExplanationFactors()) {
                ExplanationFactor factor = new ExplanationFactor();
                factor.setDecision(decision);
                factor.setName(src.getName() != null ? src.getName() : "factor");
                factor.setValue(src.getValue() != null ? src.getValue() : "");
                factor.setShapValue(src.getShapValue() != null ? src.getShapValue() : 0d);
                factor.setImpact(src.getImpact() != null ? src.getImpact() : "NEUTRE");
                factor.setRank(src.getRank() != null ? src.getRank() : 0);
                factor.setContributionPercent(src.getContributionPercent() != null ? src.getContributionPercent() : 0d);
                factor.setSource(src.getSource() != null ? src.getSource() : "BACKUP");
                decision.getExplanationFactors().add(factor);
            }
        }
        if (snapshot.getHistory() != null) {
            for (BackupHistorySnapshot src : snapshot.getHistory()) {
                if (src.getAction() == null) {
                    continue;
                }
                DecisionHistory history = new DecisionHistory();
                history.setDecision(decision);
                history.setAction(src.getAction());
                history.setPreviousStatus(src.getPreviousStatus());
                history.setNewStatus(src.getNewStatus());
                history.setPerformedById(src.getPerformedById());
                history.setPerformedByEmail(src.getPerformedByEmail());
                history.setComment(src.getComment());
                history.setJustification(src.getJustification());
                history.setEventDataJson(src.getEventDataJson());
                history.setCorrelationId(src.getCorrelationId());
                history.setCreatedAt(src.getCreatedAt());
                decision.getHistoryEntries().add(history);
            }
        }

        decisionRepository.save(decision);
    }

    private BackupUserSnapshot toUserSnapshot(Utilisateur user) {
        return BackupUserSnapshot.builder()
                .id(user.getId())
                .nom(user.getNom())
                .email(user.getEmail())
                .role(user.getRole())
                .actif(user.isActif())
                .dateCreation(user.getDateCreation())
                .build();
    }

    private BackupDecisionSnapshot toDecisionSnapshot(Decision d) {
        BackupDecisionSnapshot.BackupDecisionSnapshotBuilder builder = BackupDecisionSnapshot.builder()
                .decisionId(d.getDecisionId())
                .timestamp(d.getTimestamp())
                .prompt(d.getPrompt())
                .contexte(d.getContexte())
                .modelName(d.getModelName())
                .modelVersion(d.getModelVersion())
                .reponse(d.getReponse())
                .statutValidation(d.getStatutValidation())
                .previousHash(d.getPreviousHash())
                .currentHash(d.getCurrentHash())
                .featuresJson(d.getFeaturesJson())
                .suggestedDecision(d.getSuggestedDecision())
                .confidenceScore(d.getConfidenceScore())
                .riskLevel(d.getRiskLevel())
                .probabilitiesJson(d.getProbabilitiesJson())
                .resumeConsensus(d.getResumeConsensus())
                .consensusJson(d.getConsensusJson())
                .explanationSource(d.getExplanationSource())
                .businessDataHash(d.getBusinessDataHash())
                .sourcesHash(d.getSourcesHash())
                .agentResponsesHash(d.getAgentResponsesHash())
                .humanDecision(d.getHumanDecision())
                .validatorEmail(d.getValidatorEmail())
                .domaine(d.getDomaine())
                .dossierReference(d.getDossierReference())
                .description(d.getDescription())
                .datasetVersion(d.getDatasetVersion())
                .sourceDonnees(d.getSourceDonnees())
                .accordAvecIa(d.getAccordAvecIa())
                .justificationHumaine(d.getJustificationHumaine())
                .validateurId(d.getValidateurId())
                .validateurRole(d.getValidateurRole())
                .submittedAt(d.getSubmittedAt())
                .validatedAt(d.getValidatedAt())
                .createdBy(d.getCreatedBy())
                .updatedAt(d.getUpdatedAt());

        if (d.getCreditData() != null) {
            CreditDecisionData src = d.getCreditData();
            builder.creditData(BackupCreditSnapshot.builder()
                    .age(src.getAge())
                    .dureeMois(src.getDureeMois())
                    .typeContrat(src.getTypeContrat())
                    .statutLogement(src.getStatutLogement())
                    .incidentPaiementBam(src.getIncidentPaiementBam())
                    .montantDemandeMad(src.getMontantDemandeMad())
                    .nouvelleEcheanceMad(src.getNouvelleEcheanceMad())
                    .revenuMensuelMad(src.getRevenuMensuelMad())
                    .tauxEndettement(src.getTauxEndettement())
                    .build());
        }
        if (d.getMedicalData() != null) {
            MedicalDecisionData src = d.getMedicalData();
            builder.medicalData(BackupMedicalSnapshot.builder()
                    .age(src.getAge())
                    .grossesses(src.getGrossesses())
                    .glycemieMgDl(src.getGlycemieMgDl())
                    .pressionArterielleMmhg(src.getPressionArterielleMmhg())
                    .epaisseurPliCutaneMm(src.getEpaisseurPliCutaneMm())
                    .insulineMicroUMl(src.getInsulineMicroUMl())
                    .imcKgM2(src.getImcKgM2())
                    .build());
        }
        if (d.getEducationData() != null) {
            EducationDecisionData src = d.getEducationData();
            builder.educationData(BackupEducationSnapshot.builder()
                    .ageInscription(src.getAgeInscription())
                    .noteAdmission(src.getNoteAdmission())
                    .noteQualificationPrecedente(src.getNoteQualificationPrecedente())
                    .unitesValideesS1(src.getUnitesValideesS1())
                    .moyenneS1(src.getMoyenneS1())
                    .unitesValideesS2(src.getUnitesValideesS2())
                    .moyenneS2(src.getMoyenneS2())
                    .tauxChomage(src.getTauxChomage())
                    .tauxInflation(src.getTauxInflation())
                    .pib(src.getPib())
                    .sexe(src.getSexe())
                    .boursier(src.getBoursier())
                    .fraisAJour(src.getFraisAJour())
                    .debiteur(src.getDebiteur())
                    .deplace(src.getDeplace())
                    .international(src.getInternational())
                    .build());
        }
        if (d.getSources() != null) {
            builder.sources(d.getSources().stream().map(src -> BackupSourceSnapshot.builder()
                    .sourceType(src.getSourceType())
                    .name(src.getName())
                    .description(src.getDescription())
                    .url(src.getUrl())
                    .documentReference(src.getDocumentReference())
                    .contentHash(src.getContentHash())
                    .metadataJson(src.getMetadataJson())
                    .createdById(src.getCreatedById())
                    .createdByEmail(src.getCreatedByEmail())
                    .createdAt(src.getCreatedAt())
                    .build()).toList());
        }
        if (d.getReponsesAgents() != null) {
            builder.agentResponses(d.getReponsesAgents().stream().map(src -> BackupAgentSnapshot.builder()
                    .agentKey(src.getAgentKey())
                    .modelId(src.getModelId())
                    .modelName(src.getModelName())
                    .provider(src.getProvider())
                    .requestedModelId(src.getRequestedModelId())
                    .actualModelId(src.getActualModelId())
                    .fallbackUsed(src.getFallbackUsed())
                    .fallbackReason(src.getFallbackReason())
                    .responseHash(src.getResponseHash())
                    .retryCount(src.getRetryCount())
                    .reponseBrute(src.getReponseBrute())
                    .reponseNormalisee(src.getReponseNormalisee())
                    .decisionProposee(src.getDecisionProposee())
                    .confianceDeclaree(src.getConfianceDeclaree())
                    .niveauRisque(src.getNiveauRisque())
                    .resume(src.getResume())
                    .explication(src.getExplication())
                    .recommandationsJson(src.getRecommandationsJson())
                    .dureeMs(src.getDureeMs())
                    .nombreTokens(src.getNombreTokens())
                    .statut(src.getStatut())
                    .codeErreur(src.getCodeErreur())
                    .timestamp(src.getTimestamp())
                    .build()).toList());
        }
        if (d.getExplanationFactors() != null) {
            builder.explanationFactors(d.getExplanationFactors().stream().map(src -> BackupFactorSnapshot.builder()
                    .name(src.getName())
                    .value(src.getValue())
                    .shapValue(src.getShapValue())
                    .impact(src.getImpact())
                    .rank(src.getRank())
                    .contributionPercent(src.getContributionPercent())
                    .source(src.getSource())
                    .build()).toList());
        }
        if (d.getHistoryEntries() != null) {
            builder.history(d.getHistoryEntries().stream().map(src -> BackupHistorySnapshot.builder()
                    .action(src.getAction())
                    .previousStatus(src.getPreviousStatus())
                    .newStatus(src.getNewStatus())
                    .performedById(src.getPerformedById())
                    .performedByEmail(src.getPerformedByEmail())
                    .comment(src.getComment())
                    .justification(src.getJustification())
                    .eventDataJson(src.getEventDataJson())
                    .correlationId(src.getCorrelationId())
                    .createdAt(src.getCreatedAt())
                    .build()).toList());
        }
        return builder.build();
    }

    private void touchAssociations(Decision d) {
        if (d.getSources() != null) {
            d.getSources().size();
        }
        if (d.getReponsesAgents() != null) {
            d.getReponsesAgents().size();
        }
        if (d.getExplanationFactors() != null) {
            d.getExplanationFactors().size();
        }
        if (d.getHistoryEntries() != null) {
            d.getHistoryEntries().size();
        }
        if (d.getCreditData() != null) {
            d.getCreditData().getAge();
        }
        if (d.getMedicalData() != null) {
            d.getMedicalData().getAge();
        }
        if (d.getEducationData() != null) {
            d.getEducationData().getAgeInscription();
        }
    }

    private BackupJob requireJob(UUID id) {
        return backupJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sauvegarde introuvable."));
    }

    private Path jobFile(UUID id) {
        Path file = backupDir.resolve(id + ".json").normalize();
        if (!file.startsWith(backupDir)) {
            throw new IllegalArgumentException("Chemin de sauvegarde invalide.");
        }
        return file;
    }

    private BackupJobResponse toResponse(BackupJob job) {
        return BackupJobResponse.builder()
                .id(job.getId())
                .createdAt(job.getCreatedAt())
                .createdByEmail(job.getCreatedByEmail())
                .filename(job.getFilename())
                .sizeBytes(job.getSizeBytes())
                .packSha256(job.getPackSha256())
                .decisionCount(job.getDecisionCount())
                .userCount(job.getUserCount())
                .status(job.getStatus())
                .lastVerifiedAt(job.getLastVerifiedAt())
                .lastRestoredAt(job.getLastRestoredAt())
                .restoreUsersCreated(job.getRestoreUsersCreated())
                .restoreUsersSkipped(job.getRestoreUsersSkipped())
                .restoreDecisionsCreated(job.getRestoreDecisionsCreated())
                .restoreDecisionsSkipped(job.getRestoreDecisionsSkipped())
                .filePresent(Files.isRegularFile(jobFile(job.getId())))
                .build();
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
