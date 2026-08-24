# Design Document: Remove VALIDATEUR Role

## Overview

This design describes the removal of the legacy VALIDATEUR role from the Tracabilité IA system, reducing the role enumeration from 10 to 9 roles. The VALIDATEUR role has been superseded by three domain-specific validator roles (RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, RESPONSABLE_PEDAGOGIQUE), and its removal eliminates cross-domain validation in favor of strict domain isolation.

The design covers modifications to the role enumeration, security authorization mapping, database schema constraints, bootstrap account initialization, and database migration scripts. The changes preserve backward compatibility for the UTILISATEUR legacy role and maintain domain isolation for all validator roles.

## System Architecture

### Current State (10 Roles)

The system currently defines 10 roles in `RoleEnum`:

```java
public enum RoleEnum {
    ADMINISTRATEUR,
    AUDITEUR,
    AGENT_CREDIT,
    AGENT_SANTE,
    AGENT_PEDAGOGIQUE,
    RESPONSABLE_CREDIT,
    PROFESSIONNEL_SANTE,
    RESPONSABLE_PEDAGOGIQUE,
    UTILISATEUR,    // Legacy - preserved for compatibility
    VALIDATEUR      // Legacy - TO BE REMOVED
}
```

**Authorization Mapping (Current):**
- `VALIDATEUR` → `ROLE_VALIDATOR`
- `RESPONSABLE_CREDIT` → `ROLE_VALIDATOR` + `ROLE_CREDIT_VALIDATOR`
- `PROFESSIONNEL_SANTE` → `ROLE_VALIDATOR` + `ROLE_MEDICAL_VALIDATOR`
- `RESPONSABLE_PEDAGOGIQUE` → `ROLE_VALIDATOR` + `ROLE_EDUCATION_VALIDATOR`

**Bootstrap Accounts (Current):**
- `validateur@tracabilite.ia` (VALIDATEUR role) - TO BE REMOVED
- `credit@tracabilite.ia` (RESPONSABLE_CREDIT role) - PRESERVED
- `sante@tracabilite.ia` (PROFESSIONNEL_SANTE role) - PRESERVED
- `pedago@tracabilite.ia` (RESPONSABLE_PEDAGOGIQUE role) - PRESERVED
- `admin@tracabilite.ia`, `user@tracabilite.ia`, `auditeur@tracabilite.ia` - PRESERVED

### Target State (9 Roles)

After this change, the system will have 9 roles:

```java
public enum RoleEnum {
    ADMINISTRATEUR,
    AUDITEUR,
    AGENT_CREDIT,
    AGENT_SANTE,
    AGENT_PEDAGOGIQUE,
    RESPONSABLE_CREDIT,
    PROFESSIONNEL_SANTE,
    RESPONSABLE_PEDAGOGIQUE,
    /** Legacy — compatibilité. Ne plus proposer à la création. */
    UTILISATEUR
}
```

**Authorization Mapping (Target):**
- `RESPONSABLE_CREDIT` → `ROLE_VALIDATOR` + `ROLE_CREDIT_VALIDATOR`
- `PROFESSIONNEL_SANTE` → `ROLE_VALIDATOR` + `ROLE_MEDICAL_VALIDATOR`
- `RESPONSABLE_PEDAGOGIQUE` → `ROLE_VALIDATOR` + `ROLE_EDUCATION_VALIDATOR`
- No cross-domain `VALIDATEUR` role

**Bootstrap Accounts (Target):**
- `validateur@tracabilite.ia` - DELETED
- All domain-specific validator accounts preserved
- All other bootstrap accounts preserved

## Component Design

### 1. Role Enumeration (`RoleEnum.java`)

**Location:** `backend/src/main/java/com/pfa/tracabilite_ia/enumeration/RoleEnum.java`

**Changes:**
- Remove `VALIDATEUR` enum value
- Preserve `UTILISATEUR` enum value with legacy documentation comment
- Update Javadoc to reference only 9 roles and remove VALIDATEUR from legacy role description

**Implementation:**

