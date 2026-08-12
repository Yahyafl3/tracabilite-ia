# Requirements Document

## Introduction

Ce document définit les exigences pour l'implémentation de 9 dashboards professionnels distincts dans le système de traçabilité des décisions IA. Chaque dashboard est adapté aux besoins spécifiques d'un rôle utilisateur (ADMINISTRATEUR, AGENT_CREDIT, AGENT_SANTE, AGENT_PEDAGOGIQUE, VALIDATEUR, RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, RESPONSABLE_PEDAGOGIQUE, AUDITEUR) et affiche des indicateurs clés de performance (KPI), des graphiques et des données en temps réel tout en respectant strictement l'isolation des données selon le rôle et le domaine de l'utilisateur.

## Glossary

- **Dashboard_System**: Le système de tableaux de bord professionnels
- **API_Backend**: L'API REST Spring Boot fournissant les données statistiques
- **Dashboard_Component**: Le composant Angular affichant un dashboard spécifique à un rôle
- **KPI_Card**: Composant réutilisable affichant un indicateur clé de performance
- **Chart_Component**: Composant réutilisable affichant un graphique (donut, bar, line)
- **Decision**: Une décision IA enregistrée dans le système avec un domaine (CREDIT/MEDICAL/EDUCATION) et un statut
- **Domain**: Le domaine métier d'une décision (CREDIT, MEDICAL, EDUCATION)
- **Role**: Le rôle de l'utilisateur authentifié (ADMINISTRATEUR, AGENT_CREDIT, etc.)
- **Data_Isolation**: Le mécanisme garantissant qu'un utilisateur voit uniquement les données auxquelles il a accès
- **Agent_User**: Un utilisateur avec rôle AGENT_CREDIT, AGENT_SANTE ou AGENT_PEDAGOGIQUE
- **Validator_User**: Un utilisateur avec rôle VALIDATEUR, RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE ou RESPONSABLE_PEDAGOGIQUE
- **Creator**: L'utilisateur ayant créé une décision
- **Authenticated_User**: L'utilisateur actuellement connecté et authentifié
- **Dashboard_Stats**: Les statistiques agrégées fournies par l'API pour un dashboard
- **Skeleton_Loader**: L'indicateur de chargement affiché pendant la récupération des données
- **Empty_State**: L'état visuel affiché quand aucune donnée n'est disponible
- **Time_Range**: La période temporelle pour les graphiques d'évolution (7 jours, 30 jours)
- **Real_Data**: Données provenant exclusivement de l'API, sans valeurs fictives ou générées
- **Responsive_Layout**: La mise en page s'adaptant aux écrans desktop, tablette et mobile
- **Dark_Mode**: Le thème sombre compatible avec le thème existant de l'application

## Requirements

### Requirement 1: Backend Dashboard Statistics API

**User Story:** En tant que développeur frontend, je veux une API REST fournissant les statistiques de dashboard, afin que je puisse afficher des données réelles selon le rôle de l'utilisateur authentifié.

#### Acceptance Criteria

1. THE API_Backend SHALL expose an endpoint `/api/dashboard/stats` that returns dashboard statistics
2. WHEN a request is made to `/api/dashboard/stats`, THE API_Backend SHALL determine the user's role from the authentication context
3. WHEN a request is made to `/api/dashboard/stats`, THE API_Backend SHALL apply data isolation rules based on the authenticated user's role and domain
4. THE API_Backend SHALL return statistics only for decisions the authenticated user is authorized to view
5. WHEN the authenticated user is an Agent_User, THE API_Backend SHALL include only decisions created by the user or created by ADMINISTRATEUR in the same domain
6. WHEN the authenticated user is VALIDATEUR, THE API_Backend SHALL include only CREDIT domain decisions
7. WHEN the authenticated user is ADMINISTRATEUR or AUDITEUR, THE API_Backend SHALL include decisions from all domains
8. THE API_Backend SHALL return KPI values calculated from real database queries
9. THE API_Backend SHALL return timeline statistics for the requested time range (7 or 30 days)
10. THE API_Backend SHALL return status distribution statistics (EN_ATTENTE_VALIDATION, VALIDEE, REJETEE)
11. THE API_Backend SHALL return domain distribution statistics when the user has multi-domain access
12. THE API_Backend SHALL return top creators statistics when applicable to the user's role
13. IF no data exists for a requested statistic, THEN THE API_Backend SHALL return zero or empty collection
14. THE API_Backend SHALL complete statistics calculation within 2 seconds for datasets up to 10000 decisions

