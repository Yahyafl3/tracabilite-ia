import {
  Component,
  HostListener,
  PLATFORM_ID,
  inject,
  signal,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../../../../shared/icon.component';
import { LogoComponent } from '../../../../shared/logo.component';
import { ScrollService } from '../../../../shared/scroll.service';
import { ThemeService } from '../../../../shared/theme.service';

interface NavLink {
  label: string;
  target: string;
}

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [IconComponent, LogoComponent, RouterLink],
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
    { label: 'Fonctionnalités', target: 'fonctionnalites' },
    { label: 'Comment ça marche', target: 'fonctionnement' },
    { label: 'Sécurité', target: 'securite' },
    { label: 'Tarifs', target: 'tarifs' },
    { label: 'Contact', target: 'contact' },
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
