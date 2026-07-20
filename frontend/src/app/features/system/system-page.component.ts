import { Component, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Card } from 'primeng/card';
import { Button } from 'primeng/button';
import { Tag } from 'primeng/tag';

export type SystemPageCode = '403' | '404' | '500';

@Component({
  selector: 'app-system-page',
  standalone: true,
  imports: [CommonModule, RouterLink, Card, Button, Tag],
  templateUrl: './system-page.component.html',
  styleUrl: './system-page.component.scss',
})
export class SystemPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly location = inject(Location);

  readonly code = (this.route.snapshot.data['code'] as SystemPageCode) ?? '404';
  readonly title = (this.route.snapshot.data['title'] as string) ?? 'Page introuvable';
  readonly message =
    (this.route.snapshot.data['message'] as string) ??
    'La ressource demandée est introuvable.';
  readonly severity =
    (this.route.snapshot.data['severity'] as 'danger' | 'warn' | 'info' | 'secondary') ?? 'secondary';

  goBack(): void {
    this.location.back();
  }
}
