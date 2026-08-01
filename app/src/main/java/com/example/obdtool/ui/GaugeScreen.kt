package com.example.obdtool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.obdtool.obd.ObdPid

/** The set of gauges shown on the dashboard, Torque-Pro style. */
private val dashboardPids = listOf(
    ObdPid.ENGINE_RPM,
    ObdPid.VEHICLE_SPEED,
    ObdPid.THROTTLE_POS,
    ObdPid.COOLANT_TEMP,
    ObdPid.INTAKE_MAP,
    ObdPid.BATTERY_VOLTAGE
)

@Composable
fun GaugeScreen(viewModel: ObdViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { viewModel.startLivePolling() }, enabled = !viewModel.isPolling) {
                Text("شروع")
            }
            Button(onClick = { viewModel.stopLivePolling() }, enabled = viewModel.isPolling) {
                Text("توقف")
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(dashboardPids) { pid ->
                CircularGauge(
                    label = pid.shortLabel,
                    value = viewModel.liveData[pid],
                    min = pid.gaugeMin,
                    max = pid.gaugeMax,
                    unit = pid.unit
                )
            }
        }
    }
}
