import { Component } from '@angular/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

interface UseCase {
  icon: string;
  sector: string;
  description: string;
}

interface Testimonial {
  quote: string;
  name: string;
  role: string;
  initials: string;
}

@Component({
  selector: 'app-testimonials',
  standalone: true,
  imports: [IconComponent, RevealDirective],
  templateUrl: './testimonials.component.html',
  styleUrl: './testimonials.component.scss',
})
export class TestimonialsComponent {
  readonly useCases: UseCase[] = [
    {
      icon: 'landmark',
      sector: 'Finance & Assurance',
      description:
        'Traçabilité des décisions de crédit, scoring et détection de fraude, avec justification réglementaire complète.',
    },
    {
      icon: 'users',
      sector: 'Ressources Humaines',
      description:
        'Transparence des tris de CV et évaluations automatisées, pour un recrutement non discriminatoire et auditable.',
    },
    {
      icon: 'heart-pulse',
      sector: 'Santé',
      description:
        "Audit des diagnostics et recommandations assistés par IA, avec explicabilité des décisions cliniques critiques.",
    },
    {
      icon: 'building',
      sector: 'Secteur Public',
      description:
        'Décisions administratives automatisées traçables et explicables, au service de la transparence citoyenne.',
    },
  ];

  readonly testimonials: Testimonial[] = [
    {
      quote:
        "En cas de contrôle, nous produisons l'historique complet d'une décision en quelques minutes. La conformité AI Act n'est plus un angle mort.",
      name: 'Camille Rousseau',
      role: 'Responsable Conformité, banque de détail',
      initials: 'CR',
    },
    {
      quote:
        "L'explicabilité intégrée a changé nos échanges avec le métier et le juridique. Chaque décision IA est enfin défendable.",
      name: 'Karim Benali',
      role: 'DSI, groupe assurantiel',
      initials: 'KB',
    },
    {
      quote:
        'Le chaînage cryptographique nous garantit un audit trail infalsifiable. Nos auditeurs externes ont validé la démarche du premier coup.',
      name: 'Élodie Fontaine',
      role: 'Data Science Lead, secteur santé',
      initials: 'EF',
    },
  ];
}
