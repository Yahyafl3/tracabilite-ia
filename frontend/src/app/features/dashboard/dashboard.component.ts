import { Component, computed } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../../shared/icon.component';

export interface RecentDecision {
  id: string;
  prompt: string;
  agent: string;
  fournisseur: string;
  modele: string;
  statut: 'APPROUVEE' | 'MODIFIEE' | 'REJETEE' | 'EN_ATTENTE';
  timestamp: string;
}

const DEMO_DECISIONS: RecentDecision[] = [
  { id: '1', prompt: 'Analyse de support étudiant',  agent: 'ChatGPT', fournisseur: 'OpenAI',    modele: 'gpt-4.1',         statut: 'APPROUVEE', timestamp: '2025-07-14T09:12:00' },
  { id: '2', prompt: 'Réponse pédagogique sur Java', agent: 'ChatGPT', fournisseur: 'OpenAI',    modele: 'gpt-4.1',         statut: 'APPROUVEE', timestamp: '2025-07-14T09:18:00' },
  { id: '3', prompt: 'Synthèse d\'un article',       agent: 'ChatGPT', fournisseur: 'OpenAI',    modele: 'gpt-4.1',         statut: 'MODIFIEE',  timestamp: '2025-07-14T09:45:00' },
  { id: '4', prompt: 'Correction d\'un devoir',      agent: 'Claude',  fournisseur: 'Anthropic', modele: 'claude-sonnet-4', statut: 'APPROUVEE', timestamp: '2025-07-14T10:02:00' },
  { id: '5', prompt: 'Explication de SQL',           agent: 'Claude',  fournisseur: 'Anthropic', modele: 'claude-sonnet-4', statut: 'REJETEE',   timestamp: '2025-07-14T10:20:00' },
  { id: '6', prompt: 'Génération de résumé',         agent: 'Claude',  fournisseur: 'Anthropic', modele: 'claude-sonnet-4', statut: 'REJETEE',   timestamp: '2025-07-14T10:35:00' },
  { id: '7', prompt: 'Aide à la programmation',      agent: 'Gemini',  fournisseur: 'Google',    modele: 'gemini-2.5',      statut: 'APPROUVEE', timestamp: '2025-07-14T11:00:00' },
  { id: '8', prompt: 'Révision de contenu',          agent: 'Gemini',  fournisseur: 'Google',    modele: 'gemini-2.5',      statut: 'MODIFIEE',  timestamp: '2025-07-14T11:18:00' },
  { id: '9', prompt: 'Réponse multi-étapes',         agent: 'Gemini',  fournisseur: 'Google',    modele: 'gemini-2.5',      statut: 'REJETEE',   timestamp: '2025-07-14T11:40:00' },
];

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, DecimalPipe, RouterLink, IconComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {

  // ── KPI stats ────────────────────────────────────────────────
  readonly totalDecisions  = DEMO_DECISIONS.length;
  readonly totalApprouvees = DEMO_DECISIONS.filter(d => d.statut === 'APPROUVEE').length;
  readonly totalModifiees  = DEMO_DECISIONS.filter(d => d.statut === 'MODIFIEE').length;
  readonly totalRejetees   = DEMO_DECISIONS.filter(d => d.statut === 'REJETEE').length;
  readonly totalEnAttente  = DEMO_DECISIONS.filter(d => d.statut === 'EN_ATTENTE').length;
  readonly tauxValidation  = Math.round((this.totalApprouvees / this.totalDecisions) * 100);

  readonly kpiCards = [
    { label: 'Total décisions', value: this.totalDecisions,  unit: '', icon: 'file-text',   accent: 'indigo', trend: '+9 ce mois',                       trendUp: true  },
    { label: 'Approuvées',      value: this.totalApprouvees, unit: '', icon: 'check-circle', accent: 'green',  trend: `${this.tauxValidation}% du total`, trendUp: true  },
    { label: 'En attente',      value: this.totalEnAttente,  unit: '', icon: 'history',      accent: 'amber',  trend: 'À valider',                        trendUp: false },
    { label: 'Agents actifs',   value: 3,                    unit: '', icon: 'server',       accent: 'violet', trend: 'ChatGPT · Claude · Gemini',        trendUp: true  },
  ];

  // ── Donut chart ──────────────────────────────────────────────
  readonly DONUT_R   = 60;
  readonly DONUT_GAP = 16;
  readonly DONUT_CX  = 90;
  readonly DONUT_CY  = 90;

  readonly donutSegments = computed(() => {
    const data = [
      { label: 'Approuvées', value: this.totalApprouvees, color: '#10b981' },
      { label: 'Modifiées',  value: this.totalModifiees,  color: '#f59e0b' },
      { label: 'Rejetées',   value: this.totalRejetees,   color: '#ef4444' },
      { label: 'En attente', value: this.totalEnAttente,  color: '#94a3b8' },
    ].filter(d => d.value > 0);

    const total = data.reduce((s, d) => s + d.value, 0);
    const r  = this.DONUT_R;
    const ir = r - this.DONUT_GAP;
    const cx = this.DONUT_CX;
    const cy = this.DONUT_CY;
    let angle = -Math.PI / 2;

    return data.map(seg => {
      const fraction = seg.value / total;
      const sweep    = fraction * 2 * Math.PI;
      const x1o = cx + r  * Math.cos(angle); const y1o = cy + r  * Math.sin(angle);
      const x1i = cx + ir * Math.cos(angle); const y1i = cy + ir * Math.sin(angle);
      angle += sweep;
      const x2o = cx + r  * Math.cos(angle); const y2o = cy + r  * Math.sin(angle);
      const x2i = cx + ir * Math.cos(angle); const y2i = cy + ir * Math.sin(angle);
      const large = sweep > Math.PI ? 1 : 0;
      const path = `M ${x1o} ${y1o} A ${r} ${r} 0 ${large} 1 ${x2o} ${y2o} L ${x2i} ${y2i} A ${ir} ${ir} 0 ${large} 0 ${x1i} ${y1i} Z`;
      return { ...seg, path, pct: Math.round(fraction * 100) };
    });
  });

  // ── Recent decisions ─────────────────────────────────────────
  readonly recentDecisions = DEMO_DECISIONS.slice().reverse();

  statutLabel(s: RecentDecision['statut']): string {
    return { APPROUVEE: 'Approuvée', MODIFIEE: 'Modifiée', REJETEE: 'Rejetée', EN_ATTENTE: 'En attente' }[s];
  }

  statutClass(s: RecentDecision['statut']): string {
    return { APPROUVEE: 'chip--approved', MODIFIEE: 'chip--modified', REJETEE: 'chip--rejected', EN_ATTENTE: 'chip--pending' }[s];
  }

  agentAvatarClass(index: number): string {
    return `avatar--${(index % 4) + 1}`;
  }

  formatTime(iso: string): string {
    return new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' });
  }
}
