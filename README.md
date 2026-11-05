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

Pour l'instant un restaurant n'a pas d'évaluation, donc `moyenne` vaut `-1`.

```powershell
curl.exe http://localhost:8082/restaurant

curl.exe -H "Content-Type: application/json" -d "{\"nom\":\"Le Jules Verne\",\"adresse\":\"Tour Eiffel, Paris\"}" http://localhost:8082/restaurant

curl.exe http://localhost:8082/restaurant/1

curl.exe -X PUT -H "Content-Type: application/json" -d "{\"nom\":\"Le Cinq\",\"adresse\":\"31 avenue George V\"}" http://localhost:8082/restaurant/1
```

## Tests

```powershell
.\mvnw.cmd test
```
