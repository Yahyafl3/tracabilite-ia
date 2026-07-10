import { Component, inject } from '@angular/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';
import { ScrollService } from '../../../../shared/scroll.service';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [IconComponent, RevealDirective],
  templateUrl: './hero.component.html',
  styleUrl: './hero.component.scss',
})
export class HeroComponent {
  private readonly scroll = inject(ScrollService);

  readonly badges = [
    { icon: 'shield-check', label: 'AI Act ready' },
    { icon: 'lock', label: 'RGPD' },
    { icon: 'check-circle', label: 'Audit trail immuable' },
  ];

  goTo(id: string): void {
    this.scroll.scrollTo(id);
  }
}
