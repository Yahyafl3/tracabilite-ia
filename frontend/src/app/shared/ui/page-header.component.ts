import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="ui-page-header">
      @if (breadcrumbs.length) {
        <nav class="ui-page-header__breadcrumb" aria-label="Fil d'Ariane">
          @for (crumb of breadcrumbs; track $index; let last = $last) {
            @if (!last) {
              <span class="ui-page-header__crumb">{{ crumb }}</span>
              <span class="ui-page-header__sep" aria-hidden="true">/</span>
            } @else {
              <strong class="ui-page-header__crumb ui-page-header__crumb--current">{{ crumb }}</strong>
            }
          }
        </nav>
      }
      <div class="ui-page-header__row">
        <div class="ui-page-header__content">
          <h1 class="ui-page-header__title">{{ title }}</h1>
          @if (subtitle) {
            <p class="ui-page-header__subtitle">{{ subtitle }}</p>
          }
        </div>
        <div class="ui-page-header__actions">
          <ng-content />
        </div>
      </div>
    </header>
  `,
})
export class PageHeaderComponent {
  @Input() breadcrumbs: string[] = [];
  @Input({ required: true }) title!: string;
  @Input() subtitle?: string;
}
