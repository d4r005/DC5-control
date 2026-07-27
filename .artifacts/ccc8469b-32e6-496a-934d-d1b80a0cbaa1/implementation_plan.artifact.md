# Plan de Implementación: Corrección de Paneles en Blanco y Errores de Acceso (Web)

Este plan aborda la resolución de los problemas donde los paneles de "Diseño/Diploma", "Agentes Capacitadores" y "Usuarios" aparecen vacíos en la versión web, además de corregir errores de codificación en el archivo principal.

## Problemas Identificados
1.  **Caracteres Corruptos**: El archivo `index.html` contiene símbolos extraños (ej. `ÔÇô`) debido a un error de codificación en ediciones previas, lo que puede causar fallos en la ejecución de JavaScript.
2.  **Paneles Vacíos**: La lógica de carga de datos en `loadSection` parece interrumpirse, probablemente por errores no capturados o fallos en la inicialización de `currentUser`.
3.  **Pestaña Diploma**: El contenido de la pestaña "Diploma" no se muestra correctamente a pesar de estar seleccionado.

## Cambios Propuestos

### Componente: Interfaz Web

#### [MODIFY] [index.html](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/index.html)

1.  **Corrección de Codificación**:
    - Reemplazar todos los símbolos corruptos por sus equivalentes correctos (guiones, acentos, iconos).
    - Asegurar que el archivo se guarde en formato UTF-8 limpio.

2.  **Refactorización de `initApp` y `loadSection`**:
    - Añadir validaciones para asegurar que `currentUser` no sea nulo antes de acceder a sus propiedades.
    - Implementar un flujo de carga más robusto: mostrar la interfaz primero y luego cargar los datos de forma asíncrona sin bloquear toda la aplicación.
    - Asegurar que las tablas de "Agentes" y "Usuarios" muestren mensajes de "Sin datos" o "Cargando" en lugar de quedar totalmente en blanco.

3.  **Mejora de la Pestaña Diploma**:
    - Actualizar el contenido de `design-content-diploma` para mostrar la plantilla actual y eliminar el mensaje de "Próximamente".
    - Verificar que la función `setDesignTab` no tenga duplicados y maneje correctamente los elementos del DOM.

## Verificación

### Manual
1.  Iniciar sesión con `d4r005@gmail.com`.
2.  Navegar al panel de **Usuarios** y verificar que aparezca la lista (Dario y Cynthia).
3.  Navegar al panel de **Agentes** y verificar que aparezca la lista de la base de datos.
4.  Entrar a **Diseño -> Diploma** y confirmar que se ve la vista previa de la plantilla.
5.  Revisar que no haya errores en la consola del navegador (F12).