```java
/**
 * Rôles métier des comptes internes.
 * <p>
 * {@link #AGENT_CREDIT}, {@link #AGENT_SANTE}, {@link #AGENT_PEDAGOGIQUE} sont les agents
 * créateurs de décisions par domaine. Ils ne valident pas.
 * {@link #RESPONSABLE_CREDIT}, {@link #PROFESSIONNEL_SANTE},
 * {@link #RESPONSABLE_PEDAGOGIQUE} sont les validateurs spécialisés par domaine.
 * {@link #UTILISATEUR} est un rôle LEGACY conservé pour compatibilité ; 
 * il n'est plus proposé à la création de nouveaux comptes.
 * {@link #ADMINISTRATEUR} gère les comptes et a une visibilité globale.
 */
public enum RoleEnum {
    ADMINISTRATEUR,
    AUDITEUR,
    AGENT_CREDIT,
    AGENT_SANTE,
    AGENT_PEDAGOGIQUE,
    RESPONSABLE_CREDIT,
    PROFESSIONNEL_SANTE,
    RESPONSABLE_PEDAGOGIQUE,
    /** Legacy — compatibilité. Ne plus proposer à la création. */
    UTILISATEUR
}
```

**Rationale:**
- Removing VALIDATEUR eliminates cross-domain validation capability
- Preserving UTILISATEUR maintains backward compatibility for existing accounts
- Documentation update clarifies that only UTILISATEUR is legacy (not VALIDATEUR)

### 2. Authorization Mapper (`RoleAuthorityMapper.java`)

**Location:** `backend/src/main/java/com/pfa/tracabilite_ia/security/RoleAuthorityMapper.java`

**Changes:**
- Remove `VALIDATEUR` case from `mapToSpringRole()` switch statement
- Preserve domain validator logic in `fromRoleClaim()` method
- Preserve `domainAuthorities()` method mapping for all three domain validators
- Ensure RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, and RESPONSABLE_PEDAGOGIQUE map to `ROLE_VALIDATOR` authority

**Implementation:**

```java
public static String mapToSpringRole(String role) {
    return switch (role.toUpperCase()) {
        case "ADMIN", "ADMINISTRATEUR" -> "ROLE_ADMIN";
        case "USER", "UTILISATEUR",
             "AGENT_CREDIT", "AGENT_SANTE", "AGENT_PEDAGOGIQUE" -> "ROLE_USER";
        case "VALIDATOR",  // Keep for potential JWT claims compatibility
             "RESPONSABLE_CREDIT", "PROFESSIONNEL_SANTE", "RESPONSABLE_PEDAGOGIQUE"
                -> "ROLE_VALIDATOR";
        case "AUDITOR", "AUDITEUR" -> "ROLE_AUDITOR";
        default -> role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
    };
}
```

**Key Points:**
- Removed `"VALIDATEUR"` from the ROLE_VALIDATOR case
- Kept `"VALIDATOR"` for potential external JWT claims compatibility
- All three domain validators continue to map to `ROLE_VALIDATOR`
- `domainAuthorities()` method remains unchanged, providing domain-specific authorities

**Domain Authority Mapping (Unchanged):**

```java
public static Collection<GrantedAuthority> domainAuthorities(String role) {
    return switch (role.toUpperCase()) {
        case "RESPONSABLE_CREDIT" -> List.of(
                new SimpleGrantedAuthority("ROLE_VALIDATOR"),
                new SimpleGrantedAuthority("ROLE_CREDIT_VALIDATOR")
        );
        case "PROFESSIONNEL_SANTE" -> List.of(
                new SimpleGrantedAuthority("ROLE_VALIDATOR"),
                new SimpleGrantedAuthority("ROLE_MEDICAL_VALIDATOR")
        );
        case "RESPONSABLE_PEDAGOGIQUE" -> List.of(
                new SimpleGrantedAuthority("ROLE_VALIDATOR"),
                new SimpleGrantedAuthority("ROLE_EDUCATION_VALIDATOR")
        );
        default -> fromRoleClaim(role);
    };
}
```

