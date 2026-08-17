import { Component, Input, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Message } from 'primeng/message';
import { TranslatePipe } from '@ngx-translate/core';
import type { DecisionResponse } from '../../../../core/models/decision.models';
import { TranslationService } from '../../../../core/i18n/translation.service';

@Component({
  selector: 'app-education-decision-details',
  standalone: true,
  imports: [CommonModule, Message, TranslatePipe],
  templateUrl: './education-decision-details.component.html',
  styleUrl: './education-decision-details.component.scss',
})
export class EducationDecisionDetailsComponent {
  private readonly i18n = inject(TranslationService);
  @Input({ required: true }) decision!: DecisionResponse;

  readonly warning = computed(() => {
    this.i18n.currentLang();
    return this.i18n.t('domainMeta.EDUCATION.warning');
  });

  get data() {
    return this.decision.educationData ?? {};
  }

  text(value: unknown): string {
    if (value == null || value === '') return this.i18n.t('common.dash');
    return String(value);
  }
}
