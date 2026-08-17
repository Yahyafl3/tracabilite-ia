import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Card } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Button } from 'primeng/button';
import { Skeleton } from 'primeng/skeleton';
import { Message } from 'primeng/message';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { TranslatePipe } from '@ngx-translate/core';
import {
  BackupAdminService,
  type BackupJob,
  type BackupJobStatus,
} from '../../../core/services/backup-admin.service';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';
import { TranslationService } from '../../../core/i18n/translation.service';
import { CopyHashComponent } from '../../../shared/ui/copy-hash.component';

@Component({
  selector: 'app-backup-admin',
  standalone: true,
  imports: [
    CommonModule,
    Card,
    TableModule,
    Tag,
    Button,
    Skeleton,
    Message,
    ConfirmDialog,
    TranslatePipe,
    CopyHashComponent,
  ],
  providers: [ConfirmationService],
  templateUrl: './backup-admin.component.html',
  styleUrl: './backup-admin.component.scss',
})
export class BackupAdminComponent {
  private readonly backupService = inject(BackupAdminService);
  private readonly confirmation = inject(ConfirmationService);
  private readonly i18n = inject(TranslationService);

  readonly jobs = signal<BackupJob[]>([]);
  readonly loading = signal(true);
  readonly creating = signal(false);
  readonly busyId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.backupService.list().subscribe({
      next: (jobs) => {
        this.jobs.set(jobs);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, this.i18n.t('admin.backup.loadError')));
        this.loading.set(false);
      },
    });
  }

  create(): void {
    this.creating.set(true);
    this.error.set(null);
    this.success.set(null);
    this.backupService.create().subscribe({
      next: () => {
        this.creating.set(false);
        this.success.set(this.i18n.t('admin.backup.created'));
        this.load();
      },
      error: (err) => {
        this.creating.set(false);
        this.error.set(resolveHttpErrorMessage(err, this.i18n.t('admin.backup.createError')));
      },
    });
  }

  verify(job: BackupJob): void {
    this.busyId.set(job.id);
    this.error.set(null);
    this.success.set(null);
    this.backupService.verify(job.id).subscribe({
      next: (result) => {
        this.busyId.set(null);
        this.success.set(
          result.valid ? this.i18n.t('admin.backup.verifyOk') : this.i18n.t('admin.backup.verifyFail'),
        );
        this.load();
      },
      error: (err) => {
        this.busyId.set(null);
        this.error.set(resolveHttpErrorMessage(err, this.i18n.t('admin.backup.verifyError')));
      },
    });
  }

  confirmRestore(job: BackupJob): void {
    this.confirmation.confirm({
      header: this.i18n.t('admin.backup.restoreTitle'),
      message: this.i18n.t('admin.backup.restoreMessage'),
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: this.i18n.t('admin.backup.restore'),
      rejectLabel: this.i18n.t('common.cancel'),
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => this.restore(job),
    });
  }

  restore(job: BackupJob): void {
    this.busyId.set(job.id);
    this.error.set(null);
    this.success.set(null);
    this.backupService.restore(job.id).subscribe({
      next: (result) => {
        this.busyId.set(null);
        this.success.set(
          this.i18n.t('admin.backup.restored', {
            decisions: result.decisionsCreated,
            skipped: result.decisionsSkipped,
            users: result.usersCreated,
          }),
        );
        this.load();
      },
      error: (err) => {
        this.busyId.set(null);
        this.error.set(resolveHttpErrorMessage(err, this.i18n.t('admin.backup.restoreError')));
      },
    });
  }

  download(job: BackupJob): void {
    this.busyId.set(job.id);
    this.error.set(null);
    this.backupService.download(job.id).subscribe({
      next: (blob) => {
        this.busyId.set(null);
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = job.filename || `backup-${job.id}.json`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.busyId.set(null);
        this.error.set(resolveHttpErrorMessage(err, this.i18n.t('admin.backup.downloadError')));
      },
    });
  }

  statusLabel(status: BackupJobStatus): string {
    this.i18n.currentLang();
    return this.i18n.t(`admin.backup.status.${status}`);
  }

  statusSeverity(status: BackupJobStatus): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    if (status === 'VERIFIED_OK' || status === 'RESTORED') return 'success';
    if (status === 'VERIFIED_TAMPERED') return 'danger';
    if (status === 'MISSING_FILE') return 'warn';
    return 'info';
  }

  formatSize(bytes: number): string {
    this.i18n.currentLang();
    if (bytes < 1024) {
      return this.i18n.t('admin.backup.bytes', { count: bytes });
    }
    return this.i18n.t('admin.backup.kilobytes', { count: (bytes / 1024).toFixed(1) });
  }

  formatDate(value?: string | null): string {
    this.i18n.currentLang();
    if (!value) return this.i18n.t('common.dash');
    return new Date(value).toLocaleString(this.i18n.localeId());
  }
}
