package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RootLogEntity
import com.example.data.StorageBreakdown
import com.example.data.VirtualAppEntity
import com.example.data.VirtualCallLogEntity
import com.example.data.VirtualContactEntity
import com.example.data.VirtualFileEntity
import com.example.data.VirtualMessageEntity
import com.example.data.VirtualNoteEntity
import com.example.data.VirtualPhotoEntity
import com.example.data.YvanDatabase
import com.example.data.YvanLang
import com.example.data.YvanOsConfig
import com.example.data.YvanOsPreferences
import com.example.data.YvanRepository
import com.example.data.YvanStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

enum class OsPhase {
    BOOT_INTRO,
    LOCK_SCREEN,
    HOME,
    APP_DRAWER,
    ACTIVE_APP,
    RECENTS
}

data class TerminalLine(
    val text: String,
    val isCommand: Boolean = false,
    val isError: Boolean = false
)

data class YvanOsUiState(
    val config: YvanOsConfig = YvanOsConfig(),
    val apps: List<VirtualAppEntity> = emptyList(),
    val files: List<VirtualFileEntity> = emptyList(),
    val contacts: List<VirtualContactEntity> = emptyList(),
    val messages: List<VirtualMessageEntity> = emptyList(),
    val callLogs: List<VirtualCallLogEntity> = emptyList(),
    val notes: List<VirtualNoteEntity> = emptyList(),
    val photos: List<VirtualPhotoEntity> = emptyList(),
    val rootLogs: List<RootLogEntity> = emptyList(),
    val storage: StorageBreakdown = StorageBreakdown(),
    val isInitialized: Boolean = false
) {
    val strings: YvanStrings get() = YvanStrings(YvanLang.fromCode(config.languageCode))
    val installedApps: List<VirtualAppEntity> get() = apps.filter { it.isInstalled }
    val dockApps: List<VirtualAppEntity> get() = installedApps.filter { it.isPinnedToDock }.take(4)
    val homeGridApps: List<VirtualAppEntity> get() = installedApps.filter { !it.isPinnedToDock }
}

class YvanViewModel(application: Application) : AndroidViewModel(application) {
    private val db = YvanDatabase.getInstance(application)
    private val prefs = YvanOsPreferences(application)
    val repository = YvanRepository(db.yvanDao(), prefs)

    private val _initialized = MutableStateFlow(false)

    private val baseFlow = combine(
        repository.configFlow,
        repository.appsFlow,
        repository.filesFlow,
        repository.contactsFlow,
        repository.messagesFlow
    ) { config, apps, files, contacts, messages ->
        Quintuple(config, apps, files, contacts, messages)
    }

    private val extraFlow = combine(
        repository.callLogsFlow,
        repository.notesFlow,
        repository.photosFlow,
        repository.rootLogsFlow,
        repository.storageFlow
    ) { callLogs, notes, photos, rootLogs, storage ->
        Quintuple(callLogs, notes, photos, rootLogs, storage)
    }

