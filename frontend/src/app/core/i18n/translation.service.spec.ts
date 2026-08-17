import { TestBed } from '@angular/core/testing';
import { providePrimeNG } from 'primeng/config';
import { PrimeNG } from 'primeng/config';
import { LANGUAGE_STORAGE_KEY } from './i18n.constants';
import { provideI18nTesting } from './provide-i18n';
import { TranslationService } from './translation.service';

describe('TranslationService', () => {
  let service: TranslationService;

  beforeEach(async () => {
    localStorage.removeItem(LANGUAGE_STORAGE_KEY);
    document.documentElement.dir = 'ltr';
    document.documentElement.lang = 'fr';

    await TestBed.configureTestingModule({
      providers: [...provideI18nTesting(), providePrimeNG({})],
    }).compileComponents();

    service = TestBed.inject(TranslationService);
    await new Promise((resolve) => setTimeout(resolve, 0));
  });

  afterEach(async () => {
    await new Promise<void>((resolve, reject) => {
      service.use('fr', false).subscribe({ next: () => resolve(), error: reject });
    });
    localStorage.removeItem(LANGUAGE_STORAGE_KEY);
  });

  it('defaults to French and exposes domain labels', () => {
    expect(service.currentLang()).toBe('fr');
    expect(service.t('nav.dashboard')).toBe('Tableau de bord');
    expect(service.t('risk.high')).toBe('Risque Élevé');
    expect(service.t('dashboard.kpis.avgCompliance')).toBe('Conformité Moyenne');
  });

  it('applies English copy, LTR and PrimeNG empty-state text', async () => {
    await new Promise<void>((resolve, reject) => {
      service.use('en').subscribe({ next: () => resolve(), error: reject });
    });

    expect(service.currentLang()).toBe('en');
    expect(service.isRtl()).toBe(false);
    expect(document.documentElement.dir).toBe('ltr');
    expect(service.t('nav.dashboard')).toBe('Dashboard');
    expect(TestBed.inject(PrimeNG).getTranslation('emptyMessage')).toBe('No results found');
  });

  it('applies Arabic copy and RTL on the html element', async () => {
    await new Promise<void>((resolve, reject) => {
      service.use('ar').subscribe({ next: () => resolve(), error: reject });
    });

    expect(service.currentLang()).toBe('ar');
    expect(service.isRtl()).toBe(true);
    expect(document.documentElement.dir).toBe('rtl');
    expect(document.documentElement.lang).toBe('ar');
    expect(service.t('nav.dashboard')).toBe('لوحة التحكم');
    expect(localStorage.getItem(LANGUAGE_STORAGE_KEY)).toBe('ar');
  });
});
