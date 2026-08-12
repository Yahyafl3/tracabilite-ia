# Implementation Plan: Professional Role Dashboards

## Overview

This implementation plan breaks down the creation of 9 role-specific professional dashboards with backend API endpoints for statistics, strict data isolation enforcement, and Angular frontend components using PrimeNG. The system will provide real-time KPIs, charts, and decision tables tailored to each user role (ADMINISTRATEUR, AGENT_CREDIT, AGENT_SANTE, AGENT_PEDAGOGIQUE, VALIDATEUR, RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, RESPONSABLE_PEDAGOGIQUE, AUDITEUR) while enforcing security at the backend layer.

## Tasks

- [x] 1. Create Backend DTOs and Data Models
  - Create DashboardStatsDTO with all nested DTO classes (KPIValues, TimelineDataPoint, StatusDistribution, DomainDistribution, CreatorStats, RecentDecisionDTO, ValidationActionDTO)
  - Add Bean Validation annotations to all DTO fields
  - Create ErrorResponse DTO for error handling
  - Ensure all DTOs are serializable to JSON
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10_

- [x] 2. Implement Backend Data Isolation Service
  - [x] 2.1 Create DashboardIsolationService
    - Implement buildDashboardScope(Utilisateur user) method
    - Implement agentScopeSpec() for agent roles (own decisions + ADMINISTRATEUR decisions in domain)
    - Implement domainOnlySpec() for validator/manager mono-domain roles
    - Implement allDomainsSpec() for ADMINISTRATEUR and AUDITEUR
    - Handle all 9 role types with proper scoping logic
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [ ]* 2.2 Write scoping tests for DashboardIsolationService
    - Test ADMINISTRATEUR sees all domains
    - Test AGENT_CREDIT sees own + admin decisions in CREDIT domain only
    - Test AGENT_SANTE sees own + admin decisions in MEDICAL domain only
    - Test AGENT_PEDAGOGIQUE sees own + admin decisions in EDUCATION domain only
    - Test VALIDATEUR sees CREDIT domain only
    - Test RESPONSABLE_CREDIT sees CREDIT domain only
    - Test PROFESSIONNEL_SANTE sees MEDICAL domain only
    - Test RESPONSABLE_PEDAGOGIQUE sees EDUCATION domain only
    - Test AUDITEUR sees all domains
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5, 17.6, 17.7, 17.8, 17.9, 17.10, 17.11, 17.12_

- [x] 3. Implement Backend Dashboard Service
  - [x] 3.1 Create DashboardService with calculateDashboardStats method
    - Inject DecisionRepository, UtilisateurRepository, AuditLogRepository, DashboardIsolationService
    - Call DashboardIsolationService to get scoped Specification
    - Execute decisionRepository.findAll(scope) to fetch authorized decisions
    - Implement buildDashboardDTO to orchestrate all statistic calculations
    - _Requirements: 1.2, 1.3, 1.4, 2.3, 2.4_

  - [x] 3.2 Implement KPI calculation methods
    - Implement calculateKPIs() for all role-specific KPI values (total, pending, validated, rejected, acceptance rate, validation rate, compliance rate)
    - Implement calculateTimeline() for temporal statistics with configurable time range
    - Implement calculateStatusDistribution() for decision status counts
    - Implement calculateDomainDistribution() for multi-domain users
    - Implement calculateTopCreators() for creator statistics
    - Implement getRecentDecisions() for last 10 decisions
    - Implement getRecentValidations() for last 10 validation actions
    - _Requirements: 1.8, 1.9, 1.10, 1.11, 1.12, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 4.10_

  - [x] 3.3 Implement role-specific statistic calculation branches
    - Implement ADMINISTRATEUR-specific statistics (global multi-domain)
    - Implement Agent role statistics (personal activity in domain)
    - Implement VALIDATEUR statistics (CREDIT validation queue)
    - Implement Manager role statistics (domain supervision)
    - Implement AUDITEUR statistics (global read-only audit)
    - _Requirements: 4.1-4.10, 5.1-5.11, 6.1-6.9, 7.1-7.11, 8.1-8.10_

  - [ ]* 3.4 Write unit tests for DashboardService calculation methods
    - Test KPI calculations with sample data sets
    - Test timeline aggregation for 7-day and 30-day ranges
    - Test status distribution calculations
    - Test domain distribution calculations
    - Test top creators ranking
    - Test empty data set handling (returns zeros/empty collections)
    - _Requirements: 1.13, 1.14_

