import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { IconComponent } from '../../shared/icon.component';
import { ValidationService } from '../../core/services/validation.service';
import { DecisionResponse, StatutDecisionEnum } from '../../core/models/decision.models';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-validation-queue',
  standalone: true,
  imports: [CommonModule, RouterModule, IconComponent],
  templateUrl: './validation-queue.component.html',
  styleUrl: './validation-queue.component.scss',
})
export class ValidationQueueComponent {
  private readonly validationService = inject(ValidationService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly decisions = signal<DecisionResponse[]>([]);
  readonly totalElements = signal(0);

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

  statutChipClass(statut: StatutDecisionEnum): string {
    const map: Record<StatutDecisionEnum, string> = {
      [StatutDecisionEnum.APPROUVEE]: 'chip--approved',
      [StatutDecisionEnum.MODIFIEE]: 'chip--modified',
      [StatutDecisionEnum.REJETEE]: 'chip--rejected',
      [StatutDecisionEnum.EN_ATTENTE]: 'chip--pending',
      [StatutDecisionEnum.BROUILLON]: 'chip--pending',
    };
    return map[statut] ?? 'chip--pending';
  }

  decisionChipClass(decision?: string): string {
    if (decision === 'APPROUVER') return 'chip--approved';
    if (decision === 'REJETER') return 'chip--rejected';
    return 'chip--pending';
  }
}
