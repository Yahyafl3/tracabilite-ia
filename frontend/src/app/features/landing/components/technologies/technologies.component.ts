import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

@Component({
  selector: 'app-technologies',
  standalone: true,
  imports: [IconComponent, RevealDirective, TranslatePipe],
  templateUrl: './technologies.component.html',
  styleUrl: './technologies.component.scss',
})
export class TechnologiesComponent {
  readonly groups = [
    { titleKey: 'landing.tech.frontend', items: ['Angular 21', 'PrimeNG', 'Sakai / Aura'] },
    { titleKey: 'landing.tech.backend', items: ['Spring Boot 3.4', 'Java 17'] },
    { titleKey: 'landing.tech.data', items: ['PostgreSQL'] },
    { titleKey: 'landing.tech.ml', items: ['Python', 'Flask', 'Scikit-learn', 'SHAP'] },
    { titleKey: 'landing.tech.genai', items: ['Groq', 'OpenRouter (compatibilité historique)'] },
    { titleKey: 'landing.tech.security', items: ['JWT', 'Docker Compose', 'SHA-256'] },
  ];
}
