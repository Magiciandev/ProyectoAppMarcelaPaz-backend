# mpaz-backend — MPAZ PRO 2.º (fases 2 y 3)

Backend de la app MPAZ PRO 2.º (DSY1105). Sigue la estructura del ejemplo del profesor: proyectos Maven
independientes (cada uno con su `mvnw`), un API Gateway, Docker Compose y MySQL en el equipo.

**Estado:** fase 2 (`api-gateway`, `ms-auth`, `ms-contenidos`) validada. Fase 3 (`ms-actividades-evaluaciones` y
`ms-admin`) entregada, pendiente de compilar y probar. Falta `ms-progreso` (fase 4). El contrato completo está en el
documento «Contrato de la API y modelo de datos v2».

## Estructura

| Proyecto | Puerto | Qué hace |
| --- | --- | --- |
| `api-gateway` | 9090 | Única puerta de entrada de la app. Enruta por prefijo y valida el JWT |
| `ms-auth` | 9091 | Login (usuario + PIN), emisión del JWT, `GET /auth/me` |
| `ms-contenidos` | 9092 | Asignaturas, unidades, objetivos de aprendizaje y contenidos; visibilidad por rol y estado |
| `ms-actividades-evaluaciones` | 9093 | Banco de preguntas (4 tipos), actividades, prueba final por OA y paquete de evaluación para la caché de la app |
| `ms-progreso` | 9094 | (fase 4) |
| `ms-admin` | 9095 | Ciclo de 5 estados de un OA (aprobaciones) e historial. Solo UTP |

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
4. Prueba con newman (desde la carpeta `postman`), en este orden:
   `npx newman run MPAZ-fase2.postman_collection.json` y después `npx newman run MPAZ-fase3.postman_collection.json`.
   Deberían pasar todas las pruebas. La de la fase 3 se puede repetir: crea sus propios datos cada vez.

Al primer arranque, `ms-auth`, `ms-contenidos` y `ms-actividades-evaluaciones` cargan sus datos ficticios si las
tablas están vacías. Las preguntas de prueba de `ms-actividades-evaluaciones` suponen los ids de objetivo 1, 3, 4 y 5 que
siembra `ms-contenidos`: no borres esos datos de prueba (los objetivos que crees después reciben ids nuevos).

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

## Preguntas de prueba (ms-actividades-evaluaciones)

| OA | Estado | Contenido de prueba |
| --- | --- | --- |
| 1 (LEN-OA01) | PUBLICADO | 4 preguntas (selección, completación, secuencia, asociación), 4 actividades y la prueba final |
| 3 (MAT-OA01) | PUBLICADO | 2 preguntas, 1 actividad y la prueba final |
| 4 (CIE-OA01) | AUTORIZADO | 1 pregunta, 1 actividad y la prueba final |
| 5 (HIS-OA01) | EN_REVISION | 1 pregunta y la prueba final |
| 2 (LEN-OA02) | BORRADOR | nada |

## Cómo funciona la aprobación (ms-admin)

`POST /admin/aprobaciones` con `{ "entidad": "OBJETIVO", "entidadId": 1, "accion": "AUTORIZAR" }` (solo UTP).
Acciones: `ENVIAR_REVISION`, `AUTORIZAR`, `RECHAZAR`, `PUBLICAR`, `DESHABILITAR`, `REABRIR`.
`ms-admin` valida la transición, llama **directamente** a `ms-contenidos` y a `ms-actividades-evaluaciones` por sus rutas
`/interno/…` (reenviando el token del usuario UTP) y solo si ambos responden bien deja el registro en el historial.
Si el segundo falla, intenta revertir el primero y responde 502. Para enviar a revisión el OA necesita al menos un
contenido y una prueba final con al menos una pregunta.

## Decisiones de esta fase

- **JWT:** HS256, secreto en la variable `SECRET`, vigencia de 24 horas (`mpaz.jwt.horas`). Lleva `sub` (id del usuario)
  y `rol`. El gateway y cada servicio lo validan. El PIN se guarda con BCrypt.
- **Roles:** los servicios leen el claim `rol`. Escribir contenido (POST, PUT, DELETE) es solo de UTP; las lecturas se
  filtran por estado según el rol.
- **Datos iniciales:** se cargan con un `CommandLineRunner` (`DataLoader`), no con Liquibase, para no depender del
  orden entre Liquibase y la creación de tablas de Hibernate. El esquema lo crea Hibernate (`ddl-auto=update`).
- **Versiones:** los servicios usan Spring Boot 4.0.5 (como el ejemplo). El gateway usa Spring Boot 3.3.5 y Spring
  Cloud 2023.0.3 (reactivo), porque así está el del ejemplo del profesor.
- **Tablas puente:** `actividad_pregunta` y `prueba_pregunta` llevan un `id` propio más una restricción única
  (actividad/prueba + pregunta), en lugar de la llave primaria compuesta del contrato: es más simple con JPA.
- **Solucionario:** el paquete de evaluación incluye las respuestas correctas para que la app corrija sin red
  (limitación conocida del MVP). El banco de preguntas (`GET /evaluaciones/preguntas`) es solo para UTP.
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

`.\mvnw test -s ..\configuracion-maven\settings.xml` dentro de cada servicio. No necesitan base de datos.