- [x] 4. Checkpoint - Backend Services Complete
  - Ensure all backend service classes compile without errors
  - Ensure all tests pass
  - Verify scoping logic correctly filters decisions by role
  - Ask the user if questions arise

- [x] 5. Implement Backend REST Controller
  - [x] 5.1 Create DashboardController with /api/dashboard/stats endpoint
    - Add @RestController and @RequestMapping("/api/dashboard") annotations
    - Inject DashboardService and AuthService
    - Implement getDashboardStats() method with @GetMapping("/stats")
    - Add @PreAuthorize("isAuthenticated()") security annotation
    - Retrieve current user from AuthService
    - Call DashboardService.calculateDashboardStats(currentUser)
    - Return ResponseEntity.ok(stats)
    - _Requirements: 1.1, 1.2, 2.1, 2.2_

  - [x] 5.2 Add exception handlers to DashboardController
    - Add @ExceptionHandler for AuthenticationException (401 Unauthorized)
    - Add @ExceptionHandler for AccessDeniedException (403 Forbidden)
    - Add @ExceptionHandler for QueryTimeoutException (500 Server Error with retry guidance)
    - Add @ExceptionHandler for generic Exception (500 Server Error)
    - Log all exceptions with user context for audit
    - _Requirements: 2.8_

  - [ ]* 5.3 Write integration tests for DashboardController
    - Test authenticated access returns 200 OK
    - Test unauthenticated access returns 401 Unauthorized
    - Test each role receives correctly scoped data
    - Test API response JSON structure matches DashboardStatsDTO
    - Test query timeout handling
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 17.13, 17.14, 17.15_

- [x] 6. Implement Frontend Dashboard Service
  - Create DashboardService in frontend/src/app/services/dashboard.service.ts
  - Inject HttpClient
  - Implement getDashboardStats() method calling /api/dashboard/stats
  - Add timeout(10000) operator for 10-second timeout
  - Implement handleError() method for HTTP error handling (status 0, 403, 500)
  - Return Observable<DashboardStatsDTO>
  - _Requirements: 1.1, 9.3, 13.7_

- [x] 7. Create Frontend Shared Components
  - [x] 7.1 Create KPI Card Component
    - Create frontend/src/app/shared/components/kpi-card/kpi-card.component.ts
    - Add @Input properties: label, value, icon, trend, loading, unit
    - Implement formattedValue getter with thousand separators and percentage formatting
    - Implement trendIcon getter for up/down arrows
    - Create template with p-card, skeleton loader, icon, label, value, and trend display
    - Style with PrimeNG Sakai design patterns
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8, 10.9, 10.10_

  - [x] 7.2 Create Donut Chart Component
    - Create frontend/src/app/shared/components/donut-chart/donut-chart.component.ts
    - Add @Input properties: title, data (label, value, color array)
    - Implement ngOnChanges to build Chart.js data structure
    - Check for empty data and set isEmpty flag
    - Configure chart options with legend and percentage tooltips
    - Create template with p-card, p-chart, and empty state
    - _Requirements: 11.1, 11.2, 11.5, 11.6, 11.7, 11.8, 11.9, 11.10_

  - [x] 7.3 Create Line Chart Component
    - Create frontend/src/app/shared/components/line-chart/line-chart.component.ts
    - Add @Input properties: title, data (date, count array)
    - Implement ngOnChanges to build Chart.js line data structure
    - Implement formatDate() helper for French date formatting
    - Check for empty data and set isEmpty flag
    - Configure chart options with Y-axis starting at zero
    - Create template with p-card, p-chart, and empty state
    - _Requirements: 11.1, 11.4, 11.5, 11.6, 11.7, 11.8, 11.9, 11.10_

  - [ ]* 7.4 Write unit tests for shared components
    - Test KPI card renders with all input variations
    - Test KPI card displays skeleton loader when loading=true
    - Test donut chart renders with data
    - Test donut chart shows empty state when data is empty
    - Test line chart renders with timeline data
    - Test line chart shows empty state when data is empty
    - _Requirements: 10.7, 11.6, 11.7_

- [x] 8. Checkpoint - Frontend Shared Components Complete
  - Ensure all shared components compile without TypeScript errors
  - Ensure all components render correctly in Storybook or dev environment
  - Verify PrimeNG theme compatibility
  - Ask the user if questions arise

