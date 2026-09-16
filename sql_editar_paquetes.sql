-- ═══════════════════════════════════════════════════════════════════
-- EDICIÓN DE PAQUETES (SOLO ADMIN)
--
-- Permite que un usuario con rol ADMIN edite el precio, los créditos
-- y el estado (activo) de los paquetes de compra desde la propia app,
-- sin entrar al panel de Supabase.
--
-- Seguridad:
--   · anon sigue SIN ningún acceso a credit_packages.
--   · Solo ADMIN (rol en app_users, aprobado) puede hacer UPDATE.
--   · El INSERT/DELETE sigue restringido (solo service role).
--   · Se parchea el RPC del webhook para validar el monto contra el
--     precio FIJADO EN LA ORDEN al crearla — así, si el admin cambia
--     un precio mientras hay órdenes pendientes, los pagos en curso
--     no se rechazan por "amount_mismatch".
--
-- Pegar en: Supabase Dashboard → SQL Editor → New query → Run
-- Es idempotente: se puede ejecutar varias veces sin error.
-- ═══════════════════════════════════════════════════════════════════

-- ── 1) Permiso de UPDATE solo para autenticados (anon: NADA) ───────
revoke all on credit_packages from anon;
grant select, update on credit_packages to authenticated;

-- ── 2) Política: solo ADMIN puede editar paquetes ──────────────────
drop policy if exists "packages_update_admin" on credit_packages;
create policy "packages_update_admin" on credit_packages
  for update to authenticated
  using (is_admin_user())
  with check (is_admin_user());

-- ── 3) RPC del webhook: validar monto contra la ORDEN, no contra el
--    precio actual del paquete (soporta ediciones de precio en vivo)
--    Los créditos a acreditar sí salen del paquete actual.
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

  -- El paquete debe seguir activo
  select * into v_pack from credit_packages
    where id = v_order.pack_id and active;
  if not found then
    return jsonb_build_object('ok', false, 'reason', 'package_inactive');
  end if;

  -- El monto pagado debe coincidir con el precio FIJADO EN LA ORDEN
  -- (no con el precio actual del paquete: el admin puede haber
  -- editado el precio mientras esta orden estaba en curso)
  if coalesce(p_amount, 0) + 0.01 < v_order.amount then
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
-- Debe listar la política nueva y los 3 paquetes
select id, name, price, credits, active from credit_packages order by sort;
