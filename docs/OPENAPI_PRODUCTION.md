# OpenAPI / Swagger en production

> **État actuel (bloquant) :** Swagger / OpenAPI **publics** — écart B5.
> **Cible prod :** documentation interactive **désactivée**.

> Contrats API multidomain : `API_MULTIDOMAINE.md` (source humaine de vérité tant que OpenAPI n’est pas versionné séparément).

---

## 1. Règle production

| Environnement | Swagger UI / `/v3/api-docs` |
|---------------|----------------------------|
| `local` | Autorisé (aide au dev) |
| `test` / CI | Désactivé ou non exposé |
| `staging` | Désactivé par défaut ; activation ponctuelle derrière auth admin si besoin |
| `prod` | **OFF** |

Propriétés cibles SpringDoc :

```properties
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

---

## 2. Pourquoi désactiver

- Réduit la surface de reconnaissance des endpoints.
- Évite l’exposition de schémas (y compris champs MEDICAL).
- Les attaquants n’ont pas besoin d’une UI pour scanner — mais ne pas faciliter.

---

## 3. Contrats API (maintenus hors Swagger prod)

Documenter et versionner :

- Auth : login, refresh éventuel, reset password
- Décisions legacy + multidomain (`/api/decisions/...`)
- Validation file d’attente
- Users admin
- Health (sans détails)

Référence : `docs/API_MULTIDOMAINE.md`, tests contrôleurs Spring, specs Angular.

**Versionnement URL `/api/v1` :** **planifié** (écart M3) — aujourd’hui préfixe `/api` non versionné.

---

## 4. Publication contrôlée (si nécessaire)

Si un partenaire a besoin du contrat :

1. Exporter `openapi.json` depuis un build **local/staging**.
2. Distribuer hors bande (portail privé), pas via l’URL prod.
3. Retirer champs internes / exemples MEDICAL détaillés si possible.

---

## 5. Checklist déploiement

- [ ] Swagger UI inaccessible anonymement en prod
- [ ] `/v3/api-docs` inaccessible ou authentifié
- [ ] Endpoints de test AI publics revus (`/api/ai/ping`, etc.) — écart E5
- [ ] Contrat multidomain à jour dans `API_MULTIDOMAINE.md`
