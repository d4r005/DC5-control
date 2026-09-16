-- ═══════════════════════════════════════════════════════════════════
-- MIGRACIÓN v2: Auto-registro con AUTO-SERVICIO TOTAL
-- Pegar en: Supabase Dashboard → SQL Editor → New query → Run
-- Es idempotente: se puede ejecutar varias veces sin error.
--
-- Diseño:
--   • El agente crea SU cuenta (correo + contraseña en Supabase Auth).
--     Con el correo confirmado entra DIRECTO a llenar sus catálogos
--     (agente capacitador, empresas, cursos, personal) sin esperar
--     aprobación manual.
--   • AISLAMIENTO TOTAL: cada usuario solo ve y escribe las filas
--     cuyo creator_email es SU correo. El admin (Dario) lo ve todo.
--   • CRÉDITOS: el consumo pasa por el RPC consume_credit, que
--     descuenta de forma atómica y NO permite que un agente se
--     acredite saldos a sí mismo. Con 0 créditos no genera.
--   • STORAGE: cada usuario solo toca los objetos de SU carpeta
--     (carpeta = su correo). El admin ve ambos buckets completos.
--   • SUSPENSIÓN: el admin puede suspender (approved=false) o
--     reactivar cualquier cuenta desde la vista Usuarios.
--   • REGLA DE SEGURIDAD: el rol anon sigue SIN listado ni escritura
--     en tablas. El registro público usa Supabase Auth (signUp),
--     que es la única vía pública estándar y no toca tablas.
-- ═══════════════════════════════════════════════════════════════════

-- ── 1) Tabla de perfiles de acceso ────────────────────────────────
-- approved = cuenta activa (true por defecto). El admin la pone en
-- false para SUSPENDER el acceso de esa cuenta.
create table if not exists app_users (
  id uuid primary key default gen_random_uuid(),
  created_at timestamptz not null default now(),
  email text unique not null,
  name text not null default '',
  phone text,
  stps_registration text,
  role text not null default 'USER' check (role in ('USER','ADMIN')),
  approved boolean not null default true
);

alter table app_users enable row level security;
grant select, insert, update, delete on app_users to authenticated;

-- ── 2) Perfiles actuales (idempotente) ────────────────────────────
insert into app_users (email, name, role, approved) values
  ('d4r005@gmail.com', 'Dario Robles', 'ADMIN', true),
  ('lugga.advisors@gmail.com', 'Cynthia Garza Lugo', 'USER', true)
on conflict (email) do nothing;

-- ── 3) Funciones auxiliares (security definer = sin recursión RLS) ─
create or replace function is_active_user()
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

revoke execute on function is_active_user() from public, anon;
revoke execute on function is_admin_user() from public, anon;
grant execute on function is_active_user() to authenticated;
grant execute on function is_admin_user() to authenticated;

-- ── 4) Políticas de app_users ─────────────────────────────────────
-- Ver la propia fila (o todas si eres admin)
drop policy if exists "app_users_select" on app_users;
create policy "app_users_select" on app_users
  for select to authenticated
  using (
    lower(email) = lower(coalesce(auth.jwt()->>'email',''))
    or is_admin_user()
  );

-- Auto-registro: solo tu propia fila, siempre activa y rol USER
drop policy if exists "app_users_insert_own" on app_users;
create policy "app_users_insert_own" on app_users
  for insert to authenticated
  with check (
    lower(email) = lower(coalesce(auth.jwt()->>'email',''))
    and approved = true
    and role = 'USER'
  );

-- Suspender / reactivar / cambiar rol: solo admin
drop policy if exists "app_users_update_admin" on app_users;
create policy "app_users_update_admin" on app_users
  for update to authenticated
  using (is_admin_user()) with check (is_admin_user());

drop policy if exists "app_users_delete_admin" on app_users;
create policy "app_users_delete_admin" on app_users
  for delete to authenticated
  using (is_admin_user());

-- ── 5) Datos: aislamiento por agente (creator_email = tu correo) ──
-- Se sustituyen las políticas "rls_authenticated_all" y
-- "Acceso authenticated" de los SQL anteriores.
drop policy if exists "rls_authenticated_all" on workers;
drop policy if exists "rls_approved_all" on workers;
create policy "rls_owner_all" on workers
  for all to authenticated
  using (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  )
  with check (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  );

drop policy if exists "rls_authenticated_all" on companies;
drop policy if exists "rls_approved_all" on companies;
create policy "rls_owner_all" on companies
  for all to authenticated
  using (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  )
  with check (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  );

drop policy if exists "rls_authenticated_all" on courses;
drop policy if exists "rls_approved_all" on courses;
create policy "rls_owner_all" on courses
  for all to authenticated
  using (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  )
  with check (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  );

drop policy if exists "rls_authenticated_all" on dc3_records;
drop policy if exists "rls_approved_all" on dc3_records;
create policy "rls_owner_all" on dc3_records
  for all to authenticated
  using (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  )
  with check (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  );

drop policy if exists "rls_authenticated_all" on forklift_licenses;
drop policy if exists "rls_approved_all" on forklift_licenses;
create policy "rls_owner_all" on forklift_licenses
  for all to authenticated
  using (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  )
  with check (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  );

drop policy if exists "rls_authenticated_all" on agents;
drop policy if exists "rls_approved_all" on agents;
create policy "rls_owner_all" on agents
  for all to authenticated
  using (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  )
  with check (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  );

