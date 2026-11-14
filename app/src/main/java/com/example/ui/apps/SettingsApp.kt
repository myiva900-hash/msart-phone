package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.YvanOsUiState
import com.example.ui.YvanViewModel
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RootWarningRed
import com.example.ui.theme.YvanAccentPreset
import java.util.Locale

@Composable
fun SettingsApp(
    state: YvanOsUiState,
    viewModel: YvanViewModel,
    onClose: () -> Unit
) {
    val config = state.config
    val strings = state.strings
    val storage = state.storage

    var nameInput by remember(config.deviceName) { mutableStateOf(config.deviceName) }
    var modelInput by remember(config.deviceModel) { mutableStateOf(config.deviceModel) }
    var greetingInput by remember(config.bootGreeting) { mutableStateOf(config.bootGreeting) }
    var pinInput by remember(config.lockPin) { mutableStateOf(config.lockPin) }
    var confirmReset by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        VirtualAppHeader(
            title = strings.t("Igenamiterere", "OS Settings"),
            icon = Icons.Rounded.Settings,
            accentColor = Color(0xFF7C4DFF),
            isRootBadgeVisible = config.isRootEnabled,
            onClose = onClose
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Identity & Build.prop section
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = strings.t("Umwirondoro wa Phone 2", "Phone 2 Identity & Build"),
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text(strings.t("Izina rya Telefoni", "Device Name")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = modelInput,
                            onValueChange = { modelInput = it },
                            label = { Text(strings.t("Ubwoko (Model)", "Model Name")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = greetingInput,
                            onValueChange = { greetingInput = it },
                            label = { Text(strings.t("Ubutumwa bwa Lock Screen", "Boot / Lock Greeting")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                viewModel.updateConfig {
                                    it.copy(
                                        deviceName = nameInput.ifBlank { "Yvan android" },
                                        deviceModel = modelInput.ifBlank { "Yvan Root Pro 16G" },
                                        bootGreeting = greetingInput
                                    )
                                }
                                viewModel.showToast(strings.t("Byabitswe neza!", "OS Identity updated!"))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.t("Bika Impinduka", "Apply Identity Changes"))
                        }
                    }
                }
            }

            // Appearance & Language
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.t("Isura, Theme & Ururimi", "Theme, Wallpaper & Language"), style = MaterialTheme.typography.titleMedium)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.t("Dark Mode (Umwijima)", "Dark OS Mode"))
                            Switch(
                                checked = config.darkTheme,
                                onCheckedChange = { dark -> viewModel.updateConfig { it.copy(darkTheme = dark) } }
                            )
                        }

                        Text(strings.t("Ibara rya Sisitemu (Accent)", "OS Accent Color"), style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            YvanAccentPreset.entries.forEach { preset ->
                                val selected = config.accentTheme == preset.key
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(preset.primaryDark)
                                        .border(
                                            width = if (selected) 3.dp else 1.dp,
                                            color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            viewModel.updateConfig { it.copy(accentTheme = preset.key) }
                                        }
                                )
                            }
                        }

                        Text("Wallpaper", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = config.wallpaperKey == "emerald_cyber",
                                onClick = { viewModel.updateConfig { it.copy(wallpaperKey = "emerald_cyber") } },
                                label = { Text("Cyber Emerald") }
                            )
                            FilterChip(
                                selected = config.wallpaperKey == "crimson_silk",
                                onClick = { viewModel.updateConfig { it.copy(wallpaperKey = "crimson_silk") } },
                                label = { Text("Crimson Silk") }
                            )
                        }

                        Text(strings.t("Ururimi (Language)", "Language"), style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = config.languageCode == "rw",
                                onClick = { viewModel.updateConfig { it.copy(languageCode = "rw") } },
                                label = { Text("Kinyarwanda") }
                            )
                            FilterChip(
                                selected = config.languageCode == "en",
                                onClick = { viewModel.updateConfig { it.copy(languageCode = "en") } },
                                label = { Text("English") }
                            )
                        }
                    }
                }
            }

            // Security PIN & Boot Intro Video
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.t("Umutekano & Intro Video", "PIN Lock & Boot Intro"), style = MaterialTheme.typography.titleMedium)
                        }
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) pinInput = it },
                            label = { Text(strings.t("PIN (ubusa = nta PIN)", "4-6 digit PIN (blank = no PIN)")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.updateConfig { it.copy(lockPin = pinInput) }
                                    viewModel.showToast(strings.t("PIN yavuguruwe!", "PIN updated!"))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(strings.t("Emeza PIN", "Save PIN"))
                            }
                            OutlinedButton(
                                onClick = {
                                    pinInput = ""
                                    viewModel.updateConfig { it.copy(lockPin = "") }
                                }
                            ) {
                                Text(strings.t("Kuraho", "Clear"))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.t("Erekana Boot Intro Video buri gihe", "Play Intro Video on Boot"))
                            Switch(
                                checked = config.showBootIntroOnStart,
                                onCheckedChange = { show -> viewModel.updateConfig { it.copy(showBootIntroOnStart = show) } }
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.replayBootIntro() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.t("Subiramo Intro Video ya Yvan android", "Replay Yvan android Intro Video"))
                        }
                    }
                }
            }

            // 16 GB Storage Breakdown
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.SdStorage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.storage16Gb, style = MaterialTheme.typography.titleMedium)
                        }
                        LinearProgressIndicator(
                            progress = { storage.usedFraction },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape)
                        )
                        StorageRow("System OS (/system)", String.format(Locale.US, "%.2f GB", storage.systemOsMb / 1024f))
                        StorageRow("Apps (/data/app)", String.format(Locale.US, "%.0f MB", storage.appsMb))
                        StorageRow("Files & Photos (/sdcard)", String.format(Locale.US, "%.1f MB", storage.filesAndMediaMb))
                        StorageRow("Cache (/cache)", String.format(Locale.US, "%.0f MB", storage.cacheMb))
                        StorageRow(strings.freeLabel, String.format(Locale.US, "%.2f GB", storage.freeGb))
                        OutlinedButton(
                            onClick = { viewModel.wipeCache() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.t("Siba Cache", "Wipe Cache"))
                        }
                    }
                }
            }

            // Factory Reset
            item {
                Button(
                    onClick = { confirmReset = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RootWarningRed, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.t("Subiza Phone 2 ku ntangiriro (Factory Reset)", "Factory Reset Phone 2"))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(strings.t("Wemeje Factory Reset?", "Confirm Factory Reset?")) },
            text = {
                Text(
                    strings.t(
                        "Ibi bisiba app zose washyizemo, amadosiye, PIN na settings muri Phone 2 bikayisubiza kuri 0.",
                        "This erases all user apps, files, notes, and custom settings inside Phone 2."
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmReset = false
                        viewModel.factoryReset()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RootWarningRed)
                ) {
                    Text(strings.t("Emeza Reset", "Reset Now"))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text(strings.t("Hagarika", "Cancel"))
                }
            }
        )
    }
}

@Composable
private fun StorageRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp)
    }
}
