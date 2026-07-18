import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { IconComponent } from '../icon.component';

@Component({
  selector: 'app-error-state',
  standalone: true,
  imports: [IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="ui-error-state" role="alert">
      <app-icon name="shield-alert" [size]="20" />
      <p>{{ message }}</p>
    </div>
  `,
})
export class ErrorStateComponent {
  @Input({ required: true }) message!: string;
}
