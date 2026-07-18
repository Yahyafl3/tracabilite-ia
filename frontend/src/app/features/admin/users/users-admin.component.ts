import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { IconComponent } from '../../../shared/icon.component';
import {
  PageHeaderComponent,
  StatusBadgeComponent,
  EmptyStateComponent,
  ErrorStateComponent,
  LoadingSkeletonComponent,
} from '../../../shared/ui';
import { AuthService } from '../../../core/services/auth.service';
import {
  CreateManagedUserRequest,
  MANAGED_USER_ROLES,
  ManagedUser,
  UpdateManagedUserRequest,
  UserAdminService,
} from '../../../core/services/user-admin.service';
import { UserRole } from '../../../core/models/auth.models';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';
import { roleChipClass, roleLabel } from '../../../core/utils/label.util';

type FormMode = 'create' | 'edit';

@Component({
  selector: 'app-users-admin',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    IconComponent,
    PageHeaderComponent,
    StatusBadgeComponent,
    EmptyStateComponent,
    ErrorStateComponent,
    LoadingSkeletonComponent,
  ],
  templateUrl: './users-admin.component.html',
  styleUrl: './users-admin.component.scss',
})
export class UsersAdminComponent {
  private readonly userAdminService = inject(UserAdminService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly roles = MANAGED_USER_ROLES;
  readonly users = signal<ManagedUser[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly formOpen = signal(false);
  readonly formMode = signal<FormMode>('create');
  readonly editingUserId = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.minLength(6)]],
    role: [UserRole.VALIDATEUR, Validators.required],
  });

  constructor() {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.error.set(null);
    this.userAdminService.list().subscribe({
      next: (items) => {
        this.users.set(items);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger les utilisateurs.'));
        this.loading.set(false);
      },
    });
  }

  openCreateForm(): void {
    this.formMode.set('create');
    this.editingUserId.set(null);
    this.form.reset({
      nom: '',
      email: '',
      motDePasse: '',
      role: UserRole.VALIDATEUR,
    });
    this.form.controls.motDePasse.setValidators([Validators.required, Validators.minLength(6)]);
    this.form.controls.motDePasse.updateValueAndValidity();
    this.success.set(null);
    this.formOpen.set(true);
  }

  openEditForm(user: ManagedUser): void {
    if (!this.isManaged(user)) {
      this.error.set('Seuls les comptes Administrateur, Validateur et Auditeur sont modifiables ici.');
      return;
    }
    this.formMode.set('edit');
    this.editingUserId.set(user.id);
    this.form.reset({
      nom: user.nom,
      email: user.email,
      motDePasse: '',
      role: user.role,
    });
    this.form.controls.motDePasse.setValidators([Validators.minLength(6)]);
    this.form.controls.motDePasse.updateValueAndValidity();
    this.success.set(null);
    this.formOpen.set(true);
  }

  closeForm(): void {
    this.formOpen.set(false);
    this.editingUserId.set(null);
  }

  submitForm(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    const value = this.form.getRawValue();
    if (this.formMode() === 'create') {
      const request: CreateManagedUserRequest = {
        nom: value.nom,
        email: value.email,
        motDePasse: value.motDePasse,
        role: value.role,
      };
      this.userAdminService.create(request).subscribe({
        next: () => this.onSaveSuccess('Utilisateur créé avec succès.'),
        error: (err) => this.onSaveError(err),
      });
      return;
    }

    const id = this.editingUserId();
    if (!id) return;

    const request: UpdateManagedUserRequest = {
      nom: value.nom,
      email: value.email,
      role: value.role,
    };
    if (value.motDePasse.trim()) {
      request.motDePasse = value.motDePasse;
    }

    this.userAdminService.update(id, request).subscribe({
      next: () => this.onSaveSuccess('Utilisateur mis à jour avec succès.'),
      error: (err) => this.onSaveError(err),
    });
  }

  deleteUser(user: ManagedUser): void {
    if (!this.isManaged(user)) {
      this.error.set('Seuls les comptes Administrateur, Validateur et Auditeur sont supprimables ici.');
      return;
    }
    if (user.id === this.authService.currentUser?.id) {
      this.error.set('Impossible de supprimer votre propre compte.');
      return;
    }
    if (!confirm(`Supprimer le compte ${user.email} ?`)) {
      return;
    }

    this.error.set(null);
    this.success.set(null);
    this.userAdminService.delete(user.id).subscribe({
      next: () => {
        this.success.set('Utilisateur supprimé.');
        this.loadUsers();
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, 'Suppression impossible.'));
      },
    });
  }

  isManaged(user: ManagedUser): boolean {
    return this.roles.includes(user.role);
  }

  roleLabel = roleLabel;
  roleChipClass = roleChipClass;

  userInitial(user: ManagedUser): string {
    return user.nom?.charAt(0)?.toUpperCase() ?? user.email?.charAt(0)?.toUpperCase() ?? 'U';
  }

  isCurrentUser(user: ManagedUser): boolean {
    return user.id === this.authService.currentUser?.id;
  }

  private onSaveSuccess(message: string): void {
    this.saving.set(false);
    this.success.set(message);
    this.formOpen.set(false);
    this.loadUsers();
  }

  private onSaveError(err: unknown): void {
    this.saving.set(false);
    this.error.set(resolveHttpErrorMessage(err, 'Enregistrement impossible.'));
  }
}
