package com.example.obdtool

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.obdtool.ui.ActuatorScreen
import com.example.obdtool.ui.ConnectScreen
import com.example.obdtool.ui.DtcScreen
import com.example.obdtool.ui.GaugeScreen
import com.example.obdtool.ui.HistoryScreen
import com.example.obdtool.ui.LiveDataScreen
import com.example.obdtool.ui.ObdViewModel

class MainActivity : ComponentActivity() {

    private val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { /* results handled implicitly; screens check isConnected before use */ }

        permissionLauncher.launch(requiredPermissions)

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    ObdApp()
                }
            }
        }
    }
}

private data class BottomTab(val route: String, val label: String)

private val bottomTabs = listOf(
    BottomTab("connect", "اتصال"),
    BottomTab("dashboard", "داشبورد"),
    BottomTab("live", "داده زنده"),
    BottomTab("history", "نمودار"),
    BottomTab("dtc", "کدهای خطا"),
    BottomTab("actuators", "عملگرها")
)

@Composable
fun ObdApp() {
    val navController = rememberNavController()
    val viewModel: ObdViewModel = viewModel()

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            NavHost(navController = navController, startDestination = "connect") {
                composable("connect") {
                    ConnectScreen(viewModel) { device: BluetoothDevice ->
                        viewModel.connect(device)
                        navController.navigate("dashboard")
                    }
                }
                composable("dashboard") { GaugeScreen(viewModel) }
                composable("live") { LiveDataScreen(viewModel) }
                composable("history") { HistoryScreen(viewModel) }
                composable("dtc") { DtcScreen(viewModel) }
                composable("actuators") { ActuatorScreen(viewModel) }
            }
        }

        NavigationBar {
            bottomTabs.forEach { tab ->
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(tab.route) },
                    label = { Text(tab.label) },
                    icon = {}
                )
            }
        }
    }
}
