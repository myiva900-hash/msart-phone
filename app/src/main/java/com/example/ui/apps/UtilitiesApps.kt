package com.example.ui.apps

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.VirtualNoteEntity
import com.example.ui.YvanOsUiState
import com.example.ui.YvanViewModel
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RootWarningRed
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GalleryApp(
    state: YvanOsUiState,
    viewModel: YvanViewModel,
    onClose: () -> Unit
) {
    val strings = state.strings
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            val sizeMb = try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    (pfd.statSize / (1024f * 1024f)).coerceAtLeast(0.4f)
                } ?: 2.4f
            } catch (_: Exception) {
                2.4f
            }
            viewModel.addPhoto(uri.toString(), "IMG_${System.currentTimeMillis() % 10000}", sizeMb)
        }
    }

    Scaffold(
        topBar = {
            VirtualAppHeader(
                title = strings.t("Amafoto", "Gallery"),
                icon = Icons.Rounded.PhotoLibrary,
                accentColor = Color(0xFFFF4081),
                onClose = onClose
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            ) {
                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = "Add Photo")
            }
        }
    ) { padding ->
        if (state.photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = strings.t("Nta mafoto ari muri Phone 2", "No photos in Phone 2 yet"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = strings.t(
                            "Kanda + wongere amafoto mu bubiko bwa 16 GB.",
                            "Tap + to import photos into your 16 GB virtual storage."
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.photos, key = { it.id }) { photo ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column {
                            AsyncImage(
                                model = photo.uriString,
                                contentDescription = photo.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.1f MB", photo.sizeMb),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp
                                )
                                IconButton(
                                    onClick = { viewModel.deletePhoto(photo.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
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
fun NotesApp(
    state: YvanOsUiState,
    viewModel: YvanViewModel,
    onClose: () -> Unit
) {
    val strings = state.strings
    var editingNote by remember { mutableStateOf<VirtualNoteEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var rootLocked by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            VirtualAppHeader(
                title = strings.t("Inyandiko", "Notes"),
                icon = Icons.Rounded.EditNote,
                accentColor = Color(0xFFFFD54F),
                onClose = onClose
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingNote = null
                    title = ""
                    content = ""
                    rootLocked = false
                    showEditor = true
                }
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "New Note")
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
            items(state.notes, key = { it.id }) { note ->
                val blocked = note.isRootProtected && !state.config.isRootEnabled
                Surface(
                    onClick = {
                        if (blocked) {
                            viewModel.showToast(strings.t("Iyi nyandiko irinzwe na Root (# su)!", "Note requires Root (# su) active!"))
                        } else {
                            editingNote = note
                            title = note.title
                            content = note.content
                            rootLocked = note.isRootProtected
                            showEditor = true
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(note.title, style = MaterialTheme.typography.titleMedium)
                                if (note.isRootProtected) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        Icons.Rounded.Lock,
                                        contentDescription = "Root Protected",
                                        tint = if (blocked) RootWarningRed else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (blocked) "•••••• (Root Locked)" else note.content,
                                maxLines = 2,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.deleteNote(note.id) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(strings.t("Inyandiko", "Note Editor")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(strings.t("Umutwe", "Title")) }, singleLine = true)
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text(strings.t("Ibirimo", "Body")) }, modifier = Modifier.height(140.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = rootLocked, onCheckedChange = { rootLocked = it })
                        Text(strings.t("Rinda na Root (# su)", "Protect with Root (# su)"))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.saveNote(editingNote?.id ?: 0, title.trim(), content, rootLocked)
                        showEditor = false
                    }
                }) { Text(strings.t("Bika", "Save")) }
            },
            dismissButton = {
                TextButton(onClick = { showEditor = false }) { Text(strings.t("Hagarika", "Cancel")) }
            }
        )
    }
}

@Composable
fun ClockApp(
    state: YvanOsUiState,
    onClose: () -> Unit
) {
    val strings = state.strings
    var tab by remember { mutableIntStateOf(0) }
    var now by remember { mutableStateOf(Date()) }
    var swRunning by remember { mutableStateOf(false) }
    var swElapsedMs by remember { mutableLongStateOf(0L) }
    var timerSec by remember { mutableIntStateOf(60) }
    var timerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(1000)
        }
    }

    LaunchedEffect(swRunning) {
        while (swRunning) {
            delay(100)
            swElapsedMs += 100
        }
    }

    LaunchedEffect(timerRunning) {
        while (timerRunning && timerSec > 0) {
            delay(1000)
            timerSec -= 1
            if (timerSec == 0) timerRunning = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        VirtualAppHeader(
            title = strings.t("Isaha", "Clock"),
            icon = Icons.Rounded.Schedule,
            accentColor = Color(0xFF40C4FF),
            onClose = onClose
        )

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(strings.t("Isaha", "Clock")) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Stopwatch") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Timer") })
        }

        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            when (tab) {
                0 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now),
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(now),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                1 -> {
                    val secs = (swElapsedMs / 1000) % 60
                    val mins = (swElapsedMs / 60000)
                    val tenths = (swElapsedMs / 100) % 10
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text(
                            text = String.format(Locale.US, "%02d:%02d.%d", mins, secs, tenths),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { swRunning = !swRunning }) {
                                Text(if (swRunning) strings.t("Hagarika", "Pause") else strings.t("Tangira", "Start"))
                            }
                            OutlinedButton(onClick = { swRunning = false; swElapsedMs = 0L }) {
                                Text("Reset")
                            }
                        }
                    }
                }
                2 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", timerSec / 60, timerSec % 60),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timerSec == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { timerSec = (timerSec + 30).coerceAtMost(3600) }) { Text("+30s") }
                            OutlinedButton(onClick = { timerSec = (timerSec + 60).coerceAtMost(3600) }) { Text("+1m") }
                            OutlinedButton(onClick = { timerRunning = false; timerSec = 60 }) { Text("Reset") }
                        }
                        Button(onClick = { if (timerSec > 0) timerRunning = !timerRunning }) {
                            Text(if (timerRunning) strings.t("Hagarika", "Pause") else strings.t("Tangira", "Start"))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorApp(
    state: YvanOsUiState,
    onClose: () -> Unit
) {
    val strings = state.strings
    var display by remember { mutableStateOf("0") }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var clearOnNext by remember { mutableStateOf(false) }

    fun pressDigit(d: String) {
        if (clearOnNext || display == "0") {
            display = if (d == ".") "0." else d
            clearOnNext = false
        } else {
            if (d == "." && display.contains(".")) return
            display += d
        }
    }

    fun evaluate() {
        val a = firstOperand ?: return
        val op = pendingOp ?: return
        val b = display.toDoubleOrNull() ?: return
        val res = when (op) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "÷" -> if (b != 0.0) a / b else Double.NaN
            else -> b
        }
        display = if (res.isNaN()) "Error" else if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.4f", res).trimEnd('0').trimEnd('.')
        firstOperand = null
        pendingOp = null
        clearOnNext = true
    }

    fun pressOp(op: String) {
        if (firstOperand != null && !clearOnNext) evaluate()
        firstOperand = display.toDoubleOrNull()
        pendingOp = op
        clearOnNext = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        VirtualAppHeader(
            title = strings.t("Kubara", "Calculator"),
            icon = Icons.Rounded.Calculate,
            accentColor = Color(0xFF69F0AE),
            onClose = onClose
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = display,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 20.dp)
            )

            val rows = listOf(
                listOf("C", "⌫", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        val isEquals = key == "="
                        val isOp = key in listOf("÷", "×", "-", "+", "%")
                        Surface(
                            onClick = {
                                when (key) {
                                    "C" -> { display = "0"; firstOperand = null; pendingOp = null }
                                    "⌫" -> display = if (display.length > 1) display.dropLast(1) else "0"
                                    "%" -> display = ((display.toDoubleOrNull() ?: 0.0) / 100.0).toString()
                                    "=" -> evaluate()
                                    "÷", "×", "-", "+" -> pressOp(key)
                                    else -> pressDigit(key)
                                }
                            },
                            modifier = Modifier
                                .weight(if (key == "0") 2f else 1f)
                                .height(64.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = when {
                                isEquals -> MaterialTheme.colorScheme.primary
                                isOp -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = key,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isEquals) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
