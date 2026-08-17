import { TranslateLoader, type TranslationObject } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import ar from '../../../assets/i18n/ar.json';
import en from '../../../assets/i18n/en.json';
import fr from '../../../assets/i18n/fr.json';
import { DEFAULT_LANGUAGE, type AppLanguage } from './i18n.constants';

const LOCALES: Record<AppLanguage, TranslationObject> = {
  fr: fr as unknown as TranslationObject,
  en: en as unknown as TranslationObject,
  ar: ar as unknown as TranslationObject,
};

/** Bundled fallback used by tests and SSR when HTTP assets are unavailable. */
export class StaticTranslateLoader implements TranslateLoader {
  getTranslation(lang: string): Observable<TranslationObject> {
    return of(LOCALES[lang as AppLanguage] ?? LOCALES[DEFAULT_LANGUAGE]);
  }
}
