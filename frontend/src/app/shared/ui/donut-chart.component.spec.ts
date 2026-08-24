import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DonutChartComponent, DonutChartDataPoint } from './donut-chart.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('DonutChartComponent', () => {
  let component: DonutChartComponent;
  let fixture: ComponentFixture<DonutChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DonutChartComponent, NoopAnimationsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(DonutChartComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Empty State', () => {
    it('should display empty state when data is empty array', () => {
      component.title = 'Test Chart';
      component.data = [];
      fixture.detectChanges();

      expect(component.isEmpty).toBe(true);
      const emptyState = fixture.nativeElement.querySelector('.empty-state');
      expect(emptyState).toBeTruthy();
      expect(emptyState.textContent).toContain('Aucune donnée disponible');
    });

    it('should display empty state when all values are zero', () => {
      component.title = 'Test Chart';
      component.data = [
        { label: 'Item 1', value: 0, color: '#ff0000' },
        { label: 'Item 2', value: 0, color: '#00ff00' }
      ];
      fixture.detectChanges();

      expect(component.isEmpty).toBe(true);
      const emptyState = fixture.nativeElement.querySelector('.empty-state');
      expect(emptyState).toBeTruthy();
    });

    it('should display empty state when data is null', () => {
      component.title = 'Test Chart';
      component.data = null as any;
      fixture.detectChanges();

      expect(component.isEmpty).toBe(true);
    });
  });

  describe('Chart Rendering', () => {
    it('should render chart when valid data is provided', () => {
      component.title = 'Status Distribution';
      component.data = [
        { label: 'Validée', value: 10, color: '#22c55e' },
        { label: 'En attente', value: 5, color: '#f59e0b' },
        { label: 'Rejetée', value: 2, color: '#ef4444' }
      ];
      fixture.detectChanges();

      expect(component.isEmpty).toBe(false);
      const chart = fixture.nativeElement.querySelector('p-chart');
      expect(chart).toBeTruthy();
    });

    it('should build correct Chart.js data structure', () => {
      const testData: DonutChartDataPoint[] = [
        { label: 'Category A', value: 30, color: '#3b82f6' },
        { label: 'Category B', value: 70, color: '#8b5cf6' }
      ];

      component.title = 'Test Chart';
      component.data = testData;
      component.ngOnChanges({ data: { currentValue: testData, previousValue: null, firstChange: true, isFirstChange: () => true } });

      expect(component.chartData).toBeDefined();
      expect(component.chartData.labels).toEqual(['Category A', 'Category B']);
      expect(component.chartData.datasets[0].data).toEqual([30, 70]);
      expect(component.chartData.datasets[0].backgroundColor).toEqual(['#3b82f6', '#8b5cf6']);
    });

    it('should configure chart options with legend at bottom', () => {
      component.title = 'Test Chart';
      component.data = [
        { label: 'Item 1', value: 50, color: '#ff0000' }
      ];
      component.ngOnChanges({ data: { currentValue: component.data, previousValue: null, firstChange: true, isFirstChange: () => true } });

      expect(component.chartOptions).toBeDefined();
      expect(component.chartOptions.plugins.legend.position).toBe('bottom');
    });

    it('should configure percentage tooltips', () => {
      component.title = 'Test Chart';
      component.data = [
        { label: 'Item 1', value: 25, color: '#ff0000' },
        { label: 'Item 2', value: 75, color: '#00ff00' }
      ];
      component.ngOnChanges({ data: { currentValue: component.data, previousValue: null, firstChange: true, isFirstChange: () => true } });

      const tooltipCallback = component.chartOptions.plugins.tooltip.callbacks.label;
      const mockContext = {
        label: 'Item 1',
        parsed: 25,
        dataset: { data: [25, 75] }
      };

      const result = tooltipCallback(mockContext);
      expect(result).toBe('Item 1: 25 (25.0%)');
    });

    it('should handle percentage calculation correctly', () => {
      component.title = 'Test Chart';
      component.data = [
        { label: 'A', value: 33, color: '#ff0000' },
        { label: 'B', value: 67, color: '#00ff00' }
      ];
      component.ngOnChanges({ data: { currentValue: component.data, previousValue: null, firstChange: true, isFirstChange: () => true } });

      const tooltipCallback = component.chartOptions.plugins.tooltip.callbacks.label;
      const mockContext = {
        label: 'A',
        parsed: 33,
        dataset: { data: [33, 67] }
      };

      const result = tooltipCallback(mockContext);
      expect(result).toBe('A: 33 (33.0%)');
    });
  });

  describe('Data Updates', () => {
    it('should update chart when data changes', () => {
      component.title = 'Test Chart';
      component.data = [
        { label: 'Initial', value: 10, color: '#ff0000' }
      ];
      fixture.detectChanges();

      expect(component.chartData.labels).toEqual(['Initial']);

      // Update data
      component.data = [
        { label: 'Updated', value: 20, color: '#00ff00' }
      ];
      component.ngOnChanges({ data: { currentValue: component.data, previousValue: [{ label: 'Initial', value: 10, color: '#ff0000' }], firstChange: false, isFirstChange: () => false } });
      fixture.detectChanges();

      expect(component.chartData.labels).toEqual(['Updated']);
      expect(component.chartData.datasets[0].data).toEqual([20]);
    });

    it('should transition from empty to populated state', () => {
      component.title = 'Test Chart';
      component.data = [];
      fixture.detectChanges();

      expect(component.isEmpty).toBe(true);

      // Add data
      component.data = [{ label: 'New', value: 5, color: '#ff0000' }];
      component.ngOnChanges({ data: { currentValue: component.data, previousValue: [], firstChange: false, isFirstChange: () => false } });
      fixture.detectChanges();

      expect(component.isEmpty).toBe(false);
    });

    it('should transition from populated to empty state', () => {
      component.title = 'Test Chart';
      component.data = [{ label: 'Item', value: 5, color: '#ff0000' }];
      fixture.detectChanges();

      expect(component.isEmpty).toBe(false);

      // Clear data
      component.data = [];
      component.ngOnChanges({ data: { currentValue: [], previousValue: component.data, firstChange: false, isFirstChange: () => false } });
      fixture.detectChanges();

      expect(component.isEmpty).toBe(true);
    });
  });

  describe('Title Display', () => {
    it('should display the provided title', () => {
      component.title = 'My Custom Chart Title';
      component.data = [{ label: 'Item', value: 10, color: '#ff0000' }];
      fixture.detectChanges();

      const card = fixture.nativeElement.querySelector('p-card');
      expect(card.getAttribute('ng-reflect-header')).toBe('My Custom Chart Title');
    });
  });

  describe('Chart Cutout Configuration', () => {
    it('should configure 60% cutout for donut appearance', () => {
      component.title = 'Test Chart';
      component.data = [{ label: 'Item', value: 10, color: '#ff0000' }];
      component.ngOnChanges({ data: { currentValue: component.data, previousValue: null, firstChange: true, isFirstChange: () => true } });

      expect(component.chartOptions.cutout).toBe('60%');
    });
  });
});
