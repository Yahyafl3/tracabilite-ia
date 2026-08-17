import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { BackupAdminComponent } from './backup-admin.component';
import { BackupAdminService } from '../../../core/services/backup-admin.service';
import { provideI18nTesting } from '../../../core/i18n/provide-i18n';

describe('BackupAdminComponent', () => {
  let fixture: ComponentFixture<BackupAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BackupAdminComponent],
      providers: [
        ...provideI18nTesting(),
        {
          provide: BackupAdminService,
          useValue: {
            list: () =>
              of([
                {
                  id: 'b1',
                  createdAt: '2026-08-17T10:00:00.000Z',
                  createdByEmail: 'admin@test.fr',
                  filename: 'backup-b1.json',
                  sizeBytes: 2048,
                  packSha256: 'abc123def456abc123def456abc123def456abc123def456abc123def456abcd',
                  decisionCount: 3,
                  userCount: 2,
                  status: 'CREATED',
                  filePresent: true,
                },
              ]),
            create: () => of({}),
            verify: () => of({ valid: true }),
            restore: () => of({}),
            download: () => of(new Blob()),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BackupAdminComponent);
    fixture.detectChanges();
  });

  it('lists backups with SHA-256 hash and create action', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Sauvegarde');
    expect(text).toContain('admin@test.fr');
    expect(text).toContain('abc123def456');
    expect(text).toContain('Créer une sauvegarde');
  });
});
