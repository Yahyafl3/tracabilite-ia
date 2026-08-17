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
import { TranslatePipe } from '@ngx-translate/core';
import { SupportService } from '../../core/services/support.service';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';
import { TranslationService } from '../../core/i18n/translation.service';
import { LanguageSwitcherComponent } from '../../layout/language-switcher/language-switcher.component';

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
    TranslatePipe,
    LanguageSwitcherComponent,
  ],
  templateUrl: './support.component.html',
  styleUrl: './support.component.scss',
})
export class SupportComponent {
  private readonly fb = inject(FormBuilder);
  private readonly supportService = inject(SupportService);
  private readonly i18n = inject(TranslationService);

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
          this.successMessage.set(res.message || this.i18n.t('support.success'));
          this.form.reset();
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(
            resolveHttpErrorMessage(err, this.i18n.t('support.error')),
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
    if (c.errors['required']) return this.i18n.t('login.required');
    if (c.errors['email']) return this.i18n.t('login.invalidEmail');
    if (c.errors['minlength']) {
      return this.i18n.t('login.minLength', { min: c.errors['minlength'].requiredLength });
    }
    if (c.errors['maxlength']) {
      return this.i18n.t('support.maxLength', { max: c.errors['maxlength'].requiredLength });
    }
    return this.i18n.t('support.invalid');
  }
}
