# Implementation Plan: Remove VALIDATEUR Role

## Overview

This plan implements the removal of the legacy VALIDATEUR role from the Tracabilité IA backend system, reducing the role enumeration from 10 to 9 roles. The VALIDATEUR role has been superseded by three domain-specific validator roles (RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, RESPONSABLE_PEDAGOGIQUE). This change enforces strict domain isolation by eliminating cross-domain validation capability.

The implementation modifies the role enumeration, security authorization mapping, database schema constraints, bootstrap account initialization, and creates a database migration script to remove the legacy validateur@tracabilite.ia account.

## Tasks

- [x] 1. Update RoleEnum enumeration
  - Remove VALIDATEUR enum value from RoleEnum.java
  - Update Javadoc to reference 9 roles instead of 10
  - Remove VALIDATEUR from legacy role description in Javadoc
  - Preserve UTILISATEUR enum value with legacy documentation comment
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 2. Update RoleAuthorityMapper security configuration
  - [x] 2.1 Remove VALIDATEUR from mapToSpringRole method
    - Remove "VALIDATEUR" case from switch statement in mapToSpringRole()
    - Preserve "VALIDATOR" case for JWT compatibility
    - Ensure RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, RESPONSABLE_PEDAGOGIQUE map to ROLE_VALIDATOR
    - Verify UTILISATEUR continues to map to ROLE_USER
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 9.3_
  
  - [ ]* 2.2 Write property test for domain validator authority mapping
    - **Property 1: Domain Validator Authority Mapping**
    - **Validates: Requirements 2.2, 2.3, 2.4, 2.6, 7.1, 7.2, 7.3, 8.1, 8.2**
    - Test that fromRoleClaim() returns both ROLE_VALIDATOR and domain-specific authority for each domain validator role
    - Use parameterized test with all three domain validator roles
    - Verify RESPONSABLE_CREDIT returns ROLE_VALIDATOR + ROLE_CREDIT_VALIDATOR
    - Verify PROFESSIONNEL_SANTE returns ROLE_VALIDATOR + ROLE_MEDICAL_VALIDATOR
    - Verify RESPONSABLE_PEDAGOGIQUE returns ROLE_VALIDATOR + ROLE_EDUCATION_VALIDATOR

- [x] 3. Create database migration V7
  - [x] 3.1 Create V7__remove_legacy_validateur.sql migration file
    - Create file in backend/src/main/resources/db/migration/
    - Add migration header comments explaining purpose
    - Implement DELETE statement for validateur@tracabilite.ia account using LOWER(email) for case-insensitive matching
    - Implement DROP CONSTRAINT IF EXISTS for utilisateur_role_check
    - Implement ADD CONSTRAINT with 9 roles (exclude VALIDATEUR)
    - Include all 9 roles in alphabetical groups: ADMINISTRATEUR, AUDITEUR, UTILISATEUR, 3 agents, 3 domain validators
    - Add verification comments at end of migration
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 4.10, 4.11, 4.12, 4.13, 5.1, 5.2, 5.3_

- [ ] 4. Update DataInitializer bootstrap configuration
  - [ ] 4.1 Remove seedValidateur method and invocation
    - Delete seedValidateur() method entirely from DataInitializer.java
    - Remove seedValidateur() invocation from seedDemoData() method
    - Preserve all other seed method invocations (seedAdmin, seedUser, seedAuditeur, seedDomainValidators)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9_
  
  - [~] 4.2 Update updateRoleCheckConstraint method
    - Update updateRoleCheckConstraint() to drop utilisateur_role_check constraint
    - Update updateRoleCheckConstraint() to create constraint with 9 roles
    - Use identical role list as V7 migration (exclude VALIDATEUR)
    - Ensure method executes during application startup
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_
  
  - [ ]* 4.3 Write property test for constraint consistency
    - **Property 2: Migration and Runtime Constraint Consistency**
    - **Validates: Requirement 6.3**
    - Extract role list from V7 migration SQL
    - Extract role list from updateRoleCheckConstraint() Java code
    - Normalize both to sets and verify equality
    - Ensure both contain exactly 9 roles
    - Ensure both exclude VALIDATEUR

- [~] 5. Checkpoint - Verify compilation and constraint consistency
  - Ensure all code compiles without errors
  - Run Property 2 test to verify V7 migration and DataInitializer have identical role lists
  - Ask the user if questions arise

- [ ] 6. Write integration tests for bootstrap accounts
  - [~] 6.1 Write test to verify validateur account is NOT created
    - Create fresh database test scenario
    - Run DataInitializer.seedDemoData()
    - Query for validateur@tracabilite.ia account
    - Assert account does not exist
    - _Requirements: 3.1, 3.2_
  
  - [~] 6.2 Write test to verify domain validator accounts ARE created
    - Create fresh database test scenario
    - Run DataInitializer.seedDemoData()
    - Query for credit@tracabilite.ia, sante@tracabilite.ia, pedago@tracabilite.ia
    - Assert all three accounts exist with correct roles
    - Verify RESPONSABLE_CREDIT role for credit@tracabilite.ia
    - Verify PROFESSIONNEL_SANTE role for sante@tracabilite.ia
    - Verify RESPONSABLE_PEDAGOGIQUE role for pedago@tracabilite.ia
    - _Requirements: 3.3, 3.4, 3.5, 3.6, 8.5_
  
  - [~] 6.3 Write test to verify other bootstrap accounts are preserved
    - Create fresh database test scenario
    - Run DataInitializer.seedDemoData()
    - Query for admin@tracabilite.ia, user@tracabilite.ia, auditeur@tracabilite.ia
    - Assert all three accounts exist with correct roles
    - Verify UTILISATEUR role for user@tracabilite.ia
    - _Requirements: 3.7, 3.8, 3.9, 9.2, 9.4_

