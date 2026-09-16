-- ═══════════════════════════════════════════════════════════════════
-- MIGRACIÓN: Auto-registro de agentes con aprobación del administrador
-- Pegar en: Supabase Dashboard → SQL Editor → New query → Run
-- Es idempotente: se puede ejecutar varias veces sin error.
--
-- IMPORTANTE: ejecutar ANTES de difundir el enlace de registro. El
-- botón "Crear cuenta" de app.html solo aparece cuando esta tabla
-- existe; el registro queda deshabilitado hasta ejecutar este SQL.
--
-- Qué hace:
--   1) Crea la tabla app_users (perfiles de acceso: rol, aprobado,
--      teléfono y registro STPS del agente).
--   2) Siembra los perfiles actuales (admin y Cynthia) ya aprobados.
--   3) RLS de app_users: cada usuario autenticado puede crear SU
--      propia fila "en revisión" (approved=false, role=USER); solo
--      el admin aprobado puede actualizar/eliminar/borrar filas.
--   4) Endurece el resto de tablas: el acceso de datos pasa de
--      "cualquier autenticado" a "solo autenticados APROBADOS".
--      Una cuenta en revisión no ve ni escribe NADA.
--   5) El rol anon sigue SIN acceso de escritura ni listado
--      (regla de seguridad del proyecto). El único cambio es que
--      ahora también puede llamar a auth.signUp, que es la vía
--      pública estándar de Supabase Auth y no toca tablas.
-- ═══════════════════════════════════════════════════════════════════

-- ── 1) Tabla de perfiles de acceso ────────────────────────────────
create table if not exists app_users (
  id uuid primary key default gen_random_uuid(),
  created_at timestamptz not null default now(),
  email text unique not null,
  name text not null default '',
  phone text,
  stps_registration text,
  role text not null default 'USER' check (role in ('USER','ADMIN')),
  approved boolean not null default false
);

alter table app_users enable row level security;

grant select, insert, update, delete on app_users to authenticated;

-- ── 2) Perfiles actuales (idempotente) ────────────────────────────
insert into app_users (email, name, role, approved) values
  ('d4r005@gmail.com', 'Dario Robles', 'ADMIN', true),
  ('lugga.advisors@gmail.com', 'Cynthia Garza Lugo', 'USER', true)
on conflict (email) do nothing;

-- ── 3) Funciones auxiliares (security definer = sin recursión RLS) ─
create or replace function is_approved_user()
returns boolean
language sql
security definer
set search_path = public, pg_temp
as $$
  select exists (
    select 1 from app_users
    where lower(email) = lower(coalesce(auth.jwt()->>'email',''))
      and approved
  );
$$;

create or replace function is_admin_user()
returns boolean
language sql
security definer
set search_path = public, pg_temp
as $$
  select exists (
    select 1 from app_users
    where lower(email) = lower(coalesce(auth.jwt()->>'email',''))
      and approved and role = 'ADMIN'
  );
$$;

revoke execute on function is_approved_user() from public, anon;
revoke execute on function is_admin_user() from public, anon;
grant execute on function is_approved_user() to authenticated;
grant execute on function is_admin_user() to authenticated;

-- ── 4) Políticas de app_users ─────────────────────────────────────
-- Ver la propia fila (o todas si eres admin aprobado)
drop policy if exists "app_users_select" on app_users;
create policy "app_users_select" on app_users
  for select to authenticated
  using (
    lower(email) = lower(coalesce(auth.jwt()->>'email',''))
    or is_admin_user()
  );

-- Auto-registro: solo tu propia fila, siempre "en revisión" y rol USER
drop policy if exists "app_users_insert_own" on app_users;
create policy "app_users_insert_own" on app_users
  for insert to authenticated
  with check (
    lower(email) = lower(coalesce(auth.jwt()->>'email',''))
    and approved = false
    and role = 'USER'
  );

-- Aprobar / cambiar rol: solo admin aprobado
drop policy if exists "app_users_update_admin" on app_users;
create policy "app_users_update_admin" on app_users
  for update to authenticated
  using (is_admin_user())
  with check (is_admin_user());

-- Rechazar solicitud: solo admin aprobado
drop policy if exists "app_users_delete_admin" on app_users;
create policy "app_users_delete_admin" on app_users
  for delete to authenticated
  using (is_admin_user());

-- ── 5) Endurecer el acceso a las tablas de datos ──────────────────
-- Antes: cualquier usuario autenticado (con solo crearse una cuenta
-- en Supabase Auth) tenía acceso total. Ahora: solo aprobados.
-- (Se sustituyen las políticas "rls_authenticated_all" y
-- "Acceso authenticated" de los SQL anteriores.)

drop policy if exists "rls_authenticated_all" on workers;
drop policy if exists "rls_approved_all" on workers;
create policy "rls_approved_all" on workers
  for all to authenticated
  using (is_approved_user()) with check (is_approved_user());

drop policy if exists "rls_authenticated_all" on companies;
drop policy if exists "rls_approved_all" on companies;
create policy "rls_approved_all" on companies
  for all to authenticated
  using (is_approved_user()) with check (is_approved_user());

drop policy if exists "rls_authenticated_all" on courses;
drop policy if exists "rls_approved_all" on courses;
create policy "rls_approved_all" on courses
  for all to authenticated
  using (is_approved_user()) with check (is_approved_user());

drop policy if exists "rls_authenticated_all" on dc3_records;
drop policy if exists "rls_approved_all" on dc3_records;
create policy "rls_approved_all" on dc3_records
  for all to authenticated
  using (is_approved_user()) with check (is_approved_user());

drop policy if exists "rls_authenticated_all" on forklift_licenses;
drop policy if exists "rls_approved_all" on forklift_licenses;
create policy "rls_approved_all" on forklift_licenses
  for all to authenticated
  using (is_approved_user()) with check (is_approved_user());

drop policy if exists "rls_authenticated_all" on agents;
drop policy if exists "rls_approved_all" on agents;
create policy "rls_approved_all" on agents
  for all to authenticated
  using (is_approved_user()) with check (is_approved_user());

drop policy if exists "rls_authenticated_all" on agent_designs;
drop policy if exists "rls_approved_all" on agent_designs;
create policy "rls_approved_all" on agent_designs
  for all to authenticated
  using (is_approved_user()) with check (is_approved_user());

drop policy if exists "Acceso authenticated" on agent_credits;
drop policy if exists "rls_approved_all" on agent_credits;
create policy "rls_approved_all" on agent_credits
  for all to authenticated
  using (is_approved_user()) with check (is_approved_user());

drop policy if exists "Acceso authenticated" on credit_transactions;
drop policy if exists "rls_approved_all" on credit_transactions;
create policy "rls_approved_all" on credit_transactions
  for all to authenticated
  using (is_approved_user()) with check (is_approved_user());

-- ── 6) Verificación de la migración (ejecutar y esperar true,true) ─
select
  (select count(*) from app_users) as perfiles_totales,
  (select count(*) from app_users where approved) as aprobados;
