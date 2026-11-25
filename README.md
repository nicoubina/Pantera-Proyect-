# Pantera Fitness

Sistema web para la gestión de un gimnasio: clases y horarios, reservas con cupo y lista de espera, membresías, control de acceso con QR (el cliente muestra su QR y recepción o el profesor lo escanean con la cámara), penalizaciones por inasistencias, rutinas de entrenamiento, alertas, notificaciones internas y ocupación del gimnasio en tiempo real. Tiene tres tipos de usuario: **cliente**, **profesor** y **administrador**.

## Cómo probarlo (sin instalar nada)

| Opción | Cómo se abre | Base de datos |
|---|---|---|
| **A. Web publicada** | Abrir **https://pantera-fitness.onrender.com**. La primera carga puede tardar hasta 1 minuto porque el plan gratuito "duerme" el servidor cuando no se usa. | PostgreSQL en Supabase (datos persistentes) |
| **B. Ejecutable para Windows** | Descomprimir `PanteraFitness-Windows.zip` y hacer doble clic en **Iniciar Pantera Fitness.bat**. Incluye su propio Java: no requiere instalaciones. Se abre solo en `http://localhost:8090`. | H2 en memoria, con datos demo frescos en cada inicio |

### Usuarios de prueba

Todos usan la contraseña **123456**.

| Email | Rol | Qué permite probar |
|---|---|---|
| `cliente@panterfitness.com` | Cliente | Reservas, lista de espera, asistencias, rutina asignada, QR, notificaciones |
| `vencido@panterfitness.com` | Cliente | Membresía vencida: la reserva se rechaza con un mensaje claro |
| `sancionado@panterfitness.com` | Cliente | Penalización activa: solo puede reservar en los 30 minutos previos |
| `espera@panterfitness.com` | Cliente | Primero en la lista de espera de una clase llena |
| `profesor@panterfitness.com` | Profesor | Clases asignadas, alumnos, registro de asistencia, rutinas |
| `profesora@panterfitness.com` | Profesora | Segunda profesora (Yoga y Spinning) |
| `admin@panterfitness.com` | Administrador | Gestión completa: usuarios, clases, horarios, membresías, penalizaciones, alertas, métricas |

Los datos iniciales (seeds) se generan con las fechas del día: la agenda semanal de clases se renueva sola, así que siempre hay clases para reservar.

### Ingreso con QR

1. El cliente reserva una clase y abre **Mi QR** en el celular: es un código QR real con su código personal.
2. En la entrada, recepción (administrador) o el profesor abren **Escanear QR**, activan la cámara y apuntan al QR. También se puede escribir el código o usar un lector USB.
3. El sistema busca la reserva confirmada de ese socio para la clase de ese horario y registra la asistencia con la **hora del servidor**: desde 30 minutos antes del inicio; más de 10 minutos tarde queda como ausente. La pantalla del cliente se actualiza sola.

La cámara del navegador necesita HTTPS (la web publicada lo tiene) o `localhost`.

## Documentación

| Documento | Contenido |
|---|---|
| `docs/Arquitectura_Pantera_Fitness.pdf` | Documentación de arquitectura y diseño (TP3 corregido): contexto, alcance, restricciones, C4, DER y diccionario de datos, UML, contrato de API y decisiones (ADR) |
| `docs/Manual_Tecnico_Testing_y_Seguridad.pdf` | Estrategia de pruebas, matriz de casos y medidas de seguridad |
| `docs/Manual_de_Usuario_Pantera_Fitness.pdf` | Todas las pantallas por tipo de usuario, con capturas anotadas |
| `docs/historial/` | Registros de revisiones anteriores del proyecto |

## Estructura del repositorio

```text
Pantera-Proyect-/
├── panterafitnes/            Frontend: Next.js 15 + React 19 (se compila como sitio estático)
├── panterafitnes-backend/    Backend: Java 21 + Spring Boot 3 (API REST, seguridad JWT, JPA)
├── supabase/migrations/      Migraciones SQL de PostgreSQL (esquema de 13 tablas)
├── distribucion/windows/     Lanzador y LEEME del ejecutable para Windows
├── scripts/                  Script que arma el ejecutable para Windows
├── Dockerfile, render.yaml   Despliegue web en un único servicio (Render, plan gratuito)
└── docs/                     Documentación de entrega
```

En producción el frontend compilado se sirve desde el mismo servidor Spring Boot que expone la API (`/api/**`): una sola URL, sin configuración de CORS y un solo proceso que desplegar.

## Ejecutar en modo desarrollo

Requisitos: **Java JDK 21** y **Node.js 22 o superior**. No hace falta instalar Gradle ni PostgreSQL.

