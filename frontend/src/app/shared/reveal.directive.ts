import {
  AfterViewInit,
  Directive,
  ElementRef,
  Input,
  OnDestroy,
  PLATFORM_ID,
  Renderer2,
  inject,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

/**
 * Animation légère au scroll (fade-in / slide-up).
 * Ajoute la classe `.reveal` puis `.is-visible` quand l'élément entre
 * dans le viewport (via IntersectionObserver). Côté serveur (SSR), le
 * contenu reste visible pour préserver le SEO et éviter tout clignotement.
 */
@Directive({
  selector: '[appReveal]',
  standalone: true,
})
export class RevealDirective implements AfterViewInit, OnDestroy {
  /** Délai (ms) avant l'animation — utile pour créer un effet de cascade. */
  @Input() revealDelay = 0;

  private readonly el = inject(ElementRef<HTMLElement>);
  private readonly renderer = inject(Renderer2);
  private readonly platformId = inject(PLATFORM_ID);
  private observer?: IntersectionObserver;

  ngAfterViewInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    const node: HTMLElement = this.el.nativeElement;
    this.renderer.addClass(node, 'reveal');

    if (this.revealDelay) {
      this.renderer.setStyle(node, 'transition-delay', `${this.revealDelay}ms`);
    }

    if (typeof IntersectionObserver === 'undefined') {
      this.renderer.addClass(node, 'is-visible');
      return;
    }

    this.observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            this.renderer.addClass(node, 'is-visible');
            this.observer?.unobserve(node);
          }
        });
      },
      { threshold: 0.15, rootMargin: '0px 0px -60px 0px' }
    );

    this.observer.observe(node);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}
