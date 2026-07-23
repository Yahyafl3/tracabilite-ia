import { TestBed } from '@angular/core/testing';
import { LayoutService, THEME_STORAGE_KEY } from './layout.service';

describe('LayoutService', () => {
  beforeEach(() => {
    localStorage.removeItem(THEME_STORAGE_KEY);
    document.documentElement.classList.remove('app-dark');
    document.documentElement.removeAttribute('data-theme');
    TestBed.resetTestingModule();
  });

  it('persists dark mode preference in localStorage', () => {
    const service = TestBed.inject(LayoutService);
    service.setDarkTheme(true);

    expect(localStorage.getItem(THEME_STORAGE_KEY)).toBe('dark');
    expect(document.documentElement.classList.contains('app-dark')).toBe(true);
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');

    service.setDarkTheme(false);
    expect(localStorage.getItem(THEME_STORAGE_KEY)).toBe('light');
    expect(document.documentElement.classList.contains('app-dark')).toBe(false);
  });

  it('restores dark theme from localStorage on init', () => {
    localStorage.setItem(THEME_STORAGE_KEY, 'dark');
    const service = TestBed.inject(LayoutService);
    expect(service.isDarkTheme()).toBe(true);
    expect(document.documentElement.classList.contains('app-dark')).toBe(true);
  });

  it('toggles mobile menu state', () => {
    const service = TestBed.inject(LayoutService);
    vi.spyOn(service, 'isDesktop').mockReturnValue(false);

    expect(service.isMobileMenuOpen()).toBe(false);
    service.onMenuToggle();
    expect(service.layoutState().mobileMenuActive).toBe(true);
    service.closeMobileMenu();
    expect(service.isMobileMenuOpen()).toBe(false);
  });
});
