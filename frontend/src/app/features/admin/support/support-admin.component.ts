import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Card } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { Message } from 'primeng/message';
import { Skeleton } from 'primeng/skeleton';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import {
  SupportMessage,
  SupportMessageStatus,
  SupportService,
} from '../../../core/services/support.service';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';

@Component({
  selector: 'app-support-admin',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    Card,
    TableModule,
    Tag,
    Button,
    Dialog,
    InputText,
    Select,
    Textarea,
    Message,
    Skeleton,
    PaginatorModule,
  ],
  templateUrl: './support-admin.component.html',
  styleUrl: './support-admin.component.scss',
})
export class SupportAdminComponent {
  private readonly supportService = inject(SupportService);

  readonly messages = signal<SupportMessage[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly detailOpen = signal(false);
  readonly selected = signal<SupportMessage | null>(null);

  readonly search = signal('');
  readonly statusFilter = signal<SupportMessageStatus | null>(null);
  readonly page = signal(0);
  readonly size = signal(10);
  readonly totalElements = signal(0);

  readonly statusOptions = [
    { label: 'Tous les statuts', value: null as SupportMessageStatus | null },
    { label: 'NEW', value: 'NEW' as SupportMessageStatus },
    { label: 'IN_PROGRESS', value: 'IN_PROGRESS' as SupportMessageStatus },
    { label: 'RESOLVED', value: 'RESOLVED' as SupportMessageStatus },
    { label: 'CLOSED', value: 'CLOSED' as SupportMessageStatus },
  ];

  readonly statusEditOptions = this.statusOptions.filter((o) => o.value !== null);

  readonly empty = computed(() => !this.loading() && this.messages().length === 0);

  constructor() {
    this.loadMessages();
  }

  loadMessages(): void {
    this.loading.set(true);
    this.error.set(null);
    this.supportService
      .getMessages({
        status: this.statusFilter(),
        q: this.search(),
        page: this.page(),
        size: this.size(),
      })
      .subscribe({
        next: (page) => {
          this.messages.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger les demandes.'));
        },
      });
  }

  onSearchChange(value: string): void {
    this.search.set(value);
    this.page.set(0);
    this.loadMessages();
  }

  onStatusFilterChange(value: SupportMessageStatus | null): void {
    this.statusFilter.set(value);
    this.page.set(0);
    this.loadMessages();
  }

  onPageChange(event: PaginatorState): void {
    this.page.set(event.page ?? 0);
    this.size.set(event.rows ?? 10);
    this.loadMessages();
  }

  openDetail(item: SupportMessage): void {
    this.error.set(null);
    this.success.set(null);
    this.selected.set(item);
    this.detailOpen.set(true);
    this.supportService.getMessageById(item.id).subscribe({
      next: (detail) => this.selected.set(detail),
      error: (err) =>
        this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger le détail.')),
    });
  }

  closeDetail(): void {
    this.detailOpen.set(false);
    this.selected.set(null);
  }

  updateSelectedStatus(status: SupportMessageStatus): void {
    const current = this.selected();
    if (!current || this.saving()) return;

    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);
    this.supportService.updateStatus(current.id, status).subscribe({
      next: (updated) => {
        this.saving.set(false);
        this.selected.set(updated);
        this.success.set('Statut mis à jour.');
        this.loadMessages();
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(resolveHttpErrorMessage(err, 'Impossible de mettre à jour le statut.'));
      },
    });
  }

  statusSeverity(status: SupportMessageStatus): 'info' | 'warn' | 'success' | 'secondary' | 'contrast' {
    switch (status) {
      case 'NEW':
        return 'info';
      case 'IN_PROGRESS':
        return 'warn';
      case 'RESOLVED':
        return 'success';
      case 'CLOSED':
        return 'secondary';
      default:
        return 'contrast';
    }
  }

  formatDate(value: string | null | undefined): string {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString('fr-FR');
  }
}
