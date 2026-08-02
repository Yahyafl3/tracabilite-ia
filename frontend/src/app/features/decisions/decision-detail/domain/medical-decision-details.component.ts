import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Message } from 'primeng/message';
import type { DecisionResponse } from '../../../../core/models/decision.models';
import { DOMAIN_META } from '../../../../core/config/domains/domain.config';

@Component({
  selector: 'app-medical-decision-details',
  standalone: true,
  imports: [CommonModule, Message],
  templateUrl: './medical-decision-details.component.html',
  styleUrl: './medical-decision-details.component.scss',
})
export class MedicalDecisionDetailsComponent {
  @Input({ required: true }) decision!: DecisionResponse;

  readonly warning = DOMAIN_META.MEDICAL.warning!;

  get data() {
    return this.decision.medicalData ?? {};
  }

  text(value: unknown): string {
    if (value == null || value === '') return '—';
    return String(value);
  }
}
