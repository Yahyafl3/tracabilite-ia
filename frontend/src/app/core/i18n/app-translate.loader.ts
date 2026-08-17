import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { TranslateLoader, type TranslationObject } from '@ngx-translate/core';
import { catchError, type Observable } from 'rxjs';
import { StaticTranslateLoader } from './static-translate.loader';

@Injectable()
export class AppTranslateLoader implements TranslateLoader {
  private readonly http = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly bundled = new StaticTranslateLoader();

  getTranslation(lang: string): Observable<TranslationObject> {
    if (!isPlatformBrowser(this.platformId)) {
      return this.bundled.getTranslation(lang);
    }

    return this.http.get<TranslationObject>(`/assets/i18n/${lang}.json`).pipe(
      catchError(() => this.bundled.getTranslation(lang)),
    );
  }
}
