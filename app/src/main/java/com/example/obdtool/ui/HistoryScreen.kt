package com.example.obdtool.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.obdtool.obd.ObdPid

@Composable
fun HistoryScreen(viewModel: ObdViewModel) {
    var selectedPid by remember { mutableStateOf(ObdPid.ENGINE_RPM) }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box {
                OutlinedButton(onClick = { menuExpanded = true }) {
                    Text(selectedPid.shortLabel)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    defaultPollingPids.forEach { pid ->
                        DropdownMenuItem(
                            text = { Text(pid.shortLabel) },
                            onClick = { selectedPid = pid; menuExpanded = false }
                        )
                    }
                }
            }
            Button(onClick = { viewModel.clearHistory() }) {
                Text("پاک کردن لاگ")
            }
        }

        Spacer(Modifier.height(16.dp))

        val points = viewModel.history.mapNotNull { sample ->
            sample.values[selectedPid]?.let { sample.timestampMs to it }
        }

        if (points.size < 2) {
            Text("برای دیدن نمودار، اول از تب داشبورد یا داده زنده «شروع» را بزن تا داده جمع‌آوری شود.")
        } else {
            val minY = points.minOf { it.second }
            val maxY = points.maxOf { it.second }.let { if (it == minY) it + 1.0 else it }
            val minX = points.first().first
            val maxX = points.last().first.let { if (it == minX) it + 1 else it }

            Text("min: %.1f   max: %.1f   ${selectedPid.unit}".format(minY, maxY))
            Spacer(Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                // axes
                drawLine(Color.Gray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
                drawLine(Color.Gray, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 2f)

                val path = androidx.compose.ui.graphics.Path()
                points.forEachIndexed { index, (t, v) ->
                    val xFraction = (t - minX).toFloat() / (maxX - minX).toFloat()
                    val yFraction = ((v - minY) / (maxY - minY)).toFloat()
                    val x = xFraction * size.width
                    val y = size.height - (yFraction * size.height)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = Color(0xFF4CAF50), style = Stroke(width = 4f))
            }

            Spacer(Modifier.height(8.dp))
            Text("تعداد نمونه‌ها: ${points.size}")
        }
    }
}
