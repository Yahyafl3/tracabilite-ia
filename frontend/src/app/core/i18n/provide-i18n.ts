import { inject, provideAppInitializer, type EnvironmentProviders, type Provider } from '@angular/core';
import { provideTranslateLoader, provideTranslateService } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';
import { AppTranslateLoader } from './app-translate.loader';
import { DEFAULT_LANGUAGE } from './i18n.constants';
import { StaticTranslateLoader } from './static-translate.loader';
import { TranslationService } from './translation.service';

export function provideAppI18n(): Array<Provider | EnvironmentProviders> {
  return [
    provideTranslateService({
      fallbackLang: DEFAULT_LANGUAGE,
      loader: provideTranslateLoader(AppTranslateLoader),
    }),
    provideAppInitializer(() => firstValueFrom(inject(TranslationService).init())),
  ];
}

/** Loads bundled JSON so unit tests do not depend on HTTP assets. */
export function provideI18nTesting(): Array<Provider | EnvironmentProviders> {
  return [
    provideTranslateService({
      fallbackLang: DEFAULT_LANGUAGE,
      lang: DEFAULT_LANGUAGE,
      loader: provideTranslateLoader(StaticTranslateLoader),
    }),
    provideAppInitializer(() => firstValueFrom(inject(TranslationService).init(DEFAULT_LANGUAGE))),
  ];
}
