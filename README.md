# mpaz-backend — MPAZ PRO 2.º (fases 2 a 4)

Backend de la app MPAZ PRO 2.º (DSY1105). Sigue la estructura del ejemplo del profesor: proyectos Maven
independientes (cada uno con su `mvnw`), un API Gateway, Docker Compose y MySQL en el equipo.

**Estado:** fases 2 y 3 validadas (`api-gateway`, `ms-auth`, `ms-contenidos`, `ms-actividades-evaluaciones`,
`ms-admin`). Fase 4 (`ms-progreso`) entregada, pendiente de compilar y probar. El contrato completo está en el
documento «Contrato de la API y modelo de datos v2».

## Estructura

| Proyecto | Puerto | Qué hace |
| --- | --- | --- |
| `api-gateway` | 9090 | Única puerta de entrada de la app. Enruta por prefijo y valida el JWT |
| `ms-auth` | 9091 | Login (usuario + PIN), emisión del JWT, `GET /auth/me` |
| `ms-contenidos` | 9092 | Asignaturas, unidades, objetivos de aprendizaje y contenidos; visibilidad por rol y estado |
| `ms-actividades-evaluaciones` | 9093 | Banco de preguntas (4 tipos), actividades, prueba final por OA y paquete de evaluación para la caché de la app |
| `ms-progreso` | 9094 | Asignaciones, sincronización de intentos y progreso, monitoreo, informes pedagógicos y reinicio |
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
   `npx newman run MPAZ-fase2.postman_collection.json`, después `...MPAZ-fase3...` y después `...MPAZ-fase4...`.
   Deberían pasar todas las pruebas. Las de las fases 3 y 4 se pueden repetir: preparan sus propios datos cada vez.

Al primer arranque, `ms-auth`, `ms-contenidos`, `ms-actividades-evaluaciones` y `ms-progreso` cargan sus datos
ficticios si las tablas están vacías. Las preguntas de prueba de `ms-actividades-evaluaciones` suponen los ids de objetivo 1, 3, 4 y 5 que
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

## Datos de prueba de ms-progreso

Cursos `2° A` (id 1) y `2° B` (id 2), ambos del docente (id 2). Estudiantes: Ana, Beto, Carla y Gael (ids 3 a 6, 2° A);
Diego, Eva, Isidora y Felipe (ids 7 a 10, 2° B). Asignaciones (ventana: de hace 7 días a 60 días más, 3 intentos):
objetivos 1 y 3 al 2° A, objetivo 1 al 2° B. Ana tiene el objetivo 1 completo y un intento de 75 %; Beto lleva el
objetivo 1 a la mitad y no se conecta hace 10 días (sirve para probar el mensaje de inactividad). Los ids de usuario,
objetivos y pruebas se suponen igual que en los otros servicios: no borres sus datos de prueba.

## Cómo funciona la sincronización (ms-progreso)

`POST /progreso/sync` (solo estudiante) recibe lo hecho sin conexión. Cada intento y cada avance se evalúan por
separado y reciben su resultado, de modo que reenviar un lote nunca duplica nada (el `uuid` los hace idempotentes):

- `ACEPTADO`, `DUPLICADO` (ya estaba) o `RECHAZADO` con un motivo.
- Motivos de rechazo, en este orden: `SIN_ASIGNACION`, `FUERA_DE_FECHA`, `ANTERIOR_AL_REINICIO`, `LIMITE_INTENTOS`;
  y `DATOS_INVALIDOS` cuando el elemento viene incompleto o con números imposibles.
- Los intentos se ordenan por la fecha del dispositivo: valen los primeros N (el límite de la asignación) y para
  informes y monitoreo cuenta el **mejor** porcentaje entre los aceptados.
- La fecha del dispositivo se compara con la ventana de la asignación y de la prueba en la zona horaria
  `America/Santiago` (propiedad `mpaz.zona-horaria`).
- Si el docente reinicia el avance, lo hecho en el dispositivo antes de esa fecha deja de contar. El reinicio archiva
  los intentos (no los borra) y la respuesta de la sincronización trae `ultimoReinicio` para que la app limpie su copia.
- Cada sincronización actualiza la última conexión y la última sincronización del estudiante.

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
- **Informe sin JSON:** el desglose del informe se guarda en tablas (`informe_objetivo`, `informe_respuesta`) y no en
  una columna `desglose_json`, para no depender de la serialización JSON del servidor.
- **Avance por asignatura:** los servicios devuelven identificadores de objetivo; agrupar por asignatura lo hace la app
  con su caché de contenidos.
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
