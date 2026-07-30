import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Message } from 'primeng/message';
import type { DecisionResponse } from '../../../../core/models/decision.models';
import { DOMAIN_META } from '../../../../core/config/domains/domain.config';

@Component({
  selector: 'app-education-decision-details',
  standalone: true,
  imports: [CommonModule, Message],
  templateUrl: './education-decision-details.component.html',
  styleUrl: './education-decision-details.component.scss',
})
export class EducationDecisionDetailsComponent {
  @Input({ required: true }) decision!: DecisionResponse;

  readonly warning = DOMAIN_META.EDUCATION.warning!;

  get data() {
    return this.decision.educationData ?? {};
  }

  text(value: unknown): string {
    if (value == null || value === '') return '—';
    return String(value);
  }
}