### Requirement 2: Data Isolation Enforcement

**User Story:** En tant qu'administrateur système, je veux que l'isolation des données soit strictement appliquée au niveau du backend, afin qu'aucun utilisateur ne puisse accéder à des données non autorisées.

#### Acceptance Criteria

1. THE API_Backend SHALL never trust role or domain parameters sent from the frontend
2. THE API_Backend SHALL determine the user's role and domain exclusively from the authentication context
3. WHEN calculating statistics, THE API_Backend SHALL apply the existing DecisionScopeService isolation rules
4. THE API_Backend SHALL filter decisions using the same isolation logic as the existing decision endpoints
5. FOR ALL Agent_Users, THE API_Backend SHALL scope statistics to decisions where (creator equals authenticated user OR creator role equals ADMINISTRATEUR) AND domain equals user domain
6. FOR ALL Validator_Users with mono-domain access, THE API_Backend SHALL scope statistics to their assigned domain only
7. FOR ALL requests, THE API_Backend SHALL verify that returned statistics contain only authorized data
8. IF an unauthorized access attempt is detected, THEN THE API_Backend SHALL log the attempt and return an empty result set

### Requirement 3: Dashboard DTOs and Data Models

**User Story:** En tant que développeur backend, je veux des DTOs structurés pour les statistiques de dashboard, afin de fournir une API cohérente et typée au frontend.

#### Acceptance Criteria

1. THE API_Backend SHALL define a DashboardStatsDTO containing all dashboard statistics
2. THE DashboardStatsDTO SHALL include a KPIValues object containing numeric key performance indicators
3. THE DashboardStatsDTO SHALL include a TimelineData collection containing temporal statistics
4. THE DashboardStatsDTO SHALL include a StatusDistribution object containing decision status counts
5. THE DashboardStatsDTO SHALL include a DomainDistribution object containing decision counts by domain
6. THE DashboardStatsDTO SHALL include a TopCreators collection containing creator statistics
7. THE DashboardStatsDTO SHALL include a RecentDecisions collection containing the last 10 decisions
8. THE DashboardStatsDTO SHALL include a RecentValidations collection containing the last 10 validation actions when applicable
9. THE API_Backend SHALL serialize all DTOs to JSON format
10. THE API_Backend SHALL include field validation annotations on all DTO fields

### Requirement 4: Administrator Dashboard Data

**User Story:** En tant qu'administrateur, je veux voir des statistiques globales multi-domaines, afin de superviser l'ensemble du système.

#### Acceptance Criteria

1. WHEN the authenticated user is ADMINISTRATEUR, THE API_Backend SHALL calculate total decision count across all domains
2. WHEN the authenticated user is ADMINISTRATEUR, THE API_Backend SHALL calculate pending validation count across all domains
3. WHEN the authenticated user is ADMINISTRATEUR, THE API_Backend SHALL calculate decisions created today across all domains
4. WHEN the authenticated user is ADMINISTRATEUR, THE API_Backend SHALL calculate active users count
5. WHEN the authenticated user is ADMINISTRATEUR, THE API_Backend SHALL provide decision distribution by domain (CREDIT, MEDICAL, EDUCATION)
6. WHEN the authenticated user is ADMINISTRATEUR, THE API_Backend SHALL provide decision distribution by status
7. WHEN the authenticated user is ADMINISTRATEUR, THE API_Backend SHALL provide timeline evolution for the last 7 days
8. WHEN the authenticated user is ADMINISTRATEUR, THE API_Backend SHALL provide top 5 creators by decision count
9. WHEN the authenticated user is ADMINISTRATEUR, THE API_Backend SHALL provide the 10 most recent decisions
10. WHEN the authenticated user is ADMINISTRATEUR, THE API_Backend SHALL provide the 10 most recent validation actions

