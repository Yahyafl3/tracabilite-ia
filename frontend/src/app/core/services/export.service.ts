import { Injectable } from '@angular/core';
import type { AuditRecentItemResponse } from './audit.service';
import { StatutDecisionEnum } from '../models/decision.models';
import { statutLabel } from '../utils/label.util';

@Injectable({ providedIn: 'root' })
export class ExportService {
  /**
   * Export audit data as CSV
   */
  exportAuditCSV(
    items: AuditRecentItemResponse[],
    filters?: {
      search?: string;
      statut?: StatutDecisionEnum | '';
      period?: Date[] | null;
    }
  ): void {
    const headers = [
      'Date',
      'Heure',
      'UUID Décision',
      'Prompt',
      'Statut',
      'Intégrité',
    ];

    const rows = items.map((item) => [
      this.formatDate(item.timestamp),
      this.formatTime(item.timestamp),
      item.decisionId,
      this.escapeCSV(item.prompt),
      statutLabel(item.statutValidation),
      item.integrityValid ? 'Valide' : 'Invalide',
    ]);

    const csv = [
      headers.join(','),
      ...rows.map((row) => row.join(',')),
    ].join('\n');

    const filename = this.generateFilename('audit-trail', filters, 'csv');
    this.downloadFile(csv, filename, 'text/csv;charset=utf-8;');
  }

  /**
   * Export audit data as PDF
   */
  async exportAuditPDF(
    items: AuditRecentItemResponse[],
    summary: {
      totalDecisions: number;
      validDecisions: number;
      invalidDecisions: number;
      chainIntact: boolean;
    },
    filters?: {
      search?: string;
      statut?: StatutDecisionEnum | '';
      period?: Date[] | null;
    }
  ): Promise<void> {
    // Create HTML content for PDF
    const htmlContent = this.generateAuditPDFHTML(items, summary, filters);

    // Convert HTML to PDF using print dialog
    const printWindow = window.open('', '_blank');
    if (!printWindow) {
      alert('Impossible d\'ouvrir la fenêtre d\'impression. Vérifiez que les pop-ups ne sont pas bloquées.');
      return;
    }

    printWindow.document.write(htmlContent);
    printWindow.document.close();
    
    // Wait for content to load
    printWindow.onload = () => {
      printWindow.focus();
      printWindow.print();
      // Don't close immediately to allow print dialog
      setTimeout(() => {
        printWindow.close();
      }, 100);
    };
  }

  /**
   * Generate HTML content for PDF export
   */
  private generateAuditPDFHTML(
    items: AuditRecentItemResponse[],
    summary: {
      totalDecisions: number;
      validDecisions: number;
      invalidDecisions: number;
      chainIntact: boolean;
    },
    filters?: {
      search?: string;
      statut?: StatutDecisionEnum | '';
      period?: Date[] | null;
    }
  ): string {
    const now = new Date().toLocaleString('fr-FR');
    const filterInfo = this.getFilterDescription(filters);

    return `
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Rapport d'Audit - Traçabilité IA</title>
  <style>
    @page {
      size: A4;
      margin: 2cm;
    }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica', 'Arial', sans-serif;
      font-size: 11pt;
      color: #1f2937;
      line-height: 1.5;
    }
    .header {
      margin-bottom: 2rem;
      padding-bottom: 1rem;
      border-bottom: 2px solid #4f46e5;
    }
    .header h1 {
      margin: 0 0 0.5rem;
      font-size: 24pt;
      color: #4f46e5;
    }
    .header .meta {
      color: #6b7280;
      font-size: 10pt;
    }
    .summary {
      background: #f3f4f6;
      padding: 1rem;
      margin-bottom: 2rem;
      border-radius: 0.5rem;
    }
    .summary h2 {
      margin: 0 0 1rem;
      font-size: 14pt;
      color: #374151;
    }
    .summary-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
    }
    .summary-item {
      display: flex;
      justify-content: space-between;
      padding: 0.5rem;
      background: white;
      border-radius: 0.25rem;
    }
    .summary-item .label {
      color: #6b7280;
    }
    .summary-item .value {
      font-weight: 600;
      color: #1f2937;
    }
    .filters {
      margin-bottom: 1.5rem;
      padding: 0.75rem;
      background: #fef3c7;
      border-left: 4px solid #f59e0b;
      font-size: 10pt;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 2rem;
      font-size: 9pt;
    }
    thead {
      background: #f9fafb;
    }
    th {
      padding: 0.75rem 0.5rem;
      text-align: left;
      font-weight: 600;
      color: #374151;
      border-bottom: 2px solid #d1d5db;
    }
    td {
      padding: 0.75rem 0.5rem;
      border-bottom: 1px solid #e5e7eb;
    }
    tr:last-child td {
      border-bottom: none;
    }
    .status {
      display: inline-block;
      padding: 0.25rem 0.5rem;
      border-radius: 0.25rem;
      font-size: 8pt;
      font-weight: 600;
    }
    .status-success { background: #d1fae5; color: #065f46; }
    .status-danger { background: #fee2e2; color: #991b1b; }
    .status-warn { background: #fef3c7; color: #92400e; }
    .status-info { background: #dbeafe; color: #1e40af; }
    .integrity-valid { background: #d1fae5; color: #065f46; }
    .integrity-invalid { background: #fee2e2; color: #991b1b; }
    .footer {
      margin-top: 2rem;
      padding-top: 1rem;
      border-top: 1px solid #e5e7eb;
      text-align: center;
      color: #6b7280;
      font-size: 9pt;
    }
    .prompt {
      max-width: 300px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    code {
      font-family: 'Courier New', monospace;
      font-size: 8pt;
      background: #f3f4f6;
      padding: 0.125rem 0.25rem;
      border-radius: 0.125rem;
    }
    @media print {
      .header {
        page-break-after: avoid;
      }
      tr {
        page-break-inside: avoid;
      }
    }
  </style>
</head>
<body>
  <div class="header">
    <h1>🛡️ Rapport d'Audit - Traçabilité IA</h1>
    <div class="meta">
      Généré le ${now} | ${items.length} événement(s)
    </div>
  </div>

  <div class="summary">
    <h2>Résumé de l'intégrité</h2>
    <div class="summary-grid">
      <div class="summary-item">
        <span class="label">Total décisions</span>
        <span class="value">${summary.totalDecisions}</span>
      </div>
      <div class="summary-item">
        <span class="label">Intégrité valide</span>
        <span class="value">${summary.validDecisions}</span>
      </div>
      <div class="summary-item">
        <span class="label">Intégrité invalide</span>
        <span class="value">${summary.invalidDecisions}</span>
      </div>
      <div class="summary-item">
        <span class="label">Chaîne intacte</span>
        <span class="value">${summary.chainIntact ? 'Oui ✓' : 'Non ✗'}</span>
      </div>
    </div>
  </div>

  ${filterInfo ? `<div class="filters"><strong>Filtres appliqués :</strong> ${filterInfo}</div>` : ''}

  <table>
    <thead>
      <tr>
        <th>Date & Heure</th>
        <th>UUID Décision</th>
        <th>Prompt</th>
        <th>Statut</th>
        <th>Intégrité</th>
      </tr>
    </thead>
    <tbody>
      ${items.map((item) => `
        <tr>
          <td>
            ${this.formatDate(item.timestamp)}<br>
            <span style="color: #6b7280; font-size: 8pt;">${this.formatTime(item.timestamp)}</span>
          </td>
          <td><code>${this.shortUuid(item.decisionId)}</code></td>
          <td class="prompt">${this.escapeHTML(item.prompt)}</td>
          <td>${this.getStatusBadge(item.statutValidation)}</td>
          <td>${this.getIntegrityBadge(item.integrityValid)}</td>
        </tr>
      `).join('')}
    </tbody>
  </table>

  <div class="footer">
    <p>
      <strong>Traçabilité IA</strong> - Décisions assistées et auditables<br>
      Conforme RGPD et AI Act européen
    </p>
  </div>
</body>
</html>
    `;
  }

