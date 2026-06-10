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

> **Importante:** hoy el frontend **no está conectado** al backend. El frontend funciona solo, con datos simulados guardados en `localStorage` del navegador (login, clases, reservas, ocupación, etc). El backend es una API funcional aparte, con su propia base de datos, pensada para conectarse más adelante.

## Cómo ejecutar

### Frontend

```
cd panterafitnes
npm install
npm run dev
```

Abrir [http://localhost:3000](http://localhost:3000)

### Backend

```
cd panterafitnes-backend
./gradlew bootRun
```

API disponible en `http://localhost:8080`. Consola de la base de datos H2: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:panterfitnessdb`, usuario `sa`, sin contraseña).

## Usuarios hardcodeados

### Frontend (mock, login en la app web)

| Email | Contraseña | Rol | Membresía |
|---|---|---|---|
| cliente@pantera.com | 123456 | Cliente | Activa |
| vencido@pantera.com | 123456 | Cliente | Vencida |
| profesor@pantera.com | 123456 | Profesor | - |
| admin@pantera.com | 123456 | Administrador | - |

### Backend (se crean solos al arrancar la API)

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
