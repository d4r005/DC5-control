package com.example.dc5control.util

import com.example.dc5control.data.model.Course
import com.example.dc5control.data.repository.SupabaseRepository

object CourseDefaults {
    val defaultCourses = listOf(
        Course(name = "SEGURIDAD EN TRABAJOS DE SOLDADURAS Y OXICORTE", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "001", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "MANEJO SEGURO DE MONTACARGAS", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "002", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "FORMACION DE BRIGADAS DE EMERGENCIA (EVACUACION, BUSQUEDA Y RESCATE, PRIMEROS AUXILIOS Y MANEJO DE EXTINTORES)", durationHours = "24", thematicArea = "(6000) SEGURIDAD", stpsId = "003", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "SEGURIDAD EN EL USO Y MANEJO DE MAQUINARIA PESADA EN CONSTRUCCION", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "004", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "DISEÑO Y EVALUACION DE SIMULACROS PARA EVACUACION O REPLIEGUE EN SINIESTROS", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "005", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "FORMACION DE SUPERVISORES DE SEGURIDAD Y SALUD OCUPACIONAL", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "006", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "TALLER DE CORTE Y BARBERIA PROFESIONAL", durationHours = "24", thematicArea = "(2000) SERVICIOS", stpsId = "007", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "TALLER DE MAQUILLAJE PROFESIONAL DIA, TARDE Y NOCHE", durationHours = "24", thematicArea = "(2000) SERVICIOS", stpsId = "008", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "FORMACION DE INSTRUCTORES", durationHours = "8", thematicArea = "(7000) DESARROLLO PERSONAL Y FAMILIAR", stpsId = "009", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "MEDIDAS DE SEGURIDAD EN TRABAJOS DE ALTURA (NOM-009-STPS-2011)", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "010", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "SEGURIDAD INDUSTRIAL EN LA CONSTRUCCION (NOM-031-STPS-2011)", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "011", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "OPERACION SEGURA DE EQUIPOS DE ELEVACION, MANLIFT Y PLATAFORMAS", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "012", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "TRABAJOS EN ESPACIOS CONFINADOS", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "013", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "OPERACION SEGURA DE ENGANCHE Y DESENGANCHE 5 RUEDA", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "014", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "FORMACION DE TECNICOS EN URGENCIAS MEDICAS NIVEL 1 (BASICO)", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "015", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "ASEGURAMIENTO DE ENERGIA (BLOQUEO Y ETIQUETADO LOTO)", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "016", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "MANEJO DE MATERIALES PELIGROSOS", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "017", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "OPERACION SEGURA DE VEHICULOS PESADOS 5 RUEDA", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "018", creatorEmail = "lugga.advisors@gmail.com"),
        Course(name = "FORMACION DE TECNICOS EN INHALOTERAPIA", durationHours = "24", thematicArea = "(6000) SEGURIDAD", stpsId = "019", creatorEmail = "lugga.advisors@gmail.com"),
        
        // Cursos de Dario Robles
        Course(name = "SEGURIDAD EN TRABAJOS EN ALTURAS", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "001", creatorEmail = "d4r005@gmail.com"),
        Course(name = "SEGURIDAD EN TRABAJOS DE SOLDADURA Y OXICORTE", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "002", creatorEmail = "d4r005@gmail.com"),
        Course(name = "FORMACION DE BRIGADAS DE EMERGENCIA (EVACUACION, BUSQUEDA Y RESCATE , CONTRA INCENDIOS, PRIMEROS AUXILIOS )", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "003", creatorEmail = "d4r005@gmail.com"),
        Course(name = "SEGURIDAD EN ESPACIOS CONFINADOS", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "004", creatorEmail = "d4r005@gmail.com"),
        Course(name = "ASEGURAMIENTO DE ENERGIA (LOTO)", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "005", creatorEmail = "d4r005@gmail.com"),
        Course(name = "FORMACION DE INSTRUCTORES", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "006", creatorEmail = "d4r005@gmail.com"),
        Course(name = "FORMACION DE SUPERVISORES DE SEGURIDAD Y SALUD OCUPACIONAL", durationHours = "8", thematicArea = "(6000) SEGURIDAD", stpsId = "007", creatorEmail = "d4r005@gmail.com")
    )

