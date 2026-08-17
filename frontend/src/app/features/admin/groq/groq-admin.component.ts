import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Card } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Button } from 'primeng/button';
import { Skeleton } from 'primeng/skeleton';
import { Message } from 'primeng/message';
import { TranslatePipe } from '@ngx-translate/core';
import { GroqAdminService, type GroqAdminStatus } from '../../../core/services/groq-admin.service';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';
import { TranslationService } from '../../../core/i18n/translation.service';

@Component({
  selector: 'app-groq-admin',
  standalone: true,
  imports: [CommonModule, Card, TableModule, Tag, Button, Skeleton, Message, TranslatePipe],
  templateUrl: './groq-admin.component.html',
  styleUrl: './groq-admin.component.scss',
})
export class GroqAdminComponent {
  private readonly groqAdminService = inject(GroqAdminService);
  private readonly i18n = inject(TranslationService);

  readonly status = signal<GroqAdminStatus | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  /** Provider affiché pour les agents actifs (valeur API fixe GROQ, sans clé). */
  readonly providerLabel = 'GROQ';

  constructor() {
    this.loadStatus();
  }

  loadStatus(): void {
    this.loading.set(true);
    this.error.set(null);
    this.groqAdminService.getStatus().subscribe({
      next: (status) => {
        this.status.set(status);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, this.i18n.t('admin.groq.loadError')));
        this.loading.set(false);
      },
    });
  }

  keyAvailabilityLabel(status: GroqAdminStatus): string {
    this.i18n.currentLang();
    return status.configured
      ? this.i18n.t('admin.groq.keyOk')
      : this.i18n.t('admin.groq.keyMissing');
  }

  globalStateLabel(status: GroqAdminStatus): string {
    this.i18n.currentLang();
    if (!status.configured) {
      return this.i18n.t('admin.groq.notConfigured');
    }
    return status.reachable ? this.i18n.t('admin.groq.reachable') : this.i18n.t('admin.groq.offline');
  }

  globalSeverity(status: GroqAdminStatus): 'success' | 'warn' | 'danger' | 'secondary' {
    if (!status.configured) return 'secondary';
    return status.reachable ? 'success' : 'warn';
  }
}
