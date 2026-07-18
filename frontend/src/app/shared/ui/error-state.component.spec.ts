import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ErrorStateComponent } from './error-state.component';

describe('ErrorStateComponent', () => {
  let fixture: ComponentFixture<ErrorStateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ErrorStateComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ErrorStateComponent);
    fixture.componentRef.setInput('message', 'Erreur de chargement des agents.');
    fixture.detectChanges();
  });

  it('renders subsection error message', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Erreur de chargement des agents');
    expect(compiled.querySelector('.ui-error-state')?.getAttribute('role')).toBe('alert');
  });
});
