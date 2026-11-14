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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VirtualFileEntity
import com.example.ui.YvanOsUiState
import com.example.ui.YvanViewModel
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RootWarningRed

@Composable
fun FileManagerApp(
    state: YvanOsUiState,
    viewModel: YvanViewModel,
    onClose: () -> Unit
) {
    val strings = state.strings
    var currentPath by remember { mutableStateOf("/") }
    var editingFile by remember { mutableStateOf<VirtualFileEntity?>(null) }
    var editBuffer by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var newFileContent by remember { mutableStateOf("") }
    var newIsFolder by remember { mutableStateOf(false) }

    val itemsInDir = remember(state.files, currentPath) {
        state.files.filter { it.parentPath == currentPath }
    }

    val isProtectedDir = currentPath.startsWith("/system") || currentPath.startsWith("/etc") || currentPath.startsWith("/data")
    val canWriteHere = !isProtectedDir || state.config.isRootEnabled

    Scaffold(
        topBar = {
            Column {
                VirtualAppHeader(
                    title = strings.t("Ububiko / Files", "Root Explorer"),
                    icon = Icons.Rounded.FolderSpecial,
                    accentColor = Color(0xFFFFCA28),
                    isRootBadgeVisible = state.config.isRootEnabled,
                    onClose = onClose
                )
                Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentPath != "/") {
                            IconButton(
                                onClick = {
                                    val trimmed = currentPath.substringBeforeLast("/")
                                    currentPath = if (trimmed.isEmpty()) "/" else trimmed
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Up")
                            }
                        }
                        Text(
                            text = "Path: $currentPath",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FloatingActionButton(
                    onClick = {
                        if (!canWriteHere) {
                            viewModel.showToast(strings.t("Fungura Root kugira uhindure $currentPath", "Enable Root to edit $currentPath"))
                        } else {
                            newIsFolder = true
                            newFileName = ""
                            newFileContent = ""
                            showCreateDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Rounded.CreateNewFolder, contentDescription = "New Folder")
                }
                FloatingActionButton(
                    onClick = {
                        if (!canWriteHere) {
                            viewModel.showToast(strings.t("Fungura Root kugira uhindure $currentPath", "Enable Root to edit $currentPath"))
                        } else {
                            newIsFolder = false
                            newFileName = ""
                            newFileContent = ""
                            showCreateDialog = true
                        }
                    }
                ) {
                    Icon(Icons.Rounded.NoteAdd, contentDescription = "New File")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (itemsInDir.isEmpty()) {
                item {
                    Text(
                        text = strings.t("Iki cyiciro kirimo ubusa", "Directory is empty"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            items(itemsInDir, key = { it.id }) { file ->
                val locked = file.requiresRoot && !state.config.isRootEnabled
                Surface(
                    onClick = {
                        if (file.isDirectory) {
                            currentPath = if (currentPath == "/") "/${file.name}" else "$currentPath/${file.name}"
                        } else if (locked) {
                            viewModel.showToast(strings.t("Dosiyeyo isaba Root (# su)!", "Requires Root (# su) permission!"))
                        } else {
                            editingFile = file
                            editBuffer = file.content
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (file.isDirectory) Icons.Rounded.Folder else Icons.Rounded.Description,
                            contentDescription = null,
                            tint = if (file.isDirectory) Color(0xFFFFCA28) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(file.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${if (file.isDirectory) "DIR" else "${file.sizeKb} KB"}${if (file.requiresRoot) " • ROOT" else ""}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (locked) {
                            Icon(Icons.Rounded.Lock, contentDescription = "Root Locked", tint = RootWarningRed)
                        } else if (!file.isDirectory) {
                            IconButton(onClick = { viewModel.deleteFile(file) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    editingFile?.let { file ->
        AlertDialog(
            onDismissRequest = { editingFile = null },
            title = { Text(file.name) },
            text = {
                OutlinedTextField(
                    value = editBuffer,
                    onValueChange = { editBuffer = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp
                    )
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateFile(file, editBuffer)
                    editingFile = null
                }) {
                    Text(strings.t("Bika", "Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingFile = null }) {
                    Text(strings.t("Funga", "Close"))
                }
            }
        )
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    if (newIsFolder) strings.t("Kora Ububiko Bushya", "New Folder")
                    else strings.t("Kora Dosiye Nshya", "New File")
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        label = { Text(strings.t("Izina", "Name")) },
                        singleLine = true
                    )
                    if (!newIsFolder) {
                        OutlinedTextField(
                            value = newFileContent,
                            onValueChange = { newFileContent = it },
                            label = { Text(strings.t("Ibirimo", "Content")) },
                            modifier = Modifier.height(120.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            viewModel.createFile(
                                parentPath = currentPath,
                                name = newFileName.trim(),
                                isDir = newIsFolder,
                                content = newFileContent,
                                requiresRoot = isProtectedDir
                            )
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text(strings.t("Bika", "Create"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(strings.t("Hagarika", "Cancel"))
                }
            }
        )
    }
}
