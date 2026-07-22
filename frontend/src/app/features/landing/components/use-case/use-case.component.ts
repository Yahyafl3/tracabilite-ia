import { Component } from '@angular/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

interface UseCaseCard {
  icon: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-use-case',
  standalone: true,
  imports: [IconComponent, RevealDirective],
  templateUrl: './use-case.component.html',
  styleUrl: './use-case.component.scss',
})
export class UseCaseComponent {
  readonly cards: UseCaseCard[] = [
    {
      icon: 'bar-chart',
      title: 'Analyse du risque',
      description:
        'Prédiction ML sur les données financières de la demande, avec score de confiance et niveau de risque.',
    },
    {
      icon: 'lightbulb',
      title: 'Explicabilité de la prédiction',
      description:
        'Facteurs SHAP pour comprendre ce qui pousse le modèle à accepter ou refuser.',
    },
    {
      icon: 'shield-check',
      title: 'Validation et audit',
      description:
        'Consensus multi-agents, sources, historique et décision humaine enregistrée.',
    },
  ];
}
