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

  it('formats confidence as percentage', () => {
    fixture.componentRef.setInput('confidence', 82.456);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('82');
    expect(text).toContain('%');
  });

  it('shows empty label when confidence is missing', () => {
    fixture.componentRef.setInput('confidence', null);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Non fournie');
  });
});
