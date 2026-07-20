import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Card } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { Message } from 'primeng/message';
import { Skeleton } from 'primeng/skeleton';
import { Divider } from 'primeng/divider';
import { ConfirmationService } from 'primeng/api';
import { ValidationService } from '../../core/services/validation.service';
import { DecisionResponse } from '../../core/models/decision.models';
import { consensusLabel, mlConfidence, mlDecision } from '../../core/models/decision.models';
import { decisionLabel } from '../../core/utils/label.util';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';
import { ConfidenceDisplayComponent } from '../../shared/ui';

@Component({
  selector: 'app-validation-queue',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    Card,
    TableModule,
    Tag,
    Button,
    Dialog,
    ConfirmDialog,
    Message,
    Skeleton,
    Divider,
    ConfidenceDisplayComponent,
  ],
  providers: [ConfirmationService],
  templateUrl: './validation-queue.component.html',
  styleUrl: './validation-queue.component.scss',
})
export class ValidationQueueComponent {
  private readonly validationService = inject(ValidationService);
  private readonly confirmation = inject(ConfirmationService);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly decisions = signal<DecisionResponse[]>([]);
  readonly totalElements = signal(0);
  readonly summaryVisible = signal(false);
  readonly selected = signal<DecisionResponse | null>(null);
  readonly navigating = signal(false);

  constructor() {
    this.loadPending();
  }

  loadPending(): void {
    this.loading.set(true);
    this.error.set(null);
    this.validationService.getPending(0, 20).subscribe({
      next: (page) => {
        this.decisions.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger la file de validation.'));
        this.loading.set(false);
      },
    });
  }

  openSummary(row: DecisionResponse): void {
    this.selected.set(row);
    this.summaryVisible.set(true);
  }

  closeSummary(): void {
    this.summaryVisible.set(false);
    this.selected.set(null);
  }

  confirmOpenDossier(): void {
    const row = this.selected();
    if (!row) return;

    this.confirmation.confirm({
      header: 'Ouvrir le dossier de validation',
      message:
        'La validation humaine porte sur le dossier global (ML, SHAP, agents IA, consensus, sources, intégrité), jamais sur un agent isolé. Continuer ?',
      icon: 'pi pi-exclamation-circle',
      acceptLabel: 'Ouvrir le dossier',
      rejectLabel: 'Annuler',
      acceptButtonStyleClass: 'p-button-primary',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.navigating.set(true);
        this.summaryVisible.set(false);
        void this.router.navigate(['/decisions', row.decisionId]).finally(() => {
          this.navigating.set(false);
        });
      },
    });
  }

  decisionLabel = decisionLabel;
  mlDecision = mlDecision;
  mlConfidence = mlConfidence;
  consensusLabel = consensusLabel;

  mlSeverity(value: string | undefined): 'success' | 'danger' | 'secondary' | 'warn' {
    if (value === 'APPROUVER' || value === 'APPROUVEE') return 'success';
    if (value === 'REJETER' || value === 'REJETEE') return 'danger';
    if (value === 'REVIEW' || value === 'MODIFIEE') return 'warn';
    return 'secondary';
  }

  riskSeverity(risk: string | undefined): 'success' | 'warn' | 'danger' | 'secondary' {
    if (risk === 'LOW') return 'success';
    if (risk === 'MEDIUM') return 'warn';
    if (risk === 'HIGH') return 'danger';
    return 'secondary';
  }
}
