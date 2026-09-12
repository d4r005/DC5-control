-- Posición INDEPENDIENTE de la firma del agente en el DIPLOMA (X, Y)
-- Pegar en: Supabase Dashboard → SQL Editor → New query → Run
--
-- Antes, la firma del diploma se dibujaba siempre centrada sobre el nombre
-- del agente (dip_agent_x/dip_agent_y), solo se podía cambiar su tamaño
-- (dip_firma_w/dip_firma_h). Ahora la firma tiene su propio recuadro
-- arrastrable e independiente, igual que en el DC-3.
--
-- Defaults (351, 512) reproducen EXACTAMENTE la posición visual anterior
-- para los diseños ya guardados (que no tienen estas columnas todavía):
-- el código de generación usa un fallback calculado a partir de
-- dip_agent_x/dip_agent_y si estas columnas vienen NULL.

alter table public.agent_designs
  add column if not exists dip_firma_x int default 351;

alter table public.agent_designs
  add column if not exists dip_firma_y int default 512;
