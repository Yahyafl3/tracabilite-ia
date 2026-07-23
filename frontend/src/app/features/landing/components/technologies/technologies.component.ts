import { Component } from '@angular/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

interface TechGroup {
  title: string;
  items: string[];
}

@Component({
  selector: 'app-technologies',
  standalone: true,
  imports: [IconComponent, RevealDirective],
  templateUrl: './technologies.component.html',
  styleUrl: './technologies.component.scss',
})
export class TechnologiesComponent {
  readonly groups: TechGroup[] = [
    {
      title: 'Frontend',
      items: ['Angular 21', 'PrimeNG', 'Sakai / Aura'],
    },
    {
      title: 'Backend',
      items: ['Spring Boot 3.4', 'Java 17'],
    },
    {
      title: 'Données',
      items: ['PostgreSQL'],
    },
    {
      title: 'Machine Learning',
      items: ['Python', 'Flask', 'Scikit-learn', 'SHAP'],
    },
    {
      title: 'IA générative',
      items: ['Groq', 'OpenRouter (compatibilité historique)'],
    },
    {
      title: 'Sécurité & déploiement',
      items: ['JWT', 'Docker Compose', 'SHA-256'],
    },
  ];
}
