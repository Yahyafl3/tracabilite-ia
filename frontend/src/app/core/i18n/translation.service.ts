import { computed, inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { TranslateService, type InterpolationParameters } from '@ngx-translate/core';
import { PrimeNG } from 'primeng/config';
import type { Translation } from 'primeng/api';
import { Observable, tap } from 'rxjs';
import {
  APP_LANGUAGES,
  DEFAULT_LANGUAGE,
  LANGUAGE_LOCALES,
  LANGUAGE_OPTIONS,
  LANGUAGE_STORAGE_KEY,
  isAppLanguage,
  languageDirection,
  type AppLanguage,
} from './i18n.constants';
import { bindLabelTranslator } from './label-translator';

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private readonly translate = inject(TranslateService);
  private readonly primeng = inject(PrimeNG, { optional: true });
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  readonly options = LANGUAGE_OPTIONS;

  readonly currentLang = computed<AppLanguage>(() => {
    const lang = this.translate.currentLang();
    return isAppLanguage(lang) ? lang : DEFAULT_LANGUAGE;
  });

  readonly isRtl = computed(() => this.currentLang() === 'ar');

  readonly localeId = computed(() => LANGUAGE_LOCALES[this.currentLang()]);

  init(preferred?: AppLanguage): Observable<unknown> {
    this.translate.addLangs([...APP_LANGUAGES]);
    this.translate.setFallbackLang(DEFAULT_LANGUAGE);
    return this.use(preferred ?? this.readStoredLanguage(), preferred == null);
  }

  use(lang: AppLanguage, persist = true): Observable<unknown> {
    return this.translate.use(lang).pipe(
      tap(() => {
        this.bindLabels();
        this.applyDocument(lang);
        this.applyPrimeNG();
        if (persist) {
          this.persist(lang);
        }
      }),
    );
  }

  private bindLabels(): void {
    bindLabelTranslator((key, params) => this.t(key, params));
  }

  t(key: string, params?: InterpolationParameters): string {
    const value = this.translate.instant(key, params);
    return typeof value === 'string' ? value : key;
  }

  private readStoredLanguage(): AppLanguage {
    if (!this.isBrowser) {
      return DEFAULT_LANGUAGE;
    }
    try {
      const stored = localStorage.getItem(LANGUAGE_STORAGE_KEY);
      if (isAppLanguage(stored)) {
        return stored;
      }
    } catch {
      // Private mode / blocked storage.
    }
    return DEFAULT_LANGUAGE;
  }

  private persist(lang: AppLanguage): void {
    if (!this.isBrowser) {
      return;
    }
    try {
      localStorage.setItem(LANGUAGE_STORAGE_KEY, lang);
    } catch {
      // Ignore quota / privacy errors.
    }
  }

  private applyDocument(lang: AppLanguage): void {
    if (!this.isBrowser) {
      return;
    }
    const dir = languageDirection(lang);
    document.documentElement.lang = lang;
    document.documentElement.dir = dir;
    document.documentElement.classList.toggle('app-rtl', dir === 'rtl');
  }

  private applyPrimeNG(): void {
    if (!this.primeng) {
      return;
    }
    const translation = this.translate.instant('primeng');
    if (translation && typeof translation === 'object') {
      this.primeng.setTranslation(translation as unknown as Translation);
    }
  }
}
