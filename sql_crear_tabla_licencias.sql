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

-- Seguridad de nivel fila (mismo estilo que el resto del sistema)
alter table public.forklift_licenses enable row level security;

create policy "Acceso anon" on public.forklift_licenses
  for all to anon using (true) with check (true);

create policy "Acceso authenticated" on public.forklift_licenses
  for all to authenticated using (true) with check (true);

grant all on public.forklift_licenses to anon, authenticated;
