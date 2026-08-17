import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { Password } from 'primeng/password';
import { Button } from 'primeng/button';
import { Message } from 'primeng/message';
import { ProgressSpinner } from 'primeng/progressspinner';
import { AuthService } from '../../../core/services/auth.service';
import { TranslationService } from '../../../core/i18n/translation.service';
import { LanguageSwitcherComponent } from '../../../layout/language-switcher/language-switcher.component';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    TranslatePipe,
    Password,
    Button,
    Message,
    ProgressSpinner,
    LanguageSwitcherComponent,
  ],
  templateUrl: './reset-password.component.html',
  styleUrl: '../login/login.component.scss',
})
export class ResetPasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly i18n = inject(TranslationService);

  readonly isLoading = signal(false);
  readonly successMessage = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly tokenMissing = signal(false);

  private readonly token = this.route.snapshot.queryParamMap.get('token')?.trim() ?? '';

  readonly form = this.fb.nonNullable.group({
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]],
  });

  constructor() {
    if (!this.token) {
      this.tokenMissing.set(true);
      this.errorMessage.set(this.i18n.t('resetPassword.invalidLink'));
    }
  }

  onSubmit(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.tokenMissing()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { newPassword, confirmPassword } = this.form.getRawValue();
    if (newPassword !== confirmPassword) {
      this.errorMessage.set(this.i18n.t('resetPassword.mismatch'));
      return;
    }

    this.isLoading.set(true);
    this.authService
      .resetPassword({
        token: this.token,
        newPassword,
        confirmPassword,
      })
      .subscribe({
        next: (res) => {
          this.isLoading.set(false);
          this.successMessage.set(res.message || this.i18n.t('resetPassword.success'));
          this.form.reset();
          setTimeout(() => void this.router.navigate(['/auth/login']), 1800);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(err?.message || this.i18n.t('resetPassword.expired'));
        },
      });
  }

  hasError(control: string): boolean {
    const c = this.form.get(control);
    return !!c && c.invalid && (c.touched || c.dirty);
  }
}
