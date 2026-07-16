/**
 * Configuration centralisée des champs ML par secteur.
 * Alignée sur ml-service/sector_schema.py et backend CreditSchemaConfig.
 *
 * Le pipeline Scikit-learn utilise le MÊME schéma pour tous les secteurs.
 * Le secteur est une feature catégorielle encodée en OneHot (cat__sector_*).
 */
import { Validators } from '@angular/forms';

export const SECTORS = ['SERVICES', 'INDUSTRIE', 'COMMERCE', 'TECH', 'AGRICULTURE'] as const;
export type Sector = (typeof SECTORS)[number];

export type MlFeatureKey =
  | 'amount'
  | 'monthlyIncome'
  | 'companyAgeYears'
  | 'paymentIncidents'
  | 'debtRatio';

export const ML_FEATURE_KEYS: readonly MlFeatureKey[] = [
  'amount',
  'monthlyIncome',
  'companyAgeYears',
  'paymentIncidents',
  'debtRatio',
] as const;

export const ML_SCHEMA_INFO = {
  schemaType: 'unified' as const,
  description:
    'Le modèle LogisticRegression utilise un pipeline commun pour tous les secteurs. ' +
    'Le secteur est une feature catégorielle encodée en OneHot (cat__sector_*).',
  numericFeatures: [...ML_FEATURE_KEYS],
  categoricalFeatures: ['sector'] as const,
  allInputFeatures: [...ML_FEATURE_KEYS, 'sector'] as const,
  sectorHint: 'Les données demandées sont adaptées au secteur sélectionné.',
};

export interface SectorFieldConfig {
  key: MlFeatureKey;
  label: string;
  help: string;
  type: 'number';
  min: number;
  max?: number;
  step: number;
  defaultValue: number;
  required: true;
}

function field(
  key: MlFeatureKey,
  label: string,
  help: string,
  defaultValue: number,
  min: number,
  max?: number,
  step = key === 'debtRatio' ? 0.01 : 1,
): SectorFieldConfig {
  return { key, label, help, type: 'number', min, max, step, defaultValue, required: true };
}

export const sectorFields: Record<Sector, SectorFieldConfig[]> = {
  SERVICES: [
    field('amount', 'Montant demandé', 'Montant du crédit professionnel demandé.', 25000, 1),
    field('monthlyIncome', 'Revenu mensuel', 'Revenu mensuel net de l\'entreprise de services.', 15000, 1),
    field('companyAgeYears', 'Ancienneté de l\'entreprise (années)', 'Nombre d\'années d\'activité.', 5, 0),
    field('paymentIncidents', 'Incidents de paiement', 'Incidents de paiement sur les 24 derniers mois.', 0, 0),
    field('debtRatio', 'Ratio d\'endettement (0-1)', 'Endettement actuel / revenus (0 = aucun, 1 = maximum).', 0.22, 0, 1),
  ],
  INDUSTRIE: [
    field('amount', 'Montant d\'investissement industriel', 'Investissement ou crédit d\'équipement industriel.', 50000, 1),
    field('monthlyIncome', 'Chiffre d\'affaires mensuel moyen', 'Revenus mensuels récurrents de l\'activité industrielle.', 20000, 1),
    field('companyAgeYears', 'Ancienneté du site industriel (années)', 'Ancienneté de l\'exploitation industrielle.', 8, 0),
    field('paymentIncidents', 'Incidents de paiement fournisseurs', 'Retards ou incidents de paiement récents.', 1, 0),
    field('debtRatio', 'Ratio d\'endettement industriel (0-1)', 'Part de la dette dans les revenus mensuels.', 0.35, 0, 1),
  ],
  COMMERCE: [
    field('amount', 'Montant de financement commercial', 'Besoin de trésorerie ou d\'investissement commercial.', 30000, 1),
    field('monthlyIncome', 'Revenu mensuel du point de vente', 'Revenus mensuels moyens du commerce.', 12000, 1),
    field('companyAgeYears', 'Ancienneté du commerce (années)', 'Durée d\'exploitation du commerce.', 4, 0),
    field('paymentIncidents', 'Incidents de paiement', 'Incidents bancaires ou fournisseurs récents.', 0, 0),
    field('debtRatio', 'Ratio d\'endettement (0-1)', 'Niveau d\'endettement actuel.', 0.28, 0, 1),
  ],
  TECH: [
    field('amount', 'Montant de financement tech', 'Montant demandé pour développement ou croissance tech.', 40000, 1),
    field('monthlyIncome', 'Revenu mensuel récurrent (MRR)', 'Revenus mensuels (abonnements, licences, etc.).', 25000, 1),
    field('companyAgeYears', 'Ancienneté de la startup (années)', 'Ancienneté de la structure technologique.', 3, 0),
    field('paymentIncidents', 'Incidents de paiement', 'Incidents de paiement sur la période récente.', 0, 0),
    field('debtRatio', 'Ratio d\'endettement (0-1)', 'Endettement rapporté aux revenus mensuels.', 0.18, 0, 1),
  ],
  AGRICULTURE: [
    field('amount', 'Montant de financement agricole', 'Crédit pour matériel, intrants ou trésorerie agricole.', 20000, 1),
    field('monthlyIncome', 'Revenu mensuel de l\'exploitation', 'Revenus mensuels moyens de l\'exploitation.', 8000, 1),
    field('companyAgeYears', 'Ancienneté de l\'exploitation (années)', 'Ancienneté de l\'activité agricole.', 10, 0),
    field('paymentIncidents', 'Incidents de paiement', 'Retards ou incidents de paiement.', 2, 0),
    field('debtRatio', 'Ratio d\'endettement (0-1)', 'Part de la dette dans les revenus mensuels.', 0.40, 0, 1),
  ],
};

export function validatorsForField(config: SectorFieldConfig) {
  const rules = [Validators.required, Validators.min(config.min)];
  if (config.max != null) {
    rules.push(Validators.max(config.max));
  }
  return rules;
}

export function buildAnalyzePayload(raw: Record<string, unknown>, sector: Sector): {
  amount: number;
  monthlyIncome: number;
  companyAgeYears: number;
  paymentIncidents: number;
  debtRatio: number;
  sector: Sector;
  description?: string;
  includeOpenRouter?: boolean;
} {
  const allowedKeys = new Set<MlFeatureKey>(sectorFields[sector].map((f) => f.key));
  const includeOpenRouter = Boolean(raw['includeOpenRouter']);
  const payload = {
    amount: Number(raw['amount']),
    monthlyIncome: Number(raw['monthlyIncome']),
    companyAgeYears: Number(raw['companyAgeYears']),
    paymentIncidents: Number(raw['paymentIncidents']),
    debtRatio: Number(raw['debtRatio']),
    sector,
    description: typeof raw['description'] === 'string' ? raw['description'] : undefined,
    includeOpenRouter,
  };

  for (const key of ML_FEATURE_KEYS) {
    if (!allowedKeys.has(key)) {
      throw new Error(`Champ ${key} non autorisé pour le secteur ${sector}`);
    }
  }

  return payload;
}
