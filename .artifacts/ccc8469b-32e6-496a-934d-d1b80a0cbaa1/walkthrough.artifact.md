# Walkthrough: Personalización Dinámica de Diplomas

Se ha implementado la funcionalidad para cambiar la plantilla de fondo de los diplomas tanto en la aplicación móvil como en la plataforma web.

## Cambios Realizados

### 1. Gestión de Plantillas
- **Subida a GitHub**: Se ha sincronizado la versión más reciente de `plantilla_diploma.png` para que Cloudflare y la App tengan la base actualizada.
- **Plantilla Dinámica**: Ahora puedes subir una imagen propia desde el panel de **Diseño**. Esta imagen se guarda en la base de datos vinculada a tu usuario.

### 2. Plataforma Web
- **Panel de Diseño**: En la pestaña **Diploma**, se añadió el botón "📷 Cambiar Fondo".
- **Vista Previa**: Al subir una imagen, la vista previa se actualiza instantáneamente para mostrar cómo quedarán los textos sobre tu nueva plantilla.
- **Generación**: Al generar un diploma, el sistema verifica si tienes una plantilla personalizada; si no, usa la de EHS por defecto.

### 3. Aplicación Android
- **Selector de Imagen**: Se habilitó la carga de archivos en el panel de Diseño para Android.
- **Generador PDF**: El motor de PDF (`DiplomaGenerator.kt`) ahora acepta y procesa el fondo personalizado subido por el usuario.

## Cómo Usarlo
1. Entra a **Diseño**.
2. Cambia a la pestaña **Diploma**.
3. Presiona **Cambiar Fondo** y selecciona tu imagen.
4. Presiona **Guardar Todo el Diseño**.
5. Al generar cualquier diploma nuevo, verás tu fondo aplicado.

> [!TIP]
> Para mejores resultados, utiliza imágenes en formato PNG con una resolución de aproximadamente 3300x2550 píxeles (Tamaño Carta Horizontal).
