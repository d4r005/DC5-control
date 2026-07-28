# Plan de Implementación: Solución Error de Base de Datos y Plantilla de Diploma

Este plan aborda el error de columna faltante en Supabase y asegura que la funcionalidad de cambiar la plantilla del diploma funcione correctamente en todas las plataformas.

## Acción Requerida en Supabase

> [!IMPORTANT]
> Para corregir el error que ves en pantalla, es **indispensable** añadir la columna a tu tabla de Supabase. Copia y pega el siguiente código en el **SQL Editor** de tu Dashboard de Supabase y presiona "Run":
>
> ```sql
> ALTER TABLE agent_designs
> ADD COLUMN IF NOT EXISTS diploma_template_base64 TEXT;
> ```

## Cambios Propuestos

### 1. Interfaz Web (`index.html`)
- **Robustez al Guardar**: Modificar `saveDesign()` para que, si falla el guardado en la base de datos (por falta de la columna), guarde los datos localmente en el navegador como respaldo y no bloquee al usuario.
- **Detección de Errores**: Mejorar el mensaje de error para guiar al usuario sobre la falta de la columna en la BD.

### 2. Aplicación Android (`DC3DesignScreen.kt`)
- Asegurar que el guardado también sea resiliente a errores de esquema de base de datos.
- Sincronizar el nombre de los campos con la base de datos.

## Tareas Detalladas

- `[ ]` Modificar `index.html` para un guardado "seguro" con respaldo en `localStorage`.
- `[ ]` Actualizar `DC3DesignScreen.kt` para manejar errores de red o esquema al guardar el diseño.
- `[ ]` Verificar sincronización de nombres de columnas.

## Verificación
1. **Sin la columna**: El sistema debería permitir guardar (avisando que solo se guardó localmente) y generar el diploma con la imagen subida.
2. **Con la columna**: Una vez ejecutado el SQL, el sistema debería guardar permanentemente en la nube y sincronizar con otros dispositivos.
