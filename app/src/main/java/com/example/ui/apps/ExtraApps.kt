package com.example.ui.apps

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VirtualAppEntity
import com.example.ui.YvanOsUiState
import com.example.ui.YvanViewModel
import com.example.ui.resolveAppIcon
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import java.util.Locale

@Composable
fun SensorsApp(
    state: YvanOsUiState,
    onClose: () -> Unit
) {
    val strings = state.strings
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var accelZ by remember { mutableFloatStateOf(9.81f) }
    var hasAccel by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        hasAccel = accel != null
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    accelX = event.values[0]
                    accelY = event.values[1]
                    accelZ = event.values[2]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (accel != null) {
            sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        VirtualAppHeader(
            title = strings.t("Ibipimo", "Hardware Sensors"),
            icon = Icons.Rounded.Explore,
            accentColor = Color(0xFF00BFA5),
            onClose = onClose
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(strings.t("Accelerometer (Sensor Nyayo)", "Live Accelerometer Telemetry"), style = MaterialTheme.typography.titleMedium)
                        if (hasAccel) {
                            Text(String.format(Locale.US, "X: %.2f m/s²", accelX), fontFamily = JetBrainsMonoFontFamily)
                            Text(String.format(Locale.US, "Y: %.2f m/s²", accelY), fontFamily = JetBrainsMonoFontFamily)
                            Text(String.format(Locale.US, "Z: %.2f m/s²", accelZ), fontFamily = JetBrainsMonoFontFamily)
                        } else {
                            Text(strings.t("Nta sensor y'umuvuduko ibonetse kuri iyi device.", "No accelerometer sensor detected on this device."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(strings.t("Amakuru ya Hardware & OS", "Virtual & Host Hardware"), style = MaterialTheme.typography.titleMedium)
                        Text("Phone 2 Name: ${state.config.deviceName}", fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp)
                        Text("CPU Governor: ${state.config.cpuGovernor} @ ${state.config.cpuClockMhz} MHz", fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp)
                        Text("Host ABI: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"}", fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp)
                        Text("Host Android SDK: ${Build.VERSION.SDK_INT}", fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BrowserApp(
    state: YvanOsUiState,
    onClose: () -> Unit
) {
    val strings = state.strings
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("https://developer.android.com") }

    val bookmarks = listOf(
        "Android Developers" to "https://developer.android.com",
        "Magisk / Root Docs" to "https://github.com/topjohnwu/Magisk",
        "Termux Wiki" to "https://wiki.termux.com",
        "Google Search" to "https://www.google.com"
    )

    fun openExternal(url: String) {
        val clean = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(clean)))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        VirtualAppHeader(
            title = strings.t("Urubuga", "Phone 2 Browser"),
            icon = Icons.Rounded.Public,
            accentColor = Color(0xFF448AFF),
            onClose = onClose
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("URL") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { openExternal(urlInput) }) {
                    Icon(Icons.Rounded.OpenInNew, contentDescription = "Open")
                }
            }

            Text(strings.t("Imbuga Zihuse (Bookmarks)", "Quick Bookmarks"), style = MaterialTheme.typography.titleMedium)

            bookmarks.forEach { (title, url) ->
                Surface(
                    onClick = { openExternal(url) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(title, style = MaterialTheme.typography.titleMedium)
                            Text(url, fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Rounded.OpenInNew, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun GenericVirtualAppScreen(
    app: VirtualAppEntity,
    state: YvanOsUiState,
    viewModel: YvanViewModel,
    onClose: () -> Unit
) {
    val strings = state.strings
    val context = LocalContext.current
    var gameScore by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        VirtualAppHeader(
            title = if (strings.isRw) app.nameRw else app.nameEn,
            icon = resolveAppIcon(app.iconKey),
            accentColor = Color(app.colorHex),
            isRootBadgeVisible = app.hasRootAccess && state.config.isRootEnabled,
            onClose = onClose
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (strings.isRw) app.descriptionRw else app.descriptionEn,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Package: ${app.packageId} • ${app.sizeMb.toInt()} MB • v${app.versionName}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (app.packageId == "com.yvan.game.cyber") {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Rounded.SportsEsports, contentDescription = null, modifier = Modifier.size(48.dp))
                        Text("Score: $gameScore", fontFamily = SpaceGroteskFontFamily, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Button(onClick = { gameScore += 10 }) {
                            Text(strings.t("Kanda Watsinde (+10)", "Tap Cyber Node (+10)"))
                        }
                    }
                }
            } else if (app.category == "CUSTOM") {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.t("App Yihariye muri Phone 2", "User-Installed Phone 2 App"), style = MaterialTheme.typography.titleMedium)
                        }
                        if (app.customPayload.isNotBlank()) {
                            Text(app.customPayload, fontFamily = JetBrainsMonoFontFamily, fontSize = 13.sp)
                            if (app.customPayload.startsWith("http://") || app.customPayload.startsWith("https://")) {
                                Button(onClick = {
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(app.customPayload))) }
                                }) {
                                    Text(strings.t("Fungura Link", "Launch URL"))
                                }
                            }
                        }
                    }
                }
            } else {
                // Root/System Store Apps quick interactive controls
                Button(
                    onClick = {
                        viewModel.wipeCache()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.t("Tunganya Sisitemu & Siba Cache", "Optimize Virtual OS & Trim Cache"))
                }
                Button(
                    onClick = { viewModel.launchApp("com.yvan.root") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.t("Fungura YvanSU Root Console", "Open YvanSU Root Console"))
                }
            }
        }
    }
}
