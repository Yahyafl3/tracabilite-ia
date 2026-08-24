import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.models';

/**
 * DashboardRouterComponent - Role-based dashboard selector
 * 
 * This component acts as a router that redirects users to their role-specific dashboard.
 * It determines the authenticated user's role and navigates to the appropriate dashboard component.
 * 
 * Role-to-Dashboard mapping:
 * - ADMINISTRATEUR → admin-dashboard
 * - AGENT_CREDIT → agent-credit-dashboard
 * - AGENT_SANTE → agent-sante-dashboard
 * - AGENT_PEDAGOGIQUE → agent-pedagogique-dashboard
 * - RESPONSABLE_CREDIT → responsable-credit-dashboard
 * - PROFESSIONNEL_SANTE → professionnel-sante-dashboard
 * - RESPONSABLE_PEDAGOGIQUE → responsable-pedagogique-dashboard
 * - AUDITEUR → auditeur-dashboard
 * 
 * Requirements: 9.1, 9.2, 15.1-15.8
 */
@Component({
  selector: 'app-dashboard-router',
  standalone: true,
  template: '<p>Redirection...</p>'
})
export class DashboardRouterComponent implements OnInit {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.currentUser;
    
    if (!currentUser) {
      // No authenticated user, redirect to login
      this.router.navigate(['/auth/login']);
      return;
    }

    // Determine role-specific dashboard route
    const dashboardRoute = this.getDashboardRouteForRole(currentUser.role);
    this.router.navigate([dashboardRoute]);
  }

  /**
   * Maps user role to the corresponding dashboard route
   * Requirement 15.1-15.8: Role-based dashboard routing
   */
  private getDashboardRouteForRole(role: string): string {
    switch (role) {
      case UserRole.ADMINISTRATEUR:
        return '/dashboard/admin';
      
      case UserRole.AGENT_CREDIT:
        return '/dashboard/agent-credit';
      
      case UserRole.AGENT_SANTE:
        return '/dashboard/agent-sante';
      
      case UserRole.AGENT_PEDAGOGIQUE:
        return '/dashboard/agent-pedagogique';
      
      case UserRole.RESPONSABLE_CREDIT:
        return '/dashboard/responsable-credit';
      
      case UserRole.PROFESSIONNEL_SANTE:
        return '/dashboard/professionnel-sante';
      
      case UserRole.RESPONSABLE_PEDAGOGIQUE:
        return '/dashboard/responsable-pedagogique';
      
      case UserRole.AUDITEUR:
        return '/dashboard/auditeur';
      
      default:
        // Unknown role, redirect to default dashboard
        console.warn(`Unknown role: ${role}, redirecting to /decisions`);
        return '/decisions';
    }
  }
}
