import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LANGUAGE_STORAGE_KEY } from '../../core/i18n/i18n.constants';
import { provideI18nTesting } from '../../core/i18n/provide-i18n';
import { LanguageSwitcherComponent } from './language-switcher.component';

describe('LanguageSwitcherComponent', () => {
  let fixture: ComponentFixture<LanguageSwitcherComponent>;

  beforeEach(async () => {
    localStorage.removeItem(LANGUAGE_STORAGE_KEY);
    document.documentElement.removeAttribute('dir');
    document.documentElement.lang = 'fr';

    await TestBed.configureTestingModule({
      imports: [LanguageSwitcherComponent],
      providers: [...provideI18nTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(LanguageSwitcherComponent);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('renders FR, EN and AR and keeps French active by default', () => {
    const buttons = Array.from(
      fixture.nativeElement.querySelectorAll('.lang-switcher__btn'),
    ) as HTMLButtonElement[];
    expect(buttons.map((btn) => btn.textContent?.trim())).toEqual(['FR', 'EN', 'AR']);
    expect(buttons[0].classList.contains('is-active')).toBe(true);
  });

  it('switches to Arabic, persists the choice and sets RTL on the document', async () => {
    const arButton = (fixture.nativeElement as HTMLElement).querySelectorAll(
      '.lang-switcher__btn',
    )[2] as HTMLButtonElement;
    arButton.click();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(document.documentElement.dir).toBe('rtl');
    expect(document.documentElement.lang).toBe('ar');
    expect(localStorage.getItem(LANGUAGE_STORAGE_KEY)).toBe('ar');
    expect(arButton.classList.contains('is-active')).toBe(true);
  });
});
