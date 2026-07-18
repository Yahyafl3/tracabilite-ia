import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-confidence-display',
  standalone: true,
  imports: [DecimalPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (confidence != null) {
      <span class="ui-confidence">{{ confidence | number:'1.0-1' }}&nbsp;%</span>
    } @else {
      <span class="ui-confidence ui-confidence--empty">Non fournie</span>
    }
  `,
})
export class ConfidenceDisplayComponent {
  @Input() confidence: number | null | undefined;
}