### 3. Bootstrap Data Initializer (`DataInitializer.java`)

**Location:** `backend/src/main/java/com/pfa/tracabilite_ia/config/DataInitializer.java`

**Changes:**
- Remove `seedValidateur()` method entirely
- Remove `seedValidateur()` invocation from `seedDemoData()` method
- Update `updateRoleCheckConstraint()` to create constraint with 9 roles (exclude VALIDATEUR)
- Preserve all other bootstrap account creation methods

**Removed Method:**

```java
// DELETE THIS METHOD:
private void seedValidateur(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
    // ... method body removed
}
```

**Updated `seedDemoData()` Method:**

```java
@Bean
public CommandLineRunner seedDemoData(
        UtilisateurRepository utilisateurRepository,
        PasswordEncoder passwordEncoder,
        JdbcTemplate jdbcTemplate,
        AdminDemoAccountSynchronizer adminDemoAccountSynchronizer,
        @Value("${app.demo.seed-enabled:true}") boolean seedEnabled
) {
    return args -> {
        updateRoleCheckConstraint(jdbcTemplate);
        updateDecisionStatusCheckConstraint(jdbcTemplate);
        updateDecisionHistoryStatusCheckConstraints(jdbcTemplate);
        ensureUtilisateurActifColumn(jdbcTemplate);

        if (!seedEnabled) {
            log.info(">>> Seed demo desactive (app.demo.seed-enabled=false)");
            return;
        }

        long userCount = utilisateurRepository.count();
        if (userCount == 0) {
            seedAdmin(utilisateurRepository, passwordEncoder);
            seedUser(utilisateurRepository, passwordEncoder);
            // seedValidateur() REMOVED
            seedAuditeur(utilisateurRepository, passwordEncoder);
            seedDomainValidators(utilisateurRepository, passwordEncoder);
            log.info(">>> Seed initial des utilisateurs de demo termine");
        } else {
            ensureAtLeastOneAdmin(utilisateurRepository, passwordEncoder);
            seedUser(utilisateurRepository, passwordEncoder);
            // seedValidateur() REMOVED
            seedAuditeur(utilisateurRepository, passwordEncoder);
            seedDomainValidators(utilisateurRepository, passwordEncoder);
            log.info(">>> Seed utilisateurs ignore (base deja initialisee, {} compte(s))", userCount);
        }

        adminDemoAccountSynchronizer.syncAdminEmail();
    };
}
```

**Updated `updateRoleCheckConstraint()` Method:**

```java
private void updateRoleCheckConstraint(JdbcTemplate jdbcTemplate) {
    jdbcTemplate.execute("""
            ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_role_check
            """);
    jdbcTemplate.execute("""
            ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_role_check
            CHECK (role IN (
                'ADMINISTRATEUR', 'AUDITEUR', 'UTILISATEUR',
                'AGENT_CREDIT', 'AGENT_SANTE', 'AGENT_PEDAGOGIQUE',
                'RESPONSABLE_CREDIT', 'PROFESSIONNEL_SANTE', 'RESPONSABLE_PEDAGOGIQUE'
            ))
            """);
}
```

**Preserved Methods:**
- `seedAdmin()` - Creates admin@tracabilite.ia
- `seedUser()` - Creates user@tracabilite.ia (UTILISATEUR role)
- `seedAuditeur()` - Creates auditeur@tracabilite.ia
- `seedDomainValidators()` - Creates credit@tracabilite.ia, sante@tracabilite.ia, pedago@tracabilite.ia

### 4. Database Migration (`V7__remove_legacy_validateur.sql`)

**Location:** `backend/src/main/resources/db/migration/V7__remove_legacy_validateur.sql`

**Purpose:** Remove VALIDATEUR role from database schema and delete validateur@tracabilite.ia account

**Migration Script:**