Se usan **dos terminales** desde la carpeta principal del proyecto.

**Terminal 1 — backend (puerto 8080)**

```powershell
cd panterafitnes-backend
# Opción rápida, base en memoria con datos demo:
.\gradlew.bat bootRun --args="--spring.profiles.active=h2"
# Opción Supabase (requiere el archivo .env, ver abajo):
.\gradlew.bat bootRun --args="--spring.profiles.active=supabase"
```

**Terminal 2 — frontend (puerto 3000)**

```powershell
cd panterafitnes
npm ci
npm run dev
```

Abrir [http://localhost:3000](http://localhost:3000). En modo desarrollo el frontend llama a la API en `http://localhost:8080`; para otra dirección, definir `NEXT_PUBLIC_API_URL` en `panterafitnes/.env.local`.

### Configuración de Supabase (`panterafitnes-backend/.env`)

```powershell
cd panterafitnes-backend
if (!(Test-Path .env)) { Copy-Item .env.example .env }
notepad .env
```

| Variable | Valor |
|---|---|
| `SUPABASE_DB_URL` | `jdbc:postgresql://HOST:5432/postgres?sslmode=require` con el host del **Session pooler** (Supabase → Connect) |
| `SUPABASE_DB_USERNAME` | Usuario de esa conexión (por ejemplo `postgres.REFERENCIA`) |
| `SUPABASE_DB_PASSWORD` | Contraseña de la base de datos |
| `JWT_SECRET` | Secreto aleatorio de al menos 32 bytes en Base64 |
| `PANTERFITNESS_DEMO_ENABLED` | `true` para cargar los datos de demostración |

El archivo `.env` está excluido por `.gitignore`: **nunca se sube al repositorio**. Las migraciones de `supabase/migrations/` ya están aplicadas en el proyecto Supabase del equipo; solo se ejecutan, en orden, sobre un proyecto vacío.

## Pruebas

| Nivel | Herramienta | Comando | Qué cubre |
|---|---|---|---|
| Unitarias | JUnit 5 + Mockito | `cd panterafitnes-backend` y `.\gradlew.bat test` | Reglas de reserva, membresía, JWT y registro, aisladas de la base |
| Integración | Spring Boot Test + MockMvc + H2 | (mismo comando) | Endpoints, códigos HTTP, permisos por rol, consultas JPA |
| Integración frontend–API | Node | `cd panterafitnes` y `npm run test:integration` (backend H2 encendido) | Servicios HTTP del frontend contra la API real |
| End-to-end | Playwright | `cd panterafitnes`, `npx playwright install chromium` (una vez) y `npm run test:e2e` | Flujos completos en el navegador: login, roles, registro y reserva, membresía vencida |

El detalle y la matriz de casos están en el Manual Técnico.

## Despliegue web (Render, plan gratuito)

1. Crear una cuenta en [render.com](https://render.com) iniciando sesión con GitHub.
2. **New → Blueprint** y elegir este repositorio: Render lee `render.yaml` y crea el servicio `pantera-fitness` con el `Dockerfile`.
3. Completar las variables que pide (`SUPABASE_DB_URL`, `SUPABASE_DB_USERNAME`, `SUPABASE_DB_PASSWORD`) con los mismos valores del `.env`. `JWT_SECRET` se genera solo.
4. Esperar el primer despliegue (5 a 10 minutos) y abrir la URL `https://….onrender.com`.

## Generar el ejecutable para Windows

```bash
bash scripts/build-ejecutable-windows.sh
```

Compila el frontend y el backend, descarga Java 21 portable (Eclipse Temurin, con verificación SHA-256) y arma `dist/PanteraFitness-Windows.zip`.

## Problemas frecuentes

| Problema | Qué revisar |
|---|---|
| `JAVA_HOME is not set` o Java no se reconoce | Instalar JDK 21, configurar `JAVA_HOME` y abrir una terminal nueva. |
| PowerShell bloquea `npm.ps1` | Usar `npm.cmd ci` y `npm.cmd run dev`. |
| Falta `SUPABASE_DB_URL` o `JWT_SECRET` | Completar `panterafitnes-backend/.env` (no `.env.txt`) y arrancar desde esa carpeta. |
| No se puede conectar al host de Supabase | Usar el **Session pooler** (IPv4) y revisar host, puerto y usuario. |
| La página abre pero el login falla | Verificar que el backend terminó de arrancar en el puerto 8080. |
| El puerto 8080, 8090 o 3000 está ocupado | Cerrar la instancia anterior (Ctrl+C o cerrar la ventana) y volver a iniciar. |
| La web publicada tarda en responder | Es el arranque en frío del plan gratuito: esperar un minuto y recargar. |
