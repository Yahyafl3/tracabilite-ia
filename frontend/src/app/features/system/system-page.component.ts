import { Component, computed, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { Card } from 'primeng/card';
import { Button } from 'primeng/button';
import { Tag } from 'primeng/tag';
import { TranslationService } from '../../core/i18n/translation.service';
import { LanguageSwitcherComponent } from '../../layout/language-switcher/language-switcher.component';

export type SystemPageCode = '403' | '404' | '500';

@Component({
  selector: 'app-system-page',
  standalone: true,
  imports: [CommonModule, RouterLink, Card, Button, Tag, TranslatePipe, LanguageSwitcherComponent],
  templateUrl: './system-page.component.html',
  styleUrl: './system-page.component.scss',
})
export class SystemPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly location = inject(Location);
  private readonly i18n = inject(TranslationService);

  readonly code = (this.route.snapshot.data['code'] as SystemPageCode) ?? '404';
  readonly severity =
    (this.route.snapshot.data['severity'] as 'danger' | 'warn' | 'info' | 'secondary') ?? 'secondary';

  readonly title = computed(() => {
    this.i18n.currentLang();
    return this.i18n.t(`errors.${this.code}.title`);
  });

  readonly message = computed(() => {
    this.i18n.currentLang();
    return this.i18n.t(`errors.${this.code}.message`);
  });

  goBack(): void {
    this.location.back();
  }
}