### Requirement 5: Agent Dashboard Data

**User Story:** En tant qu'agent métier (crédit, santé ou pédagogique), je veux voir mes statistiques personnelles dans mon domaine, afin de suivre mon activité et mes décisions en attente.

#### Acceptance Criteria

1. WHEN the authenticated user is an Agent_User, THE API_Backend SHALL calculate the count of decisions created by the user or by ADMINISTRATEUR in the user's domain
2. WHEN the authenticated user is an Agent_User, THE API_Backend SHALL calculate the count of user's decisions with status EN_ATTENTE_VALIDATION
3. WHEN the authenticated user is an Agent_User, THE API_Backend SHALL calculate the count of user's decisions with status VALIDEE
4. WHEN the authenticated user is an Agent_User, THE API_Backend SHALL calculate the user's acceptance rate (VALIDEE / total)
5. WHEN the authenticated user is an Agent_User, THE API_Backend SHALL provide distribution of user's decisions by status
6. WHEN the authenticated user is an Agent_User, THE API_Backend SHALL provide timeline evolution of user's decision creation for the last 7 days
7. WHEN the authenticated user is an Agent_User, THE API_Backend SHALL provide the 10 most recent decisions created by the user or ADMINISTRATEUR in the user's domain
8. WHEN the authenticated user is an Agent_User, THE API_Backend SHALL provide decisions with status EN_ATTENTE_VALIDATION created by the user or ADMINISTRATEUR
9. WHEN the authenticated user is AGENT_CREDIT, THE API_Backend SHALL scope all statistics to CREDIT domain only
10. WHEN the authenticated user is AGENT_SANTE, THE API_Backend SHALL scope all statistics to MEDICAL domain only
11. WHEN the authenticated user is AGENT_PEDAGOGIQUE, THE API_Backend SHALL scope all statistics to EDUCATION domain only

### Requirement 6: Validator Dashboard Data

**User Story:** En tant que validateur, je veux voir les décisions à valider dans mon domaine, afin de traiter la file de validation et suivre mes validations.

#### Acceptance Criteria

1. WHEN the authenticated user is VALIDATEUR, THE API_Backend SHALL calculate the count of CREDIT decisions with status EN_ATTENTE_VALIDATION
2. WHEN the authenticated user is VALIDATEUR, THE API_Backend SHALL calculate the count of CREDIT decisions validated by the user
3. WHEN the authenticated user is VALIDATEUR, THE API_Backend SHALL calculate the count of CREDIT decisions rejected by the user
4. WHEN the authenticated user is VALIDATEUR, THE API_Backend SHALL calculate the total count of decisions processed by the user
5. WHEN the authenticated user is VALIDATEUR, THE API_Backend SHALL provide distribution of CREDIT decisions by status
6. WHEN the authenticated user is VALIDATEUR, THE API_Backend SHALL provide timeline evolution of user's validations for the last 7 days
7. WHEN the authenticated user is VALIDATEUR, THE API_Backend SHALL provide all CREDIT decisions with status EN_ATTENTE_VALIDATION
8. WHEN the authenticated user is VALIDATEUR, THE API_Backend SHALL provide the 10 most recent validation actions by the user
9. WHEN the authenticated user is VALIDATEUR, THE API_Backend SHALL scope all statistics to CREDIT domain only

### Requirement 7: Manager Dashboard Data

**User Story:** En tant que responsable métier (crédit, santé ou pédagogique), je veux voir les statistiques de supervision de mon domaine, afin de gérer l'activité de validation et la performance de mon équipe.

#### Acceptance Criteria

