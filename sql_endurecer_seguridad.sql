-- ═══════════════════════════════════════════════════════════════════
-- MIGRACIÓN: Endurecimiento de seguridad (RLS + Storage + Verificación)
-- Ejecutar en: Supabase Dashboard → SQL Editor → New query → Run
-- Es idempotente: se puede ejecutar varias veces sin error.
--
-- IMPORTANTE: desplegar primero el app.html nuevo (usa verify_document
-- con fallback a la consulta directa). Después ejecutar este SQL.
--
-- Qué hace:
--   1) Crea RPC público verify_document(uuid) que expone SOLO los
--      campos necesarios para validar un QR (sin enumerar la tabla).
--   2) Elimina las políticas de lectura pública de dc3_records y
--      forklift_licenses (nadie puede descargar la base completa).
--   3) Limpia cualquier política de storage que cubra a anon y crea
--      políticas solo para usuarios autenticados (bloquea el listado
--      del bucket templates, que expone emails como carpetas).
--   4) Defensa en profundidad: elimina las "Acceso anon" de los SQL
--      antiguos si alguien las volvió a crear.
-- ═══════════════════════════════════════════════════════════════════

-- ── 1) RPC de verificación pública ────────────────────────────────
-- El QR guarda el id del registro. Con la función, anon solo puede
-- preguntar "¿existe este id?" y recibir los campos de la pantalla de
-- verificación — no puede hacer select * ni enumerar filas.
create or replace function public.verify_document(p_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
  r record;
begin
  select 'DC3_RECORD' as type, agent_name, worker_name, worker_id,
         company_name, worker_pos, course_name, duration_hours,
         null::text[] as equipment, start_date, end_date
    into r
    from dc3_records
   where id = p_id;

  if not found then
    select 'FORKLIFT_LICENSE' as type, agent_name, worker_name, worker_id,
           company_name, worker_pos, course_name,
           null::text as duration_hours, equipment, start_date, end_date
      into r
      from forklift_licenses
     where id = p_id;
  end if;

  if not found then
    return null;  -- id inexistente: la UI muestra "no encontrado"
  end if;

  return jsonb_build_object(
    'type',          r.type,
    'agentName',     r.agent_name,
    'workerName',    r.worker_name,
    'workerId',      r.worker_id,
    'companyName',   r.company_name,
    'workerPos',     r.worker_pos,
    'courseName',    r.course_name,
    'durationHours', r.duration_hours,
    'equipment',     r.equipment,
    'startDate',     r.start_date,
    'endDate',       r.end_date
  );
end $$;

grant execute on function public.verify_document(uuid) to anon, authenticated;

-- ── 2) Quitar lectura pública de las tablas de documentos ─────────
-- La verificación ahora pasa por verify_document (y app.html tiene
-- fallback mientras se migra).
drop policy if exists "rls_anon_read_verify" on dc3_records;
drop policy if exists "rls_anon_read_verify" on forklift_licenses;

-- ── 3) Defensa en profundidad contra los SQL antiguos ─────────────
-- sql_crear_tabla_licencias.sql y sql_crear_tabla_creditos.sql
-- creaban políticas "Acceso anon" FOR ALL (escritura pública total).
-- Si por algún motivo existen, eliminarlas.
drop policy if exists "Acceso anon" on forklift_licenses;
drop policy if exists "Acceso anon" on agent_credits;
drop policy if exists "Acceso anon" on credit_transactions;

-- ── 4) Storage: bloquear a anon, permitir solo autenticados ───────
-- Los buckets worker-photos y templates son públicos (las URLs
-- públicas de plantillas/fotos siguen funcionando: un bucket público
-- sirve el objeto por URL sin pasar por RLS). Lo que se bloquea es el
-- LISTADO por API, que exponía los emails como nombres de carpeta.
do $$
declare pol record;
begin
  for pol in
    select policyname
      from pg_policies
     where schemaname = 'storage'
       and tablename  = 'objects'
       and roles && '{anon,public}'
  loop
    execute format('drop policy %I on storage.objects', pol.policyname);
  end loop;
end $$;

drop policy if exists "storage_authenticated_select" on storage.objects;
create policy "storage_authenticated_select" on storage.objects
  for select to authenticated
  using (bucket_id in ('worker-photos', 'templates'));

drop policy if exists "storage_authenticated_insert" on storage.objects;
create policy "storage_authenticated_insert" on storage.objects
  for insert to authenticated
  with check (bucket_id in ('worker-photos', 'templates'));

drop policy if exists "storage_authenticated_update" on storage.objects;
create policy "storage_authenticated_update" on storage.objects
  for update to authenticated
  using (bucket_id in ('worker-photos', 'templates'))
  with check (bucket_id in ('worker-photos', 'templates'));

drop policy if exists "storage_authenticated_delete" on storage.objects;
create policy "storage_authenticated_delete" on storage.objects
  for delete to authenticated
  using (bucket_id in ('worker-photos', 'templates'));

-- ═══════════════════════════════════════════════════════════════════
-- Verificación rápida (opcional, correr aparte):
--   select public.verify_document('00000000-0000-0000-0000-000000000000');
--   -- debe regresar null
-- Y con la clave anon ya NO debe poder listar:
--   GET /rest/v1/dc3_records?select=*  → 401 (bloqueado)
--   POST /rest/v1/rpc/verify_document  → 200 (funciona)
-- ═══════════════════════════════════════════════════════════════════
