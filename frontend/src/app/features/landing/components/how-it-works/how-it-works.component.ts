import { Component } from '@angular/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

interface Step {
  number: string;
  icon: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-how-it-works',
  standalone: true,
  imports: [IconComponent, RevealDirective],
  templateUrl: './how-it-works.component.html',
  styleUrl: './how-it-works.component.scss',
})
export class HowItWorksComponent {
  readonly steps: Step[] = [
    {
      number: '01',
      icon: 'file-text',
      title: 'Saisie du dossier',
      description:
        "L'utilisateur saisit le contexte, la demande et les données métier.",
    },
    {
      number: '02',
      icon: 'scan',
      title: 'Analyse ML et SHAP',
      description:
        'Le service ML génère une prédiction et explique les facteurs associés.',
    },
    {
      number: '03',
      icon: 'activity',
      title: 'Consultation multi-agents',
      description:
        'Trois agents Groq analysent le même dossier et proposent leurs recommandations.',
    },
    {
      number: '04',
      icon: 'link',
      title: 'Consensus et traçabilité',
      description:
        'Le système calcule le consensus, enregistre les sources et génère les hashes.',
    },
    {
      number: '05',
      icon: 'file-check',
      title: 'Validation humaine',
      description:
        'Le validateur consulte toutes les informations et prend la décision finale.',
    },
  ];
}