1. WHEN the authenticated user is a Validator_User with manager role, THE API_Backend SHALL calculate the total count of decisions in the user's domain
2. WHEN the authenticated user is a Validator_User with manager role, THE API_Backend SHALL calculate the count of decisions with status EN_ATTENTE_VALIDATION in the user's domain
3. WHEN the authenticated user is a Validator_User with manager role, THE API_Backend SHALL calculate the count of decisions validated in the current month in the user's domain
4. WHEN the authenticated user is a Validator_User with manager role, THE API_Backend SHALL calculate the validation rate for the user's domain
5. WHEN the authenticated user is a Validator_User with manager role, THE API_Backend SHALL provide distribution of decisions by status in the user's domain
6. WHEN the authenticated user is a Validator_User with manager role, THE API_Backend SHALL provide timeline evolution for the last 30 days in the user's domain
7. WHEN the authenticated user is a Validator_User with manager role, THE API_Backend SHALL provide top creators in the user's domain
8. WHEN the authenticated user is a Validator_User with manager role, THE API_Backend SHALL provide all decisions with status EN_ATTENTE_VALIDATION in the user's domain
9. WHEN the authenticated user is RESPONSABLE_CREDIT, THE API_Backend SHALL scope all statistics to CREDIT domain only
10. WHEN the authenticated user is PROFESSIONNEL_SANTE, THE API_Backend SHALL scope all statistics to MEDICAL domain only
11. WHEN the authenticated user is RESPONSABLE_PEDAGOGIQUE, THE API_Backend SHALL scope all statistics to EDUCATION domain only

### Requirement 8: Auditor Dashboard Data

**User Story:** En tant qu'auditeur, je veux voir les statistiques globales en lecture seule, afin d'auditer la conformité et l'activité du système.

#### Acceptance Criteria

1. WHEN the authenticated user is AUDITEUR, THE API_Backend SHALL calculate the total decision count across all domains
2. WHEN the authenticated user is AUDITEUR, THE API_Backend SHALL calculate the count of validated decisions across all domains
3. WHEN the authenticated user is AUDITEUR, THE API_Backend SHALL calculate the count of rejected decisions across all domains
4. WHEN the authenticated user is AUDITEUR, THE API_Backend SHALL calculate the global compliance rate
5. WHEN the authenticated user is AUDITEUR, THE API_Backend SHALL provide decision distribution by domain
6. WHEN the authenticated user is AUDITEUR, THE API_Backend SHALL provide decision distribution by status
7. WHEN the authenticated user is AUDITEUR, THE API_Backend SHALL provide timeline evolution for the last 30 days
8. WHEN the authenticated user is AUDITEUR, THE API_Backend SHALL provide validation activity statistics by validator
9. WHEN the authenticated user is AUDITEUR, THE API_Backend SHALL provide all decisions with read-only access
10. WHEN the authenticated user is AUDITEUR, THE API_Backend SHALL provide audit trail entries

### Requirement 9: Frontend Dashboard Components

**User Story:** En tant qu'utilisateur, je veux un dashboard adapté à mon rôle avec une interface claire et responsive, afin de visualiser mes données de manière efficace sur tout appareil.

#### Acceptance Criteria

1. THE Dashboard_System SHALL provide a distinct Dashboard_Component for each of the 9 roles
2. THE Dashboard_System SHALL display the Dashboard_Component corresponding to the authenticated user's role
3. WHEN a Dashboard_Component is loaded, THE Dashboard_Component SHALL fetch statistics from the API_Backend
4. WHILE statistics are being fetched, THE Dashboard_Component SHALL display Skeleton_Loader placeholders
5. WHEN statistics are successfully loaded, THE Dashboard_Component SHALL display the data in KPI_Cards and Chart_Components
6. IF no data is available for a section, THEN THE Dashboard_Component SHALL display a professional Empty_State message
7. THE Dashboard_Component SHALL adapt its layout for desktop screens (width >= 1024px)
8. THE Dashboard_Component SHALL adapt its layout for tablet screens (768px <= width < 1024px)
9. THE Dashboard_Component SHALL adapt its layout for mobile screens (width < 768px)
10. THE Dashboard_Component SHALL be compatible with Dark_Mode using the existing application theme

### Requirement 10: KPI Card Component

**User Story:** En tant que développeur frontend, je veux un composant KPI réutilisable, afin d'afficher de manière cohérente tous les indicateurs clés de performance.

