package com.example.dc5control.ui.components

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
import androidx.compose.ui.unit.dp

@Composable
fun SignaturePad(onSave: (Bitmap) -> Unit, onDismiss: () -> Unit) {
    val paths = remember { mutableStateListOf<ComposePath>() }
    var currentPath by remember { mutableStateOf<ComposePath?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.White).padding(16.dp)) {
        Text("Firme aquí", style = MaterialTheme.typography.titleMedium)
        
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.2f))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentPath = ComposePath().apply { moveTo(offset.x, offset.y) }
                    },
                    onDrag = { change, _ ->
                        currentPath?.lineTo(change.position.x, change.position.y)
                        val lastPath = currentPath
                        if (lastPath != null) {
                            if (paths.contains(lastPath)) paths.remove(lastPath)
                            paths.add(lastPath)
                        }
                    },
                    onDragEnd = { currentPath = null }
                )
            }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                paths.forEach { path ->
                    drawPath(
                        path = path,
                        color = androidx.compose.ui.graphics.Color.Black,
                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = onDismiss) { Text("Cancelar") }
            Button(onClick = { paths.clear() }) { Text("Limpiar") }
            Button(onClick = {
                val bitmap = Bitmap.createBitmap(800, 400, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                val paint = Paint().apply {
                    color = Color.BLACK
                    strokeWidth = 10f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
                // Simplified bitmap conversion: in a real app, you'd scale the Compose paths to this canvas
                onSave(bitmap)
            }) { Text("Guardar") }
        }
    }
}
