import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { IconComponent } from '../../../shared/icon.component';
import { DecisionService } from '../../../core/services/decision.service';
import { DecisionResponse, StatutDecisionEnum } from '../../../core/models/decision.models';

@Component({
  selector: 'app-decision-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, IconComponent],
  templateUrl: './decision-list.component.html',
  styleUrl: './decision-list.component.scss',
})
export class DecisionListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly decisionService = inject(DecisionService);

  readonly statuts = Object.values(StatutDecisionEnum);
  readonly pageSizeOptions = [5, 10, 20];
  readonly decisions = signal<DecisionResponse[]>([]);
  readonly totalElements = signal(0);
  readonly page = signal(0);
  readonly size = signal(10);
  readonly loading = signal(false);

  readonly totalPages = computed(() =>
    Math.max(1, Math.ceil(this.totalElements() / this.size()))
  );

  readonly rangeStart = computed(() =>
    this.totalElements() === 0 ? 0 : this.page() * this.size() + 1
  );

  readonly rangeEnd = computed(() =>
    Math.min((this.page() + 1) * this.size(), this.totalElements())
  );

  readonly filters = this.fb.nonNullable.group({
    search: [''],
    statut: ['' as StatutDecisionEnum | ''],
  });

  constructor() {
    this.load();
  }

  load(page = this.page(), size = this.size()): void {
    this.loading.set(true);
    const filters = this.filters.getRawValue();
    this.decisionService.search({
      search: filters.search,
      statut: filters.statut,
      page,
      size,
    }).subscribe({
      next: (response) => {
        this.decisions.set(response.content);
        this.totalElements.set(response.totalElements);
        this.page.set(response.page);
        this.size.set(response.size);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  applyFilters(): void {
    this.load(0, this.size());
  }

  onPageSizeChange(event: Event): void {
    const value = Number((event.target as HTMLSelectElement).value);
    this.load(0, value);
  }

  prevPage(): void {
    if (this.page() > 0) {
      this.load(this.page() - 1, this.size());
    }
  }

  nextPage(): void {
    if (this.page() < this.totalPages() - 1) {
      this.load(this.page() + 1, this.size());
    }
  }

  decisionChipClass(decision?: string): string {
    if (decision === 'APPROUVER') return 'chip--approved';
    if (decision === 'REJETER') return 'chip--rejected';
    return 'chip--pending';
  }

  riskChipClass(risk?: string): string {
    if (risk === 'HIGH') return 'chip--rejected';
    if (risk === 'MEDIUM') return 'chip--modified';
    if (risk === 'LOW') return 'chip--approved';
    return 'chip--pending';
  }

  statutChipClass(statut: StatutDecisionEnum): string {
    const map: Record<StatutDecisionEnum, string> = {
      [StatutDecisionEnum.APPROUVEE]: 'chip--approved',
      [StatutDecisionEnum.MODIFIEE]: 'chip--modified',
      [StatutDecisionEnum.REJETEE]: 'chip--rejected',
      [StatutDecisionEnum.EN_ATTENTE]: 'chip--pending',
      [StatutDecisionEnum.BROUILLON]: 'chip--pending',
    };
    return map[statut] ?? 'chip--pending';
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  formatTime(iso: string): string {
    return new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }
}
