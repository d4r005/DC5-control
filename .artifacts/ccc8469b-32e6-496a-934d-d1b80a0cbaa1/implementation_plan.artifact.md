# Plan de Implementación: Folio en Diplomas

Este plan detalla los pasos para generar un folio único por cada diploma y permitir su personalización en el panel de diseño.

## Acción Requerida en Supabase

> [!IMPORTANT]
> Ejecuta este SQL en el editor de Supabase para añadir las columnas necesarias a la tabla `agent_designs`:
>
> ```sql
> ALTER TABLE agent_designs
> ADD COLUMN IF NOT EXISTS dip_folio_x FLOAT,
> ADD COLUMN IF NOT EXISTS dip_folio_y FLOAT,
> ADD COLUMN IF NOT EXISTS dip_folio_sz FLOAT;
> ```

## Cambios Propuestos

### 1. Modelo de Datos
#### [MODIFY] [Models.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/data/model/Models.kt)
- Añadir `dipFolioX`, `dipFolioY` y `dipFolioSz` a `AgentDesign`.

### 2. Interfaz Web (`index.html`)
- **Panel de Diseño**: Añadir controles para el Folio (X, Y, Tamaño) en la pestaña de Diploma.
- **Lógica de Generación**:
    - Crear una función para generar folios únicos (ej. `EHS-DIP-YYYYMMDD-SERIAL`).
    - Actualizar `generateDiploma()` para incluir el folio en el PDF en la posición configurada.
- **Vista Previa**: Permitir arrastrar el cuadro de "Folio" en el canvas de diseño.

### 3. Aplicación Android
#### [MODIFY] [DC3DesignScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/DC3DesignScreen.kt)
- Añadir controles de Folio en la pestaña de diseño de diploma.

#### [MODIFY] [DiplomaGenerator.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/util/DiplomaGenerator.kt)
- Actualizar `generateDiploma` para recibir una cadena de folio y dibujarla según las coordenadas del diseño.

#### [MODIFY] [DC3GenerationScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/DC3GenerationScreen.kt)
- Generar el folio antes de llamar al creador del PDF.

## Verificación
1. **Diseño**: Mover el campo "Folio" en la web y guardar. Verificar que se mantenga la posición.
2. **Generación**: Crear un diploma y confirmar que aparezca el folio (ej. `folio: EHS-20240728-001`).
3. **Consistencia**: Verificar que el folio se genere igual tanto en la App como en la Web.
