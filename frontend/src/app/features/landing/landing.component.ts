import {
  Component,
  HostListener,
  PLATFORM_ID,
  inject,
  signal,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

import { NavbarComponent } from './components/navbar/navbar.component';
import { HeroComponent } from './components/hero/hero.component';
import { ProblemComponent } from './components/problem/problem.component';
import { FeaturesComponent } from './components/features/features.component';
import { HowItWorksComponent } from './components/how-it-works/how-it-works.component';
import { TechnologiesComponent } from './components/technologies/technologies.component';
import { SecurityComponent } from './components/security/security.component';
import { UseCaseComponent } from './components/use-case/use-case.component';
import { HumanValidationComponent } from './components/human-validation/human-validation.component';
import { CtaComponent } from './components/cta/cta.component';
import { FooterComponent } from './components/footer/footer.component';
import { IconComponent } from '../../shared/icon.component';
import { ScrollService } from '../../shared/scroll.service';

/**
 * Landing Page — présentation réelle de Traçabilité IA.
 */
@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [
    NavbarComponent,
    HeroComponent,
    ProblemComponent,
    FeaturesComponent,
    HowItWorksComponent,
    TechnologiesComponent,
    SecurityComponent,
    UseCaseComponent,
    HumanValidationComponent,
    CtaComponent,
    FooterComponent,
    IconComponent,
  ],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
})
export class LandingComponent {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly scroll = inject(ScrollService);

  readonly showBackToTop = signal(false);

  @HostListener('window:scroll')
  onScroll(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.showBackToTop.set(window.scrollY > 600);
    }
  }

  backToTop(): void {
    this.scroll.scrollToTop();
  }
}
