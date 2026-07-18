import { ChangeDetectionStrategy, Component, Input, signal } from '@angular/core';

@Component({
  selector: 'app-copy-hash',
  standalone: true,
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
        <span class="copy-hash__feedback" aria-live="polite">Copié</span>
      }
    </button>
  `,
  styleUrl: './copy-hash.component.scss',
})
export class CopyHashComponent {
  @Input({ required: true }) hash!: string;
  @Input() label?: string;
  @Input() truncateLength = 12;

  readonly copied = signal(false);

  get truncated(): string {
    if (!this.hash) return '—';
    if (this.hash.length <= this.truncateLength * 2 + 1) {
      return this.hash;
    }
    return `${this.hash.slice(0, this.truncateLength)}…${this.hash.slice(-this.truncateLength)}`;
  }

  get ariaLabelText(): string {
    const prefix = this.label ? `${this.label} : ` : 'Hash : ';
    return `${prefix}copier ${this.hash}`;
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
