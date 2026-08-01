package com.example.obdtool.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LiveDataScreen(viewModel: ObdViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row {
            Button(onClick = { viewModel.startLivePolling() }, enabled = !viewModel.isPolling) {
                Text("شروع خواندن زنده")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { viewModel.stopLivePolling() }, enabled = viewModel.isPolling) {
                Text("توقف")
            }
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(viewModel.liveData.entries.toList()) { (pid, value) ->
                ListItem(
                    headlineContent = { Text(pid.label) },
                    trailingContent = { Text(value?.let { "%.1f".format(it) } ?: "—") }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun DtcScreen(viewModel: ObdViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row {
            Button(onClick = { viewModel.readDtcCodes() }) { Text("خواندن کدهای خطا") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { viewModel.clearDtcCodes() }) { Text("پاک کردن کدها") }
        }
        Spacer(Modifier.height(16.dp))
        if (viewModel.dtcCodes.isEmpty()) {
            Text("کد خطایی ثبت نشده (یا هنوز خوانده نشده).")
        } else {
            LazyColumn {
                items(viewModel.dtcCodes) { result ->
                    ListItem(
                        headlineContent = { Text(result.code) },
                        supportingContent = { Text(result.descriptionFa) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ActuatorScreen(viewModel: ObdViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "توجه: تست عملگرها به شناسه‌ی روتین (Routine ID) اختصاصی همان خودرو نیاز دارد. " +
            "شناسه‌های واقعی Bosch M7.4.4 عمومی منتشر نشده‌اند؛ لیست زیر خالی است تا " +
            "خودت آن‌ها را (از کتاب سرویس یا مستندات رسمی) در ActuatorTester.kt اضافه کنی.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        if (viewModel.actuatorTester.knownRoutines.isEmpty()) {
            Text("هیچ روتینی تعریف نشده.")
        } else {
            LazyColumn {
                items(viewModel.actuatorTester.knownRoutines) { routine ->
                    ListItem(
                        headlineContent = { Text(routine.name) },
                        trailingContent = {
                            Row {
                                Button(onClick = { viewModel.runActuatorTest(routine) }) { Text("شروع") }
                                Spacer(Modifier.width(4.dp))
                                Button(onClick = { viewModel.stopActuatorTest(routine) }) { Text("توقف") }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("آخرین پاسخ ECU: ${viewModel.lastActuatorResponse}")
    }
}
