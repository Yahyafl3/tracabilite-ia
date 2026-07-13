package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.response.ComparaisonAgentResponse;
import com.pfa.tracabilite_ia.entities.SystemeIA;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.SystemeIARepository;
import com.pfa.tracabilite_ia.service.ComparaisonService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ComparaisonServiceImpl implements ComparaisonService {

    private final SystemeIARepository systemeIARepository;
    private final DecisionRepository decisionRepository;

    public ComparaisonServiceImpl(SystemeIARepository systemeIARepository,
                                  DecisionRepository decisionRepository) {
        this.systemeIARepository = systemeIARepository;
        this.decisionRepository = decisionRepository;
    }

    @Override
    public List<ComparaisonAgentResponse> classerAgents() {
        List<SystemeIA> systemes = systemeIARepository.findAllByActifTrueOrderByNomAsc();
        List<ComparaisonAgentResponse> resultats = new ArrayList<>();

        for (SystemeIA systemeIA : systemes) {
            long total = decisionRepository.countBySystemeIaOuNom(systemeIA.getSystemeIaId(), systemeIA.getNom());
            long approuvees = decisionRepository.countBySystemeIaOuNomAndStatut(
                    systemeIA.getSystemeIaId(), systemeIA.getNom(), StatutDecisionEnum.APPROUVEE);
            long modifiees = decisionRepository.countBySystemeIaOuNomAndStatut(
                    systemeIA.getSystemeIaId(), systemeIA.getNom(), StatutDecisionEnum.MODIFIEE);
            long rejetees = decisionRepository.countBySystemeIaOuNomAndStatut(
                    systemeIA.getSystemeIaId(), systemeIA.getNom(), StatutDecisionEnum.REJETEE);
            long enAttente = decisionRepository.countBySystemeIaOuNomAndStatut(
                    systemeIA.getSystemeIaId(), systemeIA.getNom(), StatutDecisionEnum.EN_ATTENTE);

            double score = calculerScore(total, approuvees, modifiees);

            resultats.add(new ComparaisonAgentResponse(
                    null,
                    systemeIA.getSystemeIaId(),
                    systemeIA.getNom(),
                    systemeIA.getFournisseur(),
                    systemeIA.getModele(),
                    systemeIA.getVersionModele(),
                    total,
                    approuvees,
                    modifiees,
                    rejetees,
                    enAttente,
                    score));
        }

        Comparator<ComparaisonAgentResponse> comparator = Comparator
                .comparingDouble(ComparaisonAgentResponse::getScorePourcentage).reversed()
                .thenComparing(Comparator.comparingLong(ComparaisonAgentResponse::getApprouvees).reversed())
                .thenComparing(ComparaisonAgentResponse::getNom, String.CASE_INSENSITIVE_ORDER);

        resultats.sort(comparator);

        for (int index = 0; index < resultats.size(); index++) {
            resultats.get(index).setRang(index + 1);
        }

        return resultats;
    }

    private double calculerScore(long total, long approuvees, long modifiees) {
        if (total == 0) {
            return 0.0d;
        }

        double scoreBrut = (approuvees * 1.0d) + (modifiees * 0.5d);
        return BigDecimal.valueOf((scoreBrut / total) * 100.0d)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}