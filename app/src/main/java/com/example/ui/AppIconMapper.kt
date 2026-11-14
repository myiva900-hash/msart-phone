package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shop
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

fun resolveAppIcon(iconKey: String): ImageVector = when (iconKey) {
    "phone" -> Icons.Rounded.Call
    "messages" -> Icons.Rounded.Message
    "root" -> Icons.Rounded.Security
    "store" -> Icons.Rounded.Shop
    "terminal" -> Icons.Rounded.Terminal
    "files" -> Icons.Rounded.FolderSpecial
    "settings" -> Icons.Rounded.Settings
    "gallery" -> Icons.Rounded.PhotoLibrary
    "notes" -> Icons.Rounded.EditNote
    "clock" -> Icons.Rounded.Schedule
    "calc" -> Icons.Rounded.Calculate
    "sensors" -> Icons.Rounded.Explore
    "browser" -> Icons.Rounded.Public
    "ram" -> Icons.Rounded.Memory
    "vault" -> Icons.Rounded.Lock
    "game" -> Icons.Rounded.SportsEsports
    "custom" -> Icons.Rounded.AutoAwesome
    else -> Icons.Rounded.Apps
}
