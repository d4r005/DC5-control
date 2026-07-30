# Plan de Implementación: Incorporación de Cédula Profesional

Añadir el soporte para registrar y mostrar la Cédula Profesional de los agentes capacitadores en los diplomas, permitiendo su personalización en el diseño.

## User Review Required

> [!IMPORTANT]
> **Cambio en Base de Datos:**
> Este plan requiere añadir una columna a la tabla `agents` y tres columnas a `agent_designs` en Supabase. Se proporciona el script SQL al final.

## Proposed Changes

### 1. Base de Datos (Supabase)
- Añadir `cedula_profesional` a la tabla `agents`.
- Añadir `dip_cedula_x`, `dip_cedula_y` y `dip_cedula_sz` a la tabla `agent_designs`.

### 2. Modelos de Datos (Android)

#### [MODIFY] [Models.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/data/model/Models.kt)
- Actualizar `Agent` para incluir `cedula_profesional`.
- Actualizar `AgentDesign` para incluir coordenadas y tamaño de la cédula en el diploma.

### 3. Panel de Agentes (Web y Android)

#### [MODIFY] [index.html](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/index.html)
- Añadir campo "Cédula Profesional" en el modal de creación/edición de agentes.
- Mostrar la cédula en la tabla de agentes.

#### [MODIFY] [AgentListScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/AgentListScreen.kt)
- Añadir el campo de texto en el `AgentAddEditDialog`.
- Mostrar el valor en las listas compactas y expandidas.

### 4. Personalización del Diseño (Web y Android)

#### [MODIFY] [DC3DesignScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/DC3DesignScreen.kt)
- Añadir controles de posición y tamaño para la cédula en la pestaña de "Diploma".
- Actualizar la lógica de carga y guardado del diseño.

#### [MODIFY] [index.html](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/index.html)
- Añadir el campo al estado de diseño (`designState`) y mapeos (`toSnake`/`toCamel`).
- Añadir el elemento visual y el control de arrastre (drag handle) en la previsualización del diploma.

### 5. Generación de Diplomas (Web y Android)

#### [MODIFY] [DiplomaGenerator.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/util/DiplomaGenerator.kt)
- Renderizar el texto "CÉDULA PROFESIONAL: [VALOR]" si el agente la tiene registrada, usando las coordenadas personalizadas.

#### [MODIFY] [index.html](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/index.html) (función `_buildDiplomaCore`)
- Añadir la lógica de dibujado de la cédula en el PDF generado desde la web.

## Plan de Verificación

### Manual Verification
1.  **Registro:** Editar el perfil de Cynthia en el panel de Agentes y añadir una cédula de prueba.
2.  **Diseño:** Ir a la pestaña Diploma y verificar que aparezca el nuevo recuadro de "CÉDULA". Ajustar su posición.
3.  **Generación:** Generar un diploma para un curso impartido por Cynthia y confirmar que la cédula aparezca en la posición configurada.
4.  **En Blanco:** Generar un diploma para un agente sin cédula y confirmar que el espacio quede vacío sin errores.

---

## SQL de Actualización para Supabase

```sql
-- 1. Añadir campo a Agentes
ALTER TABLE agents ADD COLUMN IF NOT EXISTS cedula_profesional TEXT;

-- 2. Añadir soporte para diseño de Cédula en Diplomas
ALTER TABLE agent_designs
ADD COLUMN IF NOT EXISTS dip_cedula_x FLOAT8 DEFAULT 396,
ADD COLUMN IF NOT EXISTS dip_cedula_y FLOAT8 DEFAULT 596,
ADD COLUMN IF NOT EXISTS dip_cedula_sz FLOAT8 DEFAULT 8;
```
