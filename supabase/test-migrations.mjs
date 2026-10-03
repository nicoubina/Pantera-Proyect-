// Local PostgreSQL (PGlite) only. Does not connect to Supabase.
// PGLITE_MODULE must point to an installed @electric-sql/pglite module outside the project.
import { readFileSync } from "node:fs";
import { pathToFileURL } from "node:url";
import assert from "node:assert/strict";
const { PGlite } = await import(pathToFileURL(process.env.PGLITE_MODULE).href);
const db = new PGlite();
await db.exec("CREATE ROLE anon; CREATE ROLE authenticated;");
await db.exec(readFileSync(new URL("./migrations/001_create_panterfitness_schema.sql", import.meta.url), "utf8"));
await db.exec(`INSERT INTO usuarios(nombre,apellido,email,password,rol,estado_membresia,qr_simulado)
 VALUES ('Persistencia','Prueba','p@example.test','test-hash','CLIENTE','ACTIVA','PF-PERSISTENCIA');`);
await db.exec(readFileSync(new URL("./migrations/20261003205829_complete_panterfitness_modules.sql", import.meta.url), "utf8"));
const { rows: users } = await db.query("SELECT email FROM usuarios");
assert.deepEqual(users, [{ email: "p@example.test" }]);
const { rows: tables } = await db.query("SELECT tablename, rowsecurity FROM pg_tables WHERE schemaname='public'");
assert.equal(tables.length, 13); assert.ok(tables.every(t => t.rowsecurity));
const { rows: grants } = await db.query("SELECT table_name FROM information_schema.table_privileges WHERE table_schema='public' AND grantee IN ('anon','authenticated','PUBLIC')");
assert.equal(grants.length, 0);
await db.exec(`INSERT INTO penalizaciones(usuario_id,motivo,fecha_inicio,fecha_fin,estado) VALUES(1,'Prueba',CURRENT_DATE,CURRENT_DATE + 30,'ACTIVA');
 INSERT INTO ejercicios(nombre,descripcion) VALUES('Sentadilla','Control');
 INSERT INTO usuarios(nombre,apellido,email,password,rol,estado_membresia,qr_simulado) VALUES('Profesor','Prueba','prof@example.test','test-hash','PROFESOR','ACTIVA','PF-PROF');
 INSERT INTO rutinas(nombre,profesor_id,cliente_id,estado) VALUES('Plan',2,1,'ACTIVA');
 INSERT INTO rutina_ejercicios(rutina_id,ejercicio_id,series,repeticiones,peso_sugerido,descanso,orden) VALUES(1,1,3,12,10,60,1);
 INSERT INTO alertas(titulo,descripcion,prioridad) VALUES('Aviso','Mensaje','ALTA');`);
console.log("PASS incremental migration: preserved users, 13 tables, RLS, revoked grants, inserts and relationships");
await db.close();
