import { Component } from '@angular/core';
import { Router } from '@angular/router';

/**
 * Legacy Material unauthorized page replaced by /403 (SystemPageComponent).
 * Kept as a thin redirect shim if any lazy import still resolves this path.
 */
@Component({
  selector: 'app-unauthorized',
  standalone: true,
  template: '',
})
export class UnauthorizedComponent {
  constructor(router: Router) {
    void router.navigateByUrl('/403', { replaceUrl: true });
  }
}
