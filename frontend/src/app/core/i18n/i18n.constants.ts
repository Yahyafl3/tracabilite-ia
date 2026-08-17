export const APP_LANGUAGES = ['fr', 'en', 'ar'] as const;

export type AppLanguage = (typeof APP_LANGUAGES)[number];

export const DEFAULT_LANGUAGE: AppLanguage = 'fr';

export const LANGUAGE_STORAGE_KEY = 'tracabilite.lang';

export const LANGUAGE_LOCALES: Record<AppLanguage, string> = {
  fr: 'fr-FR',
  en: 'en-GB',
  ar: 'ar-MA',
};

export interface LanguageOption {
  code: AppLanguage;
  shortLabel: string;
  dir: 'ltr' | 'rtl';
}

export const LANGUAGE_OPTIONS: LanguageOption[] = [
  { code: 'fr', shortLabel: 'FR', dir: 'ltr' },
  { code: 'en', shortLabel: 'EN', dir: 'ltr' },
  { code: 'ar', shortLabel: 'AR', dir: 'rtl' },
];

export function isAppLanguage(value: string | null | undefined): value is AppLanguage {
  return APP_LANGUAGES.includes(value as AppLanguage);
}

export function languageDirection(lang: AppLanguage): 'ltr' | 'rtl' {
  return lang === 'ar' ? 'rtl' : 'ltr';
}
