import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { SupportAdminComponent } from './support-admin.component';
import { SupportMessage, SupportService } from '../../../core/services/support.service';

describe('SupportAdminComponent', () => {
  let fixture: ComponentFixture<SupportAdminComponent>;
  let support: {
    getMessages: ReturnType<typeof vi.fn>;
    getMessageById: ReturnType<typeof vi.fn>;
    updateStatus: ReturnType<typeof vi.fn>;
  };

  const sample: SupportMessage = {
    id: '11111111-1111-1111-1111-111111111111',
    name: 'Jane Doe',
    email: 'jane@example.com',
    subject: 'Problème de connexion',
    message: 'Description détaillée du problème.',
    status: 'NEW',
    createdAt: '2026-07-22T10:00:00',
    updatedAt: '2026-07-22T10:00:00',
    processedAt: null,
    processedById: null,
    processedByName: null,
  };

  beforeEach(async () => {
    support = {
      getMessages: vi.fn(() =>
        of({
          content: [sample],
          page: 0,
          size: 10,
          totalElements: 1,
          totalPages: 1,
          last: true,
        }),
      ),
      getMessageById: vi.fn(() => of(sample)),
      updateStatus: vi.fn(() => of({ ...sample, status: 'IN_PROGRESS' as const })),
    };

    await TestBed.configureTestingModule({
      imports: [SupportAdminComponent],
      providers: [{ provide: SupportService, useValue: support }],
    }).compileComponents();

    fixture = TestBed.createComponent(SupportAdminComponent);
    fixture.detectChanges();
  });

  it('loads and displays support messages table', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Support');
    expect(text).toContain('Jane Doe');
    expect(text).toContain('Problème de connexion');
    expect(text).toContain('NEW');
    expect(support.getMessages).toHaveBeenCalled();
  });

  it('filters by status', () => {
    fixture.componentInstance.onStatusFilterChange('RESOLVED');
    expect(support.getMessages).toHaveBeenCalledWith(
      expect.objectContaining({ status: 'RESOLVED', page: 0 }),
    );
  });

  it('shows empty state when no messages', () => {
    support.getMessages.mockReturnValue(
      of({
        content: [],
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0,
        last: true,
      }),
    );
    fixture.componentInstance.loadMessages();
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Aucune demande de support',
    );
  });

  it('opens detail dialog and updates status', () => {
    fixture.componentInstance.openDetail(sample);
    fixture.detectChanges();
    expect(fixture.componentInstance.detailOpen()).toBe(true);
    expect(support.getMessageById).toHaveBeenCalledWith(sample.id);

    fixture.componentInstance.updateSelectedStatus('CLOSED');
    expect(support.updateStatus).toHaveBeenCalledWith(sample.id, 'CLOSED');
  });

  it('shows error when loading fails', () => {
    support.getMessages.mockReturnValue(throwError(() => ({ message: 'Accès refusé' })));
    fixture.componentInstance.loadMessages();
    fixture.detectChanges();
    expect(fixture.componentInstance.error()).toBeTruthy();
  });
});