```sql
-- Migration V7: Remove legacy VALIDATEUR role
-- This migration removes the VALIDATEUR role from the role constraint
-- and deletes the validateur@tracabilite.ia bootstrap account.
-- The VALIDATEUR role has been superseded by domain-specific validators:
-- RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, RESPONSABLE_PEDAGOGIQUE

-- Step 1: Delete the legacy validateur@tracabilite.ia account
DELETE FROM utilisateur
WHERE LOWER(email) = 'validateur@tracabilite.ia';

-- Step 2: Update the role check constraint to exclude VALIDATEUR
ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_role_check;

ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_role_check
CHECK (role IN (
    'ADMINISTRATEUR',
    'AUDITEUR',
    'UTILISATEUR',
    'AGENT_CREDIT',
    'AGENT_SANTE',
    'AGENT_PEDAGOGIQUE',
    'RESPONSABLE_CREDIT',
    'PROFESSIONNEL_SANTE',
    'RESPONSABLE_PEDAGOGIQUE'
));

-- Step 3: Verification comment
-- After this migration:
-- - Total roles: 9 (reduced from 10)
-- - VALIDATEUR role is no longer valid in database
-- - validateur@tracabilite.ia account is deleted
-- - Domain-specific validators (credit@, sante@, pedago@) are preserved
-- - UTILISATEUR legacy role is preserved for backward compatibility
```

**Migration Strategy:**
1. **Account Deletion First:** Delete validateur@tracabilite.ia before dropping constraint (prevents constraint violation)
2. **Case-Insensitive Matching:** Use `LOWER(email)` for robust email matching
3. **Constraint Recreation:** Drop and recreate constraint with 9 roles (cannot modify existing constraint)
4. **Idempotent Operations:** Use `IF EXISTS` for safe re-execution

**Rollback Strategy (if needed):**

```sql
-- Rollback V7: Restore VALIDATEUR role (NOT RECOMMENDED)
-- Only use if critical issue discovered after deployment

ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_role_check;

ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_role_check
CHECK (role IN (
    'ADMINISTRATEUR',
    'VALIDATEUR',
    'AUDITEUR',
    'UTILISATEUR',
    'AGENT_CREDIT',
    'AGENT_SANTE',
    'AGENT_PEDAGOGIQUE',
    'RESPONSABLE_CREDIT',
    'PROFESSIONNEL_SANTE',
    'RESPONSABLE_PEDAGOGIQUE'
));

-- Note: This does NOT recreate the validateur@tracabilite.ia account
-- Manual account creation required if needed
```

### 5. Domain Isolation Preservation

**No Changes Required** - The following components maintain domain isolation and require no modification:

**DecisionScopeService:**
- Continues to filter decisions by domain based on user's validator role
- RESPONSABLE_CREDIT sees only CREDIT domain decisions
- PROFESSIONNEL_SANTE sees only MEDICAL domain decisions
- RESPONSABLE_PEDAGOGIQUE sees only EDUCATION domain decisions

**Repository Query Restrictions:**
- `DecisionRepository` methods respect domain filtering
- Query methods check user's `ROLE_CREDIT_VALIDATOR`, `ROLE_MEDICAL_VALIDATOR`, or `ROLE_EDUCATION_VALIDATOR` authorities
- No cross-domain access for validators

**Authorization Configuration:**
- Security configuration already grants validator authorities to domain-specific roles
- No changes needed to `@PreAuthorize` annotations on controller endpoints

**Validation Endpoints:**
- `/api/decisions/{id}/validate` endpoint checks for `ROLE_VALIDATOR` authority
- RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, RESPONSABLE_PEDAGOGIQUE all have this authority
- Domain filtering applied at service layer prevents cross-domain validation

## Data Model

No changes to database table structures. Only changes to constraints:

### Utilisateur Table Constraint

**Before (10 roles):**
```sql
CHECK (role IN (
    'ADMINISTRATEUR', 'VALIDATEUR', 'AUDITEUR', 'UTILISATEUR',
    'AGENT_CREDIT', 'AGENT_SANTE', 'AGENT_PEDAGOGIQUE',
    'RESPONSABLE_CREDIT', 'PROFESSIONNEL_SANTE', 'RESPONSABLE_PEDAGOGIQUE'
))
```

