# Pantera Fitness

Sistema de gestión para un gimnasio: reserva de clases, lista de espera, ocupación en vivo, notificaciones y un QR simulado de ingreso. Tiene 3 tipos de usuario: **Cliente**, **Profesor** y **Administrador**.

## Tecnología usada

| Parte | Tecnología |
|---|---|
| Frontend | Next.js 15 + React 19, CSS plano (sin Tailwind) |
| Backend | Spring Boot 3 + Java 21, Spring Security con JWT |
| Base de datos | H2 en memoria (se reinicia cada vez que se levanta el backend) |

## Estructura del proyecto

- `panterafitnes/` → Frontend (Next.js)
- `panterafitnes-backend/` → Backend (API REST con Spring Boot)

> El frontend consume la API real: **Next.js → Spring Boot → H2**. Login y registro usan JWT; clases, reservas, ocupación, notificaciones y QR se consultan o actualizan mediante HTTP. localStorage guarda únicamente el JWT y el perfil mínimo. Los antiguos archivos mock permanecen como referencia, sin usarse ni como fallback.

## Cómo ejecutar

### Frontend

Requisitos: Node.js 20.9+ y npm (Node.js 24 para la prueba de integración).

Copiar `panterafitnes/.env.example` a `panterafitnes/.env.local`:

```dotenv
NEXT_PUBLIC_API_URL=http://localhost:8080
```

La URL tiene ese valor por defecto. No agregar `/api`: los servicios incluyen ese prefijo. Reiniciar Next.js después de cambiar la variable; en producción se incorpora al ejecutar el build.

```
cd panterafitnes
npm install
npm run dev
```

