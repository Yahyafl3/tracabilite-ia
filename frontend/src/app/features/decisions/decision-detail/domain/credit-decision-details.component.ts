import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import type { DecisionResponse } from '../../../../core/models/decision.models';

@Component({
  selector: 'app-credit-decision-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './credit-decision-details.component.html',
  styleUrl: './credit-decision-details.component.scss',
})
export class CreditDecisionDetailsComponent {
  @Input({ required: true }) decision!: DecisionResponse;

  get data() {
    return this.decision.creditData ?? {};
  }

  mad(value: number | undefined | null): string {
    if (value == null) return '—';
    return `${Number(value).toLocaleString('fr-FR')} MAD`;
  }

  pct(value: number | undefined | null): string {
    if (value == null) return '—';
    return `${(Number(value) * 100).toLocaleString('fr-FR', { maximumFractionDigits: 1 })} %`;
  }

  text(value: unknown): string {
    if (value == null || value === '') return '—';
    return String(value);
  }
}
