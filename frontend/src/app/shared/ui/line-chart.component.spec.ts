import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LineChartComponent, LineChartDataPoint } from './line-chart.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('LineChartComponent', () => {
  let component: LineChartComponent;
  let fixture: ComponentFixture<LineChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LineChartComponent, NoopAnimationsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(LineChartComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Empty State', () => {
    it('should display empty state when data is empty array', () => {
      component.title = 'Test Line Chart';
      component.data = [];
      fixture.detectChanges();

      expect(component.isEmpty).toBe(true);
      const emptyState = fixture.nativeElement.querySelector('.empty-state');
      expect(emptyState).toBeTruthy();
      expect(emptyState.textContent).toContain('Aucune donnée disponible');
    });

    it('should display empty state when all counts are zero', () => {
      component.title = 'Test Line Chart';
      component.data = [
        { date: '2024-01-01', count: 0 },
        { date: '2024-01-02', count: 0 }
      ];
      fixture.detectChanges();

      expect(component.isEmpty).toBe(true);
      const emptyState = fixture.nativeElement.querySelector('.empty-state');
      expect(emptyState).toBeTruthy();
    });

    it('should display empty state when data is null', () => {
      component.title = 'Test Line Chart';
      component.data = null as any;
      fixture.detectChanges();

      expect(component.isEmpty).toBe(true);
    });
  });

  describe('Chart Rendering', () => {
    it('should render chart when valid data is provided', () => {
      component.title = 'Timeline Evolution';
      component.data = [
        { date: '2024-01-01', count: 5 },
        { date: '2024-01-02', count: 10 },
        { date: '2024-01-03', count: 7 }
      ];
      fixture.detectChanges();

      expect(component.isEmpty).toBe(false);
      const chart = fixture.nativeElement.querySelector('p-chart');
      expect(chart).toBeTruthy();
    });

    it('should build correct Chart.js line data structure', () => {
      const testData: LineChartDataPoint[] = [
        { date: '2024-01-01', count: 5 },
        { date: '2024-01-02', count: 10 }
      ];

      component.title = 'Test Line Chart';
      component.data = testData;
      component.ngOnChanges({ 
        data: { 
          currentValue: testData, 
          previousValue: null, 
          firstChange: true, 
          isFirstChange: () => true 
        } 
      });

      expect(component.chartData).toBeDefined();
      expect(component.chartData.labels.length).toBe(2);
      expect(component.chartData.datasets[0].data).toEqual([5, 10]);
      expect(component.chartData.datasets[0].label).toBe('Décisions');
      expect(component.chartData.datasets[0].borderColor).toBe('#3b82f6');
    });

    it('should configure Y-axis to begin at zero', () => {
      component.title = 'Test Line Chart';
      component.data = [
        { date: '2024-01-01', count: 50 }
      ];
      component.ngOnChanges({ 
        data: { 
          currentValue: component.data, 
          previousValue: null, 
          firstChange: true, 
          isFirstChange: () => true 
        } 
      });

      expect(component.chartOptions).toBeDefined();
      expect(component.chartOptions.scales.y.beginAtZero).toBe(true);
      expect(component.chartOptions.scales.y.ticks.stepSize).toBe(1);
    });

    it('should hide legend in chart options', () => {
      component.title = 'Test Line Chart';
      component.data = [
        { date: '2024-01-01', count: 5 }
      ];
      component.ngOnChanges({ 
        data: { 
          currentValue: component.data, 
          previousValue: null, 
          firstChange: true, 
          isFirstChange: () => true 
        } 
      });

      expect(component.chartOptions.plugins.legend.display).toBe(false);
    });

    it('should configure smooth line with tension', () => {
      component.title = 'Test Line Chart';
      component.data = [
        { date: '2024-01-01', count: 5 }
      ];
      component.ngOnChanges({ 
        data: { 
          currentValue: component.data, 
          previousValue: null, 
          firstChange: true, 
          isFirstChange: () => true 
        } 
      });

      expect(component.chartData.datasets[0].tension).toBe(0.4);
      expect(component.chartData.datasets[0].fill).toBe(false);
    });
  });

  describe('Date Formatting', () => {
    it('should format dates in French locale', () => {
      component.title = 'Test Line Chart';
      component.data = [
        { date: '2024-01-15', count: 10 },
        { date: '2024-02-20', count: 15 }
      ];
      component.ngOnChanges({ 
        data: { 
          currentValue: component.data, 
          previousValue: null, 
          firstChange: true, 
          isFirstChange: () => true 
        } 
      });

      expect(component.chartData.labels.length).toBe(2);
      // French locale formatting: "15 janv.", "20 févr."
      expect(component.chartData.labels[0]).toContain('15');
      expect(component.chartData.labels[1]).toContain('20');
    });

    it('should handle different date formats', () => {
      component.title = 'Test Line Chart';
      component.data = [
        { date: '2024-03-01', count: 5 },
        { date: '2024-03-15', count: 8 },
        { date: '2024-03-30', count: 3 }
      ];
      component.ngOnChanges({ 
        data: { 
          currentValue: component.data, 
          previousValue: null, 
          firstChange: true, 
          isFirstChange: () => true 
        } 
      });

      expect(component.chartData.labels.length).toBe(3);
      component.chartData.labels.forEach((label: string) => {
        expect(typeof label).toBe('string');
        expect(label.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Data Updates', () => {
    it('should update chart when data changes', () => {
      component.title = 'Test Line Chart';
      component.data = [
        { date: '2024-01-01', count: 10 }
      ];
      fixture.detectChanges();

      expect(component.chartData.datasets[0].data).toEqual([10]);

      // Update data
      component.data = [
        { date: '2024-01-01', count: 20 },
        { date: '2024-01-02', count: 30 }
      ];
      component.ngOnChanges({ 
        data: { 
          currentValue: component.data, 
          previousValue: [{ date: '2024-01-01', count: 10 }], 
          firstChange: false, 
          isFirstChange: () => false 
        } 
      });
      fixture.detectChanges();

      expect(component.chartData.datasets[0].data).toEqual([20, 30]);
      expect(component.chartData.labels.length).toBe(2);
    });

    it('should transition from empty to populated state', () => {
      component.title = 'Test Line Chart';
      component.data = [];
      fixture.detectChanges();

      expect(component.isEmpty).toBe(true);

      // Add data
      component.data = [{ date: '2024-01-01', count: 5 }];
      component.ngOnChanges({ 
        data: { 
          currentValue: component.data, 
          previousValue: [], 
          firstChange: false, 
          isFirstChange: () => false 
        } 
      });
      fixture.detectChanges();

      expect(component.isEmpty).toBe(false);
    });

    it('should transition from populated to empty state', () => {
      component.title = 'Test Line Chart';
      component.data = [{ date: '2024-01-01', count: 5 }];
      fixture.detectChanges();

      expect(component.isEmpty).toBe(false);

      // Clear data
      component.data = [];
      component.ngOnChanges({ 
        data: { 
          currentValue: [], 
          previousValue: [{ date: '2024-01-01', count: 5 }], 
          firstChange: false, 
          isFirstChange: () => false 
        } 
      });
      fixture.detectChanges();

      expect(component.isEmpty).toBe(true);
    });
  });

  describe('Title Display', () => {
    it('should display the provided title', () => {
      component.title = 'Évolution Temporelle';
      component.data = [{ date: '2024-01-01', count: 10 }];
      fixture.detectChanges();

      const card = fixture.nativeElement.querySelector('p-card');
      expect(card.getAttribute('ng-reflect-header')).toBe('Évolution Temporelle');
    });
  });

  describe('Tooltip Configuration', () => {
    it('should configure tooltip callbacks for decision count display', () => {
      component.title = 'Test Line Chart';
      component.data = [
        { date: '2024-01-01', count: 15 }
      ];
      component.ngOnChanges({ 
        data: { 
          currentValue: component.data, 
          previousValue: null, 
          firstChange: true, 
          isFirstChange: () => true 
        } 
      });

      const labelCallback = component.chartOptions.plugins.tooltip.callbacks.label;
      const mockContext = {
        parsed: { y: 15 }
      };

      const result = labelCallback(mockContext);
      expect(result).toBe('Décisions: 15');
    });
  });

  describe('Edge Cases', () => {
    it('should handle single data point', () => {
      component.title = 'Test Line Chart';
      component.data = [{ date: '2024-01-01', count: 1 }];
      component.ngOnChanges({ 
        data: { 
          currentValue: component.data, 
          previousValue: null, 
          firstChange: true, 
          isFirstChange: () => true 
        } 
      });

      expect(component.isEmpty).toBe(false);
      expect(component.chartData.labels.length).toBe(1);
      expect(component.chartData.datasets[0].data).toEqual([1]);
    });

    it('should handle large datasets', () => {
      component.title = 'Test Line Chart';
      component.data = Array.from({ length: 30 }, (_, i) => ({
        date: `2024-01-${String(i + 1).padStart(2, '0')}`,
        count: Math.floor(Math.random() * 100)
      }));
      component.ngOnChanges({ 
        data: { 
          currentValue: component.data, 
          previousValue: null, 
          firstChange: true, 
          isFirstChange: () => true 
        } 
      });

      expect(component.isEmpty).toBe(false);
      expect(component.chartData.labels.length).toBe(30);
      expect(component.chartData.datasets[0].data.length).toBe(30);
    });

    it('should not display chart when only one data point is zero', () => {
      component.title = 'Test Line Chart';
      component.data = [{ date: '2024-01-01', count: 0 }];
      fixture.detectChanges();

      expect(component.isEmpty).toBe(true);
    });

    it('should display chart when at least one count is non-zero', () => {
      component.title = 'Test Line Chart';
      component.data = [
        { date: '2024-01-01', count: 0 },
        { date: '2024-01-02', count: 1 },
        { date: '2024-01-03', count: 0 }
      ];
      fixture.detectChanges();

      expect(component.isEmpty).toBe(false);
    });
  });
});
