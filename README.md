# Guide Michelin

API Spring Boot de l'examen. L'organisation est la même que le TP `demo` : un contrôleur, un service, un repository.

## Lancer

Depuis ce dossier :

```powershell
.\mvnw.cmd spring-boot:run
```

L'API écoute sur http://localhost:8082 (le TP `demo` reste sur 8080).

- Swagger : http://localhost:8082/swagger-ui.html
- OpenAPI : http://localhost:8082/v3/api-docs.yaml
- Console H2 : http://localhost:8082/h2-console

JDBC : `jdbc:h2:./michelin`, utilisateur `root`, mot de passe `root`.

## Restaurants

`GET /restaurant` et `GET /restaurant/{id}` renvoient aussi la liste des évaluations et la propriété `moyenne`. S'il n'y a aucune évaluation, `moyenne` vaut `-1`.

```powershell
curl.exe http://localhost:8082/restaurant

curl.exe -H "Content-Type: application/json" -d "{\"nom\":\"Le Jules Verne\",\"adresse\":\"Tour Eiffel, Paris\"}" http://localhost:8082/restaurant

curl.exe http://localhost:8082/restaurant/1

curl.exe -X PUT -H "Content-Type: application/json" -d "{\"nom\":\"Le Cinq\",\"adresse\":\"31 avenue George V\"}" http://localhost:8082/restaurant/1
```

## Évaluations

Sans le profil `oidc`, le nom de l'évaluateur est dans le JSON, comme l'auteur d'un commentaire dans le TP. Avec le profil, il vient du JWT et `GET /evaluation/mine` ne renvoie que les évaluations de la personne connectée.

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=oidc
```

Keycloak du cours : `https://keycloak.bober.ovh/realms/master`

- `lucien.bramard@michelin.fr` / `lasecuriteavantout` (USER)
- `noel.flantier@michelin.fr` / `cestbeaulhiver` (ADMIN)

Le token doit contenir `preferred_username` (ou au moins `sub`), comme ceux du TP dans Bruno. Le client `admin-cli` de Keycloak renvoie un token trop court, sans nom.

```powershell
curl.exe -H "Content-Type: application/json" -d "{\"nomEvaluateur\":\"Lucien\",\"commentaire\":\"Excellent saumon\",\"note\":3}" http://localhost:8082/restaurant/1/evaluation

curl.exe -X PUT -H "Content-Type: application/json" -d "{\"commentaire\":\"Un peu juste\",\"note\":1}" http://localhost:8082/restaurant/1/evaluation/1

curl.exe -X DELETE http://localhost:8082/restaurant/1/evaluation/1

curl.exe "http://localhost:8082/evaluation/search?q=saumon"

curl.exe "http://localhost:8082/evaluation/search?q=saumon&q=citron"

curl.exe http://localhost:8082/evaluation/mine
```

La recherche reprend Lucene du TP. `q` peut être répété, comme `isbn` pour les livres. Plusieurs mots : on garde une évaluation si elle contient au moins un des mots.

## Tests

```powershell
.\mvnw.cmd test
```
