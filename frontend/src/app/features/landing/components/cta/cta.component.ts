import { Component, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { IconComponent } from '../../../../shared/icon.component';
import { RevealDirective } from '../../../../shared/reveal.directive';

@Component({
  selector: 'app-cta',
  standalone: true,
  imports: [ReactiveFormsModule, IconComponent, RevealDirective],
  templateUrl: './cta.component.html',
  styleUrl: './cta.component.scss',
})
export class CtaComponent {
  private readonly fb = new FormBuilder();

  readonly submitted = signal(false);

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    company: ['', [Validators.required]],
    role: [''],
    message: [''],
  });

  readonly highlights = [
    'Démo personnalisée en 30 minutes',
    'Analyse de votre cas de conformité',
    'Sans engagement',
  ];

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    // Aucune intégration backend ici : confirmation côté client uniquement.
    this.submitted.set(true);
    this.form.reset();
  }

  invalid(control: string): boolean {
    const c = this.form.get(control);
    return !!c && c.invalid && (c.touched || c.dirty);
  }
}
