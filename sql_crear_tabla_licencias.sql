-- Tabla de licencias de montacargas
-- Pegar en: Supabase Dashboard → SQL Editor → New query → Run

create table public.forklift_licenses (
  id uuid primary key default gen_random_uuid(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  dc3_record_id uuid,
  worker_id text,
  worker_name text,
  worker_pos text,
  company_name text,
  course_name text,
  agent_name text,
  agent_stps text,
  start_date text,
  end_date text,
  folio text,
  equipment text[],
  logo_base64 text,
  creator_email text
);

-- Seguridad de nivel fila
-- ⚠️ NUNCA dar acceso de escritura al rol anon: la clave anónima viaja
-- en el app.html público y cualquiera podría modificar las licencias.
-- La lectura pública para validación de QRs la cubre el RPC
-- verify_document (ver sql_endurecer_seguridad.sql).
alter table public.forklift_licenses enable row level security;

drop policy if exists "Acceso anon" on public.forklift_licenses;
drop policy if exists "Acceso authenticated" on public.forklift_licenses;

create policy "Acceso authenticated" on public.forklift_licenses
  for all to authenticated using (true) with check (true);

grant all on public.forklift_licenses to authenticated;
