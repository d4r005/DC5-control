-- ═══════════════════════════════════════════════════════════════════
-- MIGRACIÓN: Activar Row Level Security (RLS) en todas las tablas
-- Es idempotente: se puede ejecutar varias veces sin error.
--
-- NOTA: sin el prefijo "public." en los nombres de tabla porque el
-- SQL Editor de Supabase ya usa el esquema public por defecto, y
-- "public.companies" se rompe al copiar/pegar desde chats que auto-
-- convierten "public.com" en un link (bug de renderizado, no de SQL).
--
-- Resultado:
--   • Sin login (anon): SOLO puede LEER dc3_records y forklift_licenses
--     (cubre DC-3, Diplomas y Constancias — todos viven en dc3_records
--     con distinto documentType: 'DC3' | 'DIPLOMA' | 'BOTH')
--   • Usuarios logueados (authenticated): acceso completo
--   • Nadie con la clave pública puede crear, modificar o borrar datos
-- ═══════════════════════════════════════════════════════════════════

-- ── 1) Activar RLS en todas las tablas de la app ──────────────────
ALTER TABLE workers             ENABLE ROW LEVEL SECURITY;
ALTER TABLE companies           ENABLE ROW LEVEL SECURITY;
ALTER TABLE courses             ENABLE ROW LEVEL SECURITY;
ALTER TABLE dc3_records         ENABLE ROW LEVEL SECURITY;
ALTER TABLE forklift_licenses   ENABLE ROW LEVEL SECURITY;
ALTER TABLE agents              ENABLE ROW LEVEL SECURITY;
ALTER TABLE agent_designs       ENABLE ROW LEVEL SECURITY;
ALTER TABLE agent_credits       ENABLE ROW LEVEL SECURITY;
ALTER TABLE credit_transactions ENABLE ROW LEVEL SECURITY;

-- ── 2) Acceso completo para usuarios autenticados ─────────────────
DROP POLICY IF EXISTS "rls_authenticated_all" ON workers;
CREATE POLICY "rls_authenticated_all" ON workers
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON companies;
CREATE POLICY "rls_authenticated_all" ON companies
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON courses;
CREATE POLICY "rls_authenticated_all" ON courses
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON dc3_records;
CREATE POLICY "rls_authenticated_all" ON dc3_records
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON forklift_licenses;
CREATE POLICY "rls_authenticated_all" ON forklift_licenses
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON agents;
CREATE POLICY "rls_authenticated_all" ON agents
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON agent_designs;
CREATE POLICY "rls_authenticated_all" ON agent_designs
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON agent_credits;
CREATE POLICY "rls_authenticated_all" ON agent_credits
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "rls_authenticated_all" ON credit_transactions;
CREATE POLICY "rls_authenticated_all" ON credit_transactions
  FOR ALL TO authenticated USING (true) WITH CHECK (true);

-- ── 3) Lectura PÚBLICA solo para verificación de QRs ────────────────
-- dc3_records incluye DC-3, Diplomas y Constancias (documentType:
-- 'DC3' | 'DIPLOMA' | 'BOTH'), y showVerification() consulta esta
-- tabla sin filtrar por tipo, así que los 3 quedan cubiertos aqui.
DROP POLICY IF EXISTS "rls_anon_read_verify" ON dc3_records;
CREATE POLICY "rls_anon_read_verify" ON dc3_records
  FOR SELECT TO anon USING (true);

DROP POLICY IF EXISTS "rls_anon_read_verify" ON forklift_licenses;
CREATE POLICY "rls_anon_read_verify" ON forklift_licenses
  FOR SELECT TO anon USING (true);
