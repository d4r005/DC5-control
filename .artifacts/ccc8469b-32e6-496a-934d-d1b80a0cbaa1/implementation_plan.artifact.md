# Plan de Implementación: Cursos Nuevos y Filtrado por Agente

Este plan detalla la adición de nuevos cursos oficiales asociados al Agente Capacitador **Dario Robles** (`STPS-ROTJ920320-IP4`) y la implementación de filtrado por agente en los paneles correspondientes tanto en Android como en la versión Web.

## Cambios Propuestos

### 1. Actualización de Datos (Android y Web)

#### [MODIFY] [CourseDefaults.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/util/CourseDefaults.kt)
Añadir los nuevos cursos a la lista `defaultCourses` con su respectivo `stpsId` y `creatorEmail` ("d4r005@gmail.com"):
1. SEGURIDAD EN TRABAJOS EN ALTURAS (8h, Seguridad, 001)
2. SEGURIDAD EN TRABAJOS DE SOLDADURA Y OXICORTE (8h, Seguridad, 002)
3. FORMACION DE BRIGADAS DE EMERGENCIA (EVACUACION, BUSQUEDA Y RESCATE , CONTRA INCENDIOS, PRIMEROS AUXILIOS ) (8h, Seguridad, 003)
4. SEGURIDAD EN ESPACIOS CONFINADOS (8h, Seguridad, 004)
5. ASEGURAMIENTO DE ENERGIA (LOTO) (8h, Seguridad, 005)
6. FORMACION DE INSTRUCTORES (8h, Seguridad, 006)
7. FORMACION DE SUPERVISORES DE SEGURIDAD Y SALUD OCUPACIONAL (8h, Seguridad, 007)

#### [MODIFY] [index.html](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/index.html)
Actualizar el arreglo `courses` en la versión web para incluir estos mismos cursos.

### 2. Filtrado por Agente (Android)

#### [MODIFY] [CourseListScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/CourseListScreen.kt)
- Añadir un selector de Agente Capacitador en la parte superior.
- Filtrar la lista de cursos mostrada según el agente seleccionado.

#### [MODIFY] [DC3HistoryScreen.kt](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/app/src/main/java/com/example/dc5control/ui/DC3HistoryScreen.kt)
- Añadir filtro por Agente Capacitador para facilitar la búsqueda de constancias específicas.

### 3. Filtrado por Agente (Web)

#### [MODIFY] [index.html](file:///C:/Users/dtruj/AndroidStudioProjects/DC5-control/index.html)
- Agregar un `<select>` en la vista de Cursos y en la de Historial para filtrar por agente.

## Verificación

### Manual
1. Abrir el panel de Cursos y verificar que aparezcan los 7 nuevos cursos de Dario Robles.
2. Cambiar el filtro de agente y confirmar que la lista se actualiza correctamente.
3. Generar un DC-3 con uno de los nuevos cursos y verificar que la clave STPS termine con el sufijo correcto (ej. -001, -002).
