# Pantera Fitness — revisión integral y entrega

Revisión realizada el 3 de octubre de 2026. Los cambios están directamente en el proyecto. Se mantuvieron Java 21, Spring Boot, Gradle, JPA, Security/JWT y Next.js/React. Se revisaron las entidades, repositorios, servicios, controllers, DTOs, enums, configuración, perfiles, migración inicial, frontend, rutas, servicios HTTP y pruebas antes de modificar archivos.

## 1. Qué faltaba en backend

| Módulo | Situación encontrada | Resultado |
|---|---|---|
| Asistencias | Entidad y repository existentes; registro dentro del QR | API de historial, detalle, filtros, registro manual y cierre automático de ausencias |
| Lista de espera | Entidad, repository y promoción dentro de ReservaService | API de consulta por usuario autenticado y horario; promoción existente ampliada para validar membresía y sanciones |
| Membresías | Estado y fechas dentro de Usuario; edición administrativa existente | Consulta propia/administrativa, validación de fechas y estado efectivo según vigencia |
| Penalizaciones | Sin módulo | Persistencia, consultas, gestión administrativa y reglas automáticas |
| Rutinas | Sin módulo | Ejercicios, rutinas asignadas, detalle y edición por profesor creador |
| Alertas | Sin módulo | Gestión administrativa y avisos activos destacados para usuarios |
| Notificaciones | Módulo funcional | Pantalla propia, tipo/fecha, lectura individual y restricción de lectura a su dueño |
| QR | Implementación funcional | Ruta propia y registro compartido con el módulo de asistencia |
| Usuarios | Listado y cambio de membresía | Creación, detalle, edición, rol y activación/desactivación |
| Horarios | CRUD en backend sin pantalla de gestión | Formulario administrativo y protección de horarios con reservas |
| Dashboard | Estadísticas calculadas parcialmente en frontend | Endpoint agregado con métricas reales |

Se corrigió el uso de JWT por usuarios desactivados, la falta de validación del vencimiento real de membresía y la consulta de clases ajenas por profesores. Se agregaron bloqueos transaccionales en cupos, registros de asistencia, cancelaciones y ocupación. Los Controllers delegan reglas en Services y devuelven DTOs.

## 2. Entidades nuevas

`Penalizacion`, `Ejercicio`, `Rutina`, `RutinaEjercicio`, `Alerta`.

Enums nuevos: `EstadoPenalizacion`, `EstadoRutina`, `PrioridadAlerta`. `MetodoRegistro` ahora contempla `QR_SIMULADO`, `MANUAL` y `AUTOMATICO`.

`Asistencia` conserva el modelo existente y agrega `penalizacionProcesada`; `horaIngreso` puede ser nula para pendientes, cancelaciones y ausencias sin ingreso. No se crearon entidades adicionales de Membresía o Profesor: se reutiliza Usuario.

## 3. Controllers nuevos

`AsistenciaController`, `ListaEsperaController`, `MembresiaController`, `PenalizacionController`, `RutinaController`, `EjercicioController`, `AlertaController`, `EstadisticasController`.

Se amplió `UsuarioController`. Se conservaron los controllers de autenticación, clases, horarios, reservas, notificaciones, ocupación y QR.

## 4. Services y repositories agregados

Services: `AccesoService`, `AsistenciaService`, `AsistenciaCierreService`, `ListaEsperaService`, `MembresiaService`, `PenalizacionService`, `RutinaService`, `AlertaService`, `EstadisticasService`.

`AsistenciaScheduler` cierra cada minuto las reservas confirmadas cuya clase terminó sin ingreso. `ModulesDataInitializer` complementa los seeds existentes cuando la carga de prueba está habilitada.

Repositories nuevos: `PenalizacionRepository`, `RutinaRepository`, `EjercicioRepository`, `AlertaRepository`. Los ejercicios de rutina se guardan por la relación JPA con cascada y eliminación de elementos huérfanos; no se necesita un repository separado para esa relación. Se ampliaron los repositories existentes de asistencia, usuarios, espera, horarios, reservas y sectores.

Reglas implementadas:

