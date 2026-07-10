import { Component } from '@angular/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

interface Feature {
  icon: string;
  title: string;
  description: string;
  accent: string;
}

@Component({
  selector: 'app-features',
  standalone: true,
  imports: [IconComponent, RevealDirective],
  templateUrl: './features.component.html',
  styleUrl: './features.component.scss',
})
export class FeaturesComponent {
  readonly features: Feature[] = [
    {
      icon: 'route',
      title: 'Traçabilité complète',
      description:
        'Capturez chaque décision de bout en bout : données d\'entrée, version du modèle, paramètres et résultat. La chaîne input → modèle → output entièrement reconstituable.',
      accent: 'indigo',
    },
    {
      icon: 'lightbulb',
      title: 'Explicabilité (XAI)',
      description:
        "Comprenez le « pourquoi » de chaque résultat grâce aux techniques d'IA explicable : importance des variables, contre-factuels et scores de confiance.",
      accent: 'violet',
    },
    {
      icon: 'scale',
      title: 'Conformité automatisée',
      description:
        'Cartographie continue de vos obligations AI Act et RGPD. Classification des risques, registres et preuves de conformité générés automatiquement.',
      accent: 'blue',
    },
    {
      icon: 'lock',
      title: 'Audit trail immuable',
      description:
        'Chaque enregistrement est horodaté et chaîné cryptographiquement (SHA-256). Toute altération est instantanément détectée : un historique infalsifiable.',
      accent: 'teal',
    },
    {
      icon: 'activity',
      title: 'Alertes sur les dérives',
      description:
        "Détection en temps réel des anomalies et du data/model drift. Vous êtes prévenu avant qu'une dérive du modèle n'impacte vos décisions.",
      accent: 'amber',
    },
    {
      icon: 'file-text',
      title: 'Rapports exportables',
      description:
        'Générez en un clic des rapports d\'audit prêts pour vos contrôleurs externes, DPO et autorités : PDF, CSV et API pour vos outils GRC.',
      accent: 'rose',
    },
  ];
}
