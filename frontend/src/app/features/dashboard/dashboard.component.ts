import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from '../../core/services/auth.service';

/**
 * Dashboard Component (Placeholder)
 * Will be expanded with full dashboard functionality
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule
  ],
  template: `
    <div class="dashboard-container">
      <mat-toolbar color="primary">
        <span>Traçabilité IA</span>
        <span class="spacer"></span>
        <span class="user-info">{{ currentUser?.prenom }} {{ currentUser?.nom }}</span>
        <button mat-icon-button [matMenuTriggerFor]="menu">
          <mat-icon>account_circle</mat-icon>
        </button>
        <mat-menu #menu="matMenu">
          <button mat-menu-item>
            <mat-icon>person</mat-icon>
            <span>Profil</span>
          </button>
          <button mat-menu-item>
            <mat-icon>settings</mat-icon>
            <span>Paramètres</span>
          </button>
          <button mat-menu-item (click)="logout()">
            <mat-icon>logout</mat-icon>
            <span>Déconnexion</span>
          </button>
        </mat-menu>
      </mat-toolbar>

      <div class="dashboard-content">
        <h1>Bienvenue sur le Dashboard</h1>
        <p>Utilisateur connecté: {{ currentUser?.email }}</p>
        <p>Rôle: {{ currentUser?.role }}</p>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      height: 100vh;
      display: flex;
      flex-direction: column;
    }

    .spacer {
      flex: 1 1 auto;
    }

    .user-info {
      margin-right: 16px;
      font-weight: 500;
    }

    .dashboard-content {
      padding: 24px;
    }
  `]
})
export class DashboardComponent {
  private readonly authService = inject(AuthService);

  get currentUser() {
    return this.authService.currentUser;
  }

  logout(): void {
    this.authService.logout();
  }
}
