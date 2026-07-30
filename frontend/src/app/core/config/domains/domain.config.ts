/**
 * Configuration des domaines métier (distincts du secteur économique crédit).
 */
export type DecisionDomain = 'CREDIT' | 'MEDICAL' | 'EDUCATION';

export const DECISION_DOMAINS: { value: DecisionDomain; label: string; description: string }[] = [
  {
    value: 'CREDIT',
    label: 'Crédit',
    description: 'Saisissez les informations de la demande de crédit.',
  },
  {
    value: 'MEDICAL',
    label: 'Médical',
    description:
      'Saisissez les données nécessaires à l’évaluation indicative du risque de diabète.',
  },
  {
    value: 'EDUCATION',
    label: 'Éducation',
    description:
      'Saisissez les données académiques nécessaires à l’évaluation du risque de décrochage.',
  },
];

export const DOMAIN_META: Record<
  DecisionDomain,
  {
    useCase: string;
    featureCount: number;
    modelLabel: string;
    modelVersion: string;
    datasetVersion: string;
    disclaimer?: string;
    warning?: string;
    validatorRole: string;
    riskLabel: string;
    humanDecisions: { label: string; value: string }[];
    featureLabels: Record<string, string>;
    sourceMeta: {
      dataType: string;
      usage: string;
      limit: string;
      synthetic: boolean;
    };
  }
> = {
  CREDIT: {
    useCase: 'Risque de défaut de paiement (synthétique Maroc)',
    featureCount: 14,
    modelLabel: 'credit_pipeline',
    modelVersion: 'credit-model-v1.0.0',
    datasetVersion: 'credit-maroc-synthetic-v1.0.0',
    validatorRole: 'RESPONSABLE_CREDIT',
    riskLabel: 'Risque de défaut',
    humanDecisions: [
      { label: 'Acceptée', value: 'ACCEPTEE' },
      { label: 'Refusée', value: 'REFUSEE' },
      { label: 'À revoir', value: 'A_REVOIR' },
    ],
    featureLabels: {
      ratio_endettement: "Ratio d'endettement",
      ratioEndettement: "Ratio d'endettement",
      revenu_mensuel_mad: 'Revenu mensuel',
      revenuMensuelMad: 'Revenu mensuel',
      incidents_paiement_24_mois: 'Incidents de paiement',
      incidentsPaiement24Mois: 'Incidents de paiement',
      montant_demande_mad: 'Montant demandé',
      montantDemandeMad: 'Montant demandé',
      type_garantie: 'Garantie',
      typeGarantie: 'Garantie',
      secteur_activite: "Secteur d'activité",
      secteurActivite: "Secteur d'activité",
      age_demandeur: 'Âge du demandeur',
      ageDemandeur: 'Âge du demandeur',
    },
    sourceMeta: {
      dataType: 'Données synthétiques contextualisées au Maroc',
      usage: 'Démonstration du risque de défaut',
      limit: 'Pas un modèle bancaire officiel',
      synthetic: true,
    },
  },
  MEDICAL: {
    useCase: 'Estimation indicative du risque de diabète',
    featureCount: 14,
    modelLabel: 'medical_pipeline',
    modelVersion: 'medical-model-v1.0.0',
    datasetVersion: 'medical-diabetes-maroc-synthetic-v1.0.0',
    disclaimer:
      'Ce module fournit une estimation de risque à titre d’aide à la décision. Il ne remplace pas un diagnostic médical ni l’avis d’un professionnel de santé.',
    warning:
      'Cette estimation constitue uniquement une aide à l’évaluation du risque. Elle ne remplace pas un diagnostic médical ni l’avis d’un professionnel de santé.',
    validatorRole: 'PROFESSIONNEL_SANTE',
    riskLabel: 'Niveau de risque',
    humanDecisions: [
      { label: 'Suivi standard', value: 'SUIVI_STANDARD' },
      { label: 'Examen complémentaire', value: 'EXAMEN_COMPLEMENTAIRE' },
      { label: 'Orientation spécialiste', value: 'ORIENTATION_SPECIALISTE' },
      { label: 'À revoir', value: 'A_REVOIR' },
    ],
    featureLabels: {
      glycemie: 'Glycémie',
      imc: 'IMC',
      age: 'Âge',
      antecedents_familiaux_diabete: 'Antécédents familiaux',
      antecedentsFamiliauxDiabete: 'Antécédents familiaux',
      hypertension: 'Hypertension',
      niveau_activite_physique: 'Activité physique',
      niveauActivitePhysique: 'Activité physique',
    },
    sourceMeta: {
      dataType: 'Données synthétiques contextualisées au Maroc',
      usage: 'Estimation indicative du risque de diabète',
      limit: 'Ne remplace pas un diagnostic médical',
      synthetic: true,
    },
  },
  EDUCATION: {
    useCase: 'Risque de décrochage — accompagnement pédagogique',
    featureCount: 15,
    modelLabel: 'education_pipeline',
    modelVersion: 'education-model-v1.0.0',
    datasetVersion: 'students-maroc-dropout-synthetic-v1.0.0',
    disclaimer:
      'Aide à l’accompagnement pédagogique. Ne constitue pas une sanction automatique contre l’étudiant.',
    warning:
      'Cette estimation sert à proposer un accompagnement pédagogique. Elle ne constitue pas une sanction automatique contre l’étudiant.',
    validatorRole: 'RESPONSABLE_PEDAGOGIQUE',
    riskLabel: 'Risque de décrochage',
    humanDecisions: [
      { label: 'Aucune intervention', value: 'AUCUNE_INTERVENTION' },
      { label: 'Accompagnement', value: 'ACCOMPAGNEMENT' },
      { label: 'Entretien pédagogique', value: 'ENTRETIEN_PEDAGOGIQUE' },
      { label: 'Tutorat', value: 'TUTORAT' },
      { label: 'Orientation', value: 'ORIENTATION' },
      { label: 'À revoir', value: 'A_REVOIR' },
    ],
    featureLabels: {
      moyenne_semestre_1: 'Moyenne semestre 1',
      moyenneSemestre1: 'Moyenne semestre 1',
      moyenne_semestre_2: 'Moyenne semestre 2',
      moyenneSemestre2: 'Moyenne semestre 2',
      taux_absence: "Taux d'absence",
      tauxAbsence: "Taux d'absence",
      modules_non_valides: 'Modules non validés',
      modulesNonValides: 'Modules non validés',
      participation: 'Participation',
    },
    sourceMeta: {
      dataType: 'Données synthétiques contextualisées au Maroc',
      usage: "Aide à l'accompagnement pédagogique",
      limit: 'Ne constitue pas une sanction automatique',
      synthetic: true,
    },
  },
};

export function resolveDomain(value?: string | null): DecisionDomain {
  if (value === 'MEDICAL' || value === 'EDUCATION' || value === 'CREDIT') {
    return value;
  }
  return 'CREDIT';
}

export function featureDisplayName(domain: DecisionDomain, raw: string): string {
  return DOMAIN_META[domain].featureLabels[raw] ?? raw.replace(/_/g, ' ');
}

export const REGIONS_MAROC = [
  'Casablanca-Settat',
  'Rabat-Salé-Kénitra',
  'Marrakech-Safi',
  'Fès-Meknès',
  'Tanger-Tétouan-Al Hoceïma',
  'Souss-Massa',
  'Oriental',
  'Béni Mellal-Khénifra',
  'Drâa-Tafilalet',
  'Guelmim-Oued Noun',
  'Laâyoune-Sakia El Hamra',
  'Dakhla-Oued Ed-Dahab',
] as const;