#### Acceptance Criteria

1. THE Dashboard_System SHALL provide a reusable KPI_Card component
2. THE KPI_Card SHALL accept a label parameter defining the KPI name
3. THE KPI_Card SHALL accept a value parameter defining the numeric value
4. THE KPI_Card SHALL accept an optional icon parameter defining the visual icon
5. THE KPI_Card SHALL accept an optional trend parameter defining the value evolution
6. THE KPI_Card SHALL display the label, value, icon and trend in a visually consistent card layout
7. THE KPI_Card SHALL display a Skeleton_Loader while the value is being loaded
8. THE KPI_Card SHALL format numeric values with appropriate thousand separators
9. THE KPI_Card SHALL display percentage values with the % symbol when applicable
10. THE KPI_Card SHALL be styled consistently with PrimeNG Sakai design patterns

### Requirement 11: Chart Components

**User Story:** En tant qu'utilisateur, je veux des graphiques clairs et interactifs, afin de visualiser les tendances et répartitions de mes données.

#### Acceptance Criteria

1. THE Dashboard_System SHALL provide Chart_Components using PrimeNG Chart and Chart.js
2. THE Dashboard_System SHALL provide a donut Chart_Component for status and domain distributions
3. THE Dashboard_System SHALL provide a bar Chart_Component for categorical comparisons
4. THE Dashboard_System SHALL provide a line Chart_Component for timeline evolutions
5. WHEN Real_Data is available, THE Chart_Component SHALL render the data visually
6. WHEN Real_Data is being fetched, THE Chart_Component SHALL display a Skeleton_Loader
7. IF no Real_Data is available, THEN THE Chart_Component SHALL display an Empty_State message
8. THE Chart_Component SHALL use colors consistent with the application theme
9. THE Chart_Component SHALL be interactive with hover tooltips showing detailed values
10. THE Chart_Component SHALL adapt its size to the available space in Responsive_Layout

### Requirement 12: Real Data Requirement

**User Story:** En tant qu'utilisateur, je veux voir uniquement des données réelles provenant du système, afin de prendre des décisions basées sur des informations fiables.

#### Acceptance Criteria

1. THE Dashboard_System SHALL display only Real_Data fetched from the API_Backend
2. THE Dashboard_System SHALL NOT generate or display fictive values using Math.random()
3. THE Dashboard_System SHALL NOT display hardcoded mock data
4. WHEN a statistic cannot be calculated, THE Dashboard_System SHALL display a zero value or empty collection
5. WHEN a statistic is unavailable due to insufficient data, THE Dashboard_System SHALL display an "indisponible" state
6. THE Dashboard_System SHALL never substitute API errors with fake data
7. IF the API_Backend returns an error, THEN THE Dashboard_System SHALL display an error state with retry option

### Requirement 13: Loading and Error States

**User Story:** En tant qu'utilisateur, je veux des indicateurs visuels clairs lors du chargement et des erreurs, afin de comprendre l'état de mon dashboard.

#### Acceptance Criteria

1. WHEN a Dashboard_Component initiates data fetching, THE Dashboard_Component SHALL display Skeleton_Loader elements
2. THE Skeleton_Loader SHALL visually represent the layout of KPI_Cards and Chart_Components
3. WHEN data fetching completes successfully, THE Dashboard_Component SHALL remove Skeleton_Loader and display data
4. IF data fetching fails, THEN THE Dashboard_Component SHALL display an error message with the failure reason
5. WHEN an error occurs, THE Dashboard_Component SHALL display a retry button
6. WHEN the retry button is clicked, THE Dashboard_Component SHALL re-attempt data fetching
7. THE Dashboard_Component SHALL display a timeout error if data fetching exceeds 10 seconds
8. THE Dashboard_Component SHALL log all loading errors to the browser console for debugging

### Requirement 14: Empty States

**User Story:** En tant qu'utilisateur, je veux des messages informatifs quand aucune donnée n'est disponible, afin de comprendre pourquoi mon dashboard est vide.

#### Acceptance Criteria