- [ ] 7. Write integration tests for database migration
  - [~] 7.1 Write test to verify V7 migration deletes validateur account
    - Create test database with validateur@tracabilite.ia account
    - Run Flyway migration V7
    - Query for validateur@tracabilite.ia account
    - Assert account is deleted (0 rows returned)
    - _Requirements: 5.1, 5.2_
  
  - [~] 7.2 Write test to verify V7 migration updates constraint
    - Create test database with old 10-role constraint
    - Run Flyway migration V7
    - Query pg_constraint for utilisateur_role_check definition
    - Assert constraint includes exactly 9 roles
    - Assert constraint excludes VALIDATEUR
    - _Requirements: 4.2, 4.3, 4.13_
  
  - [~] 7.3 Write test to verify V7 migration preserves other accounts
    - Create test database with multiple user accounts (admin, user, auditeur, domain validators)
    - Run Flyway migration V7
    - Query utilisateur table for all accounts except validateur
    - Assert count matches pre-migration count
    - Assert all accounts have same data as before migration
    - _Requirements: 5.3_
  
  - [~] 7.4 Write test to verify constraint rejects VALIDATEUR role
    - Run migration V7
    - Attempt to insert utilisateur with role = 'VALIDATEUR'
    - Assert DataIntegrityViolationException is thrown
    - Assert exception message references role constraint
    - _Requirements: 4.13_

- [~] 8. Checkpoint - Ensure all tests pass
  - Run all unit tests for RoleEnum, RoleAuthorityMapper
  - Run all integration tests for bootstrap accounts and migration
  - Run property-based tests
  - Verify all tests pass
  - Ask the user if questions arise

- [ ] 9. Write authorization tests for domain validators
  - [~] 9.1 Write test for RESPONSABLE_CREDIT validation access
    - Authenticate as credit@tracabilite.ia
    - Create CREDIT domain decision
    - Attempt to validate CREDIT decision via REST endpoint
    - Assert HTTP 200 response
    - Assert decision status updated to VALIDATED
    - _Requirements: 8.3, 8.4_
  
  - [~] 9.2 Write test for RESPONSABLE_CREDIT domain isolation
    - Authenticate as credit@tracabilite.ia
    - Create MEDICAL and EDUCATION domain decisions
    - Attempt to query MEDICAL/EDUCATION decisions
    - Assert returned list excludes non-CREDIT decisions
    - Attempt to validate MEDICAL decision via REST endpoint
    - Assert HTTP 403 Forbidden response
    - _Requirements: 7.4, 7.5_
  
  - [~] 9.3 Write test for PROFESSIONNEL_SANTE validation access
    - Authenticate as sante@tracabilite.ia
    - Create MEDICAL domain decision
    - Attempt to validate MEDICAL decision via REST endpoint
    - Assert HTTP 200 response
    - Assert decision status updated to VALIDATED
    - _Requirements: 8.3_
  
  - [~] 9.4 Write test for RESPONSABLE_PEDAGOGIQUE validation access
    - Authenticate as pedago@tracabilite.ia
    - Create EDUCATION domain decision
    - Attempt to validate EDUCATION decision via REST endpoint
    - Assert HTTP 200 response
    - Assert decision status updated to VALIDATED
    - _Requirements: 8.3_

- [ ] 10. Update existing tests that reference VALIDATEUR
  - [~] 10.1 Search codebase for hardcoded "VALIDATEUR" strings in tests
    - Use grep to find all test files containing "VALIDATEUR"
    - Update test assertions to use domain-specific roles
    - Update test data fixtures to use RESPONSABLE_CREDIT instead of VALIDATEUR
    - Remove or update tests that specifically test VALIDATEUR role behavior
    - _Requirements: 1.1, 2.1_

- [~] 11. Final checkpoint - Complete verification
  - Run full test suite (unit + integration tests)
  - Verify RoleEnum has exactly 9 values
  - Verify VALIDATEUR does not exist in RoleEnum
  - Verify UTILISATEUR exists in RoleEnum with legacy comment
  - Verify V7 migration file exists and is syntactically valid
  - Verify DataInitializer does not create validateur account
  - Verify all domain validator accounts are created correctly
  - Stage all modified files using git add
  - Do NOT commit or push changes
  - Ask the user if questions arise

## Notes

- Tasks marked with `*` are optional property-based tests and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation throughout implementation
- Focus on backend changes only - frontend role selection UI updates are out of scope
- Migration V7 is automatically executed by Flyway on application startup
- The implementation preserves domain isolation by maintaining domain-specific validator roles
- UTILISATEUR legacy role is preserved for backward compatibility
- No git commit or push operations should be performed - changes remain staged only

## Task Dependency Graph

```json
{
  "waves": [
    {
      "id": 0,
      "tasks": ["1"]
    },
    {
      "id": 1,
      "tasks": ["2.1", "3.1"]
    },
    {
      "id": 2,
      "tasks": ["2.2", "4.1"]
    },
    {
      "id": 3,
      "tasks": ["4.2"]
    },
    {
      "id": 4,
      "tasks": ["4.3"]
    },
    {
      "id": 5,
      "tasks": ["6.1", "6.2", "6.3"]
    },
    {
      "id": 6,
      "tasks": ["7.1", "7.2", "7.3", "7.4"]
    },
    {
      "id": 7,
      "tasks": ["9.1", "9.2", "9.3", "9.4"]
    },
    {
      "id": 8,
      "tasks": ["10.1"]
    }
  ]
}
```
