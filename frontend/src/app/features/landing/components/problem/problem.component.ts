import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

@Component({
  selector: 'app-problem',
  standalone: true,
  imports: [IconComponent, RevealDirective, TranslatePipe],
  templateUrl: './problem.component.html',
  styleUrl: './problem.component.scss',
})
export class ProblemComponent {
  readonly problems = [
    { icon: 'eye-off', titleKey: 'landing.problem.items.transparency.title', textKey: 'landing.problem.items.transparency.text' },
    { icon: 'history', titleKey: 'landing.problem.items.audit.title', textKey: 'landing.problem.items.audit.text' },
    { icon: 'users', titleKey: 'landing.problem.items.human.title', textKey: 'landing.problem.items.human.text' },
  ];
}
