package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ExplanationFactor;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DecisionExportService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    /** Bornes pour requêtes PostgreSQL (évite paramètres timestamp NULL non typés). */
    private static final LocalDateTime EXPORT_FROM_MIN = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final LocalDateTime EXPORT_TO_MAX = LocalDateTime.of(2999, 12, 31, 23, 59, 59);

    private final DecisionRepository decisionRepository;
    private final AuditLogService auditLogService;

    public DecisionExportService(DecisionRepository decisionRepository, AuditLogService auditLogService) {
        this.decisionRepository = decisionRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public byte[] exportCsv(
            DecisionDomain domaine,
            StatutDecisionEnum statut,
            LocalDateTime from,
            LocalDateTime to,
            String validateur,
            Utilisateur user
    ) {
        List<Decision> decisions = decisionRepository.findForExport(
                domaine, statut, boundFrom(from), boundTo(to), normalizeValidateur(validateur));
        auditLogService.record(null, user, "EXPORT", null, null,
                "Export CSV n=" + decisions.size()
                        + " domaine=" + domaine
                        + " statut=" + statut,
                null);

        StringBuilder sb = new StringBuilder();
        sb.append('\ufeff'); // UTF-8 BOM for Excel
        sb.append(String.join(",", headers())).append('\n');
        for (Decision d : decisions) {
            sb.append(String.join(",", row(d))).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Excel SpreadsheetML (XML) — ouvre dans Excel sans dépendance Apache POI.
     * Content-Type: application/vnd.ms-excel ; extension .xls recommandée côté client.
     */
    @Transactional
    public byte[] exportExcelXml(
            DecisionDomain domaine,
            StatutDecisionEnum statut,
            LocalDateTime from,
            LocalDateTime to,
            String validateur,
            Utilisateur user
    ) {
        List<Decision> decisions = decisionRepository.findForExport(
                domaine, statut, boundFrom(from), boundTo(to), normalizeValidateur(validateur));
        auditLogService.record(null, user, "EXPORT", null, null,
                "Export ExcelXML n=" + decisions.size()
                        + " domaine=" + domaine
                        + " statut=" + statut,
                null);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<?mso-application progid=\"Excel.Sheet\"?>\n");
        xml.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" ");
        xml.append("xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n");
        xml.append("<Worksheet ss:Name=\"Decisions\"><Table>\n");

        xml.append("<Row>");
        for (String h : headers()) {
            xml.append("<Cell><Data ss:Type=\"String\">").append(xmlEscape(h)).append("</Data></Cell>");
        }
        xml.append("</Row>\n");

        for (Decision d : decisions) {
            xml.append("<Row>");
            for (String cell : rowRaw(d)) {
                xml.append("<Cell><Data ss:Type=\"String\">").append(xmlEscape(cell)).append("</Data></Cell>");
            }
            xml.append("</Row>\n");
        }
        xml.append("</Table></Worksheet></Workbook>");
        return xml.toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<String> headers() {
        return List.of(
                "decisionId",
                "domaine",
                "dossierReference",
                "objetDossier",
                "dateCreation",
                "recommandationIa",
                "scoreConfianceIa",
                "facteursPrincipaux",
                "decisionFinale",
                "justificationHumaine",
                "validateurId",
                "validateurRole",
                "dateValidation",
                "statut",
                "accordAvecIa",
                "modelVersion",
                "datasetVersion",
                "sourceDonnees",
                "hashIntegrite",
                "commentaire"
        );
    }

    private List<String> row(Decision d) {
        return rowRaw(d).stream().map(this::csvEscape).toList();
    }

    private List<String> rowRaw(Decision d) {
        DecisionDomain domain = d.getDomaine() != null ? d.getDomaine() : DecisionDomain.CREDIT;
        boolean medical = domain == DecisionDomain.MEDICAL;
        return List.of(
                n(d.getDecisionId()),
                domain.name(),
                n(d.getDossierReference()),
                n(d.getPrompt()),
                d.getTimestamp() != null ? d.getTimestamp().format(ISO) : "",
                n(d.getSuggestedDecision()),
                d.getConfidenceScore() != null ? d.getConfidenceScore().toString() : "",
                factorsSummary(d),
                n(d.getHumanDecision()),
                // Pas d'export de justification médicale détaillée sans contrôle métier supplémentaire
                medical ? "[MASQUE_MEDICAL]" : n(d.getJustificationHumaine()),
                n(d.getValidateurId()),
                n(d.getValidateurRole()),
                d.getValidatedAt() != null ? d.getValidatedAt().format(ISO) : "",
                d.getStatutValidation() != null ? d.getStatutValidation().name() : "",
                d.getAccordAvecIa() != null ? d.getAccordAvecIa().toString() : "",
                n(d.getModelVersion()),
                n(d.getDatasetVersion()),
                n(d.getSourceDonnees()),
                n(d.getCurrentHash()),
                medical ? "" : n(d.getDescription())
        );
    }

    private String factorsSummary(Decision d) {
        if (d.getExplanationFactors() == null || d.getExplanationFactors().isEmpty()) {
            return "";
        }
        return d.getExplanationFactors().stream()
                .sorted((a, b) -> Integer.compare(
                        a.getRank() != null ? a.getRank() : 99,
                        b.getRank() != null ? b.getRank() : 99))
                .limit(5)
                .map(ExplanationFactor::getName)
                .collect(Collectors.joining("|"));
    }

    private String n(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "\"\"";
        }
        String v = value.replace("\"", "\"\"");
        return "\"" + v + "\"";
    }

    private String xmlEscape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static LocalDateTime boundFrom(LocalDateTime from) {
        return from != null ? from : EXPORT_FROM_MIN;
    }

    private static LocalDateTime boundTo(LocalDateTime to) {
        return to != null ? to : EXPORT_TO_MAX;
    }

    private static String normalizeValidateur(String validateur) {
        return validateur != null ? validateur : "";
    }
}