**After (9 roles):**
```sql
CHECK (role IN (
    'ADMINISTRATEUR', 'AUDITEUR', 'UTILISATEUR',
    'AGENT_CREDIT', 'AGENT_SANTE', 'AGENT_PEDAGOGIQUE',
    'RESPONSABLE_CREDIT', 'PROFESSIONNEL_SANTE', 'RESPONSABLE_PEDAGOGIQUE'
))
```

## Error Handling

### Constraint Violation Handling

**Scenario:** Attempting to create/update a user with VALIDATEUR role after migration

```java
try {
    utilisateur.setRole(RoleEnum.VALIDATEUR); // Compile error - enum value doesn't exist
    utilisateurRepository.save(utilisateur);
} catch (DataIntegrityViolationException e) {
    // Would occur if somehow bypassed enum check
    log.error("Invalid role value - VALIDATEUR no longer supported", e);
    throw new BusinessException("Role VALIDATEUR is deprecated. Use domain-specific validator roles.");
}
```

### Migration Error Handling

If migration V7 fails:
1. Transaction rollback prevents partial application
2. Flyway records failure in `flyway_schema_history` table
3. Application startup fails with clear error message
4. Manual intervention required to fix database state

**Common Migration Issues:**
- **Existing VALIDATEUR users:** Migration will fail if users other than validateur@tracabilite.ia have VALIDATEUR role
  - **Resolution:** Manually update these users to domain-specific roles before migration
- **Foreign key violations:** If validateur user referenced in decision history
  - **Resolution:** Migration script deletes user; foreign keys should allow this (ON DELETE SET NULL or CASCADE)

### Authorization Failures

**Scenario:** Legacy code/client sends "VALIDATEUR" in role claim

```java
// RoleAuthorityMapper.mapToSpringRole() will handle gracefully:
public static String mapToSpringRole(String role) {
    return switch (role.toUpperCase()) {
        // ...
        case "VALIDATOR" -> "ROLE_VALIDATOR";  // Still supported for JWT compatibility
        // "VALIDATEUR" falls through to default case
        default -> role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
    };
}
```

Result: "VALIDATEUR" maps to "ROLE_VALIDATEUR" (not "ROLE_VALIDATOR"), which has no permissions
- User will be denied access to validation endpoints
- HTTP 403 Forbidden response
- Client must update to use domain-specific roles

## Testing Strategy

### Unit Tests

**RoleAuthorityMapper Tests:**
- Test `mapToSpringRole()` no longer accepts "VALIDATEUR"
- Test RESPONSABLE_CREDIT maps to ROLE_VALIDATOR
- Test PROFESSIONNEL_SANTE maps to ROLE_VALIDATOR
- Test RESPONSABLE_PEDAGOGIQUE maps to ROLE_VALIDATOR
- Test `domainAuthorities()` returns correct authorities for each domain validator
- Test UTILISATEUR maps to ROLE_USER (backward compatibility)

**RoleEnum Tests:**
- Verify enum count equals 9
- Verify VALIDATEUR is not present
- Verify UTILISATEUR is present
- Verify all 9 expected roles are present

### Integration Tests

**Bootstrap Account Tests:**
- Verify validateur@tracabilite.ia is NOT created on fresh database
- Verify credit@tracabilite.ia IS created with RESPONSABLE_CREDIT role
- Verify sante@tracabilite.ia IS created with PROFESSIONNEL_SANTE role
- Verify pedago@tracabilite.ia IS created with RESPONSABLE_PEDAGOGIQUE role
- Verify admin@, user@, auditeur@ accounts are created

**Migration Tests:**
- Run migration V7 on database with validateur@ account
- Verify account is deleted after migration
- Verify role constraint allows 9 roles
- Verify constraint rejects VALIDATEUR role
- Verify other accounts are not affected

**Authorization Tests:**
- Authenticate as RESPONSABLE_CREDIT
- Verify access to CREDIT domain validation endpoints
- Verify access denied to MEDICAL/EDUCATION domain decisions
- Repeat for PROFESSIONNEL_SANTE and RESPONSABLE_PEDAGOGIQUE

