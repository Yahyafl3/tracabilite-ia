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
      title: 'Des décisions IA opaques',
      description:
        "Vos modèles produisent des résultats sans justification lisible. Impossible d'expliquer à un client, un régulateur ou un juge pourquoi une décision a été prise.",
    },
    {
      icon: 'shield-alert',
      title: 'Un risque de non-conformité',
      description:
        "AI Act, RGPD, sectoriels… les obligations se multiplient. Sans traçabilité, chaque décision automatisée devient une exposition juridique et financière.",
    },
    {
      icon: 'history',
      title: "L'absence d'audit trail",
      description:
        "Aucune preuve fiable de qui a décidé quoi, quand et sur quelles données. Reconstituer l'historique lors d'un contrôle devient un cauchemar coûteux.",
    },
  ];
}