- Membresía ACTIVA y vigente para reservar. Si su fecha ya venció, la consulta muestra VENCIDA; si empieza en el futuro, PENDIENTE. Administración modifica estado y fechas desde Usuarios o Membresías.
- Cada tercer registro AUSENTE genera una sanción. El primer bloqueo dura un mes y los siguientes tres meses. Cada ciclo de conteo empieza en la primera falta y se reinicia al llegar a tres meses. Los períodos se expresan con fin inclusivo: inicio más uno/tres meses menos un día.
- Un cliente penalizado puede reservar con cupo durante los 30 minutos previos al inicio. No puede reservar anticipadamente ni entrar a espera en esa ventana si está llena. Se mantiene el máximo de una semana de anticipación y la cancelación con al menos 24 horas de margen.
- La cancelación reutiliza la promoción de ReservaService. Se salta a quienes no estén habilitados por membresía/estado/sanción y se recalculan posiciones.
- Las reservas confirmadas generan asistencia PENDIENTE; QR o registro manual actualizan ese registro. La cancelación queda en el historial. QR continúa permitiendo simulación horaria, limitada a una ventana cercana a la clase.
- Los ingresos ASISTIDA actualizan la ocupación del sector con la misma regla, tanto por QR como manualmente. La capacidad total se obtiene de los sectores en la base.
- El profesor crea/edita solo sus rutinas y asigna clientes relacionados con sus clases o rutinas existentes. Los clientes consultan solo rutinas propias; administración consulta todas.
- Una alerta activada genera notificaciones internas a usuarios activos. No se integra mensajería externa.

## 5. Endpoints nuevos y ampliados

Todos requieren autenticación, excepto login y registro públicos existentes.

| Método y endpoint | Permisos / función |
|---|---|
| GET `/api/asistencias?usuarioId=&horarioId=&estado=` | Cliente: propias; profesor: sus clases; admin: todas. Filtros opcionales |
| GET `/api/asistencias/{id}` | Mismos controles de propiedad/clase |
| POST `/api/asistencias` | Profesor relacionado/admin; `reservaId`, `estadoAsistencia` ASISTIDA/AUSENTE, desde inicio de clase |
| GET `/api/lista-espera?horarioId=` | Cliente: propia; profesor: sus clases; admin: todas |
| GET `/api/membresias/mi-membresia` | Usuario autenticado |
| GET `/api/membresias/{usuarioId}` | Propia o administración |
| GET `/api/penalizaciones` | Cliente: propias; admin: todas |
| GET `/api/penalizaciones/{id}` | Propia o administración |
| POST `/api/penalizaciones` | Administración: usuario, motivo, inicio y fin |
| PATCH `/api/penalizaciones/{id}/cancelar` | Administración |
| GET `/api/rutinas` | Según rol y propiedad |
| GET `/api/rutinas/{id}` | Según rol y propiedad |
| GET `/api/rutinas/clientes` | Profesor: clientes relacionados; admin: clientes activos |
| POST `/api/rutinas` | Profesor; crea y asigna con ejercicios |
| PUT `/api/rutinas/{id}` | Profesor creador; edita, cambia asignación y agrega/quita/reordena ejercicios |
| GET `/api/ejercicios` | Usuarios autenticados |
| POST `/api/ejercicios` | Profesor/admin |
| GET `/api/alertas` | Usuarios: activas; admin: todas |
| POST `/api/alertas` | Administración |
| PUT `/api/alertas/{id}` | Administración; incluye activar/desactivar |
| GET `/api/estadisticas` | Administración; métricas y rankings agregados |
| GET `/api/usuarios/{id}` | Administración |
| POST `/api/usuarios` | Administración |
| PUT `/api/usuarios/{id}` | Administración; rol, datos, activo y contraseña opcional |

Se reutilizan `PATCH /api/usuarios/{id}/membresia`, CRUD de `/api/clases` y `/api/horarios`, reservas, cancelación, notificaciones y `POST /api/qr/simular-ingreso`. No hay implementaciones paralelas de QR, espera ni notificaciones.

No se permite al administrador desactivar su propia cuenta o quitarse su rol. Un usuario con clases/rutinas relacionadas conserva su rol para evitar romper las relaciones. Los horarios con reservas conservan sus fechas y clase; no se desactivan ni se reduce su cupo por debajo de la ocupación confirmada. La eliminación de clases/horarios sigue la estrategia de desactivación existente.

## 6. Migración y compatibilidad

Migración nueva: [20261003205829_complete_panterfitness_modules.sql](supabase/migrations/20261003205829_complete_panterfitness_modules.sql), generada con el CLI de Supabase. Crea cinco tablas y modifica Asistencia de forma incremental. La migración `001_create_panterfitness_schema.sql` se conservó sin cambios.

