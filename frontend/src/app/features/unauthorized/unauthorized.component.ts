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
            <a mat-raised-button color="primary" routerLink="/dashboard">
              <mat-icon>home</mat-icon>
              Retour au Dashboard
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
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
    }

    .unauthorized-card {
      max-width: 500px;
      text-align: center;
      padding: 40px;

      .error-icon {
        font-size: 80px;
        width: 80px;
        height: 80px;
        color: #f44336;
        margin-bottom: 20px;
      }

      h1 {
        margin: 0 0 16px;
        color: #333;
        font-size: 32px;
      }

      p {
        margin: 0 0 32px;
        color: #666;
        font-size: 16px;
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
