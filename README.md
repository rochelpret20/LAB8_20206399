# Laboratorio 8 · Blog Telecom

Proyecto base completado con Spring Boot 2.7.18, Java 17, Thymeleaf, Spring Security, JPA y MySQL. Se mantiene el paquete `sw2.lab5` para conservar compatibilidad con el laboratorio original.

## Ejecutar con Docker

Requisitos: Docker Engine o Docker Desktop en funcionamiento y Docker Compose v2. Se necesita conexión a Internet en la primera construcción.

```sh
cp .env.example .env
docker compose up --build -d
docker compose logs -f app
```

Abrir http://localhost:8080. Esperar el mensaje `Started Lab5Application`. MySQL se inicia con el esquema y los datos del laboratorio; la aplicación espera a que MySQL responda al healthcheck. La primera construcción ejecuta las pruebas antes de generar la imagen final. No hace falta instalar Java ni MySQL en el equipo anfitrión.

Cuentas locales de demostración, todas con contraseña `Lab8Demo2026!`:

| Rol | Correo | Autor de ejemplo |
| --- | --- | --- |
| admin | fermin.shanahan@example.com | Marlon McLaughlin |
| user | abauch@example.com | Jaylan Howell |
| user | edison71@example.com | Xander Waters |

Se han asignado contraseñas BCrypt conocidas a esas tres cuentas; el resto conserva los hashes originales. Las credenciales y las contraseñas por defecto de `.env.example` son para el laboratorio local. Cambiarlas antes de un despliegue compartido. El puerto se publica solo en la interfaz local.

```sh
docker compose ps
docker compose logs --tail=100 app db
docker compose down
```

`down` conserva la base en el volumen `mysql_data`. Los scripts de `docker/init` solo se ejecutan cuando el volumen se crea vacío. Para reinicializar deliberadamente los datos, `docker compose down -v` **elimina toda la base**; luego repetir `up --build -d`.

Si 8080 está ocupado, cambiar `APP_PORT` en `.env`. La aplicación seguirá usando 8080 dentro del contenedor. `COOKIE_SECURE=true` se reserva para servir por HTTPS con un proxy; en HTTP local debe ser `false`.

## Permisos elegidos

El ZIP define dos roles y reserva el borrado de posts al administrador. Se completó esa política con comprobación de autoría:

| Acción | Sin sesión | user | admin |
| --- | --- | --- | --- |
| Iniciar sesión | Sí | Sí | Sí |
| Listar publicaciones | No | Publicados y borradores propios | Todos |
| Crear post | No | Sí, como autor autenticado | Sí, como autor autenticado |
| Editar post | No | Solo propios | Cualquiera |
| Eliminar post | No | No | Cualquiera |
| Ver y editar perfil | No | Solo propio | Solo propio |
| Listar todas las cuentas | No | No | Sí |
| Enviar solicitud | No | A otras cuentas activas | A otras cuentas activas |
| Aceptar o rechazar | No | Solo si es destinatario | Solo si es destinatario |

No se incorporó administración de altas, bajas o roles de usuarios: no forma parte de las funciones solicitadas. Las antiguas rutas incompletas de administración se retiraron para no permitir cambios de contraseña o rol mediante asignación de entidades completas.

## Uso y comprobación manual

1. Ingresar como Jaylan y abrir Posts. Se ven los posts publicados y su borrador propio. Crear un post, guardar como borrador, editarlo y luego publicarlo.
2. Intentar abrir `/post/edit?id=14` como Jaylan: es un post de Xander y devuelve 403. El control se aplica también al POST de guardado.
3. Ingresar como administrador para editar el post 14 o eliminar una publicación. El autor original se mantiene al editar. Eliminar requiere formulario POST con token CSRF.
4. Como Jaylan, ir a Amistades y enviar una solicitud a Xander. No se permite repetirla ni enviarla a uno mismo.
5. Cerrar sesión e ingresar como Xander. Aceptar o rechazar la solicitud recibida. Una aceptada figura como amistad para ambos; una rechazada se elimina y permite volver a solicitar.
6. Abrir Mi perfil y cambiar el nombre. El correo, contraseña, identificador, estado y rol no son editables por ese formulario.
7. Cerrar sesión y volver a `/post`: se solicita autenticación.

## Compilar y probar fuera de Docker

Con JDK 17 y Maven 3.9:

```sh
mvn clean verify
```

Las pruebas usan H2 en modo MySQL y MockMvc; no requieren un servidor de base de datos ni Docker. No sustituyen una prueba con MySQL real. El contenedor compila y ejecuta con Java 17. El Maven Wrapper original también se conserva (`sh mvnw test`).

Para ejecutar fuera del contenedor, inicializar una base MySQL vacía con `docker/init/01-schema.sql`, crear un usuario de aplicación con permisos sobre `Lab5`, y definir `DB_URL`, `DB_USER` y `DB_PASSWORD`. Después ejecutar `mvn spring-boot:run`. El proyecto valida el esquema; no crea ni modifica automáticamente las tablas de negocio.

## Implementación

- `Config/WebSecurityConfig`: rutas restringidas, BCrypt, CSRF activo, cierre de sesión POST e invalidación de sesión. Las rutas desconocidas se deniegan por defecto.
- `Service/CurrentUser`: obtiene la cuenta activa desde el principal autenticado; el rol no se confía a un campo manipulable del formulario ni a un atributo de sesión independiente.
- `Service/PostService`: lista según rol, valida propiedad al editar y exige administración al borrar.
- `Dto`: formularios con validación y campos permitidos explícitos. El autor de un post nuevo procede de la cuenta autenticada.
- `Service/FriendService`: impide autorreferencias, inactivos y duplicados recíprocos; bloqueo de participantes en orden fijo al enviar, clave única por pareja y bloqueo de solicitud al responder.
- `friend_request`: `PENDING` para pendientes y `ACCEPTED` para amistades. Rechazar elimina la fila. Solo el destinatario responde, incluso cuando el solicitante o un tercero es administrador.
- Sesiones JDBC con vencimiento a los 30 minutos de inactividad. Cookies HttpOnly y SameSite Lax; renovación de identificador al iniciar sesión y eliminación de cookies al salir.
- Thymeleaf escapa el contenido de posts y añade el token CSRF a los formularios con `th:action`.
- `Dockerfile`: construcción por etapas y usuario sin privilegios. Compose conserva MySQL en un volumen y no publica su puerto.

## Alcance de verificación

Consultar `VERIFICACION.md` para los resultados reales de esta entrega. En el entorno de preparación no fue posible iniciar Docker porque la instalación local carecía del ejecutable de Docker Desktop y el daemon estaba detenido. Por tanto, no se afirma haber probado el arranque de contenedores o la persistencia contra MySQL real.

## Referencias técnicas

- Spring Security, CSRF y formularios: https://docs.spring.io/spring-security/site/docs/5.7.11/reference/html5/#servlet-exploits-csrf
- Docker Compose, orden de arranque y healthchecks: https://docs.docker.com/compose/how-tos/startup-order/
- El script `Lab5_script.sql` de la raíz se conserva como referencia original. Para ejecutar la versión completada se utiliza `docker/init/01-schema.sql`.
