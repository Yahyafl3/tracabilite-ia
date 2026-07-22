import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { InputText } from 'primeng/inputtext';
import { Password } from 'primeng/password';
import { Button } from 'primeng/button';
import { Message } from 'primeng/message';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Checkbox } from 'primeng/checkbox';
import { AuthService } from '../../../core/services/auth.service';
import { LoginCredentials, UserRole } from '../../../core/models/auth.models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    InputText,
    Password,
    Button,
    Message,
    ProgressSpinner,
    Checkbox,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  loginForm: FormGroup;
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  /** UI-only: hide broken video and keep CSS gradient fallback. */
  readonly videoFailed = signal(false);
  returnUrl = '/decisions';

  constructor() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false],
    });

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/decisions';
  }

  onVideoError(): void {
    this.videoFailed.set(true);
  }

  onSubmit(): void {
    this.errorMessage.set(null);

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const credentials: LoginCredentials = this.loginForm.value;

    this.authService.login(credentials).subscribe({
      next: (response) => {
        // Role-based redirect: Auditeur goes directly to /audit
        const role = response.user?.role;
        let defaultUrl = '/decisions';
        if (role === UserRole.AUDITEUR) {
          defaultUrl = '/audit';
        }
        const target = this.route.snapshot.queryParams['returnUrl'] || defaultUrl;
        void this.router.navigate([target]);
      },
      error: (error) => {
        this.isLoading.set(false);
        this.errorMessage.set(error?.message || 'Une erreur est survenue');
      },
      complete: () => {
        this.isLoading.set(false);
      },
    });
  }

  hasError(field: string): boolean {
    const control = this.loginForm.get(field);
    return !!control && control.invalid && (control.dirty || control.touched);
  }

  getErrorMessage(fieldName: string): string {
    const field = this.loginForm.get(fieldName);

    if (field?.hasError('required')) {
      return 'Ce champ est requis';
    }
    if (field?.hasError('email')) {
      return 'Adresse email invalide';
    }
    if (field?.hasError('minlength')) {
      const minLength = field.errors?.['minlength'].requiredLength;
      return `Minimum ${minLength} caractères`;
    }
    return '';
  }
}
