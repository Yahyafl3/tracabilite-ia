import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { IconComponent } from '../../shared/icon.component';
import {
  PageHeaderComponent,
  StatusBadgeComponent,
  EmptyStateComponent,
  ErrorStateComponent,
  LoadingSkeletonComponent,
} from '../../shared/ui';
import { ValidationService } from '../../core/services/validation.service';
import { DecisionResponse } from '../../core/models/decision.models';
import { decisionChipClass } from '../../core/utils/chip-class.util';
import { decisionLabel } from '../../core/utils/label.util';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-validation-queue',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    IconComponent,
    PageHeaderComponent,
    StatusBadgeComponent,
    EmptyStateComponent,
    ErrorStateComponent,
    LoadingSkeletonComponent,
  ],
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

  decisionChipClass = decisionChipClass;
  decisionLabel = decisionLabel;
}
