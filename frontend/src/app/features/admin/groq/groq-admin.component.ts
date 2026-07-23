import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Card } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Button } from 'primeng/button';
import { Skeleton } from 'primeng/skeleton';
import { Message } from 'primeng/message';
import { GroqAdminService, type GroqAdminStatus } from '../../../core/services/groq-admin.service';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';

@Component({
  selector: 'app-groq-admin',
  standalone: true,
  imports: [CommonModule, Card, TableModule, Tag, Button, Skeleton, Message],
  templateUrl: './groq-admin.component.html',
  styleUrl: './groq-admin.component.scss',
})
export class GroqAdminComponent {
  private readonly groqAdminService = inject(GroqAdminService);

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
        this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger le statut Groq.'));
        this.loading.set(false);
      },
    });
  }

  keyAvailabilityLabel(status: GroqAdminStatus): string {
    return status.configured ? 'Clé API configurée (valeur masquée)' : 'Clé API absente';
  }

  globalStateLabel(status: GroqAdminStatus): string {
    if (!status.configured) {
      return 'Non configuré';
    }
    return status.reachable ? 'Configuré et joignable' : 'Configuré (hors ligne)';
  }

  globalSeverity(status: GroqAdminStatus): 'success' | 'warn' | 'danger' | 'secondary' {
    if (!status.configured) return 'secondary';
    return status.reachable ? 'success' : 'warn';
  }
}
