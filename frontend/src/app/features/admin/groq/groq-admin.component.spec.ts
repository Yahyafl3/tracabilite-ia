import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { GroqAdminComponent } from './groq-admin.component';
import { GroqAdminService } from '../../../core/services/groq-admin.service';
import { provideI18nTesting } from '../../../core/i18n/provide-i18n';

describe('GroqAdminComponent', () => {
  let fixture: ComponentFixture<GroqAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroqAdminComponent],
      providers: [
        ...provideI18nTesting(),
        {
          provide: GroqAdminService,
          useValue: {
            getStatus: () =>
              of({
                configured: true,
                reachable: true,
                successfulResponses: 12,
                models: [
                  {
                    agent: 'AGENT_1',
                    displayName: 'Groq Compound Mini',
                    modelId: 'groq/compound-mini',
                    available: true,
                  },
                ],
              }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GroqAdminComponent);
    fixture.detectChanges();
  });

  it('loads Groq status without exposing API key value', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Clé API configurée');
    expect(text).not.toContain('gsk_');
    expect(text).toContain('GROQ');
    expect(text).toContain('groq/compound-mini');
  });
});
