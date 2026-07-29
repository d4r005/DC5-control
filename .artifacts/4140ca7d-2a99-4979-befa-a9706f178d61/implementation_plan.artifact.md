# Plan de Solución: Error de Columnas en Supabase (dip_agent_sz)

El error `Could not find the 'dip_agent_sz' column of 'agent_designs' in the schema cache` indica que la aplicación web está intentando guardar o cargar campos que no existen en la tabla `agent_designs` de la base de datos Supabase.

Esto ocurre porque se han añadido nuevas funcionalidades de personalización de diplomas en el frontend (web) que aún no tienen soporte en la estructura de la base de datos ni en el modelo de datos de Android.

## Cambios Propuestos

### 1. Alineación de Modelos (Android)
Actualizar el data class `AgentDesign` en Android para incluir todos los campos de coordenadas y tamaños que utiliza la versión web. Esto garantiza que cuando Android descargue o suba configuraciones, no haya pérdida de datos ni errores de serialización.

#### [MODIFY] [Models.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/data/model/Models.kt)
Añadir los campos faltantes:
- Coordenadas de Slogan y Logo de cabecera.
- Coordenadas y tamaños para el Diploma (Trabajador, Curso, Duración, Fecha, Agente, STPS).

### 2. Actualización de Base de Datos (Supabase)
Proporcionar un script SQL para añadir las columnas faltantes a la tabla `agent_designs`.

## Plan de Verificación

### Verificación Automatizada
1. Compilar el proyecto Android para asegurar que el modelo `AgentDesign` es válido con los nuevos campos.
2. Ejecutar un `find` en el código Android para asegurar que la serialización de KotlinX funciona correctamente.

### Verificación Manual
1. Solicitar al usuario que ejecute el SQL en su panel de Supabase.
2. Verificar en la web (`ace-control.pages.dev`) que al guardar el diseño ya no aparece el error de columna faltante.

---

## SQL de Actualización para Supabase

> [!IMPORTANT]
> El usuario debe ejecutar este código en el **SQL Editor** de su proyecto en Supabase para solucionar el error.

```sql
ALTER TABLE agent_designs
ADD COLUMN IF NOT EXISTS header_slogan_x FLOAT8 DEFAULT 306,
ADD COLUMN IF NOT EXISTS header_slogan_y FLOAT8 DEFAULT 18,
ADD COLUMN IF NOT EXISTS header_slogan_size FLOAT8 DEFAULT 9,
ADD COLUMN IF NOT EXISTS header_slogan_font TEXT DEFAULT 'Times-Italic',
ADD COLUMN IF NOT EXISTS header_logo_w FLOAT8 DEFAULT 120,
ADD COLUMN IF NOT EXISTS header_logo_h FLOAT8 DEFAULT 55,
ADD COLUMN IF NOT EXISTS header_logo_x FLOAT8 DEFAULT 30,
ADD COLUMN IF NOT EXISTS header_logo_y FLOAT8 DEFAULT 10,
ADD COLUMN IF NOT EXISTS dip_worker_x FLOAT8 DEFAULT 396,
ADD COLUMN IF NOT EXISTS dip_worker_y FLOAT8 DEFAULT 245,
ADD COLUMN IF NOT EXISTS dip_worker_sz FLOAT8 DEFAULT 28,
ADD COLUMN IF NOT EXISTS dip_course_x FLOAT8 DEFAULT 396,
ADD COLUMN IF NOT EXISTS dip_course_y FLOAT8 DEFAULT 330,
ADD COLUMN IF NOT EXISTS dip_course_sz FLOAT8 DEFAULT 18,
ADD COLUMN IF NOT EXISTS dip_duration_x FLOAT8 DEFAULT 396,
ADD COLUMN IF NOT EXISTS dip_duration_y FLOAT8 DEFAULT 405,
ADD COLUMN IF NOT EXISTS dip_duration_sz FLOAT8 DEFAULT 12,
ADD COLUMN IF NOT EXISTS dip_date_x FLOAT8 DEFAULT 396,
ADD COLUMN IF NOT EXISTS dip_date_y FLOAT8 DEFAULT 445,
ADD COLUMN IF NOT EXISTS dip_date_sz FLOAT8 DEFAULT 11,
ADD COLUMN IF NOT EXISTS dip_agent_x FLOAT8 DEFAULT 396,
ADD COLUMN IF NOT EXISTS dip_agent_y FLOAT8 DEFAULT 572,
ADD COLUMN IF NOT EXISTS dip_agent_sz FLOAT8 DEFAULT 10,
ADD COLUMN IF NOT EXISTS dip_stps_x FLOAT8 DEFAULT 396,
ADD COLUMN IF NOT EXISTS dip_stps_y FLOAT8 DEFAULT 584,
ADD COLUMN IF NOT EXISTS dip_stps_sz FLOAT8 DEFAULT 10;
```