1. WHEN no decisions exist for a user's scope, THE Dashboard_Component SHALL display an Empty_State message
2. THE Empty_State SHALL include an explanatory message appropriate to the user's role
3. WHEN the user is an Agent_User, THE Empty_State SHALL suggest creating a first decision
4. WHEN the user is a Validator_User, THE Empty_State SHALL indicate no decisions are pending validation
5. THE Empty_State SHALL be visually consistent with PrimeNG Sakai empty state patterns
6. THE Empty_State SHALL include an appropriate icon representing the empty state
7. THE Empty_State SHALL be displayed per section (KPIs, charts, tables) independently
8. THE Empty_State SHALL NOT be displayed during initial loading (Skeleton_Loader is shown instead)

### Requirement 15: Decision Creation Action

**User Story:** En tant qu'utilisateur créateur, je veux accéder rapidement à la création de décision depuis mon dashboard, afin d'initier une nouvelle décision dans mon domaine.

#### Acceptance Criteria

1. WHEN the authenticated user is ADMINISTRATEUR, THE Dashboard_Component SHALL display a "Nouvelle Décision" button
2. WHEN the authenticated user is an Agent_User, THE Dashboard_Component SHALL display a "Nouvelle Décision" button
3. WHEN the authenticated user is a Validator_User, THE Dashboard_Component SHALL NOT display a "Nouvelle Décision" button
4. WHEN the authenticated user is AUDITEUR, THE Dashboard_Component SHALL NOT display a "Nouvelle Décision" button
5. WHEN the "Nouvelle Décision" button is clicked by ADMINISTRATEUR, THE Dashboard_System SHALL navigate to the decision creation page without domain restriction
6. WHEN the "Nouvelle Décision" button is clicked by AGENT_CREDIT, THE Dashboard_System SHALL navigate to the decision creation page with domain pre-selected to CREDIT
7. WHEN the "Nouvelle Décision" button is clicked by AGENT_SANTE, THE Dashboard_System SHALL navigate to the decision creation page with domain pre-selected to MEDICAL
8. WHEN the "Nouvelle Décision" button is clicked by AGENT_PEDAGOGIQUE, THE Dashboard_System SHALL navigate to the decision creation page with domain pre-selected to EDUCATION

### Requirement 16: Recent Decisions Table

**User Story:** En tant qu'utilisateur, je veux voir un tableau de mes décisions récentes, afin d'accéder rapidement aux décisions les plus importantes.

#### Acceptance Criteria

1. THE Dashboard_Component SHALL display a table showing the 10 most recent decisions
2. THE table SHALL display the decision reference, domain, status, creation date and creator
3. THE table SHALL be sorted by creation date in descending order (most recent first)
4. THE table SHALL apply Data_Isolation rules showing only authorized decisions
5. WHEN a table row is clicked, THE Dashboard_System SHALL navigate to the decision detail page
6. THE table SHALL display status badges with color coding (green for VALIDEE, orange for EN_ATTENTE_VALIDATION, red for REJETEE)
7. THE table SHALL be responsive and scrollable on mobile screens
8. THE table SHALL display a Skeleton_Loader while data is being fetched
9. IF no decisions exist, THEN THE table SHALL display an Empty_State message

### Requirement 17: Backend Scoping Tests

**User Story:** En tant que développeur backend, je veux des tests automatisés vérifiant l'isolation des données, afin de garantir qu'aucun rôle ne peut accéder à des données non autorisées.

#### Acceptance Criteria

