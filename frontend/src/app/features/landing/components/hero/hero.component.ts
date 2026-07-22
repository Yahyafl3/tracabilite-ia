import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';
import { ScrollService } from '../../../../shared/scroll.service';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [IconComponent, RevealDirective, RouterLink],
  templateUrl: './hero.component.html',
  styleUrl: './hero.component.scss',
})
export class HeroComponent {
  private readonly scroll = inject(ScrollService);

  readonly badges = [
    { icon: 'users', label: 'Validation humaine' },
    { icon: 'lightbulb', label: 'Explicabilité SHAP' },
    { icon: 'lock', label: 'Audit SHA-256' },
    { icon: 'activity', label: 'Multi-agents Groq' },
  ];

  goTo(id: string): void {
    this.scroll.scrollTo(id);
  }
}
