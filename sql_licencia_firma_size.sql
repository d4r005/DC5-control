-- Tamaño configurable de la firma en la LICENCIA de montacargas
-- Ejecutar una vez en Supabase (SQL Editor). Es seguro repetirlo.
alter table public.agent_designs
  add column if not exists lic_firma_w numeric default 67,
  add column if not exists lic_firma_h numeric default 16;

-- Opcional: valores iniciales equivalentes al tamaño automático anterior
update public.agent_designs set lic_firma_w = 67 where lic_firma_w is null;
update public.agent_designs set lic_firma_h = 16 where lic_firma_h is null;
