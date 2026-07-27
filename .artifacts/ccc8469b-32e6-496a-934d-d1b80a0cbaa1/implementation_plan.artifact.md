# Plan de Corrección: Claves de Registro STPS por Curso

Este plan detalla los cambios necesarios para que la clave de registro del Agente Capacitador (STPS) cambie dinámicamente según el curso seleccionado en el formato DC-3, corrigiendo además el error de doble prefijo "STPS-STPS-".

## Problemas Identificados

1.  **Sufijo Incorrecto:** Actualmente se usa una clave fija para el agente (ej. terminada en `-005`) sin importar el curso. Según la normativa, cada curso tiene un número de registro específico (ej. `-002` para Montacargas).
2.  **Doble Prefijo:** El generador de PDF añade `"STPS-"` al principio de una cadena que ya contiene `"STPS-"`, resultando en `"STPS-STPS-..."`.

## Cambios Propuestos

### Componente: Modelos de Datos

#### [MODIFY] [Models.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/data/model/Models.kt)
- Añadir el campo opcional `stpsId: String? = null` a la clase `Course` para almacenar el sufijo de registro (ej. "001", "002").

### Componente: Datos Predeterminados

#### [MODIFY] [CourseDefaults.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/util/CourseDefaults.kt)
- Actualizar la lista `defaultCourses` para incluir los sufijos conocidos:
    - MANEJO SEGURO DE MONTACARGAS -> `"002"`
    - DISEÑO Y EVALUACION DE SIMULACROS... -> `"005"`
    - (Se asignarán otros sufijos de manera secuencial para completar la lista oficial de la instructora).

### Componente: Interfaz de Usuario

#### [MODIFY] [DC3GenerationScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/DC3GenerationScreen.kt)
- Implementar la lógica para calcular la "Clave Final":
    - Si el curso tiene un `stpsId`, se reemplaza el último segmento de la clave del agente (ej. `...-005` por `...-002`).
    - Si no tiene, se usa la clave del agente tal cual.
- Asegurar que la clave no tenga el prefijo duplicado antes de enviarla al generador.

### Componente: Utilidades

#### [MODIFY] [PdfGenerator.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/util/PdfGenerator.kt)
- Eliminar el prefijo estático `"STPS-"` en la línea 243 para evitar la duplicidad. La cadena recibida ya debe estar formateada correctamente.

## Plan de Verificación

### Verificación Manual
1. Generar un DC-3 para "MANEJO SEGURO DE MONTACARGAS":
    - Verificar que la clave termine en `-002`.
    - Verificar que no diga `"STPS-STPS-"`.
2. Generar un DC-3 para "DISEÑO Y EVALUACIÓN DE SIMULACROS...":
    - Verificar que la clave termine en `-005`.
