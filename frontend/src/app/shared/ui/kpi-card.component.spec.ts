import { ComponentFixture, TestBed } from '@angular/core/testing';
import { KpiCardComponent } from './kpi-card.component';

describe('KpiCardComponent', () => {
  let fixture: ComponentFixture<KpiCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KpiCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(KpiCardComponent);
    fixture.componentRef.setInput('label', 'Total décisions');
    fixture.componentRef.setInput('value', 42);
    fixture.componentRef.setInput('icon', 'file-text');
    fixture.detectChanges();
  });

  it('renders label and value', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('.ui-kpi-card__label')?.textContent).toBe('Total décisions');
    expect(compiled.querySelector('.ui-kpi-card__value')?.textContent).toBe('42');
  });
});
