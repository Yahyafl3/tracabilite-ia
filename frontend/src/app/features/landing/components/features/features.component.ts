import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

@Component({
  selector: 'app-features',
  standalone: true,
  imports: [IconComponent, RevealDirective, TranslatePipe],
  templateUrl: './features.component.html',
  styleUrl: './features.component.scss',
})
export class FeaturesComponent {
  readonly features = [
    { icon: 'bar-chart', accent: 'indigo', titleKey: 'landing.features.items.ml.title', textKey: 'landing.features.items.ml.text' },
    { icon: 'lightbulb', accent: 'violet', titleKey: 'landing.features.items.shap.title', textKey: 'landing.features.items.shap.text' },
    { icon: 'activity', accent: 'blue', titleKey: 'landing.features.items.agents.title', textKey: 'landing.features.items.agents.text' },
    { icon: 'users', accent: 'teal', titleKey: 'landing.features.items.consensus.title', textKey: 'landing.features.items.consensus.text' },
    { icon: 'file-check', accent: 'amber', titleKey: 'landing.features.items.human.title', textKey: 'landing.features.items.human.text' },
    { icon: 'lock', accent: 'rose', titleKey: 'landing.features.items.audit.title', textKey: 'landing.features.items.audit.text' },
  ];
}
