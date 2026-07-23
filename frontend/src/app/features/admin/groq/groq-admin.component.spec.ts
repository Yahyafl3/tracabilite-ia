import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { GroqAdminComponent } from './groq-admin.component';
import { GroqAdminService } from '../../../core/services/groq-admin.service';

describe('GroqAdminComponent', () => {
  let fixture: ComponentFixture<GroqAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroqAdminComponent],
      providers: [
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
                    displayName: 'Llama 3.3 70B Versatile',
                    modelId: 'llama-3.3-70b-versatile',
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
    expect(text).toContain('llama-3.3-70b-versatile');
  });
});
