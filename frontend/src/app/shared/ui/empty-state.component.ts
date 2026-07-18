import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { IconComponent } from '../icon.component';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="ui-empty-state">
      <app-icon [name]="icon" [size]="32" />
      <p class="ui-empty-state__message">{{ message }}</p>
      <ng-content />
    </div>
  `,
})
export class EmptyStateComponent {
  @Input() icon = 'file-text';
  @Input({ required: true }) message!: string;
}