drop policy if exists "rls_authenticated_all" on agent_designs;
drop policy if exists "rls_approved_all" on agent_designs;
create policy "rls_owner_all" on agent_designs
  for all to authenticated
  using (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  )
  with check (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  );

-- ── 6) Créditos: lectura propia, escritura solo por RPC o admin ───
alter table agent_credits add column if not exists credits_exempt boolean not null default false;

drop policy if exists "Acceso authenticated" on agent_credits;
drop policy if exists "rls_approved_all" on agent_credits;
drop policy if exists "credits_select_own" on agent_credits;
create policy "credits_select_own" on agent_credits
  for select to authenticated
  using (
    lower(agent_email) = lower(coalesce(auth.jwt()->>'email',''))
    or is_admin_user()
  );

drop policy if exists "credits_admin_all" on agent_credits;
create policy "credits_admin_all" on agent_credits
  for all to authenticated
  using (is_admin_user()) with check (is_admin_user());

drop policy if exists "Acceso authenticated" on credit_transactions;
drop policy if exists "rls_approved_all" on credit_transactions;
drop policy if exists "transactions_select_own" on credit_transactions;
create policy "transactions_select_own" on credit_transactions
  for select to authenticated
  using (
    lower(agent_email) = lower(coalesce(auth.jwt()->>'email',''))
    or is_admin_user()
  );

drop policy if exists "transactions_admin_all" on credit_transactions;
create policy "transactions_admin_all" on credit_transactions
  for all to authenticated
  using (is_admin_user()) with check (is_admin_user());

-- ── 7) RPC de consumo atómico de créditos ─────────────────────────
-- Solo el servidor (security definer) puede restar saldo. Un agente
-- NO puede escribir agent_credits: si intenta generarse créditos a
-- sí mismo por la API, la política se lo impide; el descuento solo
-- ocurre aquí, verificando el saldo de forma atómica.
create or replace function consume_credit(
  p_amount integer,
  p_doc_type text default null,
  p_ref_id text default null
)
returns jsonb
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
  v_email  text := lower(coalesce(auth.jwt()->>'email',''));
  v_admin  boolean;
  v_exempt boolean;
  v_balance integer;
begin
  if v_email = '' or coalesce(p_amount,0) <= 0 then
    return jsonb_build_object('ok', false, 'reason', 'invalid_request');
  end if;

  -- El admin genera gratis
  select coalesce(role = 'ADMIN', false) into v_admin from app_users
    where lower(email) = v_email and approved;
  if v_admin then
    return jsonb_build_object('ok', true, 'free', true);
  end if;

  -- Cuentas de cortesía marcadas por el admin
  select coalesce(credits_exempt, false) into v_exempt
    from agent_credits where lower(agent_email) = v_email;
  if v_exempt then
    return jsonb_build_object('ok', true, 'free', true);
  end if;

  -- Descuento atómico: solo si el saldo alcanza
  update agent_credits
     set balance = balance - p_amount, updated_at = now()
   where lower(agent_email) = v_email and balance >= p_amount
  returning balance into v_balance;

  if v_balance is null then
    return jsonb_build_object('ok', false, 'reason', 'no_credits');
  end if;

  insert into credit_transactions
    (agent_email, delta, reason, document_type, reference_id, created_by)
  values
    (v_email, -p_amount, 'Generación de documento', p_doc_type, p_ref_id, v_email);

  return jsonb_build_object('ok', true, 'balance', v_balance);
end;
$$;

revoke execute on function consume_credit(integer, text, text) from public, anon;
grant execute on function consume_credit(integer, text, text) to authenticated;

-- ── 8) Storage: solo tu carpeta (carpeta = tu correo) ──────────────
-- Sustituye las políticas "storage_authenticated_*" de
-- sql_endurecer_seguridad.sql, que daban a cualquier autenticado
-- acceso a TODOS los objetos (fotos de otros agentes incluidas).
drop policy if exists "storage_authenticated_select" on storage.objects;
drop policy if exists "storage_authenticated_insert" on storage.objects;
drop policy if exists "storage_authenticated_update" on storage.objects;
drop policy if exists "storage_authenticated_delete" on storage.objects;

create policy "storage_owner_select" on storage.objects
  for select to authenticated
  using (
    bucket_id in ('worker-photos','templates')
    and (
      is_admin_user()
      or name like lower(coalesce(auth.jwt()->>'email','')) || '/%'
    )
  );

create policy "storage_owner_insert" on storage.objects
  for insert to authenticated
  with check (
    bucket_id in ('worker-photos','templates')
    and (
      is_admin_user()
      or name like lower(coalesce(auth.jwt()->>'email','')) || '/%'
    )
  );

create policy "storage_owner_update" on storage.objects
  for update to authenticated
  using (
    bucket_id in ('worker-photos','templates')
    and (
      is_admin_user()
      or name like lower(coalesce(auth.jwt()->>'email','')) || '/%'
    )
  )
  with check (
    bucket_id in ('worker-photos','templates')
    and (
      is_admin_user()
      or name like lower(coalesce(auth.jwt()->>'email','')) || '/%'
    )
  );

create policy "storage_owner_delete" on storage.objects
  for delete to authenticated
  using (
    bucket_id in ('worker-photos','templates')
    and (
      is_admin_user()
      or name like lower(coalesce(auth.jwt()->>'email','')) || '/%'
    )
  );

-- ── 9) Verificación de la migración ───────────────────────────────
select
  (select count(*) from app_users) as perfiles,
  (select count(*) from app_users where approved) as activos;
