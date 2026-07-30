# Diagnostic Maven PKIX (machine locale)

## Symptôme

`.\mvnw.cmd package` échoue avec :

`PKIX path building failed: unable to find valid certification path to requested target`

alors que `.\mvnw.cmd compile` et les tests unitaires déjà résolus en local passent.

## Cause identifiée (cette machine)

Le certificat présenté par `repo.maven.apache.org:443` est émis par :

**`CN=Avast Web/Mail Shield Root`** (interception HTTPS Avast).

Java 17 (`Microsoft OpenJDK 17.0.16`) ne fait pas confiance à cette CA d’antivirus dans son `cacerts`. Ce n’est **pas** un bug du code Traçabilité IA.

## Ce qu’il ne faut pas faire

- `-Dmaven.wagon.http.ssl.insecure=true`
- trust-all / désactivation SSL
- certificats non vérifiés « pour dépanner »

## Correctif sûr (optionnel)

1. Exporter le certificat racine **Avast Web/Mail Shield Root** depuis Avast ou via `keytool -printcert -sslserver repo.maven.apache.org:443`.
2. L’importer dans le truststore JDK :

```bat
keytool -importcert -noprompt -alias avast-web-shield ^
  -file avast-root.cer ^
  -keystore "%JAVA_HOME%\lib\security\cacerts" ^
  -storepass changeit
```

3. Ou désactiver temporairement le scan HTTPS Avast pour les outils de développement (préférable côté sécurité machine).

## État projet

- `compile` : OK
- tests unitaires multidomain/export : OK (dépendances déjà en cache `.m2`)
- `package` / téléchargement de plugins Maven : peut échouer tant que la CA Avast n’est pas dans le truststore Java
