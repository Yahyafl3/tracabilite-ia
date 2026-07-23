import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { InputText } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';
import { Button } from 'primeng/button';
import { Message } from 'primeng/message';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Card } from 'primeng/card';
import { SupportService } from '../../core/services/support.service';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-support',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    InputText,
    Textarea,
    Button,
    Message,
    ProgressSpinner,
    Card,
  ],
  templateUrl: './support.component.html',
  styleUrl: './support.component.scss',
})
export class SupportComponent {
  private readonly fb = inject(FormBuilder);
  private readonly supportService = inject(SupportService);

  readonly isLoading = signal(false);
  readonly successMessage = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    subject: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
    message: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]],
  });

  onSubmit(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const value = this.form.getRawValue();
    this.supportService
      .submitMessage({
        name: value.name.trim(),
        email: value.email.trim(),
        subject: value.subject.trim(),
        message: value.message.trim(),
      })
      .subscribe({
        next: (res) => {
          this.isLoading.set(false);
          this.successMessage.set(
            res.message ||
              'Votre demande a été envoyée. Notre équipe vous répondra dès que possible.',
          );
          this.form.reset();
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(
            resolveHttpErrorMessage(err, 'Impossible d’envoyer la demande. Réessayez plus tard.'),
          );
        },
      });
  }

  hasError(control: string): boolean {
    const c = this.form.get(control);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  errorText(control: string): string {
    const c = this.form.get(control);
    if (!c?.errors) return '';
    if (c.errors['required']) return 'Ce champ est requis';
    if (c.errors['email']) return 'Adresse email invalide';
    if (c.errors['minlength']) {
      return `Minimum ${c.errors['minlength'].requiredLength} caractères`;
    }
    if (c.errors['maxlength']) {
      return `Maximum ${c.errors['maxlength'].requiredLength} caractères`;
    }
    return 'Valeur invalide';
  }
}
