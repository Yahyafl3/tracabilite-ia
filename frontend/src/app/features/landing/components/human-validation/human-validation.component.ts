import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

@Component({
  selector: 'app-human-validation',
  standalone: true,
  imports: [IconComponent, RevealDirective, TranslatePipe],
  templateUrl: './human-validation.component.html',
  styleUrl: './human-validation.component.scss',
})
export class HumanValidationComponent {
  readonly actions = [
    { icon: 'check-circle', titleKey: 'landing.human.approve.title', textKey: 'landing.human.approve.text' },
    { icon: 'close', titleKey: 'landing.human.reject.title', textKey: 'landing.human.reject.text' },
    { icon: 'file-text', titleKey: 'landing.human.modify.title', textKey: 'landing.human.modify.text' },
    { icon: 'eye', titleKey: 'landing.human.review.title', textKey: 'landing.human.review.text' },
  ];
}
