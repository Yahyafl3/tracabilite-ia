import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { Card } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { Message } from 'primeng/message';
import { Skeleton } from 'primeng/skeleton';
import { ConfirmationService } from 'primeng/api';
import { Password } from 'primeng/password';
import { TranslatePipe } from '@ngx-translate/core';
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
import { roleLabel } from '../../../core/utils/label.util';
import { TranslationService } from '../../../core/i18n/translation.service';

type FormMode = 'create' | 'edit';
type StatusFilter = 'all' | 'active' | 'inactive';

@Component({
  selector: 'app-users-admin',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    Card,
    TableModule,
    Tag,
    Button,
    Dialog,
    InputText,
    Select,
    ConfirmDialog,
    Message,
    Skeleton,
    Password,
    TranslatePipe,
  ],
  providers: [ConfirmationService],
  templateUrl: './users-admin.component.html',
  styleUrl: './users-admin.component.scss',
})
export class UsersAdminComponent {
  private readonly userAdminService = inject(UserAdminService);
  private readonly authService = inject(AuthService);
  private readonly confirmation = inject(ConfirmationService);
  private readonly fb = inject(FormBuilder);
  private readonly i18n = inject(TranslationService);

  readonly roles = MANAGED_USER_ROLES;
  readonly roleOptions = computed(() => {
    this.i18n.currentLang();
    return this.roles.map((role) => ({ label: roleLabel(role), value: role }));
  });
  readonly roleFilterOptions = computed(() => [
    { label: this.i18n.t('admin.users.allRoles'), value: null as UserRole | null },
    ...this.roleOptions(),
  ]);
  readonly statusFilterOptions = computed(() => {
    this.i18n.currentLang();
    return [
      { label: this.i18n.t('admin.users.allStatuses'), value: 'all' as StatusFilter },
      { label: this.i18n.t('common.active'), value: 'active' as StatusFilter },
      { label: this.i18n.t('common.inactive'), value: 'inactive' as StatusFilter },
    ];
  });

  readonly users = signal<ManagedUser[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly formOpen = signal(false);
  readonly formMode = signal<FormMode>('create');
  readonly editingUserId = signal<string | null>(null);

  readonly search = signal('');
  readonly roleFilter = signal<UserRole | null>(null);
  readonly statusFilter = signal<StatusFilter>('all');

  readonly filteredUsers = computed(() => {
    const query = this.search().trim().toLowerCase();
    const role = this.roleFilter();
    const status = this.statusFilter();

    return this.users().filter((user) => {
      if (role && user.role !== role) return false;
      const active = user.actif !== false;
      if (status === 'active' && !active) return false;
      if (status === 'inactive' && active) return false;
      if (!query) return true;
      return (
        user.nom.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query) ||
        roleLabel(user.role).toLowerCase().includes(query)
      );
    });
  });

  readonly form = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.minLength(6)]],
    role: [UserRole.AGENT_CREDIT, Validators.required],
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
        this.error.set(resolveHttpErrorMessage(err, this.i18n.t('admin.users.loadError')));
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
      role: UserRole.AGENT_CREDIT,
    });
    this.form.controls.motDePasse.setValidators([Validators.required, Validators.minLength(6)]);
    this.form.controls.motDePasse.updateValueAndValidity();
    this.success.set(null);
    this.formOpen.set(true);
  }

  openEditForm(user: ManagedUser): void {
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
        next: () => this.onSaveSuccess(this.i18n.t('admin.users.createdSuccess')),
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
      next: () => this.onSaveSuccess(this.i18n.t('admin.users.updatedSuccess')),
      error: (err) => this.onSaveError(err),
    });
  }

  confirmDeactivate(user: ManagedUser): void {
    if (user.id === this.authService.currentUser?.id) {
      this.error.set(this.i18n.t('admin.users.cannotDeactivateSelf'));
      return;
    }

    this.confirmation.confirm({
      header: this.i18n.t('admin.users.deactivateTitle'),
      message: this.i18n.t('admin.users.deactivateMessage'),
      icon: 'pi pi-ban',
      acceptLabel: this.i18n.t('admin.users.deactivate'),
      rejectLabel: this.i18n.t('common.cancel'),
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => this.deactivateUser(user),
    });
  }

  deactivateUser(user: ManagedUser): void {
    this.error.set(null);
    this.success.set(null);
    this.userAdminService.deactivate(user.id).subscribe({
      next: () => {
        this.success.set(this.i18n.t('admin.users.deactivatedKept'));
        this.loadUsers();
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, this.i18n.t('admin.users.deactivateError')));
      },
    });
  }

  reactivateUser(user: ManagedUser): void {
    this.error.set(null);
    this.success.set(null);
    this.userAdminService.reactivate(user.id).subscribe({
      next: () => {
        this.success.set(this.i18n.t('admin.users.reactivated'));
        this.loadUsers();
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, this.i18n.t('admin.users.reactivateError')));
      },
    });
  }

  isActive(user: ManagedUser): boolean {
    return user.actif !== false;
  }

  roleLabel = roleLabel;

  get locale(): string {
    return this.i18n.localeId();
  }

  formatDate(value: string | null | undefined): string {
    if (!value) return this.i18n.t('common.dash');
    return new Date(value).toLocaleDateString(this.i18n.localeId(), {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  formatTime(value: string | null | undefined): string {
    if (!value) return '';
    return new Date(value).toLocaleTimeString(this.i18n.localeId(), {
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  roleSeverity(role: UserRole | string): 'danger' | 'warn' | 'info' | 'success' | 'secondary' {
    if (role === UserRole.ADMINISTRATEUR) return 'danger';
    if (
      role === UserRole.RESPONSABLE_CREDIT ||
      role === UserRole.PROFESSIONNEL_SANTE ||
      role === UserRole.RESPONSABLE_PEDAGOGIQUE
    ) return 'warn';
    if (role === UserRole.AUDITEUR) return 'info';
    if (
      role === UserRole.AGENT_CREDIT ||
      role === UserRole.AGENT_SANTE ||
      role === UserRole.AGENT_PEDAGOGIQUE
    ) return 'success';
    return 'secondary';
  }

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
    this.error.set(resolveHttpErrorMessage(err, this.i18n.t('admin.users.saveError')));
  }
}
