export enum RoleEnum {
  ADMIN = 'ADMIN',
  VALIDATEUR = 'VALIDATEUR',
  SYSTEME_IA = 'SYSTEME_IA',
  AUDITEUR = 'AUDITEUR'
}

export interface User {
  id: number;
  email: string;
  nom: string;
  prenom: string;
  role: RoleEnum;
  actif: boolean;
  dateCreation?: Date;
}

export interface Administrateur extends User {
  role: RoleEnum.ADMIN;
}

export interface Validateur extends User {
  role: RoleEnum.VALIDATEUR;
  specialite?: string;
  nombreValidations?: number;
}

export interface SystemeIA extends User {
  role: RoleEnum.SYSTEME_IA;
  nomModele?: string;
  versionModele?: string;
  apiKey?: string;
}

export interface Auditeur extends User {
  role: RoleEnum.AUDITEUR;
}
