package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionExportServiceTest {

    @Mock DecisionRepository decisionRepository;
    @Mock AuditLogService auditLogService;
    @InjectMocks DecisionExportService exportService;

    @Test
    void exportCsvMasksMedicalJustification() {
        Decision medical = new Decision();
        medical.setDecisionId(UUID.randomUUID());
        medical.setDomaine(DecisionDomain.MEDICAL);
        medical.setPrompt("Risque diabète");
        medical.setSuggestedDecision("RISQUE_ELEVE");
        medical.setJustificationHumaine("Détail clinique sensible");
        medical.setStatutValidation(StatutDecisionEnum.VALIDEE);

        Decision credit = new Decision();
        credit.setDecisionId(UUID.randomUUID());
        credit.setDomaine(DecisionDomain.CREDIT);
        credit.setPrompt("Crédit");
        credit.setSuggestedDecision("RISQUE_FAIBLE");
        credit.setJustificationHumaine("Accepte avec garanties");
        credit.setStatutValidation(StatutDecisionEnum.VALIDEE);

        when(decisionRepository.findForExport(any(), any(), any(), any(), any()))
                .thenReturn(List.of(medical, credit));

        Utilisateur auditor = new Utilisateur();
        auditor.setId(UUID.randomUUID());
        auditor.setRole(RoleEnum.AUDITEUR);
        auditor.setEmail("audit@test.com");

        byte[] csv = exportService.exportCsv(null, null, null, null, null, auditor);
        String text = new String(csv, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(text.contains("[MASQUE_MEDICAL]"));
        assertFalse(text.contains("Detail clinique sensible") || text.contains("Détail clinique sensible"));
        assertTrue(text.contains("Accepte avec garanties"));
        verify(auditLogService).record(eq(null), eq(auditor), eq("EXPORT"), any(), any(), any(), any());
    }

    @Test
    void exportExcelXmlContainsWorkbook() {
        when(decisionRepository.findForExport(any(), any(), any(), any(), any())).thenReturn(List.of());
        Utilisateur admin = new Utilisateur();
        admin.setRole(RoleEnum.ADMINISTRATEUR);
        byte[] xml = exportService.exportExcelXml(null, null, null, null, null, admin);
        String text = new String(xml);
        assertTrue(text.contains("Workbook"));
        assertTrue(text.contains("decisionId"));
    }
}
