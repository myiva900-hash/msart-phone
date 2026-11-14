package com.example.ui.apps

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.YvanOsUiState
import com.example.ui.YvanViewModel
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RootSuccessGreen
import com.example.ui.theme.RootWarningRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RootManagerApp(
    state: YvanOsUiState,
    viewModel: YvanViewModel,
    onClose: () -> Unit
) {
    val strings = state.strings
    val config = state.config
    val primary = MaterialTheme.colorScheme.primary
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        VirtualAppHeader(
            title = "YvanSU Root",
            icon = Icons.Rounded.Security,
            accentColor = Color(0xFF00E5FF),
            isRootBadgeVisible = config.isRootEnabled,
            onClose = onClose
        )

        val tabs = listOf(
            strings.t("Superuser", "Superuser"),
            strings.t("Kernel & CPU", "Kernel & CPU"),
            strings.t("Amateka (Logs)", "SU Logs")
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(label, maxLines = 1) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (config.isRootEnabled) primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (config.isRootEnabled) primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (config.isRootEnabled) strings.rootActive else strings.rootDisabled,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (config.isRootEnabled) primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "YvanSU v4.2 • UID 0 • /system (rw)",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = config.isRootEnabled,
                                    onCheckedChange = { enabled ->
                                        viewModel.updateConfig { it.copy(isRootEnabled = enabled) }
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = strings.t("Uburenganzira bwa Root kuri buri App", "Per-App Superuser Permissions"),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(state.installedApps, key = { it.packageId }) { app ->
                        Surface(
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (strings.isRw) app.nameRw else app.nameEn,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = app.packageId,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = app.hasRootAccess && config.isRootEnabled,
                                    onCheckedChange = { checked -> viewModel.toggleAppRoot(app, checked) }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Speed, contentDescription = null, tint = primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("CPU Governor", style = MaterialTheme.typography.titleMedium)
                                }
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("performance", "ondemand", "powersave").forEach { gov ->
                                        FilterChip(
                                            selected = config.cpuGovernor == gov,
                                            onClick = {
                                                viewModel.updateConfig { it.copy(cpuGovernor = gov) }
                                            },
                                            label = { Text(gov) }
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Max CPU Clock: ${config.cpuClockMhz} MHz",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 12.sp
                                )
                                Slider(
                                    value = config.cpuClockMhz.toFloat(),
                                    onValueChange = { mhz ->
                                        viewModel.updateConfig { it.copy(cpuClockMhz = mhz.toInt()) }
                                    },
                                    valueRange = 1200f..3200f
                                )
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Memory, contentDescription = null, tint = primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("ZRAM Virtual Swap: ${config.zramMb} MB", style = MaterialTheme.typography.titleMedium)
                                }
                                Slider(
                                    value = config.zramMb.toFloat(),
                                    onValueChange = { mb ->
                                        viewModel.updateConfig { it.copy(zramMb = (mb / 256).toInt() * 256) }
                                    },
                                    valueRange = 512f..4096f
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = strings.t("Umuvuduko wa Animation", "System Animation Scale"),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(0.5f, 1.0f, 1.5f).forEach { scale ->
                                        FilterChip(
                                            selected = config.animationScale == scale,
                                            onClick = {
                                                viewModel.updateConfig { it.copy(animationScale = scale) }
                                            },
                                            label = { Text("${scale}x") }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { viewModel.wipeCache() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Rounded.CleaningServices, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = strings.t(
                                    "Siba Cache (${state.storage.cacheMb.toInt()} MB)",
                                    "Wipe /cache Partition (${state.storage.cacheMb.toInt()} MB)"
                                )
                            )
                        }
                    }
                }
            }

            2 -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.t("Amateka ya su (${state.rootLogs.size})", "Root Audit Trail (${state.rootLogs.size})"),
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { viewModel.clearRootLogs() }) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear Logs")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.rootLogs, key = { it.id }) { log ->
                            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = log.appName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (log.granted) "GRANTED • $time" else "DENIED • $time",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = if (log.granted) RootSuccessGreen else RootWarningRed
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "$ ${log.command}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
