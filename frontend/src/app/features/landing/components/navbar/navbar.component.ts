import {
  Component,
  HostListener,
  PLATFORM_ID,
  inject,
  signal,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { IconComponent } from '../../../../shared/icon.component';
import { LogoComponent } from '../../../../shared/logo.component';
import { ScrollService } from '../../../../shared/scroll.service';
import { ThemeService } from '../../../../shared/theme.service';
import { LanguageSwitcherComponent } from '../../../../layout/language-switcher/language-switcher.component';

interface NavLink {
  labelKey: string;
  target: string;
}

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [IconComponent, LogoComponent, RouterLink, TranslatePipe, LanguageSwitcherComponent],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
})
export class NavbarComponent {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly scroll = inject(ScrollService);
  private readonly themeService = inject(ThemeService);

  readonly theme = this.themeService.theme;
  readonly scrolled = signal(false);
  readonly menuOpen = signal(false);

  readonly links: NavLink[] = [
    { labelKey: 'landing.nav.home', target: 'accueil' },
    { labelKey: 'landing.nav.features', target: 'fonctionnalites' },
    { labelKey: 'landing.nav.howItWorks', target: 'fonctionnement' },
    { labelKey: 'landing.nav.technologies', target: 'technologies' },
    { labelKey: 'landing.nav.security', target: 'securite' },
    { labelKey: 'landing.nav.useCases', target: 'cas-usage' },
  ];

  @HostListener('window:scroll')
  onScroll(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.scrolled.set(window.scrollY > 24);
    }
  }

  toggleMenu(): void {
    this.menuOpen.update((v) => !v);
  }

  toggleTheme(): void {
    this.themeService.toggle();
  }

  scrollTo(id: string): void {
    this.menuOpen.set(false);
    this.scroll.scrollTo(id);
  }
}
