package com.example.ui

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PanoramaFishEye
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.VirtualAppEntity
import com.example.data.YvanStrings
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RootSuccessGreen
import com.example.ui.theme.RootWarningRed
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PhoneShell(
    state: YvanOsUiState,
    phase: OsPhase,
    activeAppPackage: String?,
    recentPackages: List<String>,
    isShadeOpen: Boolean,
    isPowerMenuOpen: Boolean,
    viewModel: YvanViewModel,
    activeAppContent: @Composable (VirtualAppEntity) -> Unit
) {
    val strings = state.strings
    val config = state.config
    val context = LocalContext.current

    var currentTime by remember { mutableStateOf("") }
    var batteryPct by remember { mutableIntStateOf(96) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val battIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = battIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = battIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                batteryPct = ((level * 100f) / scale).toInt()
            }
            delay(15_000)
        }
    }

    val wallpaperRes = if (config.wallpaperKey == "crimson_silk") {
        R.drawable.img_yvan_wallpaper_crimson
    } else {
        R.drawable.img_yvan_wallpaper
    }

    var selectedAppOptions by remember { mutableStateOf<VirtualAppEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Wallpaper Background behind launcher/recents/drawer
        Image(
            painter = painterResource(id = wallpaperRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (config.darkTheme) {
                        Color(0xFF060A14).copy(alpha = 0.56f)
                    } else {
                        Color(0xFFF3F6FB).copy(alpha = 0.72f)
                    }
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. Virtual Phone 2 Status Bar
            VirtualStatusBar(
                deviceName = config.deviceName,
                timeText = currentTime,
                batteryPct = batteryPct,
                isRootEnabled = config.isRootEnabled,
                wifiOn = config.wifiEnabled,
                dataOn = config.mobileDataEnabled,
                bluetoothOn = config.bluetoothEnabled,
                darkTheme = config.darkTheme,
                onClickShade = { viewModel.setShadeOpen(!isShadeOpen) }
            )

            // 2. Main Phone 2 Screen Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (phase) {
                    OsPhase.HOME -> {
                        HomeLauncherScreen(
                            state = state,
                            strings = strings,
                            onLaunchApp = { viewModel.launchApp(it.packageId) },
                            onLongPressApp = { selectedAppOptions = it },
                            onOpenDrawer = { viewModel.openAppDrawer() },
                            onOpenShade = { viewModel.setShadeOpen(true) }
                        )
                    }

                    OsPhase.APP_DRAWER -> {
                        AppDrawerScreen(
                            state = state,
                            strings = strings,
                            onLaunchApp = { viewModel.launchApp(it.packageId) },
                            onLongPressApp = { selectedAppOptions = it },
                            onClose = { viewModel.goHome() }
                        )
                    }

                    OsPhase.RECENTS -> {
                        RecentsScreen(
                            state = state,
                            recentPackages = recentPackages,
                            strings = strings,
                            onSelectApp = { viewModel.launchApp(it) },
                            onCloseApp = { viewModel.closeRecent(it) },
                            onClearAll = { viewModel.clearAllRecents() }
                        )
                    }

                    OsPhase.ACTIVE_APP -> {
                        val activeApp = state.apps.firstOrNull { it.packageId == activeAppPackage }
                        if (activeApp != null) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                activeAppContent(activeApp)
                            }
                        } else {
                            viewModel.goHome()
                        }
                    }

                    else -> Unit
                }

                // Quick Settings Shade Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = isShadeOpen,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut()
                ) {
                    QuickSettingsShade(
                        state = state,
                        strings = strings,
                        viewModel = viewModel,
                        onDismiss = { viewModel.setShadeOpen(false) }
                    )
                }
            }

            // 3. Virtual Phone 2 Navigation Bar (Back, Home, Recents)
            VirtualNavigationBar(
                darkTheme = config.darkTheme,
                onBack = {
                    if (!viewModel.navigateBack()) {
                        viewModel.setPowerMenuOpen(true)
                    }
                },
                onHome = { viewModel.goHome() },
                onRecents = { viewModel.openRecents() }
            )
        }

        // Virtual Brightness Dimmer Overlay
        val dimAlpha = ((1f - config.virtualBrightness) * 0.65f).coerceIn(0f, 0.65f)
        if (dimAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimAlpha))
            )
        }

        // App Long-Press Info / Uninstall / Root Dialog
        selectedAppOptions?.let { app ->
            AlertDialog(
                onDismissRequest = { selectedAppOptions = null },
                title = { Text(if (strings.isRw) app.nameRw else app.nameEn) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (strings.isRw) app.descriptionRw else app.descriptionEn,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Package: ${app.packageId}\nVersion: ${app.versionName} • ${app.sizeMb.toInt()} MB",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (app.hasRootAccess) {
                            Text(
                                text = strings.t("Ifite uburenganzira bwa Root (# su)", "Granted Root (# su) access"),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.launchApp(app.packageId)
                        selectedAppOptions = null
                    }) {
                        Text(strings.t("Fungura", "Open"))
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!app.isSystemApp) {
                            TextButton(
                                onClick = {
                                    viewModel.uninstallApp(app)
                                    selectedAppOptions = null
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = RootWarningRed)
                            ) {
                                Text(strings.t("Kura mo", "Uninstall"))
                            }
                        }
                        TextButton(onClick = { selectedAppOptions = null }) {
                            Text(strings.t("Funga", "Close"))
                        }
                    }
                }
            )
        }

        // Power Menu Dialog
        if (isPowerMenuOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.setPowerMenuOpen(false) },
                title = { Text("${config.deviceName} • ${strings.powerMenu}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.replayBootIntro() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.rebootIntro)
                        }
                        OutlinedButton(
                            onClick = { viewModel.lockPhone2() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Lock, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.t("Funga Ecran (Lock)", "Lock Phone 2"))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.setPowerMenuOpen(false) }) {
                        Text(strings.t("Subira inyuma", "Cancel"))
                    }
                }
            )
        }
    }
}

