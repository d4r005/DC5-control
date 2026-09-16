-- ═══════════════════════════════════════════════════════════════════
-- MIGRACIÓN: Confirmación de correo propia (Resend + Worker)
-- Reemplaza el "Confirm email" de Supabase (limitado en plan gratuito)
-- por nuestro flujo propio: el correo lo envía el Worker de Cloudflare
-- vía Resend, y el token se valida en /api/email/verify.
--
-- Pegar en: Supabase Dashboard → SQL Editor → New query → Run
-- Es idempotente: se puede ejecutar varias veces sin error.
-- ═══════════════════════════════════════════════════════════════════

-- ── 1) Columna de confirmación ────────────────────────────────────
-- email_confirmed=false → la cuenta NO puede leer ni escribir NADA
-- (is_active_user lo exige). Solo se pone en true al validar el
-- token del correo.
alter table app_users
  add column if not exists email_confirmed boolean not null default false;

-- Las cuentas ya existentes (Dario y Cynthia) quedan confirmadas
update app_users
   set email_confirmed = true
 where not email_confirmed
   and lower(email) in ('d4r005@gmail.com', 'lugga.advisors@gmail.com');

-- ── 2) Usuario activo = aprobado Y con correo confirmado ───────────
create or replace function is_active_user()
returns boolean
language sql
security definer
set search_path = public, pg_temp
as $$
  select exists (
    select 1 from app_users
    where lower(email) = lower(coalesce(auth.jwt()->>'email',''))
      and approved and email_confirmed
  );
$$;

revoke execute on function is_active_user() from public, anon;
grant execute on function is_active_user() to authenticated;

-- ── 3) El RPC de consumo también exige correo confirmado ───────────
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
    where lower(email) = v_email and approved and email_confirmed;
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

-- ── 4) Verificación de la migración ───────────────────────────────
-- Debe mostrar email_confirmed = true para las 2 cuentas existentes
select email, role, approved, email_confirmed
  from app_users
 order by created_at;
