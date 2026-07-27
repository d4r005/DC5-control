# Plan de Implementación: Nuevo Panel de Diseño con Pestañas

Este plan detalla los cambios para renombrar el panel "Diseño DC-3" a "Diseño" y añadir soporte para pestañas (DC-3 y Diploma) tanto en Android como en la versión Web.

## Cambios Propuestos

### Componente: Navegación (Android y Web)

#### [MODIFY] [MainActivity.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/MainActivity.kt)
- Renombrar `Screen.DC3Design` a `Screen.Design`.
- Cambiar la etiqueta de navegación de "Diseño DC-3" a "Diseño".

#### [MODIFY] [index.html](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/index.html)
- Cambiar el texto del enlace lateral de "Diseño DC-3" a "Diseño".
- Actualizar el título de la vista.

### Componente: Pantalla de Diseño (Android)

#### [MODIFY] [DC3DesignScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/DC3DesignScreen.kt)
- Renombrar el Composable a `DesignScreen`.
- Implementar un `TabRow` con dos opciones: "DC-3" y "Diploma".
- Mantener el contenido actual bajo la pestaña "DC-3".
- Crear un contenedor vacío o inicial para "Diploma" (futura implementación).

### Componente: Diseño Web

#### [MODIFY] [index.html](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/index.html)
- Añadir una barra de pestañas en la sección `#section-dc3design`.
- Implementar la lógica para alternar entre el diseño de DC-3 y el de Diploma.

## Verificación

### Manual
1. Abrir la aplicación y verificar que el menú diga "Diseño".
2. Entrar a "Diseño" y verificar que aparezcan las pestañas "DC-3" y "Diploma".
3. Confirmar que el contenido de DC-3 se mantenga intacto.
4. Repetir la verificación en la versión web.
