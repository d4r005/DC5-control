-- ═══════════════════════════════════════════════════════════════════
-- MIGRACIÓN: Activar Row Level Security (RLS) en todas las tablas
-- Ejecutar en: Supabase Dashboard → SQL Editor → New query → Run
-- Es idempotente: se puede ejecutar varias veces sin error.
--
-- Resultado:
--   • Sin login (rol anon): SOLO puede LEER dc3_records y forklift_licenses
--     (necesario para que la verificación de QRs siga funcionando pública).
--   • Usuarios logueados (rol authenticated): acceso completo en todas las tablas.
--   • Ya nadie con la clave pública puede crear, modificar o borrar datos.
-- ═══════════════════════════════════════════════════════════════════

-- ── 1) Activar RLS en todas las tablas de la app ──────────────────
ALTER TABLE public.workers             ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.companies            ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.courses             ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.dc3_records         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.forklift_licenses   ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.agents              ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.agent_designs       ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.agent_credits       ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.credit_transactions ENABLE ROW LEVEL SECURITY;

-- ── 2) Acceso completo para usuarios autenticados ─────────────────
-- (la app solo deja entrar a los perfiles autorizados de APP_USERS;
--  las contraseñas se validan en Supabase Auth, no en el cliente)
DROP POLICY IF EXISTS "rls_authenticated_all" ON public.workers;
CREATE POLICY "rls_authenticated_all" ON public.workers
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON public.companies;
CREATE POLICY "rls_authenticated_all" ON public.companies
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON public.courses;
CREATE POLICY "rls_authenticated_all" ON public.courses
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON public.dc3_records;
CREATE POLICY "rls_authenticated_all" ON public.dc3_records
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON public.forklift_licenses;
CREATE POLICY "rls_authenticated_all" ON public.forklift_licenses
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON public.agents;
CREATE POLICY "rls_authenticated_all" ON public.agents
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON public.agent_designs;
CREATE POLICY "rls_authenticated_all" ON public.agent_designs
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON public.agent_credits;
CREATE POLICY "rls_authenticated_all" ON public.agent_credits
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON public.credit_transactions;
CREATE POLICY "rls_authenticated_all" ON public.credit_transactions
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

-- ── 3) Lectura PÚBLICA solo para verificación de QRs ────────────────
-- La página ?v=<id> consulta estas dos tablas sin login.
-- anon ya NO puede tocar workers, companies, courses, agents, etc.
DROP POLICY IF EXISTS "rls_anon_read_verify" ON public.dc3_records;
CREATE POLICY "rls_anon_read_verify" ON public.dc3_records
  FOR SELECT TO anon USING (true);

DROP POLICY IF EXISTS "rls_anon_read_verify" ON public.forklift_licenses;
CREATE POLICY "rls_anon_read_verify" ON public.forklift_licenses
  FOR SELECT TO anon USING (true);
