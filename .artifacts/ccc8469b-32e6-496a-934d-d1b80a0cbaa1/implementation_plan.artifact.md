# Plan de Restauración Total y Verificación de Filtros

Este plan tiene como objetivo recuperar todas las funcionalidades avanzadas de diplomas y asegurar que el sistema de filtrado por Agente Capacitador funcione correctamente en todas las secciones de la plataforma web.

## Problemas Identificados
1.  **Panel de Diseño de Diploma**: Se perdió la UI interactiva (Tabs, Coordenadas, Folio, Drag & Drop).
2.  **Filtro de Cursos en Modal**: Al generar un DC-3, el selector de cursos muestra todos los cursos de la base de datos en lugar de solo los del agente seleccionado.
3.  **Filtros Globales**: El filtro de la barra superior (Topbar) no está afectando a todas las secciones esperadas.

## Cambios Propuestos

### 1. Restauración del Panel de Diseño (Web)
- **UI**: Re-introducir el sistema de pestañas (DC-3 / Diploma).
- **Controles**: Restaurar los inputs para X, Y y Tamaño del Trabajador, Curso, Fecha, Firma y **Folio**.
- **Plantilla**: Restaurar la opción de "Cambiar Fondo" para el diploma.
- **Interactividad**: Rehabilitar el canvas de vista previa con soporte para arrastrar elementos.

### 2. Sincronización de Filtros por Agente (Web)

#### A. Filtro en Modal de Generación (Personal -> DC-3)
- Modificar `openDC3()` para que la lista de cursos se actualice dinámicamente cuando el usuario cambie el **Agente Capacitador** en el menú desplegable.
- Filtrar `DATA.courses` por el `creatorEmail` del agente elegido.

#### B. Filtro Global (Topbar)
- Asegurar que el filtro "Todos los Agentes" afecte a:
    - **Cursos**: Mostrar solo los cursos del agente seleccionado.
    - **Historial DC-3**: Mostrar solo los registros generados por ese agente.
    - **Personal**: (Opcional, según lógica de negocio) Filtrar trabajadores que tengan registros previos con dicho agente.

### 3. Generación de Diplomas
- Re-activar la lógica de **Folio automático** (`EHS-AAAAMMDD-SERIAL`).
- Asegurar que el PDF use las coordenadas personalizadas guardadas.
- Mantener la corrección de la franja blanca (fondo oscuro + sangrado).

### 4. Sincronización Android
- Actualizar `DesignScreen.kt` para que incluya los campos de Folio.
- Actualizar `DiplomaGenerator.kt` para aceptar el folio y procesarlo en el PDF.

## Verificación de Filtros
| Sección | Filtro Esperado | Estado Actual |
| :--- | :--- | :--- |
| **Personal (Modal)** | Cambiar Agente -> Cambia lista de Cursos | ❌ No funciona |
| **Cursos (Global)** | Seleccionar Agente -> Ver solo sus cursos | ⚠️ Parcial |
| **Historial (Global)** | Seleccionar Agente -> Ver solo sus DC-3 | ⚠️ Parcial |

## Tareas Detalladas
- `[ ]` Restaurar código de Tabs y Canvas en `index.html`.
- `[ ]` Corregir lógica de `onchange` en selectores de Agente en la Web.
- `[ ]` Actualizar `generateDiploma` con lógica de Folio.
- `[ ]` Sincronizar cambios en los archivos de la App Android.

## Verificación Final
1. Entrar a la Web.
2. Seleccionar "Jesus Dario Robles" en el filtro global y ver que la sección de Cursos cambie.
3. Ir a Personal, abrir DC-3 para un trabajador, cambiar el agente y verificar que los cursos se filtren al instante.
4. Generar un Diploma y verificar el folio.
