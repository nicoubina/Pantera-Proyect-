# Integración frontend/backend — punto 1

Implementado el 9 de septiembre de 2026. Arquitectura: **Next.js → Spring Boot → H2**.

## Resultado

- Login y registro reales con JWT; perfil validado por la API al recargar.
- Sesión en localStorage con JWT y datos mínimos, cierre y navegación por rol.
- Clases, reservas, lista de espera, ocupación y notificaciones reales.
- QR simulado con resultado de asistencia y mensaje del backend.
- Profesor: horarios asignados y reservas de sus alumnos.
- Administrador: reservas generales, clases, ocupación, usuarios y estado de membresía.
- Sin fallback mock. Las fallas de conexión se muestran explícitamente.
- H2, entidades, repositorios, reglas de reservas y CORS existentes conservados.

## Archivos creados

En `panterafitnes/`:

- `.env.example`: ejemplo de URL del backend.
- `src/data/constants.js`: roles y estados de membresía sin importar usuarios mock.
- `src/services/apiClient.js`: HTTP, JSON, JWT, timeout y errores.
- `src/services/mappers.js`: adaptación de DTOs a las vistas existentes.
- `src/services/qrService.js`: ingreso simulado.
- `src/services/userService.js`: usuarios y membresías.
- `scripts/test-integration.mjs`: prueba con los servicios reales contra la API local.

En `panterafitnes-backend/`:

- `src/test/java/com/sistema/panterafitness/security/JwtAuthenticationIntegrationTest.java`: pruebas de JWT, permisos y CORS.

En la raíz: este informe, `INTEGRACION.md`.

## Archivos modificados

En `panterafitnes/`:

- `package.json`: comando de prueba de integración.
- `src/services/authService.js`, `classService.js`, `reservationService.js`, `occupancyService.js` y `notificationService.js`: reemplazo de almacenamiento/mock por HTTP.
- `src/context/AuthContext.jsx` y `AppDataContext.jsx`: carga asíncrona, sesión, refresco y acciones.
- `src/components/auth/HomeRedirect.jsx`, `LoginForm.jsx` y `RegisterForm.jsx`: espera de respuestas reales y cuentas demo correctas.
- `src/components/clases/WeeklyClasses.jsx` y `AdminClasses.jsx`: datos reales, mensajes y acciones pendientes.
- `src/components/reservas/MyReservations.jsx`, `ProfessorReservations.jsx` y `AdminReservations.jsx`: estados, horarios y posición de espera del backend.
- `src/components/qr/QrSimulator.jsx`: QR de la cuenta, simulación horaria y confirmación solo al responder la API.
- `src/components/ocupacion/OccupancyDashboard.jsx`: ocupación real y descripción de actualización.
- `src/components/common/DashboardHome.jsx` y `ProfileView.jsx`: métricas, usuarios, membresías y fechas reales.
- `src/components/layout/AppLayout.jsx`: carga, error persistente y reintento.
- `src/components/layout/navigation.js`: constantes de rol independientes de mocks.
- `src/app/admin/layout.js`, `src/app/admin/page.js`, `src/app/cliente/layout.js`, `src/app/cliente/page.js`, `src/app/profesor/layout.js` y `src/app/profesor/page.js`: importación de constantes.

En `panterafitnes-backend/src/main/java/com/sistema/panterafitness/`:

- `security/JwtService.java`: captura el error de decodificación Base64 para usar la clave de desarrollo en texto plano. Sin este ajuste el login devolvía 500.
- `security/JwtAuthenticationFilter.java`: responde 401 ante JWT inválido/vencido.
- `config/SecurityConfig.java`: responde 401 ante solicitudes sin autenticación. Configuración CORS sin cambios.

En la raíz: `README.md`, actualizado con instalación, configuración, usuarios, demo y endpoints.

## Servicios y endpoints

| Servicio | Método y ruta |
|---|---|
| authService | POST /api/auth/login; POST /api/auth/registro; GET /api/usuarios/me |
| classService | GET /api/clases; GET /api/clases/semana; GET /api/horarios; GET /api/horarios/semana |
| reservationService | POST /api/reservas; GET /api/reservas/mis-reservas (cliente); GET /api/reservas (profesor/admin); DELETE /api/reservas/{id}/cancelar |
| occupancyService | GET /api/ocupacion/general; GET /api/ocupacion/sectores; GET /api/ocupacion/clases; GET /api/ocupacion/clases/{horarioId} |
| notificationService | GET /api/notificaciones/mis-notificaciones; PATCH /api/notificaciones/{id}/leer |
| qrService | POST /api/qr/simular-ingreso |
| userService | GET /api/usuarios; PATCH /api/usuarios/{id}/membresia |

La grilla utiliza clases/semana y la ocupación por horario. classService consulta también la ocupación individual si un horario no aparece en la respuesta de ocupación general por clases. Las funciones para horarios/semana y horarios están disponibles y se verifican en la prueba de integración.

## Verificación realizada

| Verificación | Resultado |
|---|---|
| Gradle build | Correcto, 4 pruebas ejecutadas, 0 fallas |
| npm run build | Correcto, 21 páginas generadas |
| npm run test:integration | Correcto contra Spring Boot/H2 |
| git diff --check | Correcto |
| Login web cliente, vencido, profesor y administrador | Correcto, redirección según rol |
| Registro web | Cuenta creada y sesión iniciada |
| Reserva y lista de espera web | Confirmación y posición 2 devueltas por el backend |
| Cancelación y recarga web | Reserva cancelada persistida y sesión conservada |
| Membresía vencida web | Reserva rechazada con mensaje del backend |
| QR web | ASISTIDA, notificación y aumento de ocupación |
| QR tardío y repetido mediante servicios | AUSENTE y rechazo de una reserva ya registrada |
| Notificaciones web | Marcadas leídas; persistencia comprobada también por servicios |
| Profesor y administrador web | Alumnos, reservas generales, usuarios y datos reales visibles |
| Cambio de membresía web | Correcto; cuenta vencida demo restaurada a VENCIDA |
| Backend apagado | Login muestra error de conexión |

La prueba de integración además valida login incorrecto, duplicados, el cierre por 401, la conservación de sesión ante 403, cupos antes/después de reservar y cancelar, y el perfil autenticado.

Se ejecutaron los comandos de arranque de desarrollo. Las pruebas completas finales usaron el JAR generado por Gradle y Next.js en modo dev, tras restablecer los procesos de la sesión.

## Ejecución y demo

Ver `README.md` para los comandos completos. Configurar `NEXT_PUBLIC_API_URL=http://localhost:8080` en `panterafitnes/.env.local`; arrancar Spring Boot con JDK 21 y `./gradlew bootRun` (Windows: `./gradlew.bat bootRun`), y Next.js con `npm install` y `npm run dev`.

Login: `cliente@panterfitness.com`, `vencido@panterfitness.com`, `profesor@panterfitness.com` o `admin@panterfitness.com`, contraseña `123456`.

Demo: Clases → Reservar o Unirme a lista de espera → Mis reservas → Cancelar (más de 24 h antes) → Perfil → QR → Notificaciones. Consultar Ocupación para ver los cambios.

## Límites y punto 2

- H2 continúa en memoria y se reinicia con el backend. Las pruebas crearon cuentas y acciones de demo.
- No se implementó Supabase. Para el punto 2 quedan conexión de Spring Boot a PostgreSQL, migraciones y persistencia de datos.
- El QR sigue siendo simulado; no hay lector físico ni integración externa.
- npm install informó 4 vulnerabilidades de las dependencias existentes (3 altas y 1 crítica). No se aplicaron actualizaciones ajenas a esta integración.
