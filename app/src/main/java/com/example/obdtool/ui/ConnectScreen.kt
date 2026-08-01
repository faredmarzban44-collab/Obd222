package com.example.obdtool.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.obdtool.bluetooth.ObdProtocol

@SuppressLint("MissingPermission") // caller must have requested BLUETOOTH_CONNECT already
@Composable
fun ConnectScreen(
    viewModel: ObdViewModel,
    onDeviceSelected: (BluetoothDevice) -> Unit
) {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    val pairedDevices = remember { adapter?.bondedDevices?.toList() ?: emptyList() }
    var protocolMenuExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("وضعیت: ${viewModel.connectionStatus}", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))

        Text("پروتکل ارتباطی (برای ECU های قدیمی‌تر مثل Bosch M7.4.4، KWP2000 را انتخاب کنید):")
        Spacer(Modifier.height(4.dp))
        Box {
            OutlinedButton(onClick = { protocolMenuExpanded = true }) {
                Text(viewModel.selectedProtocol.label)
            }
            DropdownMenu(
                expanded = protocolMenuExpanded,
                onDismissRequest = { protocolMenuExpanded = false }
            ) {
                ObdProtocol.entries.forEach { protocol ->
                    DropdownMenuItem(
                        text = { Text(protocol.label) },
                        onClick = {
                            viewModel.setProtocol(protocol)
                            protocolMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("دستگاه‌های بلوتوث جفت‌شده (ابتدا دانگل را از تنظیمات بلوتوث گوشی جفت کنید):")
        Spacer(Modifier.height(8.dp))

        if (pairedDevices.isEmpty()) {
            Text("هیچ دستگاه جفت‌شده‌ای پیدا نشد.")
        } else {
            LazyColumn {
                items(pairedDevices) { device ->
                    ListItem(
                        headlineContent = { Text(device.name ?: "دستگاه ناشناس") },
                        supportingContent = { Text(device.address) },
                        modifier = Modifier.clickable { onDeviceSelected(device) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
