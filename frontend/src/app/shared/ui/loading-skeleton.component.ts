import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading-skeleton',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="ui-skeleton" aria-busy="true" [attr.aria-label]="label ?? 'Chargement en cours'">
      @if (label) {
        <p class="ui-skeleton__label">{{ label }}</p>
      }
      @for (line of lineArray; track $index) {
        <div class="ui-skeleton__line" [style.width.%]="lineWidth($index)"></div>
      }
    </div>
  `,
})
export class LoadingSkeletonComponent {
  @Input() lines = 3;
  @Input() label?: string;

  get lineArray(): number[] {
    return Array.from({ length: this.lines }, (_, i) => i);
  }

  lineWidth(index: number): number {
    const widths = [100, 92, 78, 85, 65];
    return widths[index % widths.length];
  }
}