**Domain Isolation Tests:**
- Create decisions in multiple domains
- Verify RESPONSABLE_CREDIT can only query/validate CREDIT decisions
- Verify PROFESSIONNEL_SANTE can only query/validate MEDICAL decisions
- Verify RESPONSABLE_PEDAGOGIQUE can only query/validate EDUCATION decisions

### Example-Based Tests

These tests verify specific behavior with known inputs:

```java
@Test
void testResponsableCreditMapsToValidatorAuthority() {
    Collection<GrantedAuthority> authorities = 
        RoleAuthorityMapper.fromRoleClaim("RESPONSABLE_CREDIT");
    
    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_VALIDATOR")));
    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_CREDIT_VALIDATOR")));
}

@Test
void testProfessionnelSanteMapsToValidatorAuthority() {
    Collection<GrantedAuthority> authorities = 
        RoleAuthorityMapper.fromRoleClaim("PROFESSIONNEL_SANTE");
    
    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_VALIDATOR")));
    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_MEDICAL_VALIDATOR")));
}

@Test
void testUtilisateurMapsToUserAuthority() {
    String authority = RoleAuthorityMapper.mapToSpringRole("UTILISATEUR");
    assertEquals("ROLE_USER", authority);
}

@Test
void testMigrationDeletesValidateurAccount() {
    // Given: Database with validateur@tracabilite.ia account
    utilisateurRepository.save(createValidateurAccount());
    
    // When: Migration V7 executes
    flyway.migrate();
    
    // Then: Account is deleted
    assertFalse(utilisateurRepository.existsByEmailIgnoreCase("validateur@tracabilite.ia"));
}

@Test
void testMigrationPreservesOtherAccounts() {
    // Given: Database with multiple accounts
    List<Utilisateur> originalUsers = utilisateurRepository.findAll();
    long originalCount = originalUsers.stream()
        .filter(u -> !u.getEmail().equalsIgnoreCase("validateur@tracabilite.ia"))
        .count();
    
    // When: Migration V7 executes
    flyway.migrate();
    
    // Then: Other accounts are preserved
    long afterCount = utilisateurRepository.count();
    assertEquals(originalCount, afterCount);
}
```

## Deployment Considerations

### Pre-Deployment Checklist

1. **Database Audit:**
   - Query for users with VALIDATEUR role: `SELECT * FROM utilisateur WHERE role = 'VALIDATEUR'`
   - If users other than validateur@tracabilite.ia exist, migrate them to domain-specific roles manually
   
2. **Code Audit:**
   - Search codebase for hardcoded "VALIDATEUR" strings
   - Check frontend code for VALIDATEUR role references
   - Update any UI dropdown/selection lists that show VALIDATEUR as option

3. **Documentation Update:**
   - Update user manuals to reference domain-specific validators
   - Update API documentation to remove VALIDATEUR from role examples
   - Update deployment runbooks

4. **Backup:**
   - Take full database backup before deployment
   - Document rollback procedure

### Deployment Steps

1. Deploy backend code with VALIDATEUR removed from enum and mapper
2. Restart application
3. Flyway will automatically run V7 migration on startup
4. Verify migration success in `flyway_schema_history` table
5. Test domain validator login and authorization
6. Deploy frontend updates (if any)

### Post-Deployment Verification

1. **Database Verification:**
   ```sql
   -- Verify constraint has 9 roles
   SELECT conname, pg_get_constraintdef(oid) 
   FROM pg_constraint 
   WHERE conname = 'utilisateur_role_check';
   
   -- Verify validateur account deleted
   SELECT * FROM utilisateur WHERE LOWER(email) = 'validateur@tracabilite.ia';
   -- Should return 0 rows
   
   -- Verify domain validator accounts exist
   SELECT email, role FROM utilisateur 
   WHERE email IN ('credit@tracabilite.ia', 'sante@tracabilite.ia', 'pedago@tracabilite.ia');
   -- Should return 3 rows
   ```

