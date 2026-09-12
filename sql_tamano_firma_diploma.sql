-- Tamaño configurable de la firma del agente en el DIPLOMA
-- Pegar en: Supabase Dashboard → SQL Editor → New query → Run
--
-- Web y Android leen dip_firma_w / dip_firma_h del diseño del agente para
-- dibujar la firma más grande o más chica sobre el nombre del agente.
-- Defaults: 90 (ancho) x 50 (alto) puntos, igual al comportamiento anterior.

alter table public.agent_designs
  add column if not exists dip_firma_w int default 90;

alter table public.agent_designs
  add column if not exists dip_firma_h int default 50;
