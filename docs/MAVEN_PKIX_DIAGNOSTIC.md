# Diagnostic Maven PKIX (machine locale)

## Symptôme

`.\mvnw.cmd package` échoue avec :

`PKIX path building failed: unable to find valid certification path to requested target`

alors que `.\mvnw.cmd compile` et les tests unitaires déjà résolus en local passent.

## Cause identifiée (cette machine)

Le certificat présenté par `repo.maven.apache.org:443` est émis par :

**`CN=Avast Web/Mail Shield Root`** (interception HTTPS Avast).

Java / le truststore de développement ne fait pas confiance à cette CA d’antivirus. Ce n’est **pas** un bug du code Traçabilité IA.

## Ce qu’il ne faut pas faire

- `-Dmaven.wagon.http.ssl.insecure=true`
- trust-all / désactivation SSL
- certificats non vérifiés « pour dépanner »
- **versionner** `backend/avast-root.crt` ou `backend/settings.xml`
- **dépendre** de ce certificat dans le `Dockerfile` CI / production

## Docker / CI (chemin standard)

Le `backend/Dockerfile` versionné est **portable** : truststore officiel de l’image Maven/JRE uniquement.

- GitHub Actions et Linux standard : `docker compose build backend` **sans** `avast-root.crt`
- Le certificat Avast local reste **gitignored** et hors du dépôt
- La CI n’est **pas** concernée par l’interception Avast Windows

## Correctif sûr pour le JDK local (optionnel)

1. Exporter le certificat racine **Avast Web/Mail Shield Root** depuis Avast ou via `keytool -printcert -sslserver repo.maven.apache.org:443`.
2. L’importer dans le truststore JDK local (jamais dans Git) :

```bat
keytool -importcert -noprompt -alias avast-web-shield ^
  -file avast-root.crt ^
  -keystore "%JAVA_HOME%\lib\security\cacerts" ^
  -storepass changeit
```

3. Ou désactiver temporairement le scan HTTPS Avast pour les outils de développement (préférable côté sécurité machine).

## Build Docker local Windows avec Avast (optionnel)

Si `docker build` sur la machine Windows échoue encore à cause d’Avast **pendant** le stage Maven :

1. Conserver `backend/avast-root.crt` **uniquement en local** (déjà dans `.gitignore`).
2. Créer un fichier **local non versionné** `backend/Dockerfile.local-avast` (ajouter aussi à `.gitignore` si besoin) qui étend le Dockerfile standard et importe le certificat via un secret BuildKit, par exemple :

```dockerfile
# backend/Dockerfile.local-avast — LOCAL ONLY, ne pas committer le certificat
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
RUN --mount=type=secret,id=avast_ca \
    keytool -importcert -noprompt -trustcacerts -alias avast-ssl-scan \
      -file /run/secrets/avast_ca -cacerts -storepass changeit
# puis reprendre COPY pom.xml / mvn package comme dans Dockerfile
```

```bash
DOCKER_BUILDKIT=1 docker build \
  --secret id=avast_ca,src=backend/avast-root.crt \
  -f backend/Dockerfile.local-avast \
  -t tracabilite-backend-local \
  ./backend
```

Alternative : monter/utiliser `backend/settings.xml` local déjà ignoré, sans le versionner.

**Interdit** : faux certificat PEM dans Git, désactivation SSL, réintroduction de `avast-root.crt` dans le dépôt.

## État projet

- `compile` local : OK si dépendances déjà en cache `.m2`
- tests unitaires : OK en Docker CI avec PostgreSQL
- `package` host Windows / téléchargement Maven Central : peut échouer tant que la CA Avast n’est pas dans le truststore Java local (**LOCAL_MAVEN_BLOCKED_BY_PKIX**)
- build image Docker standard / CI : **ne doit pas** dépendre d’Avast