2. **Application Verification:**
   - Login as credit@tracabilite.ia
   - Navigate to decision validation page
   - Verify access to CREDIT domain decisions
   - Verify no access to MEDICAL/EDUCATION decisions

3. **Monitoring:**
   - Check application logs for authorization errors
   - Monitor for HTTP 403 responses on validation endpoints
   - Check for database constraint violations

### Rollback Procedure

If critical issues discovered post-deployment:

1. **Code Rollback:**
   - Revert to previous application version
   - Redeploy

2. **Database Rollback:**
   ```sql
   -- Restore VALIDATEUR to constraint
   ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_role_check;
   ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_role_check
   CHECK (role IN (
       'ADMINISTRATEUR', 'VALIDATEUR', 'AUDITEUR', 'UTILISATEUR',
       'AGENT_CREDIT', 'AGENT_SANTE', 'AGENT_PEDAGOGIQUE',
       'RESPONSABLE_CREDIT', 'PROFESSIONNEL_SANTE', 'RESPONSABLE_PEDAGOGIQUE'
   ));
   
   -- Manually recreate validateur@tracabilite.ia account if needed
   INSERT INTO utilisateur (id, nom, email, mot_de_passe_hash, role, actif, date_creation)
   VALUES (
       gen_random_uuid(),
       'Validateur',
       'validateur@tracabilite.ia',
       '$2a$10$...',  -- Password hash
       'VALIDATEUR',
       true,
       NOW()
   );
   ```

3. **Flyway Consideration:**
   - Flyway does not support automatic rollback
   - Must manually execute rollback SQL
   - Update `flyway_schema_history` to mark V7 as manually rolled back

## Security Considerations

### Domain Isolation Enforcement

**Principle:** Validators can only access decisions within their assigned domain.

**Enforcement Layers:**

1. **Authorization Layer:** `@PreAuthorize` annotations check `ROLE_CREDIT_VALIDATOR`, `ROLE_MEDICAL_VALIDATOR`, or `ROLE_EDUCATION_VALIDATOR`
2. **Service Layer:** `DecisionScopeService` filters queries by domain
3. **Repository Layer:** Query methods include domain filters in WHERE clauses

**No Changes Required:** Removing VALIDATEUR strengthens domain isolation by eliminating the cross-domain validator role.

### Authority Hierarchy

**Spring Security Authority Hierarchy (Unchanged):**

```
ROLE_ADMIN (Full access to all domains and operations)
  ├── ROLE_VALIDATOR (Can validate within assigned domain)
  │   ├── ROLE_CREDIT_VALIDATOR (CREDIT domain only)
  │   ├── ROLE_MEDICAL_VALIDATOR (MEDICAL domain only)
  │   └── ROLE_EDUCATION_VALIDATOR (EDUCATION domain only)
  ├── ROLE_AUDITOR (Read-only access to all domains)
  └── ROLE_USER (Can create decisions within assigned domain)
      ├── ROLE_AGENT_CREDIT (CREDIT domain only)
      ├── ROLE_AGENT_SANTE (MEDICAL domain only)
      └── ROLE_AGENT_PEDAGOGIQUE (EDUCATION domain only)
```

**Key Points:**
- ROLE_ADMIN has full access (domain isolation not applied)
- ROLE_VALIDATOR is a base authority for all domain validators
- Domain-specific authorities enforce domain isolation
- ROLE_USER is shared by UTILISATEUR and all agent roles

### Audit Trail

**No Changes Required:** Audit logging already captures role information in decision history.

**Audit Log Fields:**
- `performed_by_email`: User email
- `action`: Action performed (VALIDATE, APPROVE, REJECT, etc.)
- `previous_status`, `new_status`: Decision status transitions
- `justification`: Validation comments

**VALIDATEUR Removal Impact:**
- Historical audit logs with "VALIDATEUR" role remain unchanged
- New audit logs will show domain-specific roles (RESPONSABLE_CREDIT, etc.)
- Audit queries can still search historical VALIDATEUR actions

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

Before defining properties, we identify and eliminate redundancy:

