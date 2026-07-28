# Plan de Implementación: Personalización de Plantilla de Diploma

Este plan detalla los pasos para permitir que el usuario cambie la plantilla del diploma desde la Web y la App Android, además de asegurar que los cambios actuales lleguen al servidor.

## Tareas Iniciales
- `[ ]` Commit y Push del cambio actual en `plantilla_diploma.png` para que Cloudflare se actualice.

## Cambios Propuestos

### 1. Modelo de Datos
#### [MODIFY] [Models.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/data/model/Models.kt)
- Añadir campo `diploma_template_base64` a la data class `AgentDesign`.

### 2. Interfaz Web
#### [MODIFY] [index.html](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/index.html)
- En la pestaña de **Diseño -> Diploma**, añadir un botón para subir una nueva imagen de plantilla.
- Guardar la imagen como Base64 en la tabla `agent_designs` de Supabase.
- Actualizar la función `generateDiploma` para:
    - Intentar cargar la plantilla personalizada desde el diseño del agente.
    - Si no existe, usar la plantilla por defecto (`plantilla_diploma.png`).

### 3. Interfaz Android
#### [MODIFY] [DC3DesignScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/DC3DesignScreen.kt)
- Añadir un botón "Subir nueva plantilla" en la pestaña de Diploma.
- Implementar el selector de archivos para capturar la imagen.
- Guardar en Supabase.

### 4. Generador de PDF (Android)
#### [MODIFY] [DiplomaGenerator.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/util/DiplomaGenerator.kt)
- Modificar `generateDiploma` para recibir opcionalmente un Bitmap de la plantilla personalizada.
- Cargar dicho Bitmap si está presente; de lo contrario, cargar el asset por defecto.

## Verificación
1. **Web**: Subir una imagen de prueba como plantilla y generar un diploma para verificar que se use la nueva imagen.
2. **Android**: Repetir la prueba desde la APK.
3. **Cloudflare**: Confirmar que la URL pública muestra la última versión de la plantilla base subida por el usuario.