@Composable
private fun VirtualStatusBar(
    deviceName: String,
    timeText: String,
    batteryPct: Int,
    isRootEnabled: Boolean,
    wifiOn: Boolean,
    dataOn: Boolean,
    bluetoothOn: Boolean,
    darkTheme: Boolean,
    onClickShade: () -> Unit
) {
    val contentColor = if (darkTheme) Color.White else Color(0xFF0F172A)
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (darkTheme) Color.Black.copy(alpha = 0.48f) else Color.White.copy(alpha = 0.75f)
            )
            .clickable(onClick = onClickShade)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("virtual_status_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = timeText.ifEmpty { "12:00" },
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = contentColor
            )
            Surface(
                color = if (isRootEnabled) primary.copy(alpha = 0.20f) else contentColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (isRootEnabled) "# SU" else "STD",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRootEnabled) primary else contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Text(
                text = deviceName,
                fontSize = 11.sp,
                color = contentColor.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 120.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (bluetoothOn) {
                Icon(Icons.Rounded.Bluetooth, contentDescription = "Bluetooth", tint = contentColor, modifier = Modifier.size(14.dp))
            }
            if (wifiOn) {
                Icon(Icons.Rounded.Wifi, contentDescription = "WiFi", tint = contentColor, modifier = Modifier.size(14.dp))
            }
            if (dataOn) {
                Icon(Icons.Rounded.SignalCellularAlt, contentDescription = "5G", tint = contentColor, modifier = Modifier.size(14.dp))
            }
            Icon(Icons.Rounded.BatteryFull, contentDescription = "Battery", tint = primary, modifier = Modifier.size(15.dp))
            Text(
                text = "$batteryPct%",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = contentColor
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Open Quick Settings",
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun HomeLauncherScreen(
    state: YvanOsUiState,
    strings: YvanStrings,
    onLaunchApp: (VirtualAppEntity) -> Unit,
    onLongPressApp: (VirtualAppEntity) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenShade: () -> Unit
) {
    val config = state.config
    val storage = state.storage
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        // Phone 2 Live Root & 16 GB Telemetry Widget
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
            border = androidx.compose.foundation.BorderStroke(1.dp, primary.copy(alpha = 0.35f)),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = config.deviceName,
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${config.deviceModel} • ${config.cpuGovernor.uppercase()}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        onClick = onOpenShade,
                        color = if (config.isRootEnabled) primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (config.isRootEnabled) primary else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = if (config.isRootEnabled) primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = if (config.isRootEnabled) "ROOT #" else "LOCKED",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (config.isRootEnabled) primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.storage16Gb,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "%.2f / 16.0 GB (%d%%)",
                            storage.usedGb,
                            (storage.usedFraction * 100).toInt()
                        ),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = primary
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { storage.usedFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionPill(
                        icon = Icons.Rounded.Terminal,
                        label = "Terminal #",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            state.apps.firstOrNull { it.packageId == "com.yvan.terminal" }?.let(onLaunchApp)
                        }
                    )
                    QuickActionPill(
                        icon = Icons.Rounded.Settings,
                        label = strings.t("Igenamiterere", "OS Tweaks"),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            state.apps.firstOrNull { it.packageId == "com.yvan.settings" }?.let(onLaunchApp)
                        }
                    )
                    QuickActionPill(
                        icon = Icons.Rounded.Apps,
                        label = strings.allApps,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenDrawer
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Home App Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.homeGridApps, key = { it.packageId }) { app ->
                AppIconTile(
                    app = app,
                    isRw = strings.isRw,
                    isRootActive = config.isRootEnabled,
                    onClick = { onLaunchApp(app) },
                    onLongClick = { onLongPressApp(app) }
                )
            }
        }

        // Bottom Phone 2 Dock
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            border = androidx.compose.foundation.BorderStroke(1.dp, secondary.copy(alpha = 0.25f)),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                state.dockApps.forEach { app ->
                    AppIconTile(
                        app = app,
                        isRw = strings.isRw,
                        isRootActive = config.isRootEnabled,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onLongPressApp(app) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconTile(
    app: VirtualAppEntity,
    isRw: Boolean,
    isRootActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeColor = Color(app.colorHex)
    val label = if (isRw) app.nameRw else app.nameEn

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp)
            .testTag("app_icon_${app.packageId}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                badgeColor.copy(alpha = 0.92f),
                                badgeColor.copy(alpha = 0.58f)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = resolveAppIcon(app.iconKey),
                    contentDescription = label,
                    tint = Color(0xFF061018),
                    modifier = Modifier.size(28.dp)
                )
            }
            if (app.hasRootAccess && isRootActive) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF09131C))
                        .border(1.dp, RootSuccessGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RootSuccessGreen
                    )
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AppDrawerScreen(
    state: YvanOsUiState,
    strings: YvanStrings,
    onLaunchApp: (VirtualAppEntity) -> Unit,
    onLongPressApp: (VirtualAppEntity) -> Unit,
    onClose: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val filtered = remember(state.installedApps, query, selectedCategory) {
        state.installedApps.filter { app ->
            val matchesCat = selectedCategory == "ALL" || app.category == selectedCategory
            val matchesQuery = query.isBlank() ||
                app.nameEn.contains(query, ignoreCase = true) ||
                app.nameRw.contains(query, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.94f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.allApps} (${state.installedApps.size})",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close Drawer")
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(strings.searchApps) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            val categories = listOf("ALL", "CORE", "ROOT", "UTILITY", "CUSTOM")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.packageId }) { app ->
                    AppIconTile(
                        app = app,
                        isRw = strings.isRw,
                        isRootActive = state.config.isRootEnabled,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onLongPressApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentsScreen(
    state: YvanOsUiState,
    recentPackages: List<String>,
    strings: YvanStrings,
    onSelectApp: (String) -> Unit,
    onCloseApp: (String) -> Unit,
    onClearAll: () -> Unit
) {
    val openApps = remember(recentPackages, state.apps) {
        recentPackages.mapNotNull { pkg -> state.apps.firstOrNull { it.packageId == pkg } }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.recentApps, style = MaterialTheme.typography.titleLarge)
                if (openApps.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.clearAll)
                    }
                }
            }

            if (openApps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.ViewAgenda,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(strings.noRecentApps, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(openApps, key = { it.packageId }) { app ->
                        Surface(
                            onClick = { onSelectApp(app.packageId) },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(app.colorHex).copy(alpha = 0.45f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(app.colorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        resolveAppIcon(app.iconKey),
                                        contentDescription = null,
                                        tint = Color(0xFF061018)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (strings.isRw) app.nameRw else app.nameEn,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${app.packageId} • ${app.sizeMb.toInt()} MB",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onCloseApp(app.packageId) }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Close app")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickSettingsShade(
    state: YvanOsUiState,
    strings: YvanStrings,
    viewModel: YvanViewModel,
    onDismiss: () -> Unit
) {
    val config = state.config
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.quickSettings, style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                // 2x3 Quick Tiles
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickTile(
                        icon = Icons.Rounded.Security,
                        label = if (config.isRootEnabled) "Root ON" else "Root OFF",
                        active = config.isRootEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateConfig { it.copy(isRootEnabled = !it.isRootEnabled) } }
                    )
                    QuickTile(
                        icon = Icons.Rounded.Wifi,
                        label = "Wi-Fi",
                        active = config.wifiEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateConfig { it.copy(wifiEnabled = !it.wifiEnabled) } }
                    )
                    QuickTile(
                        icon = Icons.Rounded.SignalCellularAlt,
                        label = "5G Data",
                        active = config.mobileDataEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateConfig { it.copy(mobileDataEnabled = !it.mobileDataEnabled) } }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickTile(
                        icon = if (config.darkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        label = if (config.darkTheme) "Dark OS" else "Light OS",
                        active = config.darkTheme,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateConfig { it.copy(darkTheme = !it.darkTheme) } }
                    )
                    QuickTile(
                        icon = Icons.Rounded.Language,
                        label = if (config.languageCode == "rw") "Kinyarwanda" else "English",
                        active = true,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.updateConfig {
                                it.copy(languageCode = if (it.languageCode == "rw") "en" else "rw")
                            }
                        }
                    )
                    QuickTile(
                        icon = Icons.Rounded.Bluetooth,
                        label = "Bluetooth",
                        active = config.bluetoothEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateConfig { it.copy(bluetoothEnabled = !it.bluetoothEnabled) } }
                    )
                }

                // Virtual Brightness Slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Brightness6, contentDescription = null, tint = primary)
                    Spacer(Modifier.width(10.dp))
                    Slider(
                        value = config.virtualBrightness,
                        onValueChange = { b -> viewModel.updateConfig { it.copy(virtualBrightness = b) } },
                        valueRange = 0.25f..1.0f,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Storage Quick Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.SdStorage, contentDescription = null, tint = primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = String.format(Locale.US, "%.2f / 16.0 GB", state.storage.usedGb),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.replayBootIntro() }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Boot Video", fontSize = 12.sp)
                        }
                        Button(onClick = { viewModel.launchApp("com.yvan.settings") }) {
                            Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.t("Settings", "Settings"), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTile(
    icon: ImageVector,
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VirtualNavigationBar(
    darkTheme: Boolean,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onRecents: () -> Unit
) {
    val bg = if (darkTheme) Color(0xFF050811) else Color(0xFFE9EEF7)
    val fg = if (darkTheme) Color.White else Color(0xFF0F172A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .testTag("nav_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = fg.copy(alpha = 0.85f)
            )
        }
        IconButton(
            onClick = onHome,
            modifier = Modifier
                .size(48.dp)
                .testTag("nav_home_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.PanoramaFishEye,
                contentDescription = "Home",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(
            onClick = onRecents,
            modifier = Modifier
                .size(48.dp)
                .testTag("nav_recents_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.ViewAgenda,
                contentDescription = "Recent Apps",
                tint = fg.copy(alpha = 0.85f)
            )
        }
    }
}
