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
      icon: 'link',
      title: 'Connectez vos modèles',
      description:
        "Intégrez vos systèmes IA via notre API REST ou nos SDK. Compatible TensorFlow, PyTorch, scikit-learn et les plateformes cloud.",
    },
    {
      number: '02',
      icon: 'scan',
      title: 'Capturez chaque décision',
      description:
        "Chaque prédiction est enregistrée automatiquement : entrées, sorties, contexte et métadonnées, sans modifier votre pipeline existant.",
    },
    {
      number: '03',
      icon: 'bar-chart',
      title: 'Analysez & scorez',
      description:
        "La plateforme calcule les scores d'explicabilité, détecte les dérives et évalue le niveau de risque réglementaire de chaque décision.",
    },
    {
      number: '04',
      icon: 'file-check',
      title: "Générez vos audits",
      description:
        "Produisez à la demande des rapports d'audit complets et conformes, exportables pour vos contrôles internes et externes.",
    },
  ];
}
