# Pantera Fitness

Aplicación para gestionar un gimnasio: usuarios, clases, reservas, lista de espera, ocupación, notificaciones y un ingreso con QR simulado.

## Antes de empezar

El programa tiene dos partes y **las dos deben estar encendidas**:

- **Backend:** procesa el login y las reservas. Se ejecuta en el puerto **8080**.
- **Frontend:** es la página que abrís en el navegador. Se ejecuta en el puerto **3000**.

Necesitás **dos terminales PowerShell**: una para cada parte. Podés abrirlas desde Visual Studio Code.

### Requisitos — instalar una sola vez

- **Java JDK 21**, con `JAVA_HOME` configurado.
- **Node.js 24**, con npm.
- El proyecto descargado en tu computadora.

No necesitás instalar Gradle ni PostgreSQL en tu computadora.

Comprobá las instalaciones en PowerShell:

```powershell
java -version
javac -version
node -v
npm -v
```

Java y javac deben mostrar la versión **21**. Si un comando no se reconoce, resolvé ese problema antes de seguir. Después de instalar, cerrá y volvé a abrir las terminales.

### ¿Qué opción elegir?

| Opción | Para qué sirve | ¿Conserva los datos al apagar el backend? |
|---|---|---|
| **A. H2** | Probar el programa sin configurar Supabase | No |
| **B. Supabase** | Trabajar con la base de datos real | Sí |

**Elegí una opción para el backend. Después iniciá el frontend con el paso 2.**

Los comandos que siguen parten de la **carpeta principal del proyecto**, donde están estas dos carpetas:

```text
Pantera-Proyect-/
├── panterafitnes-backend/
└── panterafitnes/
```

Se conservan esos nombres de carpeta para que los comandos coincidan con el repositorio.

## Paso 1 — Encender el backend

### Opción A: probar rápido con H2

En la **terminal 1**, desde la carpeta principal:

```powershell
cd panterafitnes-backend
.\gradlew.bat bootRun --args="--spring.profiles.active=h2"
```

La primera vez puede tardar porque descarga las dependencias. Necesitás conexión a Internet para esa descarga.

Esperá a ver un mensaje parecido a **Started PanterfitnessApplication** y el puerto **8080**. Dejá esta terminal abierta y pasá al **paso 2**.

**Con H2, los usuarios nuevos, las reservas y los cambios se pierden cuando apagás el backend.** Los datos demo se crean de nuevo en el siguiente inicio.

### Opción B: usar Supabase y conservar los datos

#### 1. Comprobar que las tablas están creadas

**En el proyecto Supabase confirmado para Pantera Fitness, la migración ya se ejecutó el 14/09/2026. No la ejecutes otra vez.**

Solo si vas a usar **otro proyecto vacío** de Supabase:

1. Abrí ese proyecto en Supabase.
2. Entrá a **SQL Editor**.
3. Copiá el contenido completo de [001_create_panterfitness_schema.sql](supabase/migrations/001_create_panterfitness_schema.sql).
4. Ejecutalo una sola vez.

Esto crea las tablas. Los usuarios demo se crearán cuando arranque el backend.

#### 2. Crear el archivo de configuración

En la **terminal 1**, desde la carpeta principal:

```powershell
cd panterafitnes-backend
if (!(Test-Path .env)) { Copy-Item .env.example .env }
notepad .env
```

Si ya existe `.env`, el comando lo conserva. Completá el archivo con tus datos:

```dotenv
SPRING_PROFILES_ACTIVE=supabase
SUPABASE_DB_URL=jdbc:postgresql://HOST:5432/postgres?sslmode=require
SUPABASE_DB_USERNAME=USUARIO_DE_BASE_DE_DATOS
SUPABASE_DB_PASSWORD=CONTRASEÑA_DE_BASE_DE_DATOS
JWT_SECRET=TU_SECRETO_GENERADO
PANTERFITNESS_DEMO_ENABLED=true
```

**Reemplazá los textos de ejemplo. No los dejes tal como aparecen.**

| Dato | Qué poner |
|---|---|
| `HOST` | El host que aparece en **Connect** dentro de tu proyecto Supabase |
| `SUPABASE_DB_USERNAME` | El usuario de esa misma conexión |
| `SUPABASE_DB_PASSWORD` | La contraseña de la **base de datos**, no la de tu cuenta Supabase |
| `JWT_SECRET` | El valor que generás con el comando de abajo |

Para una conexión desde una red IPv4, elegí **Session pooler** en **Connect**: usa el puerto **5432** y un usuario como `postgres.REFERENCIA_DEL_PROYECTO`. Copiá el host y el usuario exactos que muestre Supabase.