    val osState: StateFlow<YvanOsUiState> = combine(
        baseFlow,
        extraFlow,
        _initialized
    ) { base, extra, ready ->
        YvanOsUiState(
            config = base.first,
            apps = base.second,
            files = base.third,
            contacts = base.fourth,
            messages = base.fifth,
            callLogs = extra.first,
            notes = extra.second,
            photos = extra.third,
            rootLogs = extra.fourth,
            storage = extra.fifth,
            isInitialized = ready
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = YvanOsUiState()
    )

    private val _phase = MutableStateFlow(OsPhase.BOOT_INTRO)
    val phase: StateFlow<OsPhase> = _phase.asStateFlow()

    private val _activeAppPackage = MutableStateFlow<String?>(null)
    val activeAppPackage: StateFlow<String?> = _activeAppPackage.asStateFlow()

    private val _recentPackages = MutableStateFlow<List<String>>(emptyList())
    val recentPackages: StateFlow<List<String>> = _recentPackages.asStateFlow()

    private val _isShadeOpen = MutableStateFlow(false)
    val isShadeOpen: StateFlow<Boolean> = _isShadeOpen.asStateFlow()

    private val _isPowerMenuOpen = MutableStateFlow(false)
    val isPowerMenuOpen: StateFlow<Boolean> = _isPowerMenuOpen.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _terminalLines = MutableStateFlow(
        listOf(
            TerminalLine("Yvan android Root Shell v4.2 (aarch64)"),
            TerminalLine("Type 'help' for commands (df -h, whoami, pm list, reboot).")
        )
    )
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
            val config = repository.configFlow.first()
            _phase.value = if (config.showBootIntroOnStart) OsPhase.BOOT_INTRO else OsPhase.LOCK_SCREEN
            _initialized.value = true
        }
    }

    fun finishBootIntro() {
        _phase.value = OsPhase.LOCK_SCREEN
    }

    fun replayBootIntro() {
        _isShadeOpen.value = false
        _isPowerMenuOpen.value = false
        _activeAppPackage.value = null
        _phase.value = OsPhase.BOOT_INTRO
    }

    fun unlockPhone2() {
        _phase.value = OsPhase.HOME
    }

    fun lockPhone2() {
        _isShadeOpen.value = false
        _isPowerMenuOpen.value = false
        _phase.value = OsPhase.LOCK_SCREEN
    }

    fun openAppDrawer() {
        _isShadeOpen.value = false
        _phase.value = OsPhase.APP_DRAWER
    }

    fun launchApp(packageId: String) {
        _isShadeOpen.value = false
        _isPowerMenuOpen.value = false
        _activeAppPackage.value = packageId
        _recentPackages.update { list ->
            listOf(packageId) + list.filterNot { it == packageId }
        }
        _phase.value = OsPhase.ACTIVE_APP
        viewModelScope.launch {
            val app = osState.value.apps.firstOrNull { it.packageId == packageId }
            if (app != null && app.hasRootAccess && osState.value.config.isRootEnabled) {
                repository.logRootAction(
                    appName = app.nameEn,
                    packageId = app.packageId,
                    command = "su -c launch ${app.packageId}",
                    granted = true
                )
            }
        }
    }

    fun goHome() {
        _isShadeOpen.value = false
        _isPowerMenuOpen.value = false
        _phase.value = OsPhase.HOME
    }

    fun openRecents() {
        _isShadeOpen.value = false
        _isPowerMenuOpen.value = false
        _phase.value = OsPhase.RECENTS
    }

    fun closeRecent(packageId: String) {
        _recentPackages.update { it.filterNot { pkg -> pkg == packageId } }
        if (_activeAppPackage.value == packageId) {
            _activeAppPackage.value = null
        }
    }

    fun clearAllRecents() {
        _recentPackages.value = emptyList()
        _activeAppPackage.value = null
        _phase.value = OsPhase.HOME
    }

    fun navigateBack(): Boolean {
        if (_isPowerMenuOpen.value) {
            _isPowerMenuOpen.value = false
            return true
        }
        if (_isShadeOpen.value) {
            _isShadeOpen.value = false
            return true
        }
        return when (_phase.value) {
            OsPhase.ACTIVE_APP, OsPhase.APP_DRAWER, OsPhase.RECENTS -> {
                _phase.value = OsPhase.HOME
                true
            }
            OsPhase.HOME -> false
            OsPhase.LOCK_SCREEN, OsPhase.BOOT_INTRO -> false
        }
    }

    fun setShadeOpen(open: Boolean) {
        _isShadeOpen.value = open
    }

    fun setPowerMenuOpen(open: Boolean) {
        _isPowerMenuOpen.value = open
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun updateConfig(transform: (YvanOsConfig) -> YvanOsConfig) {
        viewModelScope.launch {
            repository.updateConfig(transform)
        }
    }

    fun installApp(app: VirtualAppEntity) {
        viewModelScope.launch {
            val strings = osState.value.strings
            repository.installApp(app).onSuccess {
                showToast("${app.nameEn}: ${strings.installSuccess}")
            }.onFailure {
                showToast(strings.storageFullWarning)
            }
        }
    }

    fun installCustomApp(
        name: String,
        category: String,
        sizeMb: Float,
        requiresRoot: Boolean,
        payload: String
    ) {
        viewModelScope.launch {
            val strings = osState.value.strings
            repository.installCustomApp(name, category, sizeMb, requiresRoot, payload)
                .onSuccess {
                    showToast("$name: ${strings.installSuccess}")
                }
                .onFailure {
                    showToast(strings.storageFullWarning)
                }
        }
    }

    fun uninstallApp(app: VirtualAppEntity) {
        viewModelScope.launch {
            repository.uninstallApp(app)
            _recentPackages.update { it.filterNot { pkg -> pkg == app.packageId } }
            showToast("${app.nameEn}: ${osState.value.strings.uninstallSuccess}")
        }
    }

    fun toggleAppRoot(app: VirtualAppEntity, granted: Boolean) {
        viewModelScope.launch {
            if (!osState.value.config.isRootEnabled && granted) {
                showToast(osState.value.strings.t("Fungura Root Master mbere!", "Enable Root Master switch first!"))
                return@launch
            }
            repository.toggleAppRoot(app, granted)
        }
    }

    fun wipeCache() {
        viewModelScope.launch {
            repository.wipeCache()
            showToast(osState.value.strings.t("Cache yasibwe! Ububiko bwongewe.", "Cache wiped! Storage freed."))
        }
    }

    fun createFile(parentPath: String, name: String, isDir: Boolean, content: String, requiresRoot: Boolean) {
        viewModelScope.launch {
            repository.createFile(parentPath, name, isDir, content, requiresRoot)
                .onFailure { showToast(osState.value.strings.storageFullWarning) }
        }
    }

    fun updateFile(file: VirtualFileEntity, content: String) {
        viewModelScope.launch { repository.updateFileContent(file, content) }
    }

    fun deleteFile(file: VirtualFileEntity) {
        viewModelScope.launch { repository.deleteFile(file) }
    }

    fun addContact(name: String, phone: String, label: String) {
        viewModelScope.launch { repository.addContact(name, phone, label) }
    }

    fun deleteContact(id: Int) {
        viewModelScope.launch { repository.deleteContact(id) }
    }

    fun logCall(number: String, contactName: String, durationSec: Int) {
        viewModelScope.launch { repository.logCall(number, contactName, "OUTGOING", durationSec) }
    }

    fun sendMessage(contactName: String, phone: String, body: String) {
        viewModelScope.launch { repository.sendMessage(contactName, phone, body) }
    }

    fun deleteThread(phone: String) {
        viewModelScope.launch { repository.deleteThread(phone) }
    }

    fun saveNote(id: Int = 0, title: String, content: String, isRootProtected: Boolean) {
        viewModelScope.launch { repository.saveNote(id, title, content, isRootProtected) }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch { repository.deleteNote(id) }
    }

    fun addPhoto(uri: String, title: String, sizeMb: Float) {
        viewModelScope.launch {
            repository.addPhoto(uri, title, sizeMb)
                .onFailure { showToast(osState.value.strings.storageFullWarning) }
        }
    }

    fun deletePhoto(id: Int) {
        viewModelScope.launch { repository.deletePhoto(id) }
    }

    fun clearRootLogs() {
        viewModelScope.launch { repository.clearRootLogs() }
    }

    fun factoryReset() {
        viewModelScope.launch {
            repository.factoryResetPhone2()
            _recentPackages.value = emptyList()
            _activeAppPackage.value = null
            _phase.value = OsPhase.BOOT_INTRO
        }
    }

    fun runTerminalCommand(rawInput: String) {
        val cmd = rawInput.trim()
        if (cmd.isEmpty()) return

        val state = osState.value
        val isRoot = state.config.isRootEnabled
        val promptPrefix = if (isRoot) "root@yvan-phone2:/ #" else "u0_a142@yvan-phone2:/ $"

        val newLines = mutableListOf(TerminalLine("$promptPrefix $cmd", isCommand = true))
        val lower = cmd.lowercase(Locale.US)

        when {
            lower == "clear" -> {
                _terminalLines.value = listOf(TerminalLine("Console cleared."))
                return
            }
            lower == "help" -> {
                newLines += listOf(
                    TerminalLine("Available commands in Yvan android:"),
                    TerminalLine("  whoami        - Check UID (root vs shell)"),
                    TerminalLine("  su            - Toggle/verify superuser access"),
                    TerminalLine("  df -h         - Inspect 16 GB partition usage"),
                    TerminalLine("  free -m       - Show RAM & ZRAM status"),
                    TerminalLine("  uname -a      - Kernel & OS build info"),
                    TerminalLine("  pm list       - List installed Phone 2 apps"),
                    TerminalLine("  ls [path]     - List virtual files"),
                    TerminalLine("  cat <file>    - Read virtual file (e.g. /system/build.prop)"),
                    TerminalLine("  getprop       - Print OS properties"),
                    TerminalLine("  setprop name <val> - Rename Phone 2"),
                    TerminalLine("  wipe cache    - Trim /cache partition"),
                    TerminalLine("  reboot        - Replay Yvan android intro boot"),
                    TerminalLine("  clear         - Clear screen")
                )
            }
            lower == "whoami" -> {
                newLines += TerminalLine(if (isRoot) "root" else "u0_a142 (unprivileged)")
            }
            lower == "id" -> {
                newLines += TerminalLine(
                    if (isRoot) "uid=0(root) gid=0(root) groups=0(root),1004(input),3003(inet) context=u:r:su:s0"
                    else "uid=10142(u0_a142) gid=10142(u0_a142) context=u:r:untrusted_app:s0"
                )
            }
            lower == "su" -> {
                viewModelScope.launch {
                    repository.updateConfig { it.copy(isRootEnabled = true) }
                    repository.logRootAction("Terminal #", "com.yvan.terminal", "su --interactive", true)
                }
                newLines += TerminalLine("UID=0 (root) granted by YvanSU daemon.")
            }
            lower == "uname -a" -> {
                newLines += TerminalLine("Linux yvan-phone2 6.1.75-yvan-root #1 SMP PREEMPT aarch64 Android")
            }
            lower == "df -h" -> {
                val s = state.storage
                val usedGb = String.format(Locale.US, "%.2fG", s.usedGb)
                val freeGb = String.format(Locale.US, "%.2fG", s.freeGb)
                val pct = ((s.usedFraction) * 100).toInt()
                newLines += listOf(
                    TerminalLine("Filesystem      Size  Used Avail Use% Mounted on"),
                    TerminalLine("/dev/block/yvan  16G $usedGb $freeGb  $pct% /data"),
                    TerminalLine("  /system       2.4G  2.4G    0M 100% /system (rw)"),
                    TerminalLine("  /data/app      16G ${String.format(Locale.US, "%.0fM", s.appsMb)}  $freeGb   -  /data/app"),
                    TerminalLine("  /cache        950M ${String.format(Locale.US, "%.0fM", s.cacheMb)}  -     -  /cache")
                )
            }
            lower == "free -m" -> {
                newLines += listOf(
                    TerminalLine("               total        used        free"),
                    TerminalLine("Mem:            6144        2410        3734"),
                    TerminalLine("Swap (ZRAM):    ${state.config.zramMb}         312        ${(state.config.zramMb - 312).coerceAtLeast(0)}")
                )
            }
            lower == "pm list" || lower == "pm list packages" -> {
                state.installedApps.forEach { app ->
                    newLines += TerminalLine("package:${app.packageId} (${app.sizeMb.toInt()}MB)")
                }
            }
            lower.startsWith("ls") -> {
                val arg = cmd.removePrefix("ls").trim().ifEmpty { "/" }
                val matches = state.files.filter { it.parentPath == arg }
                if (matches.isEmpty()) {
                    newLines += TerminalLine("ls: $arg: Empty or directory not found", isError = true)
                } else {
                    matches.forEach { f ->
                        val tag = if (f.isDirectory) "drwxr-xr-x" else "-rw-r--r--"
                        newLines += TerminalLine("$tag  ${f.sizeKb}K  ${f.name}${if (f.isDirectory) "/" else ""}")
                    }
                }
            }
            lower.startsWith("cat ") -> {
                val target = cmd.removePrefix("cat ").trim()
                val file = state.files.firstOrNull {
                    val full = if (it.parentPath == "/") "/${it.name}" else "${it.parentPath}/${it.name}"
                    full == target || it.name == target
                }
                if (file == null || file.isDirectory) {
                    newLines += TerminalLine("cat: $target: No such file", isError = true)
                } else if (file.requiresRoot && !isRoot) {
                    newLines += TerminalLine("cat: $target: Permission denied (enable root with 'su')", isError = true)
                } else {
                    file.content.lines().forEach { line -> newLines += TerminalLine(line) }
                }
            }
            lower == "getprop" -> {
                newLines += listOf(
                    TerminalLine("[ro.product.name]: [${state.config.deviceName}]"),
                    TerminalLine("[ro.product.model]: [${state.config.deviceModel}]"),
                    TerminalLine("[ro.build.display.id]: [${state.config.osVersionLabel}]"),
                    TerminalLine("[ro.yvan.root.enabled]: [${state.config.isRootEnabled}]"),
                    TerminalLine("[sys.cpu.governor]: [${state.config.cpuGovernor}]"),
                    TerminalLine("[sys.cpu.max_freq]: [${state.config.cpuClockMhz}MHz]")
                )
            }
            lower.startsWith("setprop name ") -> {
                val newName = cmd.substringAfter("setprop name ").trim()
                if (!isRoot) {
                    newLines += TerminalLine("setprop: Permission denied (run 'su' first)", isError = true)
                } else if (newName.isNotEmpty()) {
                    viewModelScope.launch {
                        repository.updateConfig { it.copy(deviceName = newName) }
                        repository.logRootAction("Terminal #", "com.yvan.terminal", "setprop name $newName", true)
                    }
                    newLines += TerminalLine("Updated ro.product.name -> [$newName]")
                }
            }
            lower == "wipe cache" -> {
                if (!isRoot) {
                    newLines += TerminalLine("wipe: Permission denied (run 'su' first)", isError = true)
                } else {
                    wipeCache()
                    newLines += TerminalLine("Trimmed /cache partition down to 12 MB.")
                }
            }
            lower == "reboot" -> {
                newLines += TerminalLine("Rebooting Yvan android...")
                _terminalLines.update { it + newLines }
                replayBootIntro()
                return
            }
            else -> {
                newLines += TerminalLine("sh: $cmd: not found (type 'help')", isError = true)
            }
        }

        _terminalLines.update { (it + newLines).takeLast(120) }
    }
}

data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
