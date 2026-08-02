# Sécurité et confidentialité

- JWT inchangé ; secrets uniquement via variables d’environnement.
- Aucune clé API dans le code.
- Données personnelles des datasets : **fictives / synthétiques**.
- Module médical : pas de diagnostic ; export détaillé médical à restreindre.
- SHA-256 : intégrité de snapshot, pas un verrou anti-modification.
- AuditLog pour création, analyse, soumission, validation, désaccord, export.