Si usás conexión directa, el usuario normalmente es `postgres`; esa conexión requiere IPv6 o el complemento IPv4. Más detalles en la [guía de conexión de Supabase](https://supabase.com/docs/guides/database/connecting-to-postgres).

La URL del archivo debe empezar con **`jdbc:postgresql://`**. No uses la URL web del proyecto ni incluyas la contraseña dentro de esa URL.

Para generar `JWT_SECRET`, ejecutá en PowerShell:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Copiá el resultado después de `JWT_SECRET=`. Generalo **una sola vez** y conservá ese valor: cambiarlo invalida las sesiones anteriores.

Guardá y cerrá el archivo. Escribí los valores **sin comillas**. El archivo se lee como propiedades de Java; si tu contraseña contiene una barra invertida, escribila como `\\`.

**No subas `.env` a GitHub ni compartas su contenido.** Está excluido por `.gitignore`.

#### 3. Arrancar el backend

En esa misma terminal, dentro de `panterafitnes-backend`:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=supabase"
```

Esperá a ver **Started PanterfitnessApplication** y el puerto **8080**. Dejá la terminal abierta.

El perfil Supabase lee el archivo `.env` al ejecutar desde esta carpeta. Si también configuraste variables de entorno en la terminal, esas variables tienen prioridad sobre el archivo.

## Paso 2 — Encender el frontend

Abrí la **terminal 2 en la carpeta principal del proyecto**. No cierres la terminal del backend.

La primera vez:

```powershell
cd panterafitnes
if (!(Test-Path .env.local)) { Copy-Item .env.example .env.local }
npm ci
npm run dev
```

El archivo `panterafitnes/.env.local` debe contener:

```dotenv
NEXT_PUBLIC_API_URL=http://localhost:8080
```

**Este valor es el mismo con H2 y con Supabase.** No agregues `/api`.

Esperá a que la terminal muestre que la página está lista y abrí:

### [Abrir Pantera Fitness: http://localhost:3000](http://localhost:3000)

Para usar el programa entrá al puerto **3000**. El **8080** corresponde a la API del backend.

## Paso 3 — Iniciar sesión

Con los datos demo habilitados, podés usar estas cuentas:

| Email | Contraseña | Tipo de usuario |
|---|---|---|
| cliente@panterfitness.com | 123456 | Cliente con membresía activa |
| vencido@panterfitness.com | 123456 | Cliente con membresía vencida |
| profesor@panterfitness.com | 123456 | Profesor |
| admin@panterfitness.com | 123456 | Administrador |

Para una primera prueba, entrá con **cliente@panterfitness.com / 123456**.

1. Abrí **Clases** y reservá un horario con cupo que empiece entre 30 minutos y una semana después.
2. Revisá la reserva en **Mis reservas**.
3. Abrí **Notificaciones** para ver la confirmación.
4. Para cancelar, elegí una clase a la que todavía le falten **más de 24 horas**.

También se crean `cupo01@panterfitness.com` hasta `cupo20@panterfitness.com` y `espera@panterfitness.com`, con contraseña `123456`, para probar una clase llena y la lista de espera.

En Supabase, las cuentas existentes conservan sus contraseñas y cambios. Reiniciar no las restablece. Los horarios demo tampoco se renuevan cada semana: si quedaron en el pasado, hay que crear horarios nuevos.

## Cómo abrirlo las próximas veces

Abrí **dos terminales nuevas**, ambas en la carpeta principal.

**Terminal 1 — backend con Supabase** (con `.env` ya configurado):

```powershell
cd panterafitnes-backend
.\gradlew.bat bootRun --args="--spring.profiles.active=supabase"
```

Para usar H2, reemplazá `supabase` por `h2` en ese comando.

**Terminal 2 — frontend:**

```powershell
cd panterafitnes
npm run dev
```

Abrí [http://localhost:3000](http://localhost:3000). No hace falta repetir la configuración ni `npm ci` cada vez; usalo si faltan las dependencias o cambia el archivo `package-lock.json`.

### Cómo detenerlo

Presioná **Ctrl+C en cada terminal**. Si Windows pregunta si querés terminar el proceso, confirmá.

Con Supabase se guardan los datos. Con H2 se pierden.

## Problemas frecuentes

| Problema | Qué revisar |
|---|---|
| `JAVA_HOME is not set` o Java no se reconoce | Instalá JDK 21. Configurá `JAVA_HOME` con la carpeta del JDK y agregá su carpeta `bin` al `Path`. Abrí una terminal nueva. |
| `npm` no se reconoce | Instalá Node.js con npm y abrí una terminal nueva. |
| PowerShell bloquea `npm.ps1` | Usá `npm.cmd ci` y `npm.cmd run dev` en lugar de `npm`. |
| No se encuentra `gradlew.bat` | Entrá a `panterafitnes-backend` antes de ejecutar el comando. |
| npm no encuentra `package.json` | Entrá a `panterafitnes` antes de ejecutar los comandos del frontend. |
| Falta `SUPABASE_DB_URL` o `JWT_SECRET` | Completá `panterafitnes-backend/.env`, verificá que no se haya guardado como `.env.txt` y arrancá desde esa carpeta. |
| Supabase rechaza la contraseña | Revisá la contraseña de la base y el usuario de **Connect**. No uses una API key. |
| No se puede conectar al host de Supabase | Revisá host, puerto y conexión a Internet. Si tu red es IPv4, usá **Session pooler**. |
| Hibernate indica que falta una tabla | Confirmá que estás conectando al proyecto donde se ejecutó la migración. |
| La página abre, pero falla el login o no carga datos | Comprobá que el backend haya terminado de arrancar en 8080 y que `NEXT_PUBLIC_API_URL` sea correcto. |
| El puerto 8080 o 3000 está ocupado | Detené la instancia anterior con Ctrl+C y volvé a iniciar. |
| No aparecen clases de esta semana | Los horarios demo pueden ser antiguos. Reiniciar Supabase no crea una semana nueva. |
| La reserva o cancelación es rechazada | Revisá membresía, cupos y los tiempos permitidos indicados en el paso 3. |

## Comprobar que Supabase guarda los datos

1. Ejecutá el programa con la opción **Supabase**.
2. Registrá una cuenta nueva desde la página y creá una reserva.
3. Detené el backend con Ctrl+C.
4. Volvé a encenderlo con Supabase y el mismo archivo `.env`.
5. Iniciá sesión con esa cuenta y comprobá que la reserva siga en **Mis reservas**.

Cambiar de H2 a Supabase **no copia los datos de H2**: son bases distintas.

## Información para desarrollo

La conexión siempre es:

**Frontend Next.js → Backend Spring Boot → Base de datos**

Spring Boot maneja login, JWT, roles y reglas de negocio. Supabase solo se usa como PostgreSQL: no se utiliza Supabase Auth, Storage, Realtime ni Edge Functions, y el frontend no necesita claves de Supabase.

### Base de datos

- Tablas: `usuarios`, `sectores_gimnasio`, `clases_gimnasio`, `horarios_clase`, `reservas`, `lista_espera`, `asistencias` y `notificaciones`.
- [Migración SQL](supabase/migrations/001_create_panterfitness_schema.sql): crea tablas, relaciones, restricciones e índices.
- [Consulta de verificación](supabase/verify_panterfitness.sql): revisa tablas, RLS y duplicados.
- Supabase usa `ddl-auto=validate`: comprueba las tablas sin crearlas ni eliminarlas.
- Todas las tablas tienen RLS habilitado, sin políticas públicas; `anon` y `authenticated` no tienen permisos sobre ellas. El backend usa una conexión JDBC privilegiada; la autorización de cada usuario queda en Spring.
- La carga demo evita duplicados y no restablece datos existentes. Ejecutar el primer arranque con una sola instancia del backend.
- `PANTERFITNESS_DEMO_ENABLED=false` desactiva la carga demo, pero no elimina las cuentas creadas anteriormente.
- El perfil predeterminado es Supabase. Los comandos de esta guía eligen el perfil explícitamente.

### Pruebas opcionales

Estos comandos son para comprobar el código; **no son necesarios para abrir el programa**. Ejecutá cada bloque desde la carpeta principal.

Backend:

```powershell
cd panterafitnes-backend
.\gradlew.bat build
```

Frontend:

```powershell
cd panterafitnes
npm run build
```

Con el backend demo encendido, para probar los servicios HTTP:

```powershell
cd panterafitnes
npm run test:integration
```

La prueba de integración necesita dos horarios disponibles y una clase llena a más de 24 horas del inicio. Crea usuarios, reservas y asistencias en la base activa. Las pruebas Java usan H2.

### Estado de la migración

La migración se aplicó y se verificaron las ocho tablas, las restricciones, los índices y RLS. La versión registrada en Supabase es `20260914232352`.

La validación de arranque JDBC, endpoints y persistencia entre reinicios quedó pendiente en la entrega de la migración. Las pruebas Java agregadas no se ejecutaron: se preparó Java 21 temporal, pero se rechazó la ejecución de Gradle. El build del frontend tampoco se inició por falta de npm. Esta guía no implica que esas pruebas hayan pasado.

El asesor de Supabase señaló permisos de ejecución en la función preexistente `public.rls_auto_enable()`; queda por revisar [ese aviso](https://supabase.com/docs/guides/database/database-linter?lint=0028_anon_security_definer_function_executable).

Para el detalle de la integración y sus endpoints, consultá [INTEGRACION.md](INTEGRACION.md), que documenta el trabajo previo con H2.