    private val keywordsMap = mapOf(
        "SOLDADURA" to "SEGURIDAD EN TRABAJOS DE SOLDADURA Y OXICORTE",
        "MONTACARGAS" to "MANEJO SEGURO DE MONTACARGAS",
        "BRIGADAS" to "FORMACION DE BRIGADAS DE EMERGENCIA (EVACUACION, BUSQUEDA Y RESCATE , CONTRA INCENDIOS, PRIMEROS AUXILIOS )",
        "MAQUINARIA PESADA" to "SEGURIDAD EN EL USO Y MANEJO DE MAQUINARIA PESADA EN CONSTRUCCION",
        "SIMULACROS" to "DISEÑO Y EVALUACION DE SIMULACROS PARA EVACUACION O REPLIEGUE EN SINIESTROS",
        "SUPERVISORES" to "FORMACION DE SUPERVISORES DE SEGURIDAD Y SALUD OCUPACIONAL",
        "ALTURA" to "SEGURIDAD EN TRABAJOS EN ALTURAS",
        "NOM-031" to "SEGURIDAD INDUSTRIAL EN LA CONSTRUCCION (NOM-031-STPS-2011)",
        "CONSTRUCCIÓN" to "SEGURIDAD INDUSTRIAL EN LA CONSTRUCCION (NOM-031-STPS-2011)",
        "CONSTRUCCION" to "SEGURIDAD INDUSTRIAL EN LA CONSTRUCCION (NOM-031-STPS-2011)",
        "INSTRUCTORES" to "FORMACION DE INSTRUCTORES",
        "CONFINADOS" to "SEGURIDAD EN ESPACIOS CONFINADOS",
        "LOTO" to "ASEGURAMIENTO DE ENERGIA (LOTO)",
        "MATERIALES PELIGROSOS" to "MANEJO DE MATERIALES PELIGROSOS",
        "5 RUEDA" to "OPERACION SEGURA DE VEHICULOS PESADOS 5 RUEDA",
        "CORTE" to "TALLER DE CORTE Y BARBERIA PROFESIONAL",
        "BARBERIA" to "TALLER DE CORTE Y BARBERIA PROFESIONAL",
        "MAQUILLAJE" to "TALLER DE MAQUILLAJE PROFESIONAL DIA, TARDE Y NOCHE",
        "MANLIFT" to "OPERACION SEGURA DE EQUIPOS DE ELEVACION, MANLIFT Y PLATAFORMAS",
        "URGENCIAS MEDICAS" to "FORMACION DE TECNICOS EN URGENCIAS MEDICAS NIVEL 1 (BASICO)",
        "VEHICULOS PESADOS" to "OPERACION SEGURA DE VEHICULOS PESADOS 5 RUEDA",
        "INHALOTERAPIA" to "FORMACION DE TECNICOS EN INHALOTERAPIA"
    )

    fun cleanupDatabase(dbCourses: List<Course>, onComplete: () -> Unit) {
        val processedOfficialNames = mutableSetOf<String>()
        var pendingActions = 0

        dbCourses.forEach { db ->
            val dbNameUpper = db.name.uppercase()
            // Buscamos si el nombre en la DB contiene alguna de nuestras palabras clave
            val matchedOfficialName = keywordsMap.entries.find { dbNameUpper.contains(it.key.uppercase()) }?.value
            val officialTemplate = defaultCourses.find { it.name == matchedOfficialName }

            if (officialTemplate != null) {
                if (processedOfficialNames.contains(officialTemplate.name)) {
                    // Ya procesamos este curso oficial, eliminar el duplicado corto de la DB
                    db.id?.let { id ->
                        pendingActions++
                        SupabaseRepository.deleteData("courses", id) { 
                            pendingActions--
                            if(pendingActions <= 0) onComplete() 
                        }
                    }
                } else {
                    // Si el nombre no es idéntico al oficial, actualizarlo en la DB
                    if (db.name != officialTemplate.name || db.durationHours != officialTemplate.durationHours) {
                        db.id?.let { id ->
                            pendingActions++
                            val updated = officialTemplate.copy(id = id, creatorEmail = db.creatorEmail)
                            SupabaseRepository.updateData("courses", id, updated, Course.serializer()) {
                                pendingActions--
                                if(pendingActions <= 0) onComplete()
                            }
                        }
                    }
                    processedOfficialNames.add(officialTemplate.name)
                }
            }
        }
        if (pendingActions <= 0) onComplete()
    }

    fun mergeWithDefaults(dbCourses: List<Course>, userEmail: String?): List<Course> {
        // Empezamos con los cursos oficiales (Dario y Cynthia)
        val result = defaultCourses.map { it.copy(creatorEmail = userEmail) }.toMutableList()
        
        dbCourses.forEach { db ->
            val dbNameUpper = db.name.uppercase()
            // Un curso es oficial si su nombre está en defaultCourses o si coincide con keywordsMap
            val isOfficial = defaultCourses.any { it.name.equals(db.name, ignoreCase = true) } ||
                             keywordsMap.entries.any { dbNameUpper.contains(it.key.uppercase()) }
            
            if (!isOfficial) {
                // Solo agregar si no es un curso oficial y no está ya en la lista
                if (result.none { it.name.equals(db.name, ignoreCase = true) }) {
                    result.add(db)
                }
            }
        }
        return result
    }
}
