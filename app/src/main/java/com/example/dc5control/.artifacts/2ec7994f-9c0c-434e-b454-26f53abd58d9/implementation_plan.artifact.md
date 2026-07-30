# Plan de Implementación: Sistema de Verificación vía Código QR

Este plan describe la integración de códigos QR en los documentos DC-3 y Diplomas, vinculados a un sistema de verificación pública que permite validar la autenticidad de la constancia.

## User Review Required

> [!IMPORTANT]
> **Cambio en Base de Datos:**
> Se requiere añadir 6 columnas a la tabla `agent_designs` para almacenar la posición del QR en ambos formatos. El script SQL se incluye al final.

> [!NOTE]
> **URL de Verificación:**
> El código QR apuntará a `https://ace-control.pages.dev/?v=[ID_REGISTRO]`. Al escanearlo, cualquier persona podrá ver los datos de la constancia sin necesidad de iniciar sesión.

## Proposed Changes

### 1. Librerías de Generación de QR
- **Web:** Añadir `qrcode.js` vía CDN en `index.html`.
- **Android:** Añadir dependencia `com.google.zxing:core` o similar para generar el bitmap del QR.

### 2. Base de Datos (Supabase)
Añadir soporte para posicionar el QR en los diseños:
- `qr_x`, `qr_y`, `qr_sz` (para DC-3).
- `dip_qr_x`, `dip_qr_y`, `dip_qr_sz` (para Diploma).

### 3. Personalización del Diseño (Web y Android)
- **UI:** Añadir controles de coordenadas y tamaño para el QR en las pestañas "DC-3" e "Investigación".
- **Preview Web:** Añadir un recuadro de arrastre (drag handle) para posicionar el QR visualmente.

### 4. Generación de Documentos (PDF)
- **Lógica:** Al generar el PDF, obtener el ID del registro creado en Supabase, generar el QR con la URL de verificación y dibujarlo en las coordenadas configuradas.

### 5. Pantalla de Verificación Pública (Web)
- **Detección:** Al cargar `index.html`, detectar si existe el parámetro `v` en la URL (ej. `?v=uuid`).
- **Interfaz:** Si existe el parámetro, mostrar una tarjeta elegante (similar a la imagen de referencia) con:
    - Estado: "Constancia Válida" (check verde).
    - Datos: Capacitador, Empleado, CURP, Empresa, Puesto, Curso, Duración y Fechas.
    - Botón para cerrar y volver al login.

## Plan de Verificación

### Manual Verification
1.  **Diseño:** Mover el QR en la pantalla de diseño y guardar.
2.  **Generación:** Crear una constancia y verificar que el QR aparezca en el PDF.
3.  **Escaneo:** Escanear el QR con un celular. Debe abrir la página de verificación mostrando los datos exactos del trabajador.
4.  **Privacidad:** Confirmar que no se requiere login para ver la página de verificación, pero que solo se puede acceder con un ID válido.

---

## SQL de Actualización para Supabase

```sql
ALTER TABLE agent_designs
ADD COLUMN IF NOT EXISTS qr_x FLOAT8 DEFAULT 480,
ADD COLUMN IF NOT EXISTS qr_y FLOAT8 DEFAULT 60,
ADD COLUMN IF NOT EXISTS qr_sz FLOAT8 DEFAULT 60,
ADD COLUMN IF NOT EXISTS dip_qr_x FLOAT8 DEFAULT 680,
ADD COLUMN IF NOT EXISTS dip_qr_y FLOAT8 DEFAULT 500,
ADD COLUMN IF NOT EXISTS dip_qr_sz FLOAT8 DEFAULT 50;
```
