package com.example.ui.apps

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.YvanOsUiState
import com.example.ui.YvanViewModel
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RootSuccessGreen
import com.example.ui.theme.RootWarningRed
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlinx.coroutines.delay

@Composable
fun DialerApp(
    state: YvanOsUiState,
    viewModel: YvanViewModel,
    onClose: () -> Unit
) {
    val strings = state.strings
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var dialNumber by remember { mutableStateOf("") }
    var activeCallNumber by remember { mutableStateOf<String?>(null) }
    var activeCallSeconds by remember { mutableIntStateOf(0) }
    var showAddContact by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

    LaunchedEffect(activeCallNumber) {
        if (activeCallNumber != null) {
            activeCallSeconds = 0
            while (activeCallNumber != null) {
                delay(1000)
                activeCallSeconds += 1
            }
        }
    }

    if (activeCallNumber != null) {
        val number = activeCallNumber!!
        val contactName = state.contacts.firstOrNull { it.phoneNumber.replace(" ", "") == number.replace(" ", "") }?.name ?: number
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07111C))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(36.dp))
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(contactName, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(number, fontFamily = JetBrainsMonoFontFamily, color = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.height(10.dp))
                Text(
                    text = String.format("%02d:%02d • Phone 2 Line", activeCallSeconds / 60, activeCallSeconds % 60),
                    fontFamily = JetBrainsMonoFontFamily,
                    color = RootSuccessGreen
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                        context.startActivity(intent)
                    }
                ) {
                    Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.t("Hamagara kuri Telefoni nyayo", "Dial via Real Phone"))
                }

                Button(
                    onClick = {
                        viewModel.logCall(number, contactName, activeCallSeconds)
                        activeCallNumber = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RootWarningRed),
                    shape = CircleShape,
                    modifier = Modifier.size(68.dp)
                ) {
                    Icon(Icons.Rounded.CallEnd, contentDescription = "End Call", tint = Color.White)
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        VirtualAppHeader(
            title = strings.t("Telefoni", "Phone"),
            icon = Icons.Rounded.Call,
            accentColor = Color(0xFF00E676),
            onClose = onClose
        )

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(strings.t("Imibare", "Keypad")) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(strings.t("Abantu", "Contacts")) })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(strings.t("Amateka", "Recents")) })
        }

        when (tab) {
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dialNumber.ifEmpty { strings.t("Andika nimero...", "Enter number...") },
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = if (dialNumber.isEmpty()) 20.sp else 30.sp,
                            color = if (dialNumber.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        if (dialNumber.isNotEmpty()) {
                            IconButton(onClick = { dialNumber = dialNumber.dropLast(1) }) {
                                Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Backspace")
                            }
                        }
                    }

                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("*", "0", "#")
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        keys.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                row.forEach { d ->
                                    Box(
                                        modifier = Modifier
                                            .size(66.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { dialNumber += d },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(d, fontFamily = SpaceGroteskFontFamily, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                if (dialNumber.isNotBlank()) {
                                    activeCallNumber = dialNumber
                                }
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Icon(Icons.Rounded.Call, contentDescription = "Call")
                            Spacer(Modifier.width(8.dp))
                            Text(strings.t("Hamagara", "Call"))
                        }
                        OutlinedButton(
                            onClick = {
                                if (dialNumber.isNotBlank()) {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$dialNumber")))
                                }
                            },
                            modifier = Modifier.height(54.dp)
                        ) {
                            Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("SIM")
                        }
                    }
                }
            }

            1 -> {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showAddContact = true }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Contact")
                        }
                    }
                ) { p ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(p)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.contacts, key = { it.id }) { contact ->
                            Surface(
                                onClick = { activeCallNumber = contact.phoneNumber },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(contact.name, style = MaterialTheme.typography.titleMedium)
                                        Text(contact.phoneNumber, fontFamily = JetBrainsMonoFontFamily, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { viewModel.deleteContact(contact.id) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.callLogs, key = { it.id }) { log ->
                        Surface(
                            onClick = { activeCallNumber = log.number },
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
                                    Text(log.contactName, style = MaterialTheme.typography.titleMedium)
                                    Text("${log.number} • ${log.durationSec}s", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp)
                                }
                                Icon(Icons.Rounded.Call, contentDescription = "Redial", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddContact) {
        AlertDialog(
            onDismissRequest = { showAddContact = false },
            title = { Text(strings.t("Ongeramo Umuntu", "New Contact")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text(strings.t("Izina", "Name")) }, singleLine = true)
                    OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text(strings.t("Nimero", "Phone Number")) }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank() && newPhone.isNotBlank()) {
                        viewModel.addContact(newName.trim(), newPhone.trim(), "Mobile")
                        newName = ""
                        newPhone = ""
                        showAddContact = false
                    }
                }) { Text(strings.t("Bika", "Save")) }
            },
            dismissButton = {
                TextButton(onClick = { showAddContact = false }) { Text(strings.t("Hagarika", "Cancel")) }
            }
        )
    }
}

@Composable
fun MessagesApp(
    state: YvanOsUiState,
    viewModel: YvanViewModel,
    onClose: () -> Unit
) {
    val strings = state.strings
    val context = LocalContext.current
    var recipientName by remember { mutableStateOf("Yvan Support") }
    var recipientPhone by remember { mutableStateOf("+250 788 000 101") }
    var messageBody by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        VirtualAppHeader(
            title = strings.t("Ubutumwa", "Messages"),
            icon = Icons.Rounded.Message,
            accentColor = Color(0xFF29B6F6),
            onClose = onClose
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.messages, key = { it.id }) { msg ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (msg.isIncoming) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (msg.isIncoming) msg.contactName else "Phone 2 -> ${msg.contactName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = msg.phoneNumber,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(msg.body, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text(strings.t("Izina", "To")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        label = { Text(strings.t("Nimero", "Number")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = messageBody,
                        onValueChange = { messageBody = it },
                        placeholder = { Text(strings.t("Andika ubutumwa...", "Type virtual SMS...")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = {
                        if (messageBody.isNotBlank()) {
                            viewModel.sendMessage(recipientName.ifBlank { recipientPhone }, recipientPhone, messageBody.trim())
                            messageBody = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        if (recipientPhone.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$recipientPhone")).apply {
                                putExtra("sms_body", messageBody)
                            }
                            context.startActivity(intent)
                        }
                    }) {
                        Icon(Icons.Rounded.OpenInNew, contentDescription = "Send via Real SMS")
                    }
                }
            }
        }
    }
}
