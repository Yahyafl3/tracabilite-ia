import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

@Component({
  selector: 'app-cta',
  standalone: true,
  imports: [RouterLink, IconComponent, RevealDirective],
  templateUrl: './cta.component.html',
  styleUrl: './cta.component.scss',
})
export class CtaComponent {
  readonly highlights = [
    'Dossier ML + SHAP + agents + consensus',
    'Validation humaine finale',
    'Historique et intégrité SHA-256',
  ];
}
