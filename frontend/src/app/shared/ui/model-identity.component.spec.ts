import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ModelIdentityComponent } from './model-identity.component';

describe('ModelIdentityComponent', () => {
  let fixture: ComponentFixture<ModelIdentityComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModelIdentityComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ModelIdentityComponent);
    fixture.componentRef.setInput('modelName', 'LogisticRegression');
    fixture.componentRef.setInput('modelVersion', '2.1.0');
    fixture.componentRef.setInput('analyzedAt', '2026-07-18T10:00:00.000Z');
    fixture.detectChanges();
  });

  it('shows model name and version from backend payload', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('LogisticRegression');
    expect(text).toContain('2.1.0');
  });
});
