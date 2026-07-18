import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { DecisionHistoryEntry } from '../../core/services/decision-trace.service';
import { historyActionLabel, statutLabel } from '../../core/utils/label.util';

@Component({
  selector: 'app-timeline',
  standalone: true,
  imports: [DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="history-timeline">
      @for (entry of entries; track entry.historyId) {
        <article class="history-item" [class]="itemClass(entry.action)">
          <div class="history-item__head">
            <span class="history-item__action">{{ historyActionLabel(entry.action) }}</span>
            <span class="history-item__meta">{{ entry.createdAt | date:'short' }}</span>
          </div>
          @if (entry.performedByEmail) {
            <p class="history-item__meta">Par {{ entry.performedByEmail }}</p>
          }
          @if (entry.previousStatus || entry.newStatus) {
            <p class="history-item__meta">
              {{ entry.previousStatus ? statutLabel(entry.previousStatus) : '—' }}
              →
              {{ entry.newStatus ? statutLabel(entry.newStatus) : '—' }}
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
  @Input({ required: true }) entries: DecisionHistoryEntry[] = [];

  historyActionLabel = historyActionLabel;
  statutLabel = statutLabel;

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
