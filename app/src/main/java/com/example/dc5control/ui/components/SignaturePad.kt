package com.example.dc5control.ui.components

import androidx.compose.ui.graphics.Color
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path as ComposePath
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

@Composable
fun SignaturePad(onSave: (Bitmap) -> Unit, onDismiss: () -> Unit) {
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.White).padding(16.dp)) {
        Text("Firme aquí", style = MaterialTheme.typography.titleMedium)
        
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.2f))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val newPath = Path().apply { moveTo(offset.x, offset.y) }
                        paths.add(newPath)
                        currentPath = newPath
                    },
                    onDrag = { change, _ ->
                        currentPath?.lineTo(change.position.x, change.position.y)
                        // Trigger recomposition
                        val temp = currentPath
                        currentPath = null
                        currentPath = temp
                    },
                    onDragEnd = { currentPath = null }
                )
            }
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                paths.forEach { path ->
                    drawContext.canvas.nativeCanvas.drawPath(
                        path,
                        Paint().apply {
                            color = Color.BLACK
                            strokeWidth = 6f
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                            isAntiAlias = true
                        }
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = onDismiss) { Text("Cancelar") }
            Button(onClick = { paths.clear() }) { Text("Limpiar") }
            Button(onClick = {
                if (paths.isEmpty()) return@Button
                val bitmap = Bitmap.createBitmap(800, 400, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.TRANSPARENT)
                val paint = Paint().apply {
                    color = Color.BLUE // Firma en azul como en el ejemplo
                    strokeWidth = 8f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    isAntiAlias = true
                }
                
                // Escalar y dibujar rutas
                // En un caso real buscaríamos los límites, aquí simplificamos asumiendo que el usuario firmó en el centro
                paths.forEach { path ->
                    canvas.drawPath(path, paint)
                }
                onSave(bitmap)
            }) { Text("Guardar") }
        }
    }
}
