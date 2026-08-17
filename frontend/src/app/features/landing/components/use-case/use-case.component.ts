import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

@Component({
  selector: 'app-use-case',
  standalone: true,
  imports: [IconComponent, RevealDirective, TranslatePipe],
  templateUrl: './use-case.component.html',
  styleUrl: './use-case.component.scss',
})
export class UseCaseComponent {
  readonly cards = [
    { icon: 'bar-chart', titleKey: 'landing.useCase.risk.title', textKey: 'landing.useCase.risk.text' },
    { icon: 'lightbulb', titleKey: 'landing.useCase.explain.title', textKey: 'landing.useCase.explain.text' },
    { icon: 'shield-check', titleKey: 'landing.useCase.audit.title', textKey: 'landing.useCase.audit.text' },
  ];
}
