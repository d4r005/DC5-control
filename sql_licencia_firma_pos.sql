-- Posicion y tamano de la firma en la LICENCIA de montacargas (gafete CR80)
-- Ejecutar una vez en Supabase -> SQL Editor -> New query -> Run.
-- Es idempotente: se puede correr varias veces sin riesgo.

alter table public.agent_designs
  add column if not exists lic_firma_x numeric default 80,    -- X: esquina inferior izquierda (pt)
  add column if not exists lic_firma_y numeric default 47.6,  -- Y: esquina inferior izquierda (pt)
  add column if not exists lic_firma_w numeric default 67,    -- ancho maximo (pt)
  add column if not exists lic_firma_h numeric default 16;    -- alto maximo (pt)

update public.agent_designs set lic_firma_x = 80   where lic_firma_x is null;
update public.agent_designs set lic_firma_y = 47.6 where lic_firma_y is null;
update public.agent_designs set lic_firma_w = 67   where lic_firma_w is null;
update public.agent_designs set lic_firma_h = 16   where lic_firma_h is null;
