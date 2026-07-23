package com.example.dc5control.ui.components

// ── Android graphics (para Bitmap, Canvas nativo, Paint, Path) ──
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath

// ── Compose ──
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun SignaturePad(onSave: (Bitmap) -> Unit, onDismiss: () -> Unit) {
    val paths = remember { mutableStateListOf<AndroidPath>() }
    var currentPath by remember { mutableStateOf<AndroidPath?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor.White)
            .padding(16.dp)
    ) {
        Text("Firme aquí", style = MaterialTheme.typography.titleMedium)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(ComposeColor.LightGray.copy(alpha = 0.2f))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newPath = AndroidPath().apply { moveTo(offset.x, offset.y) }
                            paths.add(newPath)
                            currentPath = newPath
                        },
                        onDrag = { change, _ ->
                            currentPath?.lineTo(change.position.x, change.position.y)
                            val temp = currentPath
                            currentPath = null
                            currentPath = temp
                        },
                        onDragEnd = { currentPath = null }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePaint = AndroidPaint().apply {
                    color = android.graphics.Color.BLACK
                    strokeWidth = 6f
                    style = AndroidPaint.Style.STROKE
                    strokeCap = AndroidPaint.Cap.ROUND
                    strokeJoin = AndroidPaint.Join.ROUND
                    isAntiAlias = true
                }
                paths.forEach { path ->
                    drawContext.canvas.nativeCanvas.drawPath(path, strokePaint)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onDismiss) { Text("Cancelar") }
            Button(onClick = { paths.clear() }) { Text("Limpiar") }
            Button(onClick = {
                if (paths.isEmpty()) return@Button
                val bitmap = Bitmap.createBitmap(800, 400, Bitmap.Config.ARGB_8888)
                val canvas = AndroidCanvas(bitmap)
                canvas.drawColor(android.graphics.Color.TRANSPARENT)
                val paint = AndroidPaint().apply {
                    color = android.graphics.Color.rgb(0, 0, 139) // azul oscuro
                    strokeWidth = 8f
                    style = AndroidPaint.Style.STROKE
                    strokeCap = AndroidPaint.Cap.ROUND
                    strokeJoin = AndroidPaint.Join.ROUND
                    isAntiAlias = true
                }
                paths.forEach { path -> canvas.drawPath(path, paint) }
                onSave(bitmap)
            }) { Text("Guardar") }
        }
    }
}
