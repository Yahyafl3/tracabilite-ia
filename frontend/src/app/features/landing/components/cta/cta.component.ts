import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

@Component({
  selector: 'app-cta',
  standalone: true,
  imports: [RouterLink, IconComponent, RevealDirective, TranslatePipe],
  templateUrl: './cta.component.html',
  styleUrl: './cta.component.scss',
})
export class CtaComponent {
  readonly highlights = ['landing.cta.h1', 'landing.cta.h2', 'landing.cta.h3'];
}
