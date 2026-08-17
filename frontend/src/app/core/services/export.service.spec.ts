import { TestBed } from '@angular/core/testing';
import { ExportService } from './export.service';
import { StatutDecisionEnum } from '../models/decision.models';
import type { AuditRecentItemResponse } from './audit.service';

describe('ExportService', () => {
  let service: ExportService;

  const item: AuditRecentItemResponse = {
    decisionId: 'f3cabc86-de8d-4387-85ac-2f29d579fc21',
    prompt: 'Évaluation risque décrochage — accompagnement pédagogique',
    statutValidation: StatutDecisionEnum.VALIDEE,
    integrityValid: true,
    timestamp: '2026-08-17T16:47:00.000Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ExportService);
  });

  it('accepte un CSV', async () => {
    const blob = new Blob(['\ufeffsep=,\ndecisionId\n'], { type: 'text/csv' });
    await expect(service.assertDownloadableExport(blob)).resolves.toBe(blob);
  });

  it('rejette un JSON d’erreur déguisé en fichier', async () => {
    const blob = new Blob(['{"message":"Accès refusé"}'], { type: 'application/json' });
    await expect(service.assertDownloadableExport(blob)).rejects.toEqual({ message: 'Accès refusé' });
  });

  it('produit un CSV Excel FR (BOM, point-virgule, accents)', () => {
    const csv = service.buildAuditCsv([item]);
    expect(csv.startsWith('\uFEFF')).toBe(true);
    expect(csv).toContain(';');
    expect(csv).not.toMatch(/^[^;]+,[^;]+,/m);
    expect(csv).toContain('UUID Décision');
    expect(csv).toContain('Intégrité');
    expect(csv).toContain('Évaluation risque décrochage');
    expect(csv).toContain('Validée');
    expect(csv).toContain(item.decisionId);
    expect(csv).toContain('\r\n');
  });

  it('génère un rapport HTML d’audit avec en-tête professionnel', () => {
    const html = service.generateAuditPDFHTML(
      [item],
      { totalDecisions: 6, validDecisions: 6, invalidDecisions: 0, chainIntact: true },
    );
    expect(html).toContain("Rapport d’audit d’intégrité");
    expect(html).toContain('Traçabilité IA');
    expect(html).toContain(item.decisionId);
    expect(html).toContain('Évaluation risque décrochage');
    expect(html).not.toContain('about:blank');
    expect(html).toContain('Usage interne');
  });
});
