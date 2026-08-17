import { Component, inject } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { TranslationService } from '../../core/i18n/translation.service';
import type { AppLanguage } from '../../core/i18n/i18n.constants';

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [TranslatePipe],
  templateUrl: './language-switcher.component.html',
  styleUrl: './language-switcher.component.scss',
})
export class LanguageSwitcherComponent {
  readonly i18n = inject(TranslationService);

  select(lang: AppLanguage): void {
    if (lang === this.i18n.currentLang()) {
      return;
    }
    this.i18n.use(lang).subscribe();
  }
}
