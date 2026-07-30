# Runbook — backup et restauration PostgreSQL

> **WARNING :** ne pas coller ni exécuter depuis ce document des commandes destructives contre Neon / production. Les exemples sont pour **local / staging auto-hébergé** après substitution contrôlée des variables hors bande.

> RPO / RTO ci-dessous = **cibles aspirantes** (non mesurées à l’audit) — étiquetés `ASPIRATIONAL`.

---

## 1. Objectifs (ASPIRATIONAL)

| Indicateur | Cible aspirante | Notes |
|------------|-----------------|-------|
| **RPO** | ≤ 24 h | Perte max de données acceptable démo RC |
| **RTO** | ≤ 4 h | Temps max de rétablissement service API |
| RPO staging | ≤ 7 j | Moins critique |
| Test restore | ≥ 1× / trimestre | **NON testé** à l’audit |

Ajuster selon criticité réelle avant tout label « production-ready ».

---

## 2. Périmètre

- Base `tracabilite_ia` (users, decisions, historiques, audit, tables domaine).
- **Hors** : artefacts ML (backup fichiers `models/` séparément), secrets env (coffre).

---

## 3. Backup (logique)

### Fréquence cible

- Quotidien full `pg_dump` (ASPIRATIONAL)
- Avant toute migration schéma (obligatoire)

### Exemple local / staging

```bash
# LOCAL OU STAGING — adapter host/user/db ; NE PAS cibler Neon depuis ce runbook
pg_dump -Fc -f "backup_$(date +%Y%m%d_%H%M).dump" "$LOCAL_DATABASE_URL"
```

- Stocker hors machine app, **chiffré**.
- Enregistrer horodatage, taille, checksum.
- Ne **pas** committer les dumps dans Git (écart E7 : dumps démo déjà versionnés à nettoyer).

---

## 4. Restauration (ordre)

1. Annoncer incident / maintenance.
2. Stopper writers (backend) pour éviter écritures divergentes.
3. Provisionner instance vide **ou** restaurer sur clone — pas d’essai hasardeux sur le primary Neon via doc.
4. Restore :

```bash
# LOCAL / STAGING UNIQUEMENT
pg_restore --clean --if-exists -d "$STAGING_DATABASE_URL" backup_YYYYMMDD_HHMM.dump
```

5. Vérifier counts tables clés + login admin.
6. Redémarrer backend avec `ddl-auto=validate`.
7. Smoke : login, liste décisions, une création domaine, `/ready` ML.
8. Clôturer avec RTO/RPO **observés** (mesurer pour remplacer l’aspirational).

---

## 5. Neon / cloud managé

- Utiliser les **snapshots / PITR natifs** du fournisseur selon leur console — procédures hors de ce dépôt.
- Toute action destructive = double validation humaine.
- Ce runbook **n’autorise pas** `DROP` / restore `--clean` remote non validé.

---

## 6. Checklist post-restore

- [ ] Schéma compatible app déployée
- [ ] Pas de secrets dans les logs de restore
- [ ] Comptes démo absents si environnement prod
- [ ] MEDICAL : accès restreint revalidé
