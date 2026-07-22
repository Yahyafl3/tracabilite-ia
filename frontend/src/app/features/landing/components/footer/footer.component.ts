import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ScrollService } from '../../../../shared/scroll.service';

interface FooterColumn {
  title: string;
  links: { label: string; target?: string; route?: string }[];
}

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
})
export class FooterComponent {
  private readonly scroll = inject(ScrollService);

  readonly year = new Date().getFullYear();

  readonly columns: FooterColumn[] = [
    {
      title: 'Produit',
      links: [
        { label: 'Fonctionnalités', target: 'fonctionnalites' },
        { label: 'Fonctionnement', target: 'fonctionnement' },
        { label: 'Sécurité', target: 'securite' },
        { label: 'Cas d\'usage', target: 'cas-usage' },
      ],
    },
    {
      title: 'Application',
      links: [
        { label: 'Se connecter', route: '/auth/login' },
        { label: 'Tableau de bord', route: '/dashboard' },
      ],
    },
    {
      title: 'Projet',
      links: [
        { label: 'Technologies', target: 'technologies' },
        { label: 'Architecture', target: 'technologies' },
        { label: 'À propos du projet', target: 'probleme' },
      ],
    },
  ];

  navigate(target: string): void {
    this.scroll.scrollTo(target);
  }
}
