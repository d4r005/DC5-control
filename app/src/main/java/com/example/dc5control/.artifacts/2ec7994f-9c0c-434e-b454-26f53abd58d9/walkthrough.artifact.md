# Unificación de Plantillas a Formato PDF

Se ha migrado la lógica de los Diplomas para soportar el formato PDF como base, igualando la calidad y el flujo de trabajo de las constancias DC-3.

## Cambios Clave

### Web Interface
- **Selector Universal**: El input de Diploma ahora acepta tanto imágenes como archivos PDF.
- **Renderizado Dinámico**: La vista previa del diseño ahora detecta si la plantilla es un PDF y la renderiza usando `pdf.js`.
- **Generación Vectorial**: Los diplomas generados ahora pueden usar un PDF base cargado por el usuario, permitiendo que los textos y el QR se dibujen con precisión sobre un formato profesional.

### Android App
- **Generador Híbrido**: `DiplomaGenerator` ahora es capaz de descargar y "rellenar" un PDF remoto desde Supabase. Mantiene compatibilidad con la imagen local (fallback) si no hay una plantilla personalizada.
- **Subida Inteligente**: Al guardar el diseño en la App, el sistema detecta el tipo de archivo y lo nombra correctamente en el Storage (`diploma_base.pdf` o `.png`).

## Instrucciones para el Usuario

1.  **Prepárate**: Exporta tu plantilla de diploma actual a formato PDF.
2.  **Sube**: En la pantalla de **Diseño -> Diploma**, selecciona tu nuevo archivo PDF.
3.  **Guarda**: Presiona "Guardar diseño". Verás que la URL se actualiza en la base de datos.
4.  **Verifica**: Genera un diploma para cualquier trabajador y notarás la mejora en la nitidez de los bordes y textos del fondo.

> [!TIP]
> Si prefieres seguir usando una imagen PNG, el sistema sigue siendo compatible, pero recomendamos el cambio a PDF para una calidad de impresión óptima.
