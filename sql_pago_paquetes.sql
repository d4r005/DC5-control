-- ═══════════════════════════════════════════════════════════════════
-- PARTE 2: COMPRA DE PAQUETES CON MERCADO PAGO
--
-- Flujo: agente elige paquete → Worker crea la orden + checkout de MP
-- → MP cobra y notifica → Worker valida el pago contra los servidores
-- de MP → este RPC acredita los créditos.
--
-- Reglas de seguridad que implementa:
--   · anon NO tiene ningún acceso (ni lectura) a las tablas nuevas.
--   · El saldo SOLO se modifica por el RPC (invocado únicamente por el
--     Worker con service role tras verificar un pago real aprobado).
--   · Idempotente: si la orden ya está pagada, no vuelve a acreditar.
--   · El monto pagado se verifica contra el precio oficial del paquete.
--
-- Pegar en: Supabase Dashboard → SQL Editor → New query → Run
-- Es idempotente: se puede ejecutar varias veces sin error.
-- ═══════════════════════════════════════════════════════════════════

-- ── 1) Catálogo de paquetes (precios de la landing) ─────────────────
create table if not exists credit_packages (
  id          text primary key,
  name        text not null,
  description text,
  price       numeric(10,2) not null check (price > 0),
  credits     integer not null check (credits > 0),
  active      boolean not null default true,
  sort        integer not null default 0,
  created_at  timestamptz not null default now()
);

insert into credit_packages (id, name, description, price, credits, active, sort) values
  ('inicial',     'Paquete Inicial',     '100 documentos · $3.00 c/u',    300.00,   100, true, 1),
  ('profesional', 'Paquete Profesional', '300 documentos · $2.50 c/u',    750.00,   300, true, 2),
  ('empresa',     'Paquete Empresa',     '1,000 documentos · $2.00 c/u', 2000.00, 1000, true, 3)
on conflict (id) do update
  set name = excluded.name,
      description = excluded.description,
      price = excluded.price,
      credits = excluded.credits,
      sort = excluded.sort;

alter table credit_packages enable row level security;

-- Solo usuarios autenticados pueden ver el catálogo (anon: NADA)
drop policy if exists "packages_select" on credit_packages;
create policy "packages_select" on credit_packages
  for select to authenticated
  using (true);

revoke all on credit_packages from anon;

-- ── 2) Órdenes de compra ─────────────────────────────────────────────
create table if not exists payment_orders (
  id            uuid primary key default gen_random_uuid(),
  created_at    timestamptz not null default now(),
  agent_email   text not null,
  pack_id       text not null references credit_packages(id),
  amount        numeric(10,2) not null,
  currency      text not null default 'MXN',
  status        text not null default 'pending'
                  check (status in ('pending','paid','cancelled')),
  mp_payment_id text,
  paid_at       timestamptz
);

create index if not exists idx_payment_orders_agent
  on payment_orders (lower(agent_email), created_at desc);

-- Un mismo pago de MP no puede procesarse dos veces
create unique index if not exists idx_payment_orders_mp_payment
  on payment_orders (mp_payment_id) where mp_payment_id is not null;

alter table payment_orders enable row level security;

-- El agente ve SOLO sus propias órdenes; el admin ve todas
drop policy if exists "orders_select_own" on payment_orders;
create policy "orders_select_own" on payment_orders
  for select to authenticated
  using (
    lower(agent_email) = lower(coalesce(auth.jwt()->>'email',''))
    or is_admin_user()
  );

-- Escrituras: NADIE por REST directo (solo service role del Worker)
revoke all on payment_orders from anon, authenticated;

-- ── 3) RPC: acreditar créditos tras un pago verificado ───────────────
-- SOLO ejecutable por service_role (el Worker lo llama después de
-- validar el pago contra los servidores de Mercado Pago).
create or replace function process_payment_webhook(
  p_order         uuid,
  p_mp_payment_id text,
  p_amount        numeric
)
returns jsonb
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
  v_order   payment_orders%rowtype;
  v_pack    credit_packages%rowtype;
  v_balance integer;
begin
  if p_order is null or coalesce(p_mp_payment_id, '') = '' then
    return jsonb_build_object('ok', false, 'reason', 'invalid_request');
  end if;

  select * into v_order from payment_orders
    where id = p_order for update;
  if not found then
    return jsonb_build_object('ok', false, 'reason', 'order_not_found');
  end if;

  -- Idempotente: una orden pagada jamás acredita dos veces
  if v_order.status = 'paid' then
    return jsonb_build_object('ok', true, 'already', true);
  end if;
  if v_order.status <> 'pending' then
    return jsonb_build_object('ok', false, 'reason', 'order_not_pending');
  end if;

  -- El paquete debe seguir activo y el monto pagado debe coincidir
  select * into v_pack from credit_packages
    where id = v_order.pack_id and active;
  if not found then
    return jsonb_build_object('ok', false, 'reason', 'package_inactive');
  end if;
  if coalesce(p_amount, 0) + 0.01 < v_pack.price then
    return jsonb_build_object('ok', false, 'reason', 'amount_mismatch');
  end if;

  -- Acreditar: crea la fila si el agente no tenía saldo aún
  insert into agent_credits (agent_email, balance)
  values (v_order.agent_email, v_pack.credits)
  on conflict (agent_email) do update
    set balance = agent_credits.balance + excluded.balance,
        updated_at = now();

  insert into credit_transactions
    (agent_email, delta, reason, document_type, reference_id, created_by)
  values
    (v_order.agent_email, v_pack.credits,
     'Compra en línea: ' || v_pack.name,
     'package_purchase', p_mp_payment_id, v_order.agent_email);

  update payment_orders
     set status = 'paid', mp_payment_id = p_mp_payment_id, paid_at = now()
   where id = p_order and status = 'pending';

  select balance into v_balance from agent_credits
    where lower(agent_email) = lower(v_order.agent_email);

  return jsonb_build_object('ok', true, 'balance', v_balance);
end;
$$;

revoke execute on function process_payment_webhook(uuid, text, numeric)
  from public, anon, authenticated;
grant execute on function process_payment_webhook(uuid, text, numeric)
  to service_role;

-- ── 4) Verificación ─────────────────────────────────────────────────
-- Debe listar los 3 paquetes activos con sus precios
select id, name, price, credits, active from credit_packages order by sort;
