import { ChangeDetectionStrategy, Component, Input, inject, signal } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { TranslationService } from '../../core/i18n/translation.service';

@Component({
  selector: 'app-copy-hash',
  standalone: true,
  imports: [TranslatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button
      type="button"
      class="copy-hash"
      [attr.aria-label]="ariaLabelText"
      [title]="hash"
      (click)="copy()"
    >
      @if (label) {
        <span class="copy-hash__label">{{ label }}</span>
      }
      <code class="copy-hash__value">{{ truncated }}</code>
      @if (copied()) {
        <span class="copy-hash__feedback" aria-live="polite">{{ 'shared.copied' | translate }}</span>
      }
    </button>
  `,
  styleUrl: './copy-hash.component.scss',
})
export class CopyHashComponent {
  private readonly i18n = inject(TranslationService);
  @Input({ required: true }) hash!: string;
  @Input() label?: string;
  @Input() truncateLength = 12;

  readonly copied = signal(false);

  get truncated(): string {
    if (!this.hash) return this.i18n.t('common.dash');
    if (this.hash.length <= this.truncateLength * 2 + 1) {
      return this.hash;
    }
    return `${this.hash.slice(0, this.truncateLength)}…${this.hash.slice(-this.truncateLength)}`;
  }

  get ariaLabelText(): string {
    this.i18n.currentLang();
    const prefix = this.label ? `${this.label} : ` : `${this.i18n.t('shared.copyHash')} `;
    return `${prefix}${this.hash}`;
  }

  async copy(): Promise<void> {
    if (!this.hash) return;

    try {
      await navigator.clipboard.writeText(this.hash);
      this.copied.set(true);
      window.setTimeout(() => this.copied.set(false), 2000);
    } catch {
      // Fallback silencieux si le presse-papiers est indisponible
    }
  }
}
