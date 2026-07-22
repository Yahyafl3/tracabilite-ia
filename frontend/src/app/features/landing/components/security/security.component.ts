import { Component } from '@angular/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

interface Badge {
  icon: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-security',
  standalone: true,
  imports: [IconComponent, RevealDirective],
  templateUrl: './security.component.html',
  styleUrl: './security.component.scss',
})
export class SecurityComponent {
  readonly badges: Badge[] = [
    {
      icon: 'key',
      title: 'Authentification JWT',
      description: 'Accès protégé à l’application et aux API.',
    },
    {
      icon: 'users',
      title: 'Gestion des rôles',
      description: 'Utilisateur, validateur, auditeur et administrateur.',
    },
    {
      icon: 'history',
      title: 'Historique des événements',
      description: 'Chaque étape du dossier est horodatée et consultable.',
    },
    {
      icon: 'lock',
      title: 'Chaînage SHA-256',
      description: 'Empreintes d’intégrité pour détecter les modifications.',
    },
  ];
}
