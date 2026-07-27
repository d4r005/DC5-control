# Plantilla DC-3 Oficial STPS

Coloca aquí el archivo `plantilla_dc3.pdf` — la plantilla oficial del formato DC-3 de la STPS.

El `PdfGenerator` carga automáticamente este archivo desde los assets de la app
y escribe sobre él: nombre del trabajador, CURP, RFC, puesto, empresa, curso,
duración, fechas, agente capacitador y firma.

## Si no existe la plantilla

Si no se encuentra el archivo, `PdfGenerator` crea un PDF A4 en blanco
automáticamente para que la app no crashee, pero sin el formato oficial STPS.

## Posiciones de texto

Las coordenadas (X, Y) en `PdfGenerator.kt` están calibradas para una plantilla
A4 estándar. Ajusta los valores de `newLineAtOffset()` según tu plantilla específica.