  /**
   * Get filter description for PDF/CSV
   */
  private getFilterDescription(filters?: {
    search?: string;
    statut?: StatutDecisionEnum | '';
    period?: Date[] | null;
  }): string {
    if (!filters) return '';

    const parts: string[] = [];

    if (filters.search?.trim()) {
      parts.push(`UUID contient "${filters.search}"`);
    }

    if (filters.statut) {
      parts.push(`Statut = ${statutLabel(filters.statut)}`);
    }

    if (filters.period && filters.period.length === 2) {
      const [from, to] = filters.period;
      parts.push(`Période = ${this.formatDate(from.toISOString())} - ${this.formatDate(to.toISOString())}`);
    }

    return parts.join(', ');
  }

  /**
   * Generate filename with timestamp and filters
   */
  private generateFilename(
    prefix: string,
    filters?: {
      search?: string;
      statut?: StatutDecisionEnum | '';
      period?: Date[] | null;
    },
    extension?: string
  ): string {
    const timestamp = new Date().toISOString().slice(0, 16).replace(/[:-]/g, '');
    let filename = `${prefix}-${timestamp}`;

    if (filters?.statut) {
      filename += `-${filters.statut.toLowerCase()}`;
    }

    if (extension) {
      filename += `.${extension}`;
    }

    return filename;
  }

  /**
   * Download file with given content
   */
  private downloadFile(content: string, filename: string, mimeType: string): void {
    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  /**
   * Escape CSV special characters
   */
  private escapeCSV(value: string): string {
    if (!value) return '';
    const needsQuotes = /[",\n\r]/.test(value);
    const escaped = value.replace(/"/g, '""');
    return needsQuotes ? `"${escaped}"` : escaped;
  }

  /**
   * Escape HTML special characters
   */
  private escapeHTML(value: string): string {
    const div = document.createElement('div');
    div.textContent = value;
    return div.innerHTML;
  }

  /**
   * Format date for display
   */
  private formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  /**
   * Format time for display
   */
  private formatTime(iso: string): string {
    return new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  /**
   * Shorten UUID for display
   */
  private shortUuid(id: string): string {
    return id.length > 12 ? `${id.slice(0, 8)}…${id.slice(-4)}` : id;
  }

  /**
   * Get HTML badge for status
   */
  private getStatusBadge(statut: StatutDecisionEnum): string {
    const label = statutLabel(statut);
    let className = 'status ';
    
    if (statut === StatutDecisionEnum.APPROUVEE) className += 'status-success';
    else if (statut === StatutDecisionEnum.REJETEE) className += 'status-danger';
    else if (statut === StatutDecisionEnum.EN_ATTENTE) className += 'status-warn';
    else if (statut === StatutDecisionEnum.MODIFIEE) className += 'status-info';
    
    return `<span class="${className}">${label}</span>`;
  }

  /**
   * Get HTML badge for integrity
   */
  private getIntegrityBadge(valid: boolean): string {
    const label = valid ? 'Valide' : 'Invalide';
    const className = valid ? 'integrity-valid' : 'integrity-invalid';
    return `<span class="status ${className}">${label}</span>`;
  }
}
