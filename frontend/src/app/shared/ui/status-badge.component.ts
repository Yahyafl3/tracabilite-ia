import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="status-chip" [class]="chipClass">{{ label }}</span>`,
})
export class StatusBadgeComponent {
  @Input({ required: true }) label!: string;
  @Input() chipClass = 'chip--pending';
}
