import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

/** Défilement fluide vers une ancre de section (compatible SSR). */
@Injectable({ providedIn: 'root' })
export class ScrollService {
  private readonly platformId = inject(PLATFORM_ID);

  scrollTo(id: string): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    document
      .getElementById(id)
      ?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  scrollToTop(): void {
    if (isPlatformBrowser(this.platformId)) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }
}
