-- ═══════════════════════════════════════════════════════════════════
-- Folios únicos y atómicos para DC-3, Diplomas y Licencias
-- ───────────────────────────────────────────────────────────────────
-- Problema: el folio se calculaba en el navegador ("máximo visible + 1").
-- Como cada cuenta solo ve sus propios registros, dos usuarios calculaban
-- el mismo número; y un doble clic generaba dos documentos con el mismo
-- folio. Resultado: folios repetidos entre documentos distintos.
--
-- Solución: una secuencia por prefijo+año dentro de Postgres. El incremento
-- ocurre con bloqueo de fila, así que dos llamadas simultáneas nunca
-- obtienen el mismo número.
-- Ejecutar UNA vez en el SQL Editor de Supabase. Es idempotente.
-- ═══════════════════════════════════════════════════════════════════

-- 1) Tabla de contadores
create table if not exists folio_counters (
  prefix       text not null,
  year         int  not null,
  last_number  int  not null default 0,
  primary key (prefix, year)
);

-- 2) Función que entrega el siguiente folio de forma atómica
create or replace function next_folio(p_prefix text, p_year int)
returns text
language plpgsql
security definer
set search_path = public
as $$
declare
  v_num int;
begin
  insert into folio_counters (prefix, year, last_number)
  values (p_prefix, p_year, 1)
  on conflict (prefix, year)
  do update set last_number = folio_counters.last_number + 1
  returning last_number into v_num;

  return p_prefix || '-' || p_year || '-' || lpad(v_num::text, 4, '0');
end;
$$;

-- 3) Permisos para que la app (anon/authenticated) pueda pedir folios
grant execute on function next_folio(text, int) to anon, authenticated, service_role;

-- 4) Sembrar los contadores con el máximo ya utilizado en documentos existentes
--    (considera folio y folio_dc3, que es donde va el folio del DC-3 en los BOTH)
with folios as (
  select folio     as f from dc3_records where folio     is not null
  union all
  select folio_dc3 as f from dc3_records where folio_dc3 is not null
),
parsed as (
  select
    regexp_replace(f, '-[0-9]{4}-[0-9]+$', '')                as prefix,
    (regexp_match(f, '-([0-9]{4})-([0-9]+)$'))[1]::int        as year,
    (regexp_match(f, '-([0-9]{4})-([0-9]+)$'))[2]::int        as num
  from folios
  where f ~ '-[0-9]{4}-[0-9]+$'
)
insert into folio_counters (prefix, year, last_number)
select prefix, year, max(num)
from parsed
group by prefix, year
on conflict (prefix, year)
do update set last_number = greatest(folio_counters.last_number, excluded.last_number);

-- 5) Verificación: contadores sembrados
select prefix, year, last_number
from folio_counters
order by prefix, year;
