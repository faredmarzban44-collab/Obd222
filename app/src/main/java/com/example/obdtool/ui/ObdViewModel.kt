package com.example.obdtool.ui

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.obdtool.bluetooth.BluetoothObdManager
import com.example.obdtool.bluetooth.ObdProtocol
import com.example.obdtool.obd.ActuatorTester
import com.example.obdtool.obd.DtcReader
import com.example.obdtool.obd.DtcResult
import com.example.obdtool.obd.ObdPid
import com.example.obdtool.obd.ObdResponseParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** One polling tick's readings, used to draw history graphs. */
data class LiveSample(val timestampMs: Long, val values: Map<ObdPid, Double?>)

/** All PIDs polled by default — covers both the live-data list and the dashboard gauges. */
val defaultPollingPids = listOf(
    ObdPid.ENGINE_RPM, ObdPid.VEHICLE_SPEED, ObdPid.COOLANT_TEMP,
    ObdPid.THROTTLE_POS, ObdPid.ENGINE_LOAD, ObdPid.TIMING_ADVANCE,
    ObdPid.INTAKE_MAP, ObdPid.BATTERY_VOLTAGE, ObdPid.FUEL_LEVEL
)

private const val MAX_HISTORY_SAMPLES = 300

class ObdViewModel : ViewModel() {

    val manager = BluetoothObdManager()
    val dtcReader = DtcReader(manager)
    val actuatorTester = ActuatorTester(manager)

    var connectionStatus by mutableStateOf("قطع")
        private set

    var selectedProtocol by mutableStateOf(ObdProtocol.AUTO)
        private set

    var liveData by mutableStateOf<Map<ObdPid, Double?>>(emptyMap())
        private set

    /** Rolling history of live-data samples, newest last. Capped at MAX_HISTORY_SAMPLES. */
    val history = mutableStateListOf<LiveSample>()

    var dtcCodes by mutableStateOf<List<DtcResult>>(emptyList())
        private set

    var lastActuatorResponse by mutableStateOf("")
        private set

    var isPolling by mutableStateOf(false)
        private set

    fun setProtocol(protocol: ObdProtocol) {
        selectedProtocol = protocol
        if (manager.isConnected) {
            viewModelScope.launch { manager.setProtocol(protocol) }
        }
    }

    fun connect(device: BluetoothDevice) {
        viewModelScope.launch {
            connectionStatus = "در حال اتصال..."
            val result = manager.connect(device, selectedProtocol)
            connectionStatus = if (result.isSuccess) "متصل به ${device.name}" else "خطا در اتصال"
        }
    }

    fun disconnect() {
        manager.disconnect()
        connectionStatus = "قطع"
        isPolling = false
    }

    fun startLivePolling(pids: List<ObdPid> = defaultPollingPids) {
        if (!manager.isConnected || isPolling) return
        isPolling = true
        viewModelScope.launch {
            while (isPolling && manager.isConnected) {
                val results = mutableMapOf<ObdPid, Double?>()
                for (pid in pids) {
                    val raw = manager.sendRawCommand(pid.requestCommand())
                    results[pid] = ObdResponseParser.parse(pid, raw)
                }
                liveData = results

                history.add(LiveSample(System.currentTimeMillis(), results.toMap()))
                if (history.size > MAX_HISTORY_SAMPLES) {
                    history.removeAt(0)
                }

                delay(500)
            }
        }
    }

    fun stopLivePolling() {
        isPolling = false
    }

    fun clearHistory() {
        history.clear()
    }

    fun readDtcCodes() {
        viewModelScope.launch {
            dtcCodes = dtcReader.readStoredCodes()
        }
    }

    fun clearDtcCodes() {
        viewModelScope.launch {
            dtcReader.clearCodes()
            dtcCodes = emptyList()
        }
    }

    fun runActuatorTest(routine: ActuatorTester.ActuatorRoutine) {
        viewModelScope.launch {
            lastActuatorResponse = actuatorTester.startRoutine(routine)
        }
    }

    fun stopActuatorTest(routine: ActuatorTester.ActuatorRoutine) {
        viewModelScope.launch {
            lastActuatorResponse = actuatorTester.stopRoutine(routine)
        }
    }

    override fun onCleared() {
        super.onCleared()
        manager.disconnect()
    }
}
