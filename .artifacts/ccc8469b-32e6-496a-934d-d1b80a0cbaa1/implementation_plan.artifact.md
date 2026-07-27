# Plan de Implementación: Generación de Diplomas

Este plan detalla los pasos para implementar la generación de diplomas utilizando la nueva plantilla proporcionada, personalizando los datos para el agente capacitador **Jesus Dario Robles Trujillo**.

## Requisitos Previos

> [!IMPORTANT]
> Es necesario que guardes la imagen de la plantilla del diploma en la carpeta del proyecto: `app/src/main/assets/plantilla_diploma.jpg` para que el sistema pueda utilizarla como fondo.

## Cambios Propuestos

### 1. Utilidades de Generación

#### [NEW] [DiplomaGenerator.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/util/DiplomaGenerator.kt)
- Crear un nuevo generador que cree un PDF en formato horizontal (Landscape).
- Cargar `plantilla_diploma.jpg` como imagen de fondo.
- Dibujar un rectángulo blanco sobre la sección de firma actual para "limpiar" los datos de Cynthia.
- Escribir los datos dinámicos:
    - **Nombre del Trabajador**: Centrado, tamaño grande.
    - **Curso**: Centrado, debajo del nombre.
    - **Duración y Fecha**: Centrado, formato "CON DURACIÓN DE X HORAS DEL DD DE MM DEL AAAA".
    - **Datos del Agente (Dario)**: Al pie, centrado, sin cédula profesional.

### 2. Interfaz de Usuario (Android)

#### [MODIFY] [DC3GenerationScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/DC3GenerationScreen.kt)
- Añadir un nuevo botón "Generar Diploma" en la sección de acciones.
- Implementar la lógica para llamar a `DiplomaGenerator.generateDiploma`.
- Asegurar que al generar el diploma para Dario Robles, se use el formato solicitado (sin cédula).

### 3. Sincronización Web

#### [MODIFY] [index.html](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/index.html)
- Añadir un botón similar en la interfaz web para previsualizar/descargar el diploma.
- Implementar la lógica de dibujo en el canvas o PDF-lib para la versión web.

## Tareas Detalladas

- `[ ]` Crear `DiplomaGenerator.kt` con coordenadas estimadas para los campos.
- `[ ]` Modificar `DC3GenerationScreen.kt` para integrar el botón.
- `[ ]` Actualizar `index.html` con la nueva plantilla.

## Verificación

### Manual
1. Abrir la app y seleccionar a Dario Robles como Agente.
2. Seleccionar un trabajador y un curso.
3. Presionar "Generar Diploma".
4. Verificar que el PDF resultante tenga el fondo de la imagen, el nombre del trabajador correcto y que los datos de firma correspondan a Dario (sin cédula).
