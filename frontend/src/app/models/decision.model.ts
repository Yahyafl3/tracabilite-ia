export enum StatutDecisionEnum {
  EN_ATTENTE = 'EN_ATTENTE',
  APPROUVE = 'APPROUVE',
  REJETE = 'REJETE',
  MODIFIE = 'MODIFIE'
}

export interface Decision {
  id: number;
  systemeIaId: number;
  decisionPrecedente?: number;
  contenu: string;
  contexte: string;
  statut: StatutDecisionEnum;
  scoreConfiance: number;
  hashCourant: string;
  hashPrecedent?: string;
  dateCreation: Date;
  dateModification?: Date;
}

export interface DecisionRequest {
  contenu: string;
  contexte: string;
  scoreConfiance: number;
}

export interface DecisionResponse extends Decision {
  systemeIA?: {
    id: number;
    nom: string;
    nomModele: string;
  };
  validations?: any[];
}