- [x] 9. Implement ADMINISTRATEUR Dashboard
  - [x] 9.1 Create AdminDashboardComponent
    - Create frontend/src/app/features/dashboard/admin-dashboard/admin-dashboard.component.ts
    - Inject DashboardService and Router
    - Implement ngOnInit() calling loadDashboardData()
    - Implement loading, error, and stats signals
    - Implement retry() method
    - Implement navigateToNewDecision() method (no domain restriction)
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 15.1, 15.5_

  - [x] 9.2 Create AdminDashboardComponent template
    - Create dashboard header with role label and "Nouvelle Décision" button
    - Add skeleton loaders for loading state
    - Add error card with retry button for error state
    - Create 4-column KPI grid with: Total decisions, Pending validations, Decisions today, Active users
    - Add donut charts for: Domain distribution, Status distribution
    - Add line chart for 7-day timeline evolution
    - Add recent decisions table
    - Add recent validations table
    - Add top creators list
    - _Requirements: 9.5, 9.6, 13.1, 13.2, 13.3, 13.4, 13.5, 4.1-4.10, 16.1-16.9_

  - [x] 9.3 Style AdminDashboardComponent with responsive layout
    - Create admin-dashboard.component.scss
    - Implement 4-column grid for desktop (width >= 1024px)
    - Implement 2-column grid for tablet (768px <= width < 1024px)
    - Implement 1-column grid for mobile (width < 768px)
    - Use CSS variables for theme compatibility (dark mode)
    - Ensure touch targets >= 44x44px on mobile
    - _Requirements: 18.1, 18.2, 18.3, 18.4, 18.5, 18.6, 18.7, 18.8, 18.9, 19.1-19.8_

- [x] 10. Implement Agent Dashboards (CREDIT, SANTE, PEDAGOGIQUE)
  - [x] 10.1 Create AgentCreditDashboardComponent
    - Create frontend/src/app/features/dashboard/agent-credit-dashboard/agent-credit-dashboard.component.ts
    - Implement same structure as AdminDashboardComponent (loading, error, stats, retry)
    - Implement navigateToNewDecision() with CREDIT domain pre-selection
    - Create template with: My decisions KPI, Pending validations KPI, Validated KPI, Acceptance rate KPI
    - Add status distribution donut chart
    - Add 7-day timeline line chart
    - Add recent decisions table
    - Add pending validations list
    - Apply responsive styling
    - _Requirements: 5.1-5.11, 9.1-9.10, 15.2, 15.6, 18.1-18.10, 19.1-19.8_

  - [x] 10.2 Create AgentSanteDashboardComponent
    - Create frontend/src/app/features/dashboard/agent-sante-dashboard/agent-sante-dashboard.component.ts
    - Implement same structure as AgentCreditDashboardComponent but scoped to MEDICAL domain
    - Implement navigateToNewDecision() with MEDICAL domain pre-selection
    - Create template with same KPIs, charts, and tables as agent-credit dashboard
    - Apply responsive styling
    - _Requirements: 5.1-5.11, 9.1-9.10, 15.3, 15.7, 18.1-18.10, 19.1-19.8_

  - [x] 10.3 Create AgentPedagogiqueDashboardComponent
    - Create frontend/src/app/features/dashboard/agent-pedagogique-dashboard/agent-pedagogique-dashboard.component.ts
    - Implement same structure as AgentCreditDashboardComponent but scoped to EDUCATION domain
    - Implement navigateToNewDecision() with EDUCATION domain pre-selection
    - Create template with same KPIs, charts, and tables as agent-credit dashboard
    - Apply responsive styling
    - _Requirements: 5.1-5.11, 9.1-9.10, 15.4, 15.8, 18.1-18.10, 19.1-19.8_

- [x] 11. Implement VALIDATEUR Dashboard
  - Create frontend/src/app/features/dashboard/validateur-dashboard/validateur-dashboard.component.ts
  - Implement component structure (loading, error, stats, retry)
  - Do NOT include "Nouvelle Décision" button (validator role)
  - Create template with: Pending validations KPI, Validated by me KPI, Rejected by me KPI, Total processed KPI
  - Add status distribution donut chart (CREDIT domain)
  - Add 7-day validation activity timeline
  - Add pending validations table (all CREDIT EN_ATTENTE_VALIDATION)
  - Add recent validation actions table
  - Apply responsive styling
  - _Requirements: 6.1-6.9, 9.1-9.10, 15.3, 18.1-18.10, 19.1-19.8_

