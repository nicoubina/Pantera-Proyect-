-- Ejecutar como postgres tras aplicar la migracion. No muestra passwords.
SELECT tablename, rowsecurity
FROM pg_tables
WHERE schemaname = 'public'
  AND tablename IN ('usuarios','sectores_gimnasio','clases_gimnasio','horarios_clase',
                    'reservas','lista_espera','asistencias','notificaciones')
ORDER BY tablename;

SELECT tablename, indexname, indexdef FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename IN ('usuarios','sectores_gimnasio','clases_gimnasio','horarios_clase',
                    'reservas','lista_espera','asistencias','notificaciones')
ORDER BY tablename, indexname;

-- Debe devolver cero filas: no hay acceso anon/authenticated/PUBLIC a las tablas.
SELECT table_name, grantee, privilege_type
FROM information_schema.table_privileges
WHERE table_schema = 'public' AND grantee IN ('anon','authenticated','PUBLIC')
  AND table_name IN ('usuarios','sectores_gimnasio','clases_gimnasio','horarios_clase',
                     'reservas','lista_espera','asistencias','notificaciones');

-- Deben devolver cero filas.
SELECT usuario_id, horario_clase_id, count(*) FROM public.reservas
WHERE estado_reserva IN ('CONFIRMADA','EN_ESPERA','ASISTIDA','AUSENTE')
GROUP BY usuario_id, horario_clase_id HAVING count(*) > 1;
SELECT usuario_id, horario_clase_id, count(*) FROM public.lista_espera
WHERE activa GROUP BY usuario_id, horario_clase_id HAVING count(*) > 1;

-- Comparar antes y despues del reinicio sin operaciones concurrentes.
SELECT 'usuarios' AS tabla, count(*) AS filas FROM public.usuarios
UNION ALL SELECT 'sectores_gimnasio', count(*) FROM public.sectores_gimnasio
UNION ALL SELECT 'clases_gimnasio', count(*) FROM public.clases_gimnasio
UNION ALL SELECT 'horarios_clase', count(*) FROM public.horarios_clase
UNION ALL SELECT 'reservas', count(*) FROM public.reservas
UNION ALL SELECT 'lista_espera', count(*) FROM public.lista_espera
UNION ALL SELECT 'asistencias', count(*) FROM public.asistencias
UNION ALL SELECT 'notificaciones', count(*) FROM public.notificaciones;

-- Después de la migración incremental: cinco tablas nuevas, todas con RLS.
SELECT tablename, rowsecurity FROM pg_tables
WHERE schemaname='public' AND tablename IN
 ('penalizaciones','ejercicios','rutinas','rutina_ejercicios','alertas') ORDER BY tablename;
-- Debe devolver cero filas.
SELECT table_name, grantee, privilege_type FROM information_schema.table_privileges
WHERE table_schema='public' AND grantee IN ('anon','authenticated','PUBLIC')
AND table_name IN ('penalizaciones','ejercicios','rutinas','rutina_ejercicios','alertas');
