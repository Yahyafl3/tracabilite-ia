import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConfidenceDisplayComponent } from './confidence-display.component';

describe('ConfidenceDisplayComponent', () => {
  let fixture: ComponentFixture<ConfidenceDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfidenceDisplayComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ConfidenceDisplayComponent);
  });

  it('formats 0.8 as 80 %', () => {
    fixture.componentRef.setInput('confidence', 0.8);
    fixture.componentRef.setInput('scale', 'ratio');
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent?.replace(/\s/g, ' ') ?? '';
    expect(text).toContain('80');
    expect(text).toContain('%');
    expect(text).not.toContain('0.8');
  });

  it('formats 0.9216 as 92,16 %', () => {
    fixture.componentRef.setInput('confidence', 0.9216);
    fixture.componentRef.setInput('scale', 'ratio');
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text.replace(/\u00a0/g, ' ')).toMatch(/92[,.]16/);
    expect(text).toContain('%');
  });

  it('shows Non fournie when confidence is null', () => {
    fixture.componentRef.setInput('confidence', null);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Non fournie');
  });

  it('shows Valeur invalide when ratio is outside [0,1]', () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    fixture.componentRef.setInput('confidence', 1.5);
    fixture.componentRef.setInput('scale', 'ratio');
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Valeur invalide');
    expect(warn).toHaveBeenCalled();
    warn.mockRestore();
  });

  it('keeps ML percent scale without multiplying', () => {
    fixture.componentRef.setInput('confidence', 85);
    fixture.componentRef.setInput('scale', 'percent');
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('85');
    expect(text).toContain('%');
  });
});
