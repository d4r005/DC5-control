-- Log de auditoría de acciones de administrador
-- Pegar en: Supabase Dashboard → SQL Editor → New query → Run
-- Registra quién hizo qué: suspender/reactivar/eliminar usuarios,
-- cambios de rol, cargas de créditos, exenciones, edición de paquetes.

create table if not exists public.admin_audit_log (
  id uuid primary key default gen_random_uuid(),
  created_at timestamptz not null default now(),
  admin_email text not null,
  action text not null,
  target_email text,
  details text
);

alter table public.admin_audit_log enable row level security;

drop policy if exists "Solo admin lee auditoria" on public.admin_audit_log;
drop policy if exists "Admin inserta auditoria" on public.admin_audit_log;

-- Lectura: solo administradores
create policy "Solo admin lee auditoria" on public.admin_audit_log
  for select to authenticated
  using (is_admin_user());

-- Escritura: solo administradores (el Worker usa service role, que
-- bypassa RLS, así que sus inserciones siempre funcionan)
create policy "Admin inserta auditoria" on public.admin_audit_log
  for insert to authenticated
  with check (is_admin_user());

grant select, insert on public.admin_audit_log to authenticated;

-- ══════════════════════════════════════════════════════
-- FIX: el script de pagos (sql_pago_paquetes.sql) revocó los permisos
-- REST de payment_orders para usuarios autenticados. Sin este GRANT, la
-- sección "Mis compras", el panel "Ventas" y los KPIs del dashboard
-- (créditos vendidos / ingresos) marcan vacío aunque la RLS permitiría
-- leer las filas propias. Con el GRANT + la política "orders_select_own"
-- (ya existe) cada usuario solo ve SUS órdenes y el admin ve todas.
-- ══════════════════════════════════════════════════════
grant select on public.payment_orders to authenticated;