**Redundant Criteria Identified:**
- Requirements 8.1 and 8.2 duplicate 2.2 and 2.5 (RESPONSABLE_CREDIT authority mapping)
- Requirements 8.5 duplicates 3.4 (credit@tracabilite.ia bootstrap account)
- Requirements 9.1 duplicates 1.4 (UTILISATEUR in RoleEnum)
- Requirements 9.4 duplicates 3.8 (user@tracabilite.ia bootstrap account)
- Requirements 9.5 duplicates 1.2 (UTILISATEUR documentation)
- Requirements 7.1-7.3 duplicate 2.5 testing (domain authority mapping)

**Properties to Define:**
1. Domain validator role claim mapping (covers 2.2-2.4, 2.6, 7.1-7.3, 8.1-8.2)
2. Migration and runtime constraint consistency (covers 6.3)

All other requirements are either static code checks (SMOKE), specific example tests (EXAMPLE), or infrastructure integration tests (INTEGRATION) that do not warrant property-based testing.

### Property 1: Domain Validator Authority Mapping

*For any* domain validator role (RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, RESPONSABLE_PEDAGOGIQUE), the `fromRoleClaim()` method SHALL return a collection of authorities that includes both `ROLE_VALIDATOR` and the appropriate domain-specific validator authority (`ROLE_CREDIT_VALIDATOR`, `ROLE_MEDICAL_VALIDATOR`, or `ROLE_EDUCATION_VALIDATOR`).

**Validates: Requirements 2.2, 2.3, 2.4, 2.6, 7.1, 7.2, 7.3, 8.1, 8.2**

**Rationale:** This property ensures that domain validators have both the base validator authority and their domain-specific authority, enabling them to access validation endpoints while enforcing domain isolation.

**Test Strategy:** Generate test cases with all three domain validator roles and verify the returned authority collection contains the expected authorities.

### Property 2: Migration and Runtime Constraint Consistency

*For any* role value, if it appears in the V7 migration constraint definition, it SHALL also appear in the `updateRoleCheckConstraint()` method's constraint definition, and vice versa.

**Validates: Requirement 6.3**

**Rationale:** The database migration (V7) and runtime constraint update (DataInitializer) must define identical role sets to prevent inconsistencies between fresh database initialization and migrated databases.

**Test Strategy:** Extract role lists from both sources (V7 SQL and DataInitializer Java code), normalize them to sets, and verify set equality. This test can be implemented as a compile-time or pre-deployment verification.

## Open Questions

None. All requirements are clearly specified.

## Future Considerations

1. **Complete UTILISATEUR Deprecation:** In a future release, consider removing UTILISATEUR role entirely after migrating all legacy accounts to domain-specific roles.

2. **Frontend Updates:** This design focuses on backend changes. Frontend components that display or select roles will need corresponding updates to remove VALIDATEUR from dropdowns and role selection UIs.

3. **API Documentation:** OpenAPI/Swagger documentation should be updated to reflect the new 9-role system and remove VALIDATEUR from examples.

4. **Monitoring Dashboard:** Update any admin dashboards or monitoring tools that display role statistics to reflect the 9-role system.

5. **External Integrations:** If external systems integrate via JWT and send VALIDATEUR role claims, they will need to be updated to use domain-specific roles. The current implementation gracefully handles this by mapping unknown roles to ROLE_{ROLENAME}, which will have no permissions.

## References

- **Requirements Document:** `.kiro/specs/remove-validateur-role/requirements.md`
- **V1 Migration:** `backend/src/main/resources/db/migration/V1__multidomain_decisions.sql` (original role introduction)
- **V6 Migration:** `backend/src/main/resources/db/migration/V6__bootstrap_complete_schema.sql` (current 10-role system)
- **RoleEnum:** `backend/src/main/java/com/pfa/tracabilite_ia/enumeration/RoleEnum.java`
- **RoleAuthorityMapper:** `backend/src/main/java/com/pfa/tracabilite_ia/security/RoleAuthorityMapper.java`
- **DataInitializer:** `backend/src/main/java/com/pfa/tracabilite_ia/config/DataInitializer.java`
