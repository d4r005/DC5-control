-- ════════════════════════════════════════════════════════════════════
-- 1) Logo propio de la LICENCIA (independiente del logo del DC-3)
-- 2) Archivo inmutable de DC-3 / diplomas ya emitidos
-- Ejecutar en Supabase -> SQL Editor -> New query -> Run (idempotente)
-- ════════════════════════════════════════════════════════════════════

-- ── 1. Logo de licencia separado ─────────────────────────────────────
alter table public.agent_designs
  add column if not exists lic_logo_base64 text;

-- ── 2. Documentos emitidos (PDF congelado) ───────────────────────────
create table if not exists public.document_files (
  id uuid primary key default gen_random_uuid(),
  record_id text not null,
  kind text not null check (kind in ('DC3','DIPLOMA')),
  pdf_base64 text not null,
  creator_email text,
  created_at timestamptz not null default now(),
  unique (record_id, kind)
);

create index if not exists document_files_record_idx
  on public.document_files (record_id);

grant select, insert, update, delete on public.document_files to authenticated;

alter table public.document_files enable row level security;

drop policy if exists "document_files_owner_all" on public.document_files;
create policy "document_files_owner_all" on public.document_files
  for all to authenticated
  using (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  )
  with check (
    (is_active_user() and lower(creator_email) = lower(coalesce(auth.jwt()->>'email','')))
    or is_admin_user()
  );