- [ ] 12. Implement Manager Dashboards (RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, RESPONSABLE_PEDAGOGIQUE)
  - [-] 12.1 Create ResponsableCreditDashboardComponent
    - Create frontend/src/app/features/dashboard/responsable-credit-dashboard/responsable-credit-dashboard.component.ts
    - Implement component structure (loading, error, stats, retry)
    - Do NOT include "Nouvelle Décision" button (manager role)
    - Create template with: Total decisions KPI, Pending validations KPI, Validated this month KPI, Validation rate KPI
    - Add status distribution donut chart (CREDIT domain)
    - Add 30-day timeline line chart
    - Add pending validations table
    - Add top creators list
    - Apply responsive styling
    - _Requirements: 7.1-7.11, 9.1-9.10, 15.3, 18.1-18.10, 19.1-19.8_

  - [-] 12.2 Create ProfessionnelSanteDashboardComponent
    - Create frontend/src/app/features/dashboard/professionnel-sante-dashboard/professionnel-sante-dashboard.component.ts
    - Implement same structure as ResponsableCreditDashboardComponent but scoped to MEDICAL domain
    - Apply responsive styling
    - _Requirements: 7.1-7.11, 9.1-9.10, 15.3, 18.1-18.10, 19.1-19.8_

  - [x] 12.3 Create ResponsablePedagogiqueDashboardComponent
    - Create frontend/src/app/features/dashboard/responsable-pedagogique-dashboard/responsable-pedagogique-dashboard.component.ts
    - Implement same structure as ResponsableCreditDashboardComponent but scoped to EDUCATION domain
    - Apply responsive styling
    - _Requirements: 7.1-7.11, 9.1-9.10, 15.3, 18.1-18.10, 19.1-19.8_

- [x] 13. Implement AUDITEUR Dashboard
  - Create frontend/src/app/features/dashboard/auditeur-dashboard/auditeur-dashboard.component.ts
  - Implement component structure (loading, error, stats, retry)
  - Do NOT include "Nouvelle Décision" button (auditor role, read-only)
  - Create template with: Total decisions KPI, Validated decisions KPI, Rejected decisions KPI, Compliance rate KPI
  - Add domain distribution donut chart (all domains)
  - Add status distribution donut chart (all domains)
  - Add 30-day timeline line chart (all domains)
  - Add all decisions table (read-only)
  - Add audit trail entries table
  - Add validation activity by validator statistics
  - Apply responsive styling
  - _Requirements: 8.1-8.10, 9.1-9.10, 15.4, 18.1-18.10, 19.1-19.8_

- [ ] 14. Checkpoint - All Dashboard Components Complete
  - Ensure all 9 dashboard components compile without TypeScript errors
  - Ensure Angular build completes successfully
  - Verify routing configuration includes all dashboard components
  - Ask the user if questions arise

- [~] 15. Implement Dashboard Routing and Role-Based Display
  - Update frontend routing configuration to map roles to dashboard components
  - Implement route guard checking user authentication
  - Implement dashboard component selector based on authenticated user's role
  - Add dashboard navigation link to main menu
  - Test navigation from login to appropriate dashboard for each role
  - _Requirements: 9.1, 9.2_

- [ ] 16. Implement Empty States for All Dashboards
  - [~] 16.1 Add empty state messages to all dashboard components
    - For Agent roles: "Créez votre première décision pour voir vos statistiques"
    - For Validator roles: "Aucune décision en attente de validation"
    - For Manager roles: "Aucune décision dans votre domaine"
    - For Auditor role: "Aucune donnée disponible pour l'audit"
    - For Admin role: "Aucune décision dans le système"
    - _Requirements: 14.1, 14.2, 14.3, 14.4_

  - [~] 16.2 Add empty state styling
    - Create empty-state CSS class with icon, message, and optional action button
    - Ensure empty states use PrimeNG Sakai patterns
    - Apply to all sections: KPI grids, charts, tables
    - _Requirements: 14.5, 14.6, 14.7, 14.8_

- [~] 17. Implement Real Data Enforcement
  - Audit all dashboard components to ensure no Math.random() or hardcoded mock data
  - Verify all displayed values come from DashboardService API calls
  - Replace any placeholder/demo data with API-driven values
  - Ensure charts display empty states when API returns empty collections
  - Ensure KPIs display zero values when API returns zero
  - Add console warnings if API errors occur (no fake data fallback)
  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7_

