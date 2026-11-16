# mpaz-backend — MPAZ PRO 2.º (fase 2)

Backend de la app MPAZ PRO 2.º (DSY1105). Sigue la estructura del ejemplo del profesor: proyectos Maven
independientes (cada uno con su `mvnw`), un API Gateway, Docker Compose y MySQL en el equipo.

**Estado:** fase 2 del plan = `api-gateway` + `ms-auth` + `ms-contenidos`. Faltan `ms-actividades-evaluaciones`,
`ms-progreso` y `ms-admin` (fases 3 y 4). El contrato completo está en el documento
«Contrato de la API y modelo de datos v2».

## Estructura

| Proyecto | Puerto | Qué hace |
| --- | --- | --- |
| `api-gateway` | 9090 | Única puerta de entrada de la app. Enruta por prefijo y valida el JWT |
| `ms-auth` | 9091 | Login (usuario + PIN), emisión del JWT, `GET /auth/me` |
| `ms-contenidos` | 9092 | Asignaturas, unidades, objetivos de aprendizaje y contenidos; visibilidad por rol y estado |
| `ms-actividades-evaluaciones` | 9093 | (fase 3) |
| `ms-progreso` | 9094 | (fase 4) |
| `ms-admin` | 9095 | (fase 3) |

Rutas del gateway: `/auth/**`, `/contenidos/**`, `/evaluaciones/**`, `/progreso/**`, `/admin/**`.
Las rutas que contienen `/interno/` las bloquea el gateway (las usará ms-admin entre servicios).

## Requisitos

- Java 21 (el `mvnw` descarga Maven por el espejo configurado en `configuracion-maven/`).
- Docker Desktop.
- MySQL corriendo en tu equipo (puerto 3306, usuario `root` sin clave, como en el ejemplo del profesor).
  Los esquemas `mpaz_auth` y `mpaz_contenidos` se crean solos.
- Postman, para probar.

## Puesta en marcha (Windows)

1. Copia `.env.example` como `.env` y cambia `SECRET` por un texto de **32 caracteres o más**.
   `.env` no se sube a Git. Es el mismo secreto para el gateway y los servicios.
2. Compila: `build-all.bat`
3. Levanta todo: `docker compose up -d`
4. Importa en Postman `postman/MPAZ-fase2.postman_collection.json` y ejecútala con el Runner (en orden).
   Deberían pasar todas las pruebas.

Al primer arranque, `ms-auth` y `ms-contenidos` cargan sus datos ficticios si las tablas están vacías.

### Sin Docker (para depurar un servicio)

Define la variable de entorno `SECRET` (la misma en todos) y ejecuta, dentro de cada proyecto:
`.\mvnw spring-boot:run -s ..\configuracion-maven\settings.xml`.
Sin Docker las URL por defecto apuntan a `localhost` (MySQL y servicios).

## Usuarios de prueba (PIN `1234` para todos)

| Usuario | Rol | id |
| --- | --- | --- |
| `utp` | UTP | 1 |
| `docente` | DOCENTE | 2 |
| `ana`, `beto`, `carla`, `gael` | ESTUDIANTE (2° A) | 3 a 6 |
| `diego`, `eva`, `isidora`, `felipe` | ESTUDIANTE (2° B) | 7 a 10 |

Los ids dependen del orden de carga: no cambies el orden en `DataLoader` de `ms-auth`, porque `ms-progreso`
los usará en la fase 4.

## Datos de contenido de prueba

| OA (id) | Estado | Lo ve |
| --- | --- | --- |
| LEN-OA01 (1) | PUBLICADO | Todos |
| LEN-OA02 (2) | BORRADOR | Solo UTP |
| MAT-OA01 (3) | PUBLICADO | Todos |
| CIE-OA01 (4) | AUTORIZADO | UTP y docente |
| HIS-OA01 (5) | EN_REVISION | Solo UTP |

## Decisiones de esta fase

- **JWT:** HS256, secreto en la variable `SECRET`, vigencia de 24 horas (`mpaz.jwt.horas`). Lleva `sub` (id del usuario)
  y `rol`. El gateway y cada servicio lo validan. El PIN se guarda con BCrypt.
- **Roles:** los servicios leen el claim `rol`. Escribir contenido (POST, PUT, DELETE) es solo de UTP; las lecturas se
  filtran por estado según el rol.
- **Datos iniciales:** se cargan con un `CommandLineRunner` (`DataLoader`), no con Liquibase, para no depender del
  orden entre Liquibase y la creación de tablas de Hibernate. El esquema lo crea Hibernate (`ddl-auto=update`).
- **Versiones:** los servicios usan Spring Boot 4.0.5 (como el ejemplo). El gateway usa Spring Boot 3.3.5 y Spring
  Cloud 2023.0.3 (reactivo), porque así está el del ejemplo del profesor.
- **Docker:** `build-all.bat` solo compila; a diferencia del ejemplo, no borra contenedores ni imágenes de Docker.

## Problemas frecuentes

- `Public Key Retrieval is not allowed`: ya va el parámetro `allowPublicKeyRetrieval=true` en la URL; revisa que no
  lo hayas quitado.
- `Could not resolve placeholder 'SECRET'`: falta la variable de entorno (o el `.env` para Docker).
- `SECRET debe tener al menos 32 caracteres`: usa un secreto más largo.
- Maven no descarga dependencias: ejecuta con `-s ..\configuracion-maven\settings.xml` (usa el espejo) o corre
  `actualizar-maven.bat` para recuperar la configuración del wrapper.
- Los contenedores no llegan a MySQL: debe estar encendido en el equipo; usan `host.docker.internal:3306`.

## Pruebas unitarias

`.\mvnw test -s ..\configuracion-maven\settings.xml` dentro de `ms-auth` o `ms-contenidos`. No necesitan base de datos.