1. THE API_Backend SHALL include automated tests for ADMINISTRATEUR dashboard data scoping
2. THE API_Backend SHALL include automated tests for AGENT_CREDIT dashboard data scoping
3. THE API_Backend SHALL include automated tests for AGENT_SANTE dashboard data scoping
4. THE API_Backend SHALL include automated tests for AGENT_PEDAGOGIQUE dashboard data scoping
5. THE API_Backend SHALL include automated tests for VALIDATEUR dashboard data scoping
6. THE API_Backend SHALL include automated tests for RESPONSABLE_CREDIT dashboard data scoping
7. THE API_Backend SHALL include automated tests for PROFESSIONNEL_SANTE dashboard data scoping
8. THE API_Backend SHALL include automated tests for RESPONSABLE_PEDAGOGIQUE dashboard data scoping
9. THE API_Backend SHALL include automated tests for AUDITEUR dashboard data scoping
10. THE scoping tests SHALL verify that Agent_Users see only their own decisions and ADMINISTRATEUR's decisions in their domain
11. THE scoping tests SHALL verify that VALIDATEUR sees only CREDIT domain decisions
12. THE scoping tests SHALL verify that manager roles see only their domain's decisions
13. THE scoping tests SHALL create test data with multiple users, domains and roles
14. THE scoping tests SHALL assert that returned statistics contain only authorized decisions
15. IF a scoping test fails, THEN THE test SHALL report which unauthorized decision was included

### Requirement 18: Responsive Layout

**User Story:** En tant qu'utilisateur mobile, je veux un dashboard adapté à mon écran, afin de consulter mes données sur smartphone ou tablette.

#### Acceptance Criteria

1. WHEN the viewport width is >= 1024px, THE Dashboard_Component SHALL display KPI_Cards in a 4-column grid
2. WHEN the viewport width is >= 1024px, THE Dashboard_Component SHALL display Chart_Components in a 2-column grid
3. WHEN the viewport width is between 768px and 1023px, THE Dashboard_Component SHALL display KPI_Cards in a 2-column grid
4. WHEN the viewport width is between 768px and 1023px, THE Dashboard_Component SHALL display Chart_Components in a 1-column grid
5. WHEN the viewport width is < 768px, THE Dashboard_Component SHALL display KPI_Cards in a 1-column grid
6. WHEN the viewport width is < 768px, THE Dashboard_Component SHALL display Chart_Components in a 1-column grid
7. THE Dashboard_Component SHALL use CSS flexbox or grid for Responsive_Layout
8. THE Dashboard_Component SHALL maintain readable font sizes on all screen sizes
9. THE Dashboard_Component SHALL ensure interactive elements have minimum touch target size of 44x44px on mobile
10. THE Dashboard_Component SHALL test Responsive_Layout on Chrome DevTools device emulation

### Requirement 19: Dark Mode Compatibility

**User Story:** En tant qu'utilisateur préférant le thème sombre, je veux que les dashboards soient lisibles en mode sombre, afin de réduire la fatigue visuelle.

#### Acceptance Criteria

1. THE Dashboard_Component SHALL use CSS variables from the existing application theme
2. WHEN Dark_Mode is active, THE Dashboard_Component SHALL display light text on dark backgrounds
3. WHEN Dark_Mode is active, THE KPI_Card SHALL use dark card backgrounds with appropriate contrast
4. WHEN Dark_Mode is active, THE Chart_Component SHALL use light colors for chart elements
5. WHEN Dark_Mode is active, THE table SHALL use dark row backgrounds with light text
6. THE Dashboard_Component SHALL NOT hardcode color values in component styles
7. THE Dashboard_Component SHALL inherit theme colors from PrimeNG theme CSS variables
8. THE Dashboard_Component SHALL maintain WCAG AA contrast ratios in both light and Dark_Mode

### Requirement 20: Frontend Build and Backend Compilation

**User Story:** En tant que développeur, je veux que le projet compile sans erreur, afin d'assurer l'intégrité du code et la qualité du livrable.

#### Acceptance Criteria

1. WHEN the backend is compiled using Maven, THE compilation SHALL complete without errors
2. WHEN the backend tests are executed, ALL existing tests SHALL pass
3. WHEN the backend scoping tests are executed, ALL scoping tests SHALL pass
4. WHEN the frontend is built using Angular CLI, THE build SHALL complete without errors
5. WHEN the frontend is built, THE build SHALL produce no TypeScript compilation errors
6. WHEN the frontend is built, THE build SHALL produce no linting errors
7. THE backend compilation SHALL complete within 120 seconds
8. THE frontend build SHALL complete within 90 seconds
9. IF compilation fails, THEN THE build output SHALL clearly indicate the error location and cause
