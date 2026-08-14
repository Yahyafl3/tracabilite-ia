# Requirements Document

## Introduction

This document specifies requirements for removing the legacy VALIDATEUR role from the Tracabilité IA system. The VALIDATEUR role was part of the original 10-role architecture and has been superseded by three domain-specific validator roles: RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, and RESPONSABLE_PEDAGOGIQUE. This change reduces the system from 10 roles to 9 roles, eliminates the bootstrap validateur@tracabilite.ia account, and establishes RESPONSABLE_CREDIT as the validator for CREDIT domain decisions.

The scope includes modifications to the role enumeration, authorization mapping, database schema constraints, bootstrap account initialization, and migration scripts. The change preserves domain isolation, maintains backward compatibility for UTILISATEUR role, and does not commit or push changes to version control.

## Glossary

- **System**: The Tracabilité IA backend application
- **RoleEnum**: Java enumeration defining valid user roles
- **RoleAuthorityMapper**: Security component mapping roles to Spring Security authorities
- **UtilisateurRepository**: Data access layer for user accounts
- **DataInitializer**: Bootstrap component that seeds demo accounts on application startup
- **BootstrapAccount**: Demo user account created during initial system setup
- **MigrationScript**: SQL script executed by Flyway to evolve database schema
- **DomainValidator**: User with validation authority for a specific decision domain (CREDIT, MEDICAL, EDUCATION)
- **RoleCheckConstraint**: Database CHECK constraint enforcing valid role values
- **VALIDATEUR_Role**: Legacy cross-domain validator role being removed
- **RESPONSABLE_CREDIT_Role**: Domain-specific validator role for CREDIT domain

## Requirements

### Requirement 1: Remove VALIDATEUR Role from Enumeration

**User Story:** As a system maintainer, I want the VALIDATEUR role removed from the codebase, so that only current roles are available for assignment.

#### Acceptance Criteria

1.1 THE System SHALL remove VALIDATEUR from RoleEnum enumeration

1.2 THE System SHALL preserve VALIDATEUR legacy documentation comment in RoleEnum

1.3 THE System SHALL reduce total role count from 10 to 9 in RoleEnum

1.4 THE System SHALL retain UTILISATEUR in RoleEnum with legacy documentation

### Requirement 2: Update Authorization Mapping

**User Story:** As a security engineer, I want VALIDATEUR removed from authorization mappings, so that RESPONSABLE_CREDIT handles CREDIT domain validation.

#### Acceptance Criteria

2.1 THE System SHALL remove VALIDATEUR from RoleAuthorityMapper.mapToSpringRole switch statement

2.2 THE System SHALL map RESPONSABLE_CREDIT to ROLE_VALIDATOR authority

2.3 THE System SHALL map PROFESSIONNEL_SANTE to ROLE_VALIDATOR authority

2.4 THE System SHALL map RESPONSABLE_PEDAGOGIQUE to ROLE_VALIDATOR authority

2.5 THE System SHALL map RESPONSABLE_CREDIT to ROLE_CREDIT_VALIDATOR authority via domainAuthorities method

2.6 THE System SHALL preserve fromRoleClaim method logic for domain validators

### Requirement 3: Remove VALIDATEUR Bootstrap Account

**User Story:** As a system administrator, I want the validateur@tracabilite.ia bootstrap account removed, so that only domain-specific validators exist in demo environments.

#### Acceptance Criteria

3.1 THE System SHALL remove seedValidateur method from DataInitializer

3.2 THE System SHALL remove seedValidateur method invocation from seedDemoData

3.3 THE System SHALL preserve seedDomainValidators method and invocation

3.4 THE System SHALL preserve credit@tracabilite.ia bootstrap account creation

3.5 THE System SHALL preserve sante@tracabilite.ia bootstrap account creation

3.6 THE System SHALL preserve pedago@tracabilite.ia bootstrap account creation

3.7 THE System SHALL preserve admin@tracabilite.ia bootstrap account creation

3.8 THE System SHALL preserve user@tracabilite.ia bootstrap account creation

3.9 THE System SHALL preserve auditeur@tracabilite.ia bootstrap account creation

### Requirement 4: Update Database Role Constraint

**User Story:** As a database administrator, I want the utilisateur table constraint updated, so that VALIDATEUR values are rejected for new records.

#### Acceptance Criteria

4.1 THE System SHALL create V7__remove_legacy_validateur.sql migration script

4.2 THE System SHALL drop existing utilisateur_role_check constraint in migration

