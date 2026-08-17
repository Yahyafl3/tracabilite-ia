import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

@Component({
  selector: 'app-how-it-works',
  standalone: true,
  imports: [IconComponent, RevealDirective, TranslatePipe],
  templateUrl: './how-it-works.component.html',
  styleUrl: './how-it-works.component.scss',
})
export class HowItWorksComponent {
  readonly steps = [
    { number: '01', icon: 'file-text', titleKey: 'landing.how.steps.s1.title', textKey: 'landing.how.steps.s1.text' },
    { number: '02', icon: 'scan', titleKey: 'landing.how.steps.s2.title', textKey: 'landing.how.steps.s2.text' },
    { number: '03', icon: 'activity', titleKey: 'landing.how.steps.s3.title', textKey: 'landing.how.steps.s3.text' },
    { number: '04', icon: 'link', titleKey: 'landing.how.steps.s4.title', textKey: 'landing.how.steps.s4.text' },
    { number: '05', icon: 'file-check', titleKey: 'landing.how.steps.s5.title', textKey: 'landing.how.steps.s5.text' },
  ];
}