Abrir [http://localhost:3000](http://localhost:3000)

### Backend

Requiere **JDK 21** y `JAVA_HOME` apuntando al JDK. En Windows usar `./gradlew.bat` en lugar de `./gradlew`.

```
cd panterafitnes-backend
./gradlew bootRun
```

API disponible en `http://localhost:8080`. Consola de la base de datos H2: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:panterfitnessdb`, usuario `sa`, sin contraseña).

## Usuarios de prueba

Estas cuentas se crean al arrancar el backend y funcionan en el login web:

| Email | Contraseña | Rol | Membresía |
|---|---|---|---|
| cliente@panterfitness.com | 123456 | Cliente | Activa |
| vencido@panterfitness.com | 123456 | Cliente | Vencida |
| profesor@panterfitness.com | 123456 | Profesor | Activa |
| admin@panterfitness.com | 123456 | Administrador | Activa |

Además se crean usuarios `cupo01@panterfitness.com` ... `cupo20@panterfitness.com` y `espera@panterfitness.com` (todos con contraseña `123456`) para simular una clase de "Funcional" llena los viernes, con una persona en lista de espera.

## Endpoints del backend

Todos los endpoints, salvo `/api/auth/**`, requieren el header:
```
Authorization: Bearer <token>
```
El token se obtiene haciendo login.

### Autenticación — `/api/auth`
| Método | Ruta | Quién | Qué hace |
|---|---|---|---|
| POST | `/api/auth/registro` | Público | Crea una cuenta nueva (rol Cliente) |
| POST | `/api/auth/login` | Público | Inicia sesión y devuelve el token JWT |

### Usuarios — `/api/usuarios`
| Método | Ruta | Quién | Qué hace |
|---|---|---|---|
| GET | `/api/usuarios/me` | Cualquier usuario logueado | Datos del usuario actual |
| GET | `/api/usuarios` | Administrador | Lista todos los usuarios |
| PATCH | `/api/usuarios/{id}/membresia` | Administrador | Cambia el estado de membresía de un usuario |

### Clases — `/api/clases`
| Método | Ruta | Quién | Qué hace |
|---|---|---|---|
| GET | `/api/clases` | Todos | Lista las clases |
| GET | `/api/clases/semana` | Todos | Horarios de la semana |
| GET | `/api/clases/{id}` | Todos | Detalle de una clase |
| POST | `/api/clases` | Administrador | Crea una clase |
| PUT | `/api/clases/{id}` | Administrador | Edita una clase |
| DELETE | `/api/clases/{id}` | Administrador | Elimina una clase |

### Horarios — `/api/horarios`
| Método | Ruta | Quién | Qué hace |
|---|---|---|---|
| GET | `/api/horarios` | Todos | Lista los horarios |
| GET | `/api/horarios/semana` | Todos | Horarios de la semana |
| POST | `/api/horarios` | Administrador | Crea un horario |
| PUT | `/api/horarios/{id}` | Administrador | Edita un horario |
| DELETE | `/api/horarios/{id}` | Administrador | Elimina un horario |

### Reservas — `/api/reservas`
| Método | Ruta | Quién | Qué hace |
|---|---|---|---|
| POST | `/api/reservas` | Cliente | Reserva una clase (o entra a lista de espera si está completa) |
| GET | `/api/reservas/mis-reservas` | Cliente | Ve sus propias reservas |
| GET | `/api/reservas` | Profesor / Administrador | Lista todas las reservas |
| DELETE | `/api/reservas/{id}/cancelar` | Cliente / Administrador | Cancela una reserva |

### Ocupación — `/api/ocupacion`
| Método | Ruta | Quién | Qué hace |
|---|---|---|---|
| GET | `/api/ocupacion/general` | Todos | Ocupación total del gimnasio |
| GET | `/api/ocupacion/sectores` | Todos | Ocupación por sector (musculación, sala de clases) |
| GET | `/api/ocupacion/clases` | Todos | Ocupación de todas las clases |
| GET | `/api/ocupacion/clases/{horarioId}` | Todos | Ocupación de una clase puntual |

### Notificaciones — `/api/notificaciones`
| Método | Ruta | Quién | Qué hace |
|---|---|---|---|
| GET | `/api/notificaciones/mis-notificaciones` | Cualquier usuario logueado | Lista sus notificaciones |
| PATCH | `/api/notificaciones/{id}/leer` | Cualquier usuario logueado | Marca una notificación como leída |

### QR — `/api/qr`
| Método | Ruta | Quién | Qué hace |
|---|---|---|---|
| POST | `/api/qr/simular-ingreso` | Cliente / Administrador | Simula el check-in con QR en una clase |

## Demo de integración

1. Levantar el backend y luego el frontend. Abrir [el login](http://localhost:3000/login). Todas las cuentas de la tabla usan `123456`.
2. Entrar como cliente activo. En **Clases**, reservar un horario futuro con cupo. La API valida membresía, duplicados, superposición y anticipación de 30 minutos a una semana.
3. En **Mis reservas**, comprobar la reserva. Usar **Cancelar → Sí, cancelar** en una clase que empiece dentro de más de 24 horas. El backend decide si permite la cancelación; un rechazo se muestra en pantalla.
4. En la clase completa de Funcional del viernes, usar **Unirme a lista de espera**. La posición mostrada es la devuelta por el backend. Para probar promoción, cancelar una reserva de esa clase con un usuario `cupo01` a `cupo20` cuando falten más de 24 horas; el backend confirma al primero de la lista y genera su notificación.
5. En **Ocupación**, comparar el total, sectores y cupos por clase. Se vuelven a consultar cada 10 segundos y después de las acciones. Reservar modifica los cupos de la clase; registrar una asistencia QR incrementa la ocupación del sector.
6. En **Perfil**, seleccionar una reserva confirmada y usar **Simular ingreso** (hora de inicio) o **Simular 11 min tarde**. El resultado **ASISTIDA/AUSENTE** y el mensaje provienen de la API. Es una credencial visual y un ingreso simulado, sin lector físico.
7. Abrir **Notificaciones**, comprobar los mensajes y **Marcar leídas**. Recargar para verificar persistencia en el backend.
8. Cerrar sesión y entrar como cliente vencido. Intentar reservar: la API lo rechaza por membresía. Profesor ve sus horarios y alumnos; administrador ve reservas generales, ocupación y usuarios, y puede cambiar el estado de membresía en Inicio.
9. Registrarse desde **Registrate como cliente**. La cuenta se crea en H2 y se inicia sesión con JWT. Recargar conserva la sesión; un JWT vencido/inválido la limpia y lleva a login.

Los horarios demo se calculan al iniciar Spring Boot. La clase llena puede quedar fuera de la ventana de cancelación según el día/hora de la prueba. H2 está en memoria: reiniciar el backend borra registros, reservas y cambios de la demo.

Si el backend no responde, la app muestra un error y permite reintentar. No inventa datos ni confirma acciones localmente. Si ya había datos cargados, se indica que pueden estar desactualizados.

## Servicios y adaptación

| Servicio | Endpoints |
|---|---|
| `authService` | POST login/registro; GET `/api/usuarios/me` |
| `classService` | GET `/api/clases`, `/api/clases/semana`, `/api/horarios`, `/api/horarios/semana` |
| `reservationService` | POST `/api/reservas`; GET mis-reservas/reservas según rol; DELETE cancelar |
| `occupancyService` | GET general, sectores, clases y clases/{horarioId} bajo `/api/ocupacion` |
| `notificationService` | GET mis-notificaciones; PATCH {id}/leer |
| `qrService` | POST `/api/qr/simular-ingreso` |
| `userService` | GET `/api/usuarios`; PATCH {id}/membresia |

`apiClient.js` centraliza la URL, JWT, JSON, errores y cierre de sesión por 401. `mappers.js` adapta los DTOs: el `classId` de una reserva corresponde al **id del horario**, no al id del catálogo de clases. Las reglas de reserva permanecen en Spring Boot.

## Verificación

```sh
cd panterafitnes-backend
./gradlew build
```

```sh
cd panterafitnes
npm run build
```

Con el backend demo encendido, Node.js 24 y una clase llena a más de 24 horas:

```sh
cd panterafitnes
npm run test:integration
```

La prueba usa los servicios reales del frontend, crea una cuenta `integracion-…@example.test` y registra acciones de demo en H2 (incluidas asistencias). No usa mocks de HTTP. Ejecutarla sobre la instancia local de prueba. Las pruebas del backend también comprueban JWT ausente, inválido y vencido, permisos por rol y CORS.

## Punto 2 pendiente

H2 y su configuración permanecen sin cambios. La migración futura a Supabase/PostgreSQL requiere configurar la conexión desde Spring Boot, preparar migraciones y persistencia de datos y revisar compatibilidad. No se implementó Supabase ni acceso directo desde el frontend a la base.
