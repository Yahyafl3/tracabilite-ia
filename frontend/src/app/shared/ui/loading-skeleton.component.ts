import { ChangeDetectionStrategy, Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslationService } from '../../core/i18n/translation.service';

@Component({
  selector: 'app-loading-skeleton',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="ui-skeleton" aria-busy="true" [attr.aria-label]="resolvedLabel">
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
  private readonly i18n = inject(TranslationService);
  @Input() lines = 3;
  @Input() label?: string;

  get resolvedLabel(): string {
    this.i18n.currentLang();
    return this.label ?? this.i18n.t('shared.loadingAria');
  }

  get lineArray(): number[] {
    return Array.from({ length: this.lines }, (_, i) => i);
  }

  lineWidth(index: number): number {
    const widths = [100, 92, 78, 85, 65];
    return widths[index % widths.length];
  }
}
