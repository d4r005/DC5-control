-- Sistema de créditos por documento generado
-- Pegar en: Supabase Dashboard → SQL Editor → New query → Run

create table public.agent_credits (
  id uuid primary key default gen_random_uuid(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  agent_email text unique not null,
  balance integer not null default 0
);

create table public.credit_transactions (
  id uuid primary key default gen_random_uuid(),
  created_at timestamptz not null default now(),
  agent_email text not null,
  delta integer not null,
  reason text,
  document_type text,
  reference_id text,
  created_by text
);

-- Seguridad de nivel fila (mismo estilo que el resto del sistema)
alter table public.agent_credits enable row level security;
alter table public.credit_transactions enable row level security;

-- ⚠️ NUNCA dar acceso de escritura al rol anon: con la política
-- "Acceso anon" FOR ALL cualquiera podría regalarse créditos con la
-- clave pública. Solo usuarios autenticados.
drop policy if exists "Acceso anon" on public.agent_credits;
drop policy if exists "Acceso authenticated" on public.agent_credits;
drop policy if exists "Acceso anon" on public.credit_transactions;
drop policy if exists "Acceso authenticated" on public.credit_transactions;

create policy "Acceso authenticated" on public.agent_credits
  for all to authenticated using (true) with check (true);
create policy "Acceso authenticated" on public.credit_transactions
  for all to authenticated using (true) with check (true);

grant all on public.agent_credits to authenticated;
grant all on public.credit_transactions to authenticated;

-- Exencion de cobro: el admin puede marcar cuentas que generan sin consumir creditos
alter table public.agent_credits add column if not exists credits_exempt boolean not null default false;
