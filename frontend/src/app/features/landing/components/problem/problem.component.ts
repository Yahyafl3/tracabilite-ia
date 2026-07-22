import { Component } from '@angular/core';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

interface ProblemItem {
  icon: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-problem',
  standalone: true,
  imports: [IconComponent, RevealDirective],
  templateUrl: './problem.component.html',
  styleUrl: './problem.component.scss',
})
export class ProblemComponent {
  readonly problems: ProblemItem[] = [
    {
      icon: 'eye-off',
      title: 'Manque de transparence',
      description:
        'Les utilisateurs ne savent pas toujours pourquoi un modèle propose une décision.',
    },
    {
      icon: 'history',
      title: "Difficulté d'audit",
      description:
        "Sans historique et sans intégrité, il devient difficile de reconstruire le dossier d'une décision.",
    },
    {
      icon: 'users',
      title: 'Responsabilité humaine',
      description:
        "L'intelligence artificielle doit assister la décision sans remplacer le contrôle humain.",
    },
  ];
}
