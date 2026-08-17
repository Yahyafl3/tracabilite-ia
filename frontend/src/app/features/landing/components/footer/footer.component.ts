import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { ScrollService } from '../../../../shared/scroll.service';

interface FooterColumn {
  titleKey: string;
  links: { labelKey: string; target?: string; route?: string }[];
}

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
})
export class FooterComponent {
  private readonly scroll = inject(ScrollService);

  readonly year = new Date().getFullYear();

  readonly columns: FooterColumn[] = [
    {
      titleKey: 'landing.footer.product',
      links: [
        { labelKey: 'landing.nav.features', target: 'fonctionnalites' },
        { labelKey: 'landing.nav.howItWorks', target: 'fonctionnement' },
        { labelKey: 'landing.nav.security', target: 'securite' },
        { labelKey: 'landing.nav.useCases', target: 'cas-usage' },
      ],
    },
    {
      titleKey: 'landing.footer.application',
      links: [
        { labelKey: 'landing.nav.login', route: '/auth/login' },
        { labelKey: 'nav.dashboard', route: '/dashboard' },
      ],
    },
    {
      titleKey: 'landing.footer.project',
      links: [
        { labelKey: 'landing.nav.technologies', target: 'technologies' },
        { labelKey: 'landing.footer.architecture', target: 'technologies' },
        { labelKey: 'landing.footer.about', target: 'probleme' },
      ],
    },
  ];

  navigate(target: string): void {
    this.scroll.scrollTo(target);
  }
}
