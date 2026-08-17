import { ChangeDetectionStrategy, Component, Input, inject } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { DecisionHistoryEntry } from '../../core/services/decision-trace.service';
import { historyActionLabel, statutLabel } from '../../core/utils/label.util';
import { TranslationService } from '../../core/i18n/translation.service';

@Component({
  selector: 'app-timeline',
  standalone: true,
  imports: [TranslatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="history-timeline">
      @for (entry of entries; track entry.historyId) {
        <article class="history-item" [class]="itemClass(entry.action)">
          <div class="history-item__head">
            <span class="history-item__action">{{ actionLabel(entry.action) }}</span>
            <span class="history-item__meta">{{ formatDate(entry.createdAt) }}</span>
          </div>
          @if (entry.performedByEmail) {
            <p class="history-item__meta">{{ 'shared.byUser' | translate:{ email: entry.performedByEmail } }}</p>
          }
          @if (entry.previousStatus || entry.newStatus) {
            <p class="history-item__meta">
              {{ entry.previousStatus ? statusLabel(entry.previousStatus) : ('common.dash' | translate) }}
              →
              {{ entry.newStatus ? statusLabel(entry.newStatus) : ('common.dash' | translate) }}
            </p>
          }
          @if (entry.comment) {
            <p class="timeline-item__comment">{{ entry.comment }}</p>
          }
          @if (entry.justification) {
            <p class="timeline-item__comment">{{ entry.justification }}</p>
          }
        </article>
      }
    </div>
  `,
})
export class TimelineComponent {
  private readonly i18n = inject(TranslationService);
  @Input({ required: true }) entries: DecisionHistoryEntry[] = [];

  formatDate(value: string | Date | null | undefined): string {
    if (!value) return '';
    return new Date(value).toLocaleString(this.i18n.localeId(), {
      dateStyle: 'short',
      timeStyle: 'short',
    });
  }

  actionLabel(action: string): string {
    this.i18n.currentLang();
    return historyActionLabel(action);
  }

  statusLabel(status: string): string {
    this.i18n.currentLang();
    return statutLabel(status);
  }

  itemClass(action: string): string {
    if (action.includes('FAILED') || action.includes('REJECTED')) {
      return 'history-item--danger';
    }
    if (
      action.includes('APPROVED') ||
      action.includes('COMPLETED') ||
      action.includes('SUCCESS') ||
      action.includes('VERIFIED')
    ) {
      return 'history-item--success';
    }
    if (action.includes('MODIFIED') || action.includes('REVIEW')) {
      return 'history-item--warning';
    }
    return '';
  }
}
