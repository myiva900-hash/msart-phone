package com.example.ui.apps

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Shop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.ui.resolveAppIcon
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RootWarningRed
import java.util.Locale

@Composable
fun AppStoreApp(
    state: YvanOsUiState,
    viewModel: YvanViewModel,
    onClose: () -> Unit
) {
    val strings = state.strings
    val storage = state.storage
    var showSideloadDialog by remember { mutableStateOf(false) }

    var customName by remember { mutableStateOf("") }
    var customSizeMb by remember { mutableFloatStateOf(120f) }
    var customRoot by remember { mutableStateOf(false) }
    var customPayload by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            VirtualAppHeader(
                title = "Yvan Store",
                icon = Icons.Rounded.Shop,
                accentColor = Color(0xFFFFAB40),
                isRootBadgeVisible = state.config.isRootEnabled,
                onClose = onClose
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSideloadDialog = true }
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Sideload Custom App")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.t("Ububiko Busigaye (16 GB)", "16 GB Available Space"),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = String.format(Locale.US, "%.2f GB %s", storage.freeGb, strings.freeLabel),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { storage.usedFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            item {
                Text(
                    text = strings.t("App zo gushyira muri Phone 2", "Installable Apps for Phone 2"),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(state.apps.filter { !it.isSystemApp }, key = { it.packageId }) { app ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(app.colorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = resolveAppIcon(app.iconKey),
                                contentDescription = null,
                                tint = Color(0xFF061018)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (strings.isRw) app.nameRw else app.nameEn,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (strings.isRw) app.descriptionRw else app.descriptionEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${app.sizeMb.toInt()} MB • v${app.versionName}${if (app.hasRootAccess) " • ROOT" else ""}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        if (app.isInstalled) {
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { viewModel.launchApp(app.packageId) },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(strings.t("Fungura", "Open"), fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.uninstallApp(app) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RootWarningRed),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Uninstall", modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.t("Kura mo", "Remove"), fontSize = 11.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.installApp(app) },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(strings.t("Shyiramo", "Install"), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSideloadDialog) {
        AlertDialog(
            onDismissRequest = { showSideloadDialog = false },
            title = { Text(strings.t("Shyiramo App Yawe (Sideload)", "Sideload Custom App")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text(strings.t("Izina rya App", "App Name")) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customPayload,
                        onValueChange = { customPayload = it },
                        label = { Text(strings.t("URL cyangwa Ibirimo", "Launch URL or Custom Note")) },
                        singleLine = true
                    )
                    Text(
                        text = "${strings.t("Uburemere", "Size")}: ${customSizeMb.toInt()} MB",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = customSizeMb,
                        onValueChange = { customSizeMb = it },
                        valueRange = 20f..800f
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = customRoot, onCheckedChange = { customRoot = it })
                        Text(strings.t("Ikeneye Root (# su)", "Requires Root (# su)"))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customName.isNotBlank()) {
                            viewModel.installCustomApp(
                                name = customName.trim(),
                                category = "CUSTOM",
                                sizeMb = customSizeMb,
                                requiresRoot = customRoot,
                                payload = customPayload.trim()
                            )
                            customName = ""
                            customPayload = ""
                            showSideloadDialog = false
                        }
                    }
                ) {
                    Text(strings.t("Shyira muri Phone 2", "Install"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSideloadDialog = false }) {
                    Text(strings.t("Hagarika", "Cancel"))
                }
            }
        )
    }
}
