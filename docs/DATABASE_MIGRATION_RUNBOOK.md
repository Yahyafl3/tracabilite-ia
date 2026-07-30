# Runbook — migrations base de données

> **Flyway :** **planifié** (absent à l’audit). En attendant : scripts SQL manuels versionnés sous `backend/scripts/migration/`.

> **WARNING :** ce document ne doit **jamais** être exécuté tel quel contre une instance Neon / production. Aucune commande destructive n’est fournie pour le cloud. Adapter hors bande après revue et backup validé.

> Modèles ML = `DEMO_SYNTHETIC` — hors scope migration schéma applicatif.

---

## 1. Principes

1. **Backup d’abord** (voir `BACKUP_RESTORE_RUNBOOK.md`).
2. Migrer sur une copie / staging avant prod.
3. Valider schéma + smoke tests.
4. Préparer le rollback **avant** d’appliquer.
5. Ne jamais lancer `DROP DATABASE`, `TRUNCATE` massif, ni `ddl-auto=create` sur Neon depuis ce runbook.

---

## 2. Artefacts existants (implémentés)

| Fichier | Rôle |
|---------|------|
| `backend/scripts/migration/V1__multidomain_decisions.sql` | Ajout domaine + tables CREDIT/MEDICAL/EDUCATION |
| `backend/scripts/migration/V1__multidomain_decisions_rollback.sql` | Rollback V1 (perte des tables domaine possibles) |
| `backend/scripts/reset-local-decisions.sql` | **Local uniquement** — destructif |

Voir aussi `MIGRATION_GUIDE.md`.

---

## 3. Procédure standard (ordre)

### Étape A — Backup

- Dump logique (`pg_dump`) hors bande, stocké chiffré.
- Noter le hash / horodatage du dump.
- Vérifier que le dump est **restaurable** sur un environnement jetable.

### Étape B — Migrer (staging d’abord)

1. Stopper ou mettre en maintenance les writers si fenêtre courte.
2. Appliquer le script versionné **dans l’ordre** (V1, puis V2…).
3. Avec Flyway (**cible**) : laisser l’app démarrer avec `flyway.enabled=true` et `ddl-auto=validate`.
4. Sans Flyway (**actuel**) : exécuter le SQL via client admin **sur staging**, jamais en copiant-collant des commandes Neon dans ce doc.

Exemple **local / staging auto-hébergé** (adaptatif) :

```bash
# LOCAL / STAGING UNIQUEMENT — pas Neon
psql "$STAGING_DATABASE_URL" -f backend/scripts/migration/V1__multidomain_decisions.sql
```

### Étape C — Valider

- [ ] Colonnes / tables attendues présentes
- [ ] Décisions legacy ont `domaine = 'CREDIT'` (V1)
- [ ] Backend démarre avec `ddl-auto=validate`
- [ ] Login + création décision domaine + validation + export smoke
- [ ] ML `/ready` OK

### Étape D — Rollback (si échec)

1. Stopper le nouveau backend.
2. Redéployer l’image backend précédente.
3. Exécuter le script `*_rollback.sql` **uniquement** si les nouvelles tables peuvent être perdues et après validation métier.
4. Restaurer le dump si le rollback SQL est insuffisant.

---

## 4. Flyway (cible)

| Élément | Cible |
|---------|--------|
| Emplacement | `classpath:db/migration` |
| Naming | `V{n}__description.sql` |
| Prod | `validate` Hibernate + Flyway migrate au boot ou job séparé |
| Baseline | Sur DB déjà peuplée : `baselineOnMigrate` après inventaire |

**État actuel :** scripts manuels style Flyway, **pas** d’intégration Spring Flyway.

---

## 5. Interdits

- Exécuter `reset-local-decisions.sql` hors local.
- Lancer des commandes DDL destructives sur Neon depuis la documentation.
- Mélanger `ddl-auto=update` et migrations manuelles en prod.
- Migrer sans backup vérifié.

---

## 6. Fenêtre et communication

- Annoncer la fenêtre aux validateurs.
- Consigner : qui / quand / script / checksum dump / résultat.
- En cas d’échec : incident selon `INCIDENT_RESPONSE.md`.
