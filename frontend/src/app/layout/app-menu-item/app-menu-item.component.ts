import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Ripple } from 'primeng/ripple';

export interface AppMenuItem {
  label: string;
  icon?: string;
  routerLink?: string;
  items?: AppMenuItem[];
  exact?: boolean;
}

@Component({
  selector: '[app-menu-item]',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, Ripple],
  templateUrl: './app-menu-item.component.html',
  styleUrl: './app-menu-item.component.scss',
})
export class AppMenuItemComponent {
  @Input({ required: true }) item!: AppMenuItem;
}
