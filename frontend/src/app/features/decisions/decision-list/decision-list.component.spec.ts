import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { DecisionListComponent } from './decision-list.component';
import { DecisionService } from '../../../core/services/decision.service';

describe('DecisionListComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DecisionListComponent],
      providers: [
        provideRouter([]),
        {
          provide: DecisionService,
          useValue: {
            search: () =>
              of({
                content: [],
                totalElements: 0,
                page: 0,
                size: 10,
              }),
          },
        },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(DecisionListComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
  });
});
