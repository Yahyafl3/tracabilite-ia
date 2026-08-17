import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';
import { ScrollService } from '../../../../shared/scroll.service';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [IconComponent, RevealDirective, RouterLink, TranslatePipe],
  templateUrl: './hero.component.html',
  styleUrl: './hero.component.scss',
})
export class HeroComponent {
  private readonly scroll = inject(ScrollService);

  readonly badges = [
    { icon: 'users', labelKey: 'landing.hero.badgeHuman' },
    { icon: 'lightbulb', labelKey: 'landing.hero.badgeShap' },
    { icon: 'lock', labelKey: 'landing.hero.badgeAudit' },
    { icon: 'activity', labelKey: 'landing.hero.badgeAgents' },
  ];

  goTo(id: string): void {
    this.scroll.scrollTo(id);
  }
}