- [~] 18. Implement Loading and Error State Handling
  - Verify all dashboard components display skeleton loaders during API calls
  - Verify all dashboard components display error messages on API failures
  - Verify all dashboard components provide retry buttons on errors
  - Verify timeout errors display after 10 seconds
  - Test network error handling (offline scenario)
  - Test 401/403 error handling with appropriate messages
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7, 13.8_

- [~] 19. Implement Recent Decisions Table Component
  - Create shared DecisionsTableComponent (if not already existing)
  - Accept @Input decisions array
  - Display columns: Reference, Domain, Status, Creation Date, Creator
  - Sort by creation date descending
  - Apply status badge color coding (green=VALIDEE, orange=EN_ATTENTE_VALIDATION, red=REJETEE)
  - Implement row click navigation to decision detail page
  - Make table responsive with horizontal scroll on mobile
  - Display skeleton loader while loading
  - Display empty state when no decisions exist
  - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7, 16.8, 16.9_

- [ ] 20. Final Integration and Testing
  - [~] 20.1 Backend compilation and test execution
    - Run Maven clean install
    - Verify all backend tests pass (including new scoping tests)
    - Verify backend compiles without errors
    - Verify backend starts successfully
    - _Requirements: 20.1, 20.2, 20.3, 20.7_

  - [~] 20.2 Frontend build and compilation
    - Run Angular build (ng build)
    - Verify no TypeScript compilation errors
    - Verify no linting errors
    - Verify frontend build completes within 90 seconds
    - _Requirements: 20.4, 20.5, 20.6, 20.8_

  - [~] 20.3 End-to-end manual testing
    - Test each of the 9 role dashboards with appropriate user accounts
    - Verify data isolation: agents see only own+admin decisions, validators see only domain decisions
    - Test responsive layout on desktop, tablet, and mobile screen sizes
    - Test dark mode compatibility
    - Test "Nouvelle Décision" button navigation and domain pre-selection
    - Test empty states when no data exists
    - Test loading states and error handling
    - Test chart interactivity (hover tooltips)
    - _Requirements: 9.7, 9.8, 9.9, 9.10, 18.10, 19.8_

- [~] 21. Final Checkpoint - Complete Feature
  - Ensure all 9 dashboards display correct role-specific data
  - Ensure data isolation is enforced at backend layer
  - Ensure frontend build and backend compilation succeed
  - Ensure all automated tests pass
  - Ask the user if questions arise or if production deployment is ready

## Notes

- **Real Data Only**: No mock data or Math.random() values are allowed. All statistics must come from actual database queries via the API.
- **Security First**: Data isolation is enforced at the backend using DashboardIsolationService. The frontend never sends role/domain parameters; authentication context is used.
- **Responsive Design**: All dashboards adapt to desktop (4-col), tablet (2-col), and mobile (1-col) layouts.
- **Dark Mode**: All components use CSS variables from PrimeNG theme for automatic dark mode compatibility.
- **Performance**: Statistics queries must complete within 2 seconds for up to 10,000 decisions.
- **Error Handling**: All error states display user-friendly messages with retry options. No silent failures.
- **Testing**: Scoping tests verify that each role sees only authorized data. Integration tests verify API contract.
- **Empty States**: Each dashboard section displays contextual empty state messages when no data exists.
- **Agent Isolation**: Agent roles see only their own decisions plus ADMINISTRATEUR decisions in their domain.
- **Checkpoints**: Pause at checkpoints to verify compilation, test passage, and ask user for feedback before proceeding.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1"] },
    { "id": 1, "tasks": ["2.1"] },
    { "id": 2, "tasks": ["2.2", "3.1"] },
    { "id": 3, "tasks": ["3.2"] },
    { "id": 4, "tasks": ["3.3", "3.4"] },
    { "id": 5, "tasks": ["5.1"] },
    { "id": 6, "tasks": ["5.2", "5.3"] },
    { "id": 7, "tasks": ["6", "7.1", "7.2", "7.3"] },
    { "id": 8, "tasks": ["7.4", "9.1"] },
    { "id": 9, "tasks": ["9.2"] },
    { "id": 10, "tasks": ["9.3", "10.1"] },
    { "id": 11, "tasks": ["10.2", "10.3", "11"] },
    { "id": 12, "tasks": ["12.1", "12.2", "12.3", "13"] },
    { "id": 13, "tasks": ["15", "16.1"] },
    { "id": 14, "tasks": ["16.2", "17", "18", "19"] },
    { "id": 15, "tasks": ["20.1", "20.2"] },
    { "id": 16, "tasks": ["20.3"] }
  ]
}
```
