import { Component, inject } from '@angular/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';
import { ScrollService } from '../../../../shared/scroll.service';

interface Plan {
  name: string;
  price: string;
  period: string;
  description: string;
  features: string[];
  featured: boolean;
  cta: string;
}

@Component({
  selector: 'app-pricing',
  standalone: true,
  imports: [IconComponent, RevealDirective],
  templateUrl: './pricing.component.html',
  styleUrl: './pricing.component.scss',
})
export class PricingComponent {
  private readonly scroll = inject(ScrollService);

  readonly plans: Plan[] = [
    {
      name: 'Starter',
      price: '199 €',
      period: '/ mois',
      description: 'Pour les équipes qui démarrent leur mise en conformité.',
      features: [
        "Jusqu'à 10 000 décisions / mois",
        'Traçabilité complète input → output',
        'Explicabilité de base',
        'Rapports d\'audit PDF',
        'Support par email',
      ],
      featured: false,
      cta: 'Démarrer',
    },
    {
      name: 'Business',
      price: '599 €',
      period: '/ mois',
      description: 'Pour les organisations soumises à des contrôles réguliers.',
      features: [
        "Jusqu'à 250 000 décisions / mois",
        'Audit trail immuable (chaînage SHA-256)',
        'Explicabilité avancée (XAI)',
        'Alertes anomalies & dérives',
        'Exports API + connecteurs GRC',
        'Support prioritaire 24/7',
      ],
      featured: true,
      cta: 'Essai gratuit 14 jours',
    },
    {
      name: 'Enterprise',
      price: 'Sur mesure',
      period: '',
      description: 'Pour les grandes organisations et le secteur régulé.',
      features: [
        'Décisions illimitées',
        'Hébergement souverain dédié',
        'SLA 99,99 % garanti',
        'Conformité personnalisée AI Act',
        'Accompagnement DPO & juridique',
      ],
      featured: false,
      cta: 'Nous contacter',
    },
  ];

  goToContact(): void {
    this.scroll.scrollTo('contact');
  }
}
