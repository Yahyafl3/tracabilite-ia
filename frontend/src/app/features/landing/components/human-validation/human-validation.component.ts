import { Component } from '@angular/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

@Component({
  selector: 'app-human-validation',
  standalone: true,
  imports: [IconComponent, RevealDirective],
  templateUrl: './human-validation.component.html',
  styleUrl: './human-validation.component.scss',
})
export class HumanValidationComponent {
  readonly actions = [
    {
      title: 'Approuver',
      description: 'Confirmer la décision proposée après analyse du dossier.',
      icon: 'check-circle',
    },
    {
      title: 'Rejeter',
      description: 'Refuser la demande et enregistrer la décision humaine.',
      icon: 'close',
    },
    {
      title: 'Modifier',
      description: 'Ajuster la décision humaine tout en conservant la trace.',
      icon: 'file-text',
    },
    {
      title: 'Review',
      description: 'Demander un nouvel examen tout en gardant le statut en attente.',
      icon: 'eye',
    },
  ];
}
