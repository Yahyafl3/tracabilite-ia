import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

@Component({
  selector: 'app-security',
  standalone: true,
  imports: [IconComponent, RevealDirective, TranslatePipe],
  templateUrl: './security.component.html',
  styleUrl: './security.component.scss',
})
export class SecurityComponent {
  readonly badges = [
    { icon: 'key', titleKey: 'landing.security.jwt.title', textKey: 'landing.security.jwt.text' },
    { icon: 'users', titleKey: 'landing.security.roles.title', textKey: 'landing.security.roles.text' },
    { icon: 'history', titleKey: 'landing.security.history.title', textKey: 'landing.security.history.text' },
    { icon: 'lock', titleKey: 'landing.security.hash.title', textKey: 'landing.security.hash.text' },
  ];
}
