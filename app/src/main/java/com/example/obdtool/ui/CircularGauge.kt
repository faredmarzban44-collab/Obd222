package com.example.obdtool.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * A dark, Torque-Pro-style circular gauge: sweeps 240° from bottom-left to bottom-right,
 * white tick labels, a red needle, and the live value shown in the center.
 */
@Composable
fun CircularGauge(
    label: String,
    value: Double?,
    min: Double,
    max: Double,
    unit: String,
    modifier: Modifier = Modifier,
    tickCount: Int = 6
) {
    val startAngle = 135f  // degrees, 0 = 3 o'clock, clockwise
    val sweepAngle = 270f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            val radius = size.minDimension / 2.2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Dial background
            drawCircle(color = Color.Black, radius = radius * 1.08f, center = center)
            drawCircle(
                color = Color(0xFF2A2A2A),
                radius = radius * 1.08f,
                center = center,
                style = Stroke(width = 4f)
            )

            // Tick marks + numeric labels
            for (i in 0..tickCount) {
                val fraction = i / tickCount.toFloat()
                val angleDeg = startAngle + sweepAngle * fraction
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val outer = Offset(
                    x = center.x + radius * cos(angleRad).toFloat(),
                    y = center.y + radius * sin(angleRad).toFloat()
                )
                val inner = Offset(
                    x = center.x + (radius * 0.85f) * cos(angleRad).toFloat(),
                    y = center.y + (radius * 0.85f) * sin(angleRad).toFloat()
                )
                drawLine(Color.White, inner, outer, strokeWidth = 3f)

                val tickValue = min + (max - min) * fraction
                val labelPos = Offset(
                    x = center.x + (radius * 0.68f) * cos(angleRad).toFloat(),
                    y = center.y + (radius * 0.68f) * sin(angleRad).toFloat()
                )
                drawContext.canvas.nativeCanvas.drawText(
                    formatTick(tickValue),
                    labelPos.x,
                    labelPos.y,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = radius * 0.12f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }

            // Needle
            val clamped = (value ?: min).coerceIn(min, max)
            val valueFraction = if (max > min) ((clamped - min) / (max - min)).toFloat() else 0f
            val needleAngleDeg = startAngle + sweepAngle * valueFraction
            val needleAngleRad = Math.toRadians(needleAngleDeg.toDouble())
            val needleTip = Offset(
                x = center.x + (radius * 0.78f) * cos(needleAngleRad).toFloat(),
                y = center.y + (radius * 0.78f) * sin(needleAngleRad).toFloat()
            )
            drawLine(
                color = Color(0xFFE53935),
                start = center,
                end = needleTip,
                strokeWidth = 6f
            )
            drawCircle(color = Color(0xFFE53935), radius = radius * 0.06f, center = center)
        }

        // Label + live value overlay, centered lower half of the dial
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    style = TextStyle(fontSize = 13.sp)
                )
                Text(
                    text = value?.let { formatValue(it) + " " + unit } ?: "no data",
                    color = if (value == null) Color(0xFFE53935) else Color(0xFF4CAF50),
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

private fun formatTick(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(v)

private fun formatValue(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(v)
