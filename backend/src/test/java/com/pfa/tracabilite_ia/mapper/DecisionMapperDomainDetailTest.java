package com.pfa.tracabilite_ia.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.entities.CreditDecisionData;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.EducationDecisionData;
import com.pfa.tracabilite_ia.entities.MedicalDecisionData;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DecisionMapperDomainDetailTest {

    private DecisionMapper mapper;

    @BeforeEach
    void setUp() {
        ObjectMapper om = new ObjectMapper();
        mapper = new DecisionMapper(om, new ReponseAgentMapper(om));
    }

    @Test
    void creditDetail_exposesOnlyCreditData() {
        Decision d = base(DecisionDomain.CREDIT);
        CreditDecisionData credit = new CreditDecisionData();
        credit.setAge(35);
        credit.setTypeContrat("CDI");
        credit.setMontantDemandeMad(40000.0);
        credit.setRevenuMensuelMad(8500.0);

        DecisionResponse response = mapper.toResponse(d);
        mapper.applyDomainData(response, credit, null, null);

        assertNotNull(response.getCreditData());
        assertNull(response.getMedicalData());
        assertNull(response.getEducationData());
        assertEquals("CDI", response.getCreditData().getTypeContrat());
        assertNotNull(response.getSourcesMeta());
        assertTrue(response.getSourcesMeta().getDatasetVersion().contains("credit"));
        assertNotNull(response.getIntegrity());
        assertNotNull(response.getIntegrity().getExplanation());
    }

    @Test
    void medicalDetail_exposesOnlyMedicalData() {
        Decision d = base(DecisionDomain.MEDICAL);
        MedicalDecisionData medical = new MedicalDecisionData();
        medical.setAge(50);
        medical.setImcKgM2(30.0);
        medical.setGlycemieMgDl(140.0);

        DecisionResponse response = mapper.toResponse(d);
        mapper.applyDomainData(response, null, medical, null);

        assertNotNull(response.getMedicalData());
        assertNull(response.getCreditData());
        assertNull(response.getEducationData());
        assertEquals(30.0, response.getMedicalData().getImcKgM2());
        assertTrue(response.getSourcesMeta().getDisclaimer().toLowerCase().contains("diagnostic"));
    }

    @Test
    void educationDetail_exposesOnlyEducationData() {
        Decision d = base(DecisionDomain.EDUCATION);
        EducationDecisionData edu = new EducationDecisionData();
        edu.setMoyenneS1(9.5);
        edu.setUnitesValideesS2(2);
        edu.setSexe("HOMME");

        DecisionResponse response = mapper.toResponse(d);
        mapper.applyDomainData(response, null, null, edu);

        assertNotNull(response.getEducationData());
        assertNull(response.getCreditData());
        assertNull(response.getMedicalData());
        assertEquals("HOMME", response.getEducationData().getSexe());
        assertTrue(response.getSourcesMeta().getUsageLimit().toLowerCase().contains("accompagnement")
                || response.getSourcesMeta().getDisclaimer().toLowerCase().contains("sanction"));
    }

    private static Decision base(DecisionDomain domain) {
        Decision d = new Decision();
        d.setDecisionId(UUID.randomUUID());
        d.setDomaine(domain);
        d.setPrompt("test");
        d.setModelName("sklearn");
        d.setReponse("RISQUE_MOYEN");
        d.setStatutValidation(StatutDecisionEnum.ANALYSEE);
        d.setCreatedBy("user@tracabilite.ia");
        d.setCurrentHash("abc");
        d.setDatasetVersion(null);
        return d;
    }
}
