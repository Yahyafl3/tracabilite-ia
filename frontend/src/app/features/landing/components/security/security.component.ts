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
      icon: 'lock',
      title: 'Chiffrement AES-256 / TLS 1.3',
      description: 'Données protégées au repos et en transit.',
    },
    {
      icon: 'shield-check',
      title: 'Conformité RGPD',
      description: 'Minimisation, droit à l\'explication, registres.',
    },
    {
      icon: 'server',
      title: 'Hébergement souverain (UE)',
      description: 'Infrastructure sécurisée hébergée en Europe.',
    },
    {
      icon: 'key',
      title: 'Contrôle d\'accès (RBAC + JWT)',
      description: 'Authentification forte et rôles granulaires.',
    },
    {
      icon: 'check-circle',
      title: 'ISO 27001 & SOC 2',
      description: 'Standards de sécurité reconnus mondialement.',
    },
    {
      icon: 'history',
      title: 'Journalisation inaltérable',
      description: 'Chaînage cryptographique de chaque événement.',
    },
  ];
}
