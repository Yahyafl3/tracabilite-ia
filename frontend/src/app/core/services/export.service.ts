import { Injectable } from '@angular/core';
import type { AuditRecentItemResponse } from './audit.service';
import { StatutDecisionEnum } from '../models/decision.models';
import { statutLabel } from '../utils/label.util';

/** Excel FR / AR / Windows: séparateur `;`, BOM UTF-8, fins de ligne CRLF. */
const CSV_SEP = ';';
const CSV_BOM = '\uFEFF';

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
    const filename = this.generateFilename('audit-trail', filters, 'csv');
    this.downloadFile(this.buildAuditCsv(items), filename, 'text/csv;charset=utf-8;');
  }

  /** Public for tests: CSV Excel-compatible (BOM + `;` + CRLF). */
  buildAuditCsv(items: AuditRecentItemResponse[]): string {
    const headers = ['Date', 'Heure', 'UUID Décision', 'Prompt', 'Statut', 'Intégrité'];
    const lines = [
      headers.map((h) => this.csvCell(h)).join(CSV_SEP),
      ...items.map((item) =>
        [
          this.formatCsvDate(item.timestamp),
          this.formatTime(item.timestamp),
          item.decisionId ?? '',
          item.prompt ?? '',
          statutLabel(item.statutValidation),
          item.integrityValid ? 'Valide' : 'Invalide',
        ]
          .map((cell) => this.csvCell(cell))
          .join(CSV_SEP),
      ),
    ];
    return CSV_BOM + lines.join('\r\n') + '\r\n';
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
    const htmlContent = this.generateAuditPDFHTML(items, summary, filters);
    await this.printHtmlDocument(htmlContent, "Rapport d'audit — Traçabilité IA");
  }

  /**
   * Generate HTML content for PDF export
   */
  generateAuditPDFHTML(
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
    const generatedAt = new Date();
    const now = generatedAt.toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
    const docRef = `AUD-${generatedAt.toISOString().slice(0, 16).replace(/[-:T]/g, '')}`;
    const filterInfo = this.getFilterDescription(filters);
    const chainOk = summary.chainIntact;
    const rows = items
      .map(
        (item) => `
        <tr>
          <td class="nowrap">
            ${this.escapeHTML(this.formatCsvDate(item.timestamp))}<br>
            <span class="muted">${this.escapeHTML(this.formatTime(item.timestamp))}</span>
          </td>
          <td><code>${this.escapeHTML(item.decisionId)}</code></td>
          <td>${this.escapeHTML(item.prompt ?? '')}</td>
          <td>${this.getStatusBadge(item.statutValidation)}</td>
          <td>${this.getIntegrityBadge(item.integrityValid)}</td>
        </tr>`,
      )
      .join('');

    return `<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Rapport d'audit — Traçabilité IA — ${this.escapeHTML(docRef)}</title>
  <style>
    @page {
      size: A4 landscape;
      margin: 14mm 12mm 16mm;
    }
    * { box-sizing: border-box; }
    html, body {
      margin: 0;
      padding: 0;
      background: #fff;
    }
    body {
      font-family: "Segoe UI", Calibri, "Helvetica Neue", Arial, sans-serif;
      font-size: 10pt;
      color: #0f172a;
      line-height: 1.45;
    }
    .letterhead {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 16px;
      padding-bottom: 12px;
      border-bottom: 3px solid #1e3a5f;
    }
    .brand {
      display: flex;
      gap: 12px;
      align-items: center;
    }
    .mark {
      width: 42px;
      height: 42px;
      border-radius: 8px;
      background: #1e3a5f;
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .brand-name {
      font-size: 13pt;
      font-weight: 700;
      letter-spacing: 0.04em;
      text-transform: uppercase;
      color: #1e3a5f;
      margin: 0;
    }
    .brand-sub {
      margin: 2px 0 0;
      font-size: 9pt;
      color: #64748b;
    }
    .doc-meta {
      text-align: right;
      font-size: 8.5pt;
      color: #334155;
    }
    .doc-meta .klass {
      display: inline-block;
      margin-bottom: 6px;
      padding: 2px 8px;
      border: 1px solid #b45309;
      color: #92400e;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      font-size: 8pt;
    }
    h1 {
      margin: 14px 0 4px;
      font-size: 18pt;
      font-weight: 700;
      color: #0f172a;
    }
    .lede {
      margin: 0 0 16px;
      color: #475569;
      font-size: 10pt;
    }
    .kpis {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 8px;
      margin-bottom: 16px;
    }
    .kpi {
      border: 1px solid #e2e8f0;
      border-top: 3px solid #1e3a5f;
      padding: 10px 12px;
    }
    .kpi.warn { border-top-color: #b45309; }
    .kpi.ok { border-top-color: #047857; }
    .kpi.bad { border-top-color: #b91c1c; }
    .kpi .label {
      display: block;
      font-size: 8pt;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: #64748b;
      margin-bottom: 4px;
    }
    .kpi .value {
      font-size: 16pt;
      font-weight: 700;
      color: #0f172a;
    }
    .filters {
      margin-bottom: 12px;
      padding: 8px 10px;
      background: #f8fafc;
      border-left: 3px solid #1e3a5f;
      font-size: 9pt;
    }
    h2 {
      margin: 0 0 8px;
      font-size: 11pt;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: #1e3a5f;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      table-layout: fixed;
      font-size: 8.5pt;
    }
    thead th {
      background: #1e3a5f;
      color: #fff;
      font-weight: 600;
      text-align: left;
      padding: 8px 8px;
    }
    td {
      padding: 7px 8px;
      border-bottom: 1px solid #e2e8f0;
      vertical-align: top;
      word-wrap: break-word;
    }
    tbody tr:nth-child(even) td { background: #f8fafc; }
    .nowrap { white-space: nowrap; }
    .muted { color: #64748b; font-size: 8pt; }
    code {
      font-family: Consolas, "Courier New", monospace;
      font-size: 7.5pt;
      word-break: break-all;
    }
    .status {
      display: inline-block;
      padding: 2px 7px;
      border-radius: 3px;
      font-size: 8pt;
      font-weight: 600;
    }
    .status-success { background: #d1fae5; color: #065f46; }
    .status-danger { background: #fee2e2; color: #991b1b; }
    .status-warn { background: #fef3c7; color: #92400e; }
    .status-info { background: #dbeafe; color: #1e40af; }
    .status-neutral { background: #e2e8f0; color: #334155; }
    .integrity-valid { background: #d1fae5; color: #065f46; }
    .integrity-invalid { background: #fee2e2; color: #991b1b; }
    .attestation {
      margin-top: 16px;
      padding: 10px 12px;
      border: 1px solid #e2e8f0;
      font-size: 8.5pt;
      color: #334155;
    }
    .footer {
      margin-top: 14px;
      padding-top: 8px;
      border-top: 1px solid #cbd5e1;
      display: flex;
      justify-content: space-between;
      font-size: 8pt;
      color: #64748b;
    }
    @media print {
      thead { display: table-header-group; }
      tr, .kpi, .letterhead, .attestation { break-inside: avoid; }
    }
  </style>
</head>
<body>
  <header class="letterhead">
    <div class="brand">
      <div class="mark" aria-hidden="true">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
          <path d="M12 3l8 3v6c0 5-3.4 8.4-8 9.5C7.4 20.4 4 17 4 12V6l8-3z" stroke="#fff" stroke-width="1.7"/>
          <path d="M8.5 12.2l2.3 2.3 4.7-5" stroke="#fff" stroke-width="1.7" fill="none"/>
        </svg>
      </div>
      <div>
        <p class="brand-name">Traçabilité IA</p>
        <p class="brand-sub">Décisions assistées · explicables · auditables</p>
      </div>
    </div>
    <div class="doc-meta">
      <div class="klass">Usage interne</div>
      <div>Réf. ${this.escapeHTML(docRef)}</div>
      <div>Généré le ${this.escapeHTML(now)}</div>
      <div>${items.length} événement(s)</div>
    </div>
  </header>

  <h1>Rapport d’audit d’intégrité</h1>
  <p class="lede">
    Contrôle des empreintes SHA-256 et du journal des décisions. Document généré
    automatiquement par la plateforme — ne pas modifier hors système.
  </p>

  <section class="kpis" aria-label="Résumé de l'intégrité">
    <div class="kpi">
      <span class="label">Décisions</span>
      <span class="value">${summary.totalDecisions}</span>
    </div>
    <div class="kpi ok">
      <span class="label">Empreintes valides</span>
      <span class="value">${summary.validDecisions}</span>
    </div>
    <div class="kpi ${summary.invalidDecisions > 0 ? 'bad' : ''}">
      <span class="label">Empreintes invalides</span>
      <span class="value">${summary.invalidDecisions}</span>
    </div>
    <div class="kpi ${chainOk ? 'ok' : 'bad'}">
      <span class="label">Chaîne d’intégrité</span>
      <span class="value">${chainOk ? 'Intacte' : 'Rompue'}</span>
    </div>
  </section>

  ${filterInfo ? `<div class="filters"><strong>Périmètre :</strong> ${this.escapeHTML(filterInfo)}</div>` : ''}

  <h2>Journal des décisions</h2>
  <table>
    <colgroup>
      <col style="width:12%">
      <col style="width:28%">
      <col style="width:36%">
      <col style="width:14%">
      <col style="width:10%">
    </colgroup>
    <thead>
      <tr>
        <th>Date &amp; heure</th>
        <th>Identifiant décision</th>
        <th>Objet</th>
        <th>Statut</th>
        <th>Intégrité</th>
      </tr>
    </thead>
    <tbody>
      ${rows || `<tr><td colspan="5">Aucun événement dans le périmètre.</td></tr>`}
    </tbody>
  </table>

  <div class="attestation">
    <strong>Attestation.</strong>
    Les statuts d’intégrité ci-dessus résultent du recalcul des empreintes SHA-256
    des dossiers persistés. Une chaîne « Intacte » signifie qu’aucune empreinte
    vérifiable n’est en écart. Usage interne · RGPD · AI Act.
  </div>

  <footer class="footer">
    <span>Traçabilité IA — ${this.escapeHTML(docRef)}</span>
    <span>Confidentiel — ne pas diffuser hors du périmètre d’audit</span>
  </footer>
</body>
</html>`;
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
      parts.push(
        `Période = ${this.formatCsvDate(from.toISOString())} - ${this.formatCsvDate(to.toISOString())}`,
      );
    }

    return parts.join(' · ');
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

  private printHtmlDocument(html: string, title: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const iframe = document.createElement('iframe');
      iframe.setAttribute('aria-hidden', 'true');
      iframe.style.cssText = 'position:fixed;right:0;bottom:0;width:0;height:0;border:0;';
      document.body.appendChild(iframe);
      const win = iframe.contentWindow;
      if (!win) {
        iframe.remove();
        reject(new Error("Impossible de préparer l'impression"));
        return;
      }

      let cleaned = false;
      const cleanup = () => {
        if (cleaned) return;
        cleaned = true;
        iframe.remove();
        resolve();
      };

      win.document.open();
      win.document.write(html);
      win.document.close();
      win.document.title = title;
      win.addEventListener('afterprint', cleanup);

      const trigger = () => {
        try {
          win.focus();
          win.print();
        } catch {
          cleanup();
          return;
        }
        window.setTimeout(cleanup, 120000);
      };

      window.setTimeout(trigger, 300);
    });
  }

  /**
   * Download file with given content
   */
  private downloadFile(content: string, filename: string, mimeType: string): void {
    this.downloadBlob(new Blob([content], { type: mimeType }), filename);
  }

  /**
   * Triggers a browser download. The object URL is revoked after a delay so the
   * click handler can still read the blob (immediate revoke cancels the download).
   */
  downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.rel = 'noopener';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.setTimeout(() => URL.revokeObjectURL(url), 2000);
  }

  /**
   * HttpClient + responseType blob can surface a JSON/HTML error as a 200 Blob.
   * Reject those so the UI shows an error instead of a corrupted CSV/XLS.
   */
  async assertDownloadableExport(blob: Blob): Promise<Blob> {
    const head = (await blob.slice(0, 96).text()).replace(/^\uFEFF/, '').trimStart();
    if (head.startsWith('{') || /^<!DOCTYPE/i.test(head) || /^<html/i.test(head)) {
      const text = await blob.text();
      let message = 'Export impossible';
      try {
        const parsed = JSON.parse(text) as { message?: string; error?: string };
        message = parsed.message || parsed.error || message;
      } catch {
        if (text.trim()) {
          message = text.trim().slice(0, 280);
        }
      }
      throw { message };
    }
    return blob;
  }

  private csvCell(value: string): string {
    return `"${(value ?? '').replace(/"/g, '""')}"`;
  }

  /**
   * Escape HTML special characters
   */
  private escapeHTML(value: string): string {
    return (value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  private formatCsvDate(iso: string): string {
    const d = new Date(iso);
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    return `${dd}/${mm}/${d.getFullYear()}`;
  }

  /**
   * Format time for display
   */
  private formatTime(iso: string): string {
    return new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  /**
   * Get HTML badge for status
   */
  private getStatusBadge(statut: StatutDecisionEnum): string {
    const label = this.escapeHTML(statutLabel(statut));
    let className = 'status status-neutral';

    if (statut === StatutDecisionEnum.VALIDEE || statut === StatutDecisionEnum.APPROUVEE) {
      className = 'status status-success';
    } else if (statut === StatutDecisionEnum.REJETEE) {
      className = 'status status-danger';
    } else if (
      statut === StatutDecisionEnum.EN_ATTENTE ||
      statut === StatutDecisionEnum.EN_ATTENTE_VALIDATION ||
      statut === StatutDecisionEnum.A_REVOIR
    ) {
      className = 'status status-warn';
    } else if (statut === StatutDecisionEnum.MODIFIEE) {
      className = 'status status-info';
    }

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
