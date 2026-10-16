package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.FaceRecognitionResult

@Composable
fun FaceCanvasOverlay(
    bitmap: Bitmap,
    recognitionResult: FaceRecognitionResult?,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
    ) {
        val aspect = if (bitmap.height > 0) bitmap.width.toFloat() / bitmap.height.toFloat() else 16f / 9f

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Foto grupal analizada",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspect)
        )

        if (recognitionResult != null && recognitionResult.detectedFaces.isNotEmpty()) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
            ) {
                val canvasW = size.width
                val canvasH = size.height

                recognitionResult.detectedFaces.forEach { face ->
                    val rect = face.boundingBox
                    val left = rect.left * canvasW
                    val top = rect.top * canvasH
                    val right = rect.right * canvasW
                    val bottom = rect.bottom * canvasH
                    val width = right - left
                    val height = bottom - top

                    // Draw bounding box glow and border
                    val boxColor = if (face.matchedStudent != null) Color(0xFF10B981) else Color(0xFFF59E0B)

                    drawRoundRect(
                        color = boxColor,
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        cornerRadius = CornerRadius(12f, 12f),
                        style = Stroke(width = with(density) { 3.dp.toPx() })
                    )

                    // Draw pill background for student label
                    val labelText = "${face.label} ${(face.confidence * 100).toInt()}%"
                    val textLayout = textMeasurer.measure(
                        text = labelText,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    val pillPaddingH = 14f
                    val pillPaddingV = 8f
                    val pillW = textLayout.size.width + pillPaddingH * 2
                    val pillH = textLayout.size.height + pillPaddingV * 2

                    val pillLeft = (left + (width - pillW) / 2).coerceIn(4f, canvasW - pillW - 4f)
                    val pillTop = (top - pillH - 6f).coerceAtLeast(4f)

                    drawRoundRect(
                        color = Color(0xDD0F172A),
                        topLeft = Offset(pillLeft, pillTop),
                        size = Size(pillW, pillH),
                        cornerRadius = CornerRadius(16f, 16f)
                    )

                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(pillLeft + pillPaddingH, pillTop + pillPaddingV)
                    )
                }
            }
        }
    }
}
