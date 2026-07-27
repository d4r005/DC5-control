# Plan de Mejora de Filtrado y Nombres (Android)

Este plan aborda las solicitudes de filtrado preciso por agente y la corrección del formato del nombre del Agente Capacitador.

## Problemas Identificados
1.  **Nombre Desordenado**: La normalización anterior ordenaba las palabras alfabéticamente ("Robles Trujillo Jesus Dario" en lugar de "Jesus Dario Robles Trujillo").
2.  **Filtrado en DC-3**: El diálogo de generación de DC-3 muestra todos los cursos sin importar qué agente se seleccione.
3.  **Duplicados**: Se siguen viendo variaciones del nombre en los filtros si no se manejan bien los datos de la base de datos.

## Cambios Propuestos

### Componente: Interfaz de Usuario (Android)

#### [MODIFY] [CourseListScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/CourseListScreen.kt)
-   Simplificar la normalización de agentes para que NO ordene las palabras.
-   Asegurar que el filtro superior use el `creatorEmail` para mostrar solo los cursos del agente seleccionado.

#### [MODIFY] [DC3HistoryScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/DC3HistoryScreen.kt)
-   Aplicar la misma normalización de nombres sin ordenamiento alfabético.

#### [MODIFY] [DC3GenerationScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/DC3GenerationScreen.kt)
-   Implementar el filtrado de cursos dinámico: al seleccionar un agente, la lista de cursos se filtrará automáticamente por el `creatorEmail` de dicho agente.
-   Corregir la normalización en la carga inicial de agentes.

### Componente: Web

#### [MODIFY] [index.html](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/index.html)
-   Actualizar la función `norm` para que no ordene las palabras, permitiendo que el nombre aparezca como el usuario prefiera.

## Verificación

### Manual
1.  Abrir la pantalla de Cursos y verificar que el nombre aparezca como "Jesus Dario Robles Trujillo".
2.  Filtrar por Dario y confirmar que solo se ven sus cursos.
3.  Abrir el diálogo de Generar DC-3, seleccionar a Dario y confirmar que en el menú de cursos solo aparecen los 7 cursos oficiales de Dario.