H2 genera las tablas desde JPA con `create-drop`. Supabase mantiene `ddl-auto=validate`. Antes de iniciar la versión nueva contra Supabase, ejecutar **solo la migración incremental** en el proyecto donde ya se aplicó 001. No contiene borrados ni restablece datos.

Las tablas nuevas tienen RLS y permisos revocados a PUBLIC/anon/authenticated, como las existentes: el acceso sigue siendo JDBC mediante Spring y JWT. Este patrón conserva el aislamiento de la Data API; véase la [documentación oficial de RLS de Supabase](https://supabase.com/docs/guides/database/postgres/row-level-security).

Se verificó la migración con PostgreSQL embebido PGlite: se aplicó 001 en una base temporal vacía, se insertó un usuario, se aplicó la incremental y se comprobó que ese usuario permaneciera. También se comprobaron las 13 tablas, RLS, ausencia de grants públicos e inserciones con relaciones en las tablas nuevas. El script reproducible es [supabase/test-migrations.mjs](supabase/test-migrations.mjs), que recibe `PGLITE_MODULE` con la ruta a una instalación temporal del módulo; no conecta a Supabase.

**La migración no se aplicó al proyecto remoto y el arranque JDBC del perfil Supabase no se verificó contra la base real.** No se usaron ni publicaron credenciales. Esta validación local no sustituye comprobar el arranque real después de aplicar la migración.

## 7. Cambios de frontend

Pantallas reales de asistencias, lista de espera, membresía, penalizaciones, rutinas, notificaciones, QR, usuarios, profesores, horarios y alertas. El dashboard administrativo consulta una sola API agregada de estadísticas. Gestión de clases conectada a su CRUD; se conserva la vista de ocupación.

Los formularios permiten crear, editar, asignar, cancelar o desactivar según el módulo. Incluyen validaciones, mensajes de éxito, loading, estados vacíos, errores y reintento. Todas las rutas están en la navegación por rol; en móvil la navegación es horizontal desplazable y hay cierre de sesión visible. Se mantuvieron negro/gris, naranja y los componentes existentes.

Se eliminó el acceso rápido con cuentas/contraseñas prellenadas en la pantalla de login. Las cuentas de prueba están documentadas aquí. Los archivos mock históricos no son importados por las pantallas ni servicios activos.

Los formularios nuevos son paneles dentro de la pantalla, con Guardar/Cancelar/Cerrar, en lugar de modales. Los detalles de rutinas, usuarios y penalizaciones se despliegan en sus tarjetas.

## 8. Rutas nuevas

21 rutas nuevas: siete de cliente (`asistencias`, `lista-espera`, `membresia`, `penalizaciones`, `rutinas`, `notificaciones`, `qr`); cuatro de profesor (`asistencias`, `lista-espera`, `rutinas`, `notificaciones`); diez de administración (`usuarios`, `profesores`, `horarios`, `asistencias`, `lista-espera`, `membresias`, `penalizaciones`, `rutinas`, `notificaciones`, `alertas`). Se actualizaron `/admin` y `/admin/clases`.

## 9. Alcance y límites

Los módulos funcionales solicitados tienen backend y pantallas navegables. No se implementaron pagos, WhatsApp, correo real, molinetes, IA ni app nativa, como se pidió.

La verificación remota de Supabase queda pendiente de aplicar la migración y arrancar con la configuración privada del entorno. El QR es simulado y la ocupación sigue siendo el contador persistido del sistema de ingreso existente, sin sensores físicos ni registro de salida. Las penalizaciones vencidas se muestran FINALIZADA según su fecha aunque la fila conserve su estado original ACTIVA; ese cálculo también se utiliza para permitir reservas. Los seeds de Supabase no regeneran horarios futuros cada semana ni restablecen registros editados: crear nuevos horarios desde Administración cuando los anteriores hayan pasado.

## 10. Pruebas de backend

`gradlew.bat test`: **19 pruebas, cero fallas**. Cuatro de JWT/autenticación/CORS, dos del initializer original y trece de los módulos completos. Se verificaron fechas de membresía, registro único de QR, cierre automático de ausencia, primera/segunda sanción, reinicio trimestral, reserva inmediata con sanción, promoción y posiciones, salto de clientes vencidos, propiedad de rutinas, alertas, permisos y JWT de usuario inactivo.

`gradlew.bat bootRun --args="--spring.profiles.active=h2"`: arranque correcto en 8080 con Java 21. La integración HTTP de los servicios reales del frontend pasó para login de los cuatro perfiles de prueba, reservas/cancelación/espera, QR, notificaciones y CRUD de usuarios, membresías, clases, horarios, ejercicios, rutinas, penalizaciones, alertas y dashboard.

## 11. Build y navegación de frontend

`npm run build`: correcto; Next.js generó 42 páginas de build. Hay **39 rutas de aplicación** enumeradas abajo; el conteo de build incluye páginas internas del framework.

`npm run test:integration`: correcto contra H2 local. Se probaron logins CLIENTE/PROFESOR/ADMINISTRADOR por interfaz, navegación de las 36 rutas privadas, edición y guardado de una rutina por profesor y creación de alerta por admin. Se inspeccionó el layout móvil y el de escritorio a 1440 × 900. Capturas de verificación: [rutina de profesor](docs/capturas/rutina-profesor.jpg) y [alertas de administración](docs/capturas/alertas-admin.jpg).

## 12. Lista final de rutas

### Públicas (3)

`/` — redirección según sesión; `/login`; `/registro`.

### Cliente (12)

| Ruta | Pantalla |
|---|---|
| `/cliente` | Inicio |
| `/cliente/ocupacion` | Mapa y ocupación |
| `/cliente/clases` | Clases semanales y reserva |
| `/cliente/reservas` | Reservas y cancelación |
| `/cliente/asistencias` | Historial de asistencias |
| `/cliente/lista-espera` | Lista de espera propia |
| `/cliente/membresia` | Estado y fechas |
| `/cliente/penalizaciones` | Sanciones propias |
| `/cliente/rutinas` | Rutinas asignadas y detalle |
| `/cliente/notificaciones` | Notificaciones propias |
| `/cliente/qr` | Identificador y simulación de ingreso |
| `/cliente/perfil` | Perfil, QR y notificaciones |

### Profesor (9)

| Ruta | Pantalla |
|---|---|
| `/profesor` | Inicio |
| `/profesor/ocupacion` | Ocupación |
| `/profesor/clases` | Clases asignadas y alumnos |
| `/profesor/reservas` | Reservas de sus clases |
| `/profesor/asistencias` | Asistencia de sus alumnos y registro |
| `/profesor/lista-espera` | Espera de sus clases |
| `/profesor/rutinas` | Crear, editar y asignar rutinas |
| `/profesor/notificaciones` | Notificaciones propias |
| `/profesor/perfil` | Perfil |

### Administrador (15)

| Ruta | Pantalla |
|---|---|
| `/admin` | Métricas y rankings |
| `/admin/usuarios` | CRUD, roles y estado |
| `/admin/profesores` | Profesores y clases asignadas |
| `/admin/clases` | Gestión y ocupación por clase |
| `/admin/horarios` | Gestión de horarios |
| `/admin/reservas` | Reservas generales y cancelación |
| `/admin/asistencias` | Historial, filtros y registro |
| `/admin/lista-espera` | Listas por horario |
| `/admin/membresias` | Estado y fechas de clientes |
| `/admin/penalizaciones` | Listado, detalle, registro y cancelación |
| `/admin/rutinas` | Consulta general |
| `/admin/notificaciones` | Notificaciones propias |
| `/admin/alertas` | Crear, editar, activar/desactivar |
| `/admin/ocupacion` | Ocupación general y por clase |
| `/admin/perfil` | Perfil |

## 13. Usuarios de prueba y datos iniciales

Con `PANTERFITNESS_DEMO_ENABLED=true` se agregan datos persistidos. H2 lo habilita por defecto y recrea su base al arrancar. En Supabase la carga depende de la variable; no borra ni reescribe datos existentes. En producción puede deshabilitarse con `false`.

| Usuario | Contraseña de desarrollo | Rol / escenario |
|---|---|---|
| `cliente@panterfitness.com` | `123456` | Cliente activo con reserva, espera, asistencias, ausencia, cancelación, rutina, sanción histórica y notificaciones |
| `vencido@panterfitness.com` | `123456` | Cliente con membresía vencida |
| `sancionado@panterfitness.com` | `123456` | Cliente activo con bloqueo por penalización |
| `profesor@panterfitness.com` | `123456` | Profesor de Funcional y Musculación, con rutina y alumnos relacionados |
| `admin@panterfitness.com` | `123456` | Administrador |
| `espera@panterfitness.com` | `123456` | Primer usuario de espera en la clase llena |
| `cupo01@panterfitness.com` a `cupo20@panterfitness.com` | `123456` | Clientes que completan el cupo de la clase llena |

Las cuentas existentes en Supabase conservan sus contraseñas y cambios; la tabla describe cuentas creadas por los seeds, sin prometer restablecer una contraseña modificada. Los datos de prueba pueden cambiar después de ejecutar la integración HTTP. Para capturas limpias, iniciar H2 nuevamente.

## 14. Capturas recomendadas para el Manual de Usuario

| Pantalla / roles | Capturas de pantalla y acciones |
|---|---|
| Login / Registro | Formulario vacío, credenciales incorrectas, validación y login exitoso por rol |
| Inicio cliente | Membresía, próxima reserva, espera y alerta activa |
| Ocupación (todos) | Mapa de sectores, capacidad/porcentaje y sección de ocupación por clase desplegada |
| Clases cliente | Día desplegado, cupos disponibles, clase completa, Reservar y Unirme a lista de espera con mensaje de éxito |
| Reservas cliente | Pestañas Confirmadas / En espera / Historial; confirmación Sí, cancelar / No, mantener y estado CANCELADA |
| Asistencias cliente | Historial con ASISTIDA, AUSENTE, CANCELADA y PENDIENTE; filtro de estado |
| Lista de espera (todos) | Horario seleccionado, posición, estado activo y resultado tras promoción |
| Membresía cliente | Estado y fechas; repetir con cuenta vencida |
| Penalizaciones cliente | Sanción vigente con motivo/inicio/fin usando sancionado; detalle e histórica con cliente activo |
| Rutinas cliente/admin | Tarjeta y Ver detalle desplegado con ejercicios, series, repeticiones, kg y descanso |
| QR cliente | Identificador, reserva seleccionada, Simular ingreso, resultado ASISTIDA; con otra reserva, Simular 11 min tarde y AUSENTE |
| Notificaciones (todos) | Tipo, fecha y NO_LEIDA; acción individual de lectura y estado LEIDA; campana desplegada |
| Perfil (todos) | Datos y rol; fechas de membresía/QR en cliente |
| Inicio profesor | Horarios asignados, alumnos y asistencia registrada |
| Clases / Reservas profesor | Ver alumnos desplegado por clase, confirmados y espera |
| Asistencias profesor/admin | Filtros y botones Presente/Ausente; registrar en una clase iniciada. El cierre automático marca AUSENTE después del fin sin ingreso |
| Rutinas profesor | Crear rutina, seleccionar cliente, agregar/quitar/subir ejercicios, crear ejercicio, guardar, editar/asignar y mensaje de éxito |
| Dashboard admin | Métricas, clases populares, horarios utilizados y profesores asignados |
| Usuarios admin | Búsqueda, Ver detalle, Crear usuario, Editar cuenta/Rol, activar/desactivar y gestión de membresía |
| Profesores admin | Datos del profesor, clases asignadas desplegadas y formulario Crear profesor |
| Clases admin | Crear/Editar, selección de profesor y sector, cupo, confirmación de desactivación y ocupación |
| Horarios admin | Crear/Editar, clase, fecha, horas y cupo; confirmación de desactivación cuando no tiene reservas |
| Reservas admin | Listado general, confirmación de cancelación y promoción resultante visible en espera |
| Membresías admin | Cliente y formulario de estado/inicio/vencimiento; mensaje de éxito |
| Penalizaciones admin | Listado y detalle, Registrar penalización, confirmación de cancelación y estado CANCELADA |
| Alertas admin | Crear, Editar, prioridad, activar/desactivar y mensaje de éxito; comprobar aviso en el cliente/profesor |
| Navegación | Menú de cada rol en escritorio y barra desplazable móvil; cierre de sesión |

Para demostrar una reserva inmediata de un penalizado, crear un horario con inicio dentro de los próximos 30 minutos y cupo libre desde Administración, luego entrar con sancionado. Para registrar asistencia manual, usar un horario iniciado; no se permite marcar alumnos de clases futuras. Para demostrar promoción, cancelar una reserva confirmada de una clase llena con más de 24 horas de margen y revisar el primer usuario de espera.
