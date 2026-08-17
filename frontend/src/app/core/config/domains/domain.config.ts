import { i18nLabel } from '../../i18n/label-translator';

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
    useCase: 'Risque de défaut de paiement (dataset public)',
    featureCount: 9,
    modelLabel: 'credit_pipeline',
    modelVersion: 'credit-model-v1.0.0',
    datasetVersion: 'credit-analysis-v1.0.0',
    validatorRole: 'RESPONSABLE_CREDIT',
    riskLabel: 'Risque de défaut',
    humanDecisions: [
      { label: 'Acceptée', value: 'ACCEPTEE' },
      { label: 'Refusée', value: 'REFUSEE' },
      { label: 'À revoir', value: 'A_REVOIR' },
    ],
    featureLabels: {
      age: 'Âge',
      duree_mois: 'Durée (mois)',
      dureeMois: 'Durée (mois)',
      type_contrat: 'Type de contrat',
      typeContrat: 'Type de contrat',
      statut_logement: 'Statut logement',
      statutLogement: 'Statut logement',
      incident_paiement_bam: 'Incident paiement BAM',
      incidentPaiementBam: 'Incident paiement BAM',
      montant_demande_mad: 'Montant demandé',
      montantDemandeMad: 'Montant demandé',
      nouvelle_echeance_mad: 'Nouvelle échéance',
      nouvelleEcheanceMad: 'Nouvelle échéance',
      revenu_mensuel_mad: 'Revenu mensuel',
      revenuMensuelMad: 'Revenu mensuel',
      taux_endettement: "Taux d'endettement",
      tauxEndettement: "Taux d'endettement",
    },
    sourceMeta: {
      dataType: 'Données publiques contextualisées',
      usage: 'Démonstration du risque de défaut',
      limit: 'Pas un modèle bancaire officiel',
      synthetic: false,
    },
  },
  MEDICAL: {
    useCase: 'Estimation indicative du risque de diabète',
    featureCount: 7,
    modelLabel: 'medical_pipeline',
    modelVersion: 'medical-model-v1.0.0',
    datasetVersion: 'medical-diabetes-european-v1.0.0',
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
      age: 'Âge',
      grossesses: 'Grossesses',
      glycemie_mg_dl: 'Glycémie (mg/dL)',
      glycemieMgDl: 'Glycémie (mg/dL)',
      pression_arterielle_mmhg: 'Pression artérielle (mmHg)',
      pressionArterielleMmhg: 'Pression artérielle (mmHg)',
      epaisseur_pli_cutane_mm: 'Épaisseur pli cutané (mm)',
      epaisseurPliCutaneMm: 'Épaisseur pli cutané (mm)',
      insuline_micro_u_ml: 'Insuline (µU/mL)',
      insulineMicroUMl: 'Insuline (µU/mL)',
      imc_kg_m2: 'IMC (kg/m²)',
      imcKgM2: 'IMC (kg/m²)',
    },
    sourceMeta: {
      dataType: 'Données publiques européennes',
      usage: 'Estimation indicative du risque de diabète',
      limit: 'Ne remplace pas un diagnostic médical',
      synthetic: false,
    },
  },
  EDUCATION: {
    useCase: 'Risque de décrochage — accompagnement pédagogique',
    featureCount: 16,
    modelLabel: 'education_pipeline',
    modelVersion: 'education-model-v1.0.0',
    datasetVersion: 'education-portugal-dropout-v1.0.0',
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
      age_inscription: 'Âge à l’inscription',
      ageInscription: 'Âge à l’inscription',
      note_admission: 'Note d’admission',
      noteAdmission: 'Note d’admission',
      note_qualification_precedente: 'Note qualification précédente',
      noteQualificationPrecedente: 'Note qualification précédente',
      unites_validees_s1: 'Unités validées S1',
      unitesValideesS1: 'Unités validées S1',
      moyenne_s1: 'Moyenne S1',
      moyenneS1: 'Moyenne S1',
      unites_validees_s2: 'Unités validées S2',
      unitesValideesS2: 'Unités validées S2',
      moyenne_s2: 'Moyenne S2',
      moyenneS2: 'Moyenne S2',
      taux_chomage: 'Taux de chômage',
      tauxChomage: 'Taux de chômage',
      taux_inflation: 'Taux d’inflation',
      tauxInflation: 'Taux d’inflation',
      pib: 'PIB',
      sexe: 'Sexe',
      boursier: 'Boursier',
      frais_a_jour: 'Frais à jour',
      fraisAJour: 'Frais à jour',
      debiteur: 'Débiteur',
      deplace: 'Déplacé',
      international: 'International',
    },
    sourceMeta: {
      dataType: 'Données publiques (Portugal)',
      usage: "Aide à l'accompagnement pédagogique",
      limit: 'Ne constitue pas une sanction automatique',
      synthetic: false,
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
  const fallback = DOMAIN_META[domain].featureLabels[raw] ?? raw.replace(/_/g, ' ');
  return i18nLabel(`features.${raw}`, fallback);
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
