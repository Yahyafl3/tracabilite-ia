let translateFn: ((key: string, params?: Record<string, unknown>) => string) | null = null;

/** Bound by TranslationService so labels follow the active language. */
export function bindLabelTranslator(
  fn: ((key: string, params?: Record<string, unknown>) => string) | null,
): void {
  translateFn = fn;
}

export function i18nLabel(key: string, fallback: string, params?: Record<string, unknown>): string {
  if (!translateFn) {
    if (!params) return fallback;
    return fallback.replace(/\{\{(\w+)\}\}/g, (_, name: string) => String(params[name] ?? ''));
  }
  const value = translateFn(key, params);
  return value && value !== key ? value : fallback;
}
