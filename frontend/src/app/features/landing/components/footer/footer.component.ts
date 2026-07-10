import { Component, inject } from '@angular/core';
import { IconComponent } from '../../../../shared/icon.component';
import { ScrollService } from '../../../../shared/scroll.service';

interface FooterColumn {
  title: string;
  links: { label: string; target?: string; href?: string }[];
}

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [IconComponent],
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
        { label: 'Comment ça marche', target: 'fonctionnement' },
        { label: 'Sécurité', target: 'securite' },
        { label: 'Tarifs', target: 'tarifs' },
      ],
    },
    {
      title: 'Ressources',
      links: [
        { label: 'Documentation', target: 'fonctionnement' },
        { label: 'Cas d\'usage', target: 'cas-usage' },
        { label: 'Conformité AI Act', target: 'securite' },
        { label: 'Statut de service', href: '#' },
      ],
    },
    {
      title: 'Entreprise',
      links: [
        { label: 'À propos', href: '#' },
        { label: 'Contact', target: 'contact' },
        { label: 'Carrières', href: '#' },
        { label: 'Blog', href: '#' },
      ],
    },
  ];

  readonly legalLinks = [
    'Mentions légales',
    'Politique de confidentialité',
    'CGU',
    'Cookies',
  ];

  readonly socials = [
    { icon: 'linkedin', label: 'LinkedIn' },
    { icon: 'twitter', label: 'X (Twitter)' },
    { icon: 'github', label: 'GitHub' },
  ];

  navigate(link: { target?: string; href?: string }): void {
    if (link.target) {
      this.scroll.scrollTo(link.target);
    }
  }
}