4.3 THE System SHALL create new utilisateur_role_check constraint with 9 roles in migration

4.4 THE System SHALL include ADMINISTRATEUR in new constraint

4.5 THE System SHALL include AUDITEUR in new constraint

4.6 THE System SHALL include UTILISATEUR in new constraint

4.7 THE System SHALL include AGENT_CREDIT in new constraint

4.8 THE System SHALL include AGENT_SANTE in new constraint

4.9 THE System SHALL include AGENT_PEDAGOGIQUE in new constraint

4.10 THE System SHALL include RESPONSABLE_CREDIT in new constraint

4.11 THE System SHALL include PROFESSIONNEL_SANTE in new constraint

4.12 THE System SHALL include RESPONSABLE_PEDAGOGIQUE in new constraint

4.13 THE System SHALL exclude VALIDATEUR from new constraint

### Requirement 5: Delete Existing VALIDATEUR Account

**User Story:** As a system administrator, I want the validateur@tracabilite.ia account deleted from the database, so that legacy accounts do not remain in production.

#### Acceptance Criteria

5.1 WHEN V7__remove_legacy_validateur.sql executes, THE System SHALL delete utilisateur record WHERE email equals validateur@tracabilite.ia

5.2 WHEN V7__remove_legacy_validateur.sql executes, THE System SHALL use case-insensitive email matching

5.3 THE System SHALL preserve all other utilisateur records during migration

### Requirement 6: Update Runtime Role Constraint Synchronization

**User Story:** As a developer, I want DataInitializer.updateRoleCheckConstraint updated, so that runtime constraint matches migration schema.

#### Acceptance Criteria

6.1 THE System SHALL update updateRoleCheckConstraint method to drop utilisateur_role_check constraint

6.2 THE System SHALL update updateRoleCheckConstraint method to create constraint with 9 roles

6.3 THE System SHALL include identical role list as V7 migration in updateRoleCheckConstraint

6.4 THE System SHALL exclude VALIDATEUR from updateRoleCheckConstraint role list

6.5 THE System SHALL execute updateRoleCheckConstraint during application startup

### Requirement 7: Preserve Domain Isolation

**User Story:** As a security architect, I want domain isolation preserved, so that validators only access their assigned domain.

#### Acceptance Criteria

7.1 THE System SHALL maintain ROLE_CREDIT_VALIDATOR authority for RESPONSABLE_CREDIT

7.2 THE System SHALL maintain ROLE_MEDICAL_VALIDATOR authority for PROFESSIONNEL_SANTE

7.3 THE System SHALL maintain ROLE_EDUCATION_VALIDATOR authority for RESPONSABLE_PEDAGOGIQUE

7.4 THE System SHALL preserve DecisionScopeService domain filtering logic

7.5 THE System SHALL preserve domain-specific query restrictions in repositories

### Requirement 8: Maintain CREDIT Domain Validation Authority

**User Story:** As a CREDIT domain user, I want RESPONSABLE_CREDIT to validate CREDIT decisions, so that domain validation continues without interruption.

#### Acceptance Criteria

8.1 THE System SHALL map RESPONSABLE_CREDIT to ROLE_VALIDATOR authority

8.2 THE System SHALL map RESPONSABLE_CREDIT to ROLE_CREDIT_VALIDATOR authority

8.3 THE System SHALL authorize RESPONSABLE_CREDIT for decision validation endpoints

8.4 THE System SHALL authorize RESPONSABLE_CREDIT for CREDIT domain decision queries

8.5 THE System SHALL preserve credit@tracabilite.ia bootstrap account with RESPONSABLE_CREDIT role

### Requirement 9: Preserve UTILISATEUR Legacy Role

**User Story:** As a system maintainer, I want UTILISATEUR role preserved, so that existing user accounts continue functioning.

#### Acceptance Criteria

9.1 THE System SHALL retain UTILISATEUR in RoleEnum

9.2 THE System SHALL include UTILISATEUR in database role constraint

9.3 THE System SHALL map UTILISATEUR to ROLE_USER authority

9.4 THE System SHALL preserve user@tracabilite.ia bootstrap account

9.5 THE System SHALL retain UTILISATEUR legacy documentation comment

### Requirement 10: No Version Control Operations

**User Story:** As a developer, I want changes staged locally only, so that review occurs before committing.

#### Acceptance Criteria

10.1 THE System SHALL stage all modified files using git add

10.2 THE System SHALL NOT execute git commit

10.3 THE System SHALL NOT execute git push

10.4 THE System SHALL leave changes uncommitted in working directory
