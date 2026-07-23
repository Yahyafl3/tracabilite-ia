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
      icon: 'bar-chart',
      title: 'Prédiction Machine Learning',
      description:
        'Analyse des données métier avec conservation du modèle, de la version et du niveau de confiance.',
      accent: 'indigo',
    },
    {
      icon: 'lightbulb',
      title: 'Explication SHAP',
      description:
        'Identification des facteurs qui influencent positivement ou négativement la prédiction.',
      accent: 'violet',
    },
    {
      icon: 'activity',
      title: 'Agents IA Groq',
      description:
        "Consultation de plusieurs modèles IA afin d'obtenir des recommandations complémentaires.",
      accent: 'blue',
    },
    {
      icon: 'users',
      title: 'Consensus multi-agents',
      description:
        "Calcul du niveau d'accord entre les agents sans modifier la prédiction ML.",
      accent: 'teal',
    },
    {
      icon: 'file-check',
      title: 'Validation humaine',
      description:
        "Décision finale réalisée par un validateur à partir du dossier complet.",
      accent: 'amber',
    },
    {
      icon: 'lock',
      title: 'Audit et intégrité',
      description:
        "Historique des événements, correlation ID, hashes SHA-256 et chaînage d'intégrité.",
      accent: 'rose',
    },
  ];
}
