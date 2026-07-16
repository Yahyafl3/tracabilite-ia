import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

/**
 * Unauthorized Component
 * Displayed when user tries to access forbidden resources
 */
@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="unauthorized-container">
      <mat-card class="unauthorized-card">
        <mat-card-content>
          <mat-icon class="error-icon">block</mat-icon>
          <h1>Accès Refusé</h1>
          <p>Vous n'avez pas les permissions nécessaires pour accéder à cette ressource.</p>
          <div class="actions">
            <a mat-raised-button color="primary" routerLink="/decisions">
              <mat-icon>list</mat-icon>
              Retour aux décisions
            </a>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .unauthorized-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--grad-hero), var(--bg-soft);
      padding: 24px;
    }

    .unauthorized-card {
      max-width: 520px;
      text-align: center;
      padding: 48px;

      .error-icon {
        font-size: 88px;
        width: 88px;
        height: 88px;
        color: var(--danger);
        margin-bottom: 24px;
      }

      h1 {
        margin: 0 0 16px;
        color: var(--ink);
        font-size: 2rem;
        font-family: var(--font-display);
      }

      p {
        margin: 0 0 32px;
        color: var(--muted);
        font-size: 1.05rem;
        line-height: 1.6;
      }

      .actions {
        button, a {
          mat-icon {
            margin-right: 8px;
          }
        }
      }
    }
  `]
})
export class UnauthorizedComponent {}
