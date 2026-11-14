package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class YvanRepository(
    private val dao: YvanDao,
    private val prefs: YvanOsPreferences
) {
    val configFlow: Flow<YvanOsConfig> = prefs.configFlow
    val appsFlow: Flow<List<VirtualAppEntity>> = dao.observeAllApps()
    val filesFlow: Flow<List<VirtualFileEntity>> = dao.observeAllFiles()
    val contactsFlow: Flow<List<VirtualContactEntity>> = dao.observeContacts()
    val messagesFlow: Flow<List<VirtualMessageEntity>> = dao.observeMessages()
    val callLogsFlow: Flow<List<VirtualCallLogEntity>> = dao.observeCallLogs()
    val notesFlow: Flow<List<VirtualNoteEntity>> = dao.observeNotes()
    val photosFlow: Flow<List<VirtualPhotoEntity>> = dao.observePhotos()
    val rootLogsFlow: Flow<List<RootLogEntity>> = dao.observeRootLogs()

    val storageFlow: Flow<StorageBreakdown> = combine(
        appsFlow,
        filesFlow,
        photosFlow,
        configFlow
    ) { apps, files, photos, config ->
        val appsMb = apps.filter { it.isInstalled }.sumOf { it.sizeMb.toDouble() }.toFloat()
        val filesMb = files.sumOf { it.sizeKb.toDouble() }.toFloat() / 1024f
        val photosMb = photos.sumOf { it.sizeMb.toDouble() }.toFloat()
        StorageBreakdown(
            totalMb = 16384f,
            systemOsMb = 2460f,
            appsMb = appsMb,
            filesAndMediaMb = filesMb + photosMb,
            cacheMb = config.cacheMb
        )
    }

    suspend fun seedIfNeeded() {
        if (dao.getAppsCount() == 0) {
            dao.upsertApps(defaultVirtualApps())
        }
        if (dao.getFilesCount() == 0) {
            dao.insertFiles(defaultVirtualFiles())
            dao.insertContacts(defaultContacts())
            dao.insertMessages(defaultMessages())
            dao.insertNotes(defaultNotes())
            dao.insertRootLog(
                RootLogEntity(
                    appName = "YvanSU Daemon",
                    packageId = "com.yvan.root",
                    command = "init_su_bridge --mount /system rw",
                    granted = true
                )
            )
        }
    }

    suspend fun updateConfig(transform: (YvanOsConfig) -> YvanOsConfig) {
        prefs.updateConfig(transform)
    }

    suspend fun logRootAction(appName: String, packageId: String, command: String, granted: Boolean) {
        dao.insertRootLog(
            RootLogEntity(
                appName = appName,
                packageId = packageId,
                command = command,
                granted = granted
            )
        )
    }

    suspend fun clearRootLogs() = dao.clearRootLogs()

    suspend fun installApp(app: VirtualAppEntity): Result<Unit> {
        val storage = storageFlow.first()
        if (app.sizeMb > storage.freeMb) {
            return Result.failure(IllegalStateException("STORAGE_FULL"))
        }
        dao.upsertApp(app.copy(isInstalled = true, isPinnedToHome = true))
        prefs.updateConfig { it.copy(cacheMb = (it.cacheMb + 8f).coerceAtMost(950f)) }
        return Result.success(Unit)
    }

    suspend fun installCustomApp(
        name: String,
        category: String,
        sizeMb: Float,
        requiresRoot: Boolean,
        payload: String
    ): Result<VirtualAppEntity> {
        val storage = storageFlow.first()
        if (sizeMb > storage.freeMb) {
            return Result.failure(IllegalStateException("STORAGE_FULL"))
        }
        val cleanSlug = name.lowercase().replace(Regex("[^a-z0-9]"), "").ifEmpty { "custom" }
        val pkg = "com.yvan.user.$cleanSlug.${System.currentTimeMillis() % 10000}"
        val entity = VirtualAppEntity(
            packageId = pkg,
            nameEn = name,
            nameRw = name,
            descriptionEn = "User-installed virtual app in Phone 2",
            descriptionRw = "App washyizemo wowe muri Phone 2",
            category = category,
            iconKey = "custom",
            colorHex = 0xFF00B0FF,
            sizeMb = sizeMb,
            versionName = "1.0-user",
            isSystemApp = false,
            isInstalled = true,
            hasRootAccess = requiresRoot,
            isPinnedToHome = true,
            customPayload = payload
        )
        dao.upsertApp(entity)
        return Result.success(entity)
    }

    suspend fun uninstallApp(app: VirtualAppEntity) {
        if (app.isSystemApp) return
        if (app.category == "CUSTOM") {
            dao.deleteAppById(app.packageId)
        } else {
            dao.upsertApp(app.copy(isInstalled = false, hasRootAccess = false))
        }
    }

    suspend fun toggleAppRoot(app: VirtualAppEntity, granted: Boolean) {
        dao.upsertApp(app.copy(hasRootAccess = granted))
        logRootAction(
            appName = app.nameEn,
            packageId = app.packageId,
            command = if (granted) "su --grant ${app.packageId}" else "su --revoke ${app.packageId}",
            granted = granted
        )
    }

    suspend fun wipeCache() {
        prefs.updateConfig { it.copy(cacheMb = 12f) }
        logRootAction("YvanSU Cleaner", "com.yvan.root", "rm -rf /cache/* && fstrim /data", true)
    }

    suspend fun createFile(
        parentPath: String,
        name: String,
        isDirectory: Boolean,
        content: String,
        requiresRoot: Boolean
    ): Result<Unit> {
        val sizeKb = if (isDirectory) 4L else (content.toByteArray().size / 1024L).coerceAtLeast(1L)
        val storage = storageFlow.first()
        if (sizeKb / 1024f > storage.freeMb) {
            return Result.failure(IllegalStateException("STORAGE_FULL"))
        }
        dao.insertFile(
            VirtualFileEntity(
                parentPath = parentPath,
                name = name,
                isDirectory = isDirectory,
                content = content,
                sizeKb = sizeKb,
                requiresRoot = requiresRoot
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateFileContent(file: VirtualFileEntity, newContent: String) {
        val newKb = (newContent.toByteArray().size / 1024L).coerceAtLeast(1L)
        dao.updateFile(
            file.copy(
                content = newContent,
                sizeKb = newKb,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteFile(file: VirtualFileEntity) {
        dao.deleteFileById(file.id)
    }

    suspend fun addContact(name: String, phoneNumber: String, label: String) {
        dao.insertContact(VirtualContactEntity(name = name, phoneNumber = phoneNumber, label = label))
    }

    suspend fun deleteContact(id: Int) = dao.deleteContactById(id)

    suspend fun logCall(number: String, contactName: String, direction: String, durationSec: Int) {
        dao.insertCallLog(
            VirtualCallLogEntity(
                number = number,
                contactName = contactName,
                direction = direction,
                durationSec = durationSec
            )
        )
    }

    suspend fun sendMessage(contactName: String, phoneNumber: String, body: String) {
        dao.insertMessage(
            VirtualMessageEntity(
                contactName = contactName,
                phoneNumber = phoneNumber,
                body = body,
                isIncoming = false
            )
        )
    }

    suspend fun deleteThread(phoneNumber: String) = dao.deleteThread(phoneNumber)

    suspend fun saveNote(id: Int = 0, title: String, content: String, isRootProtected: Boolean) {
        dao.insertNote(
            VirtualNoteEntity(
                id = id,
                title = title,
                content = content,
                isRootProtected = isRootProtected,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteNote(id: Int) = dao.deleteNoteById(id)

    suspend fun addPhoto(uriString: String, title: String, sizeMb: Float): Result<Unit> {
        val storage = storageFlow.first()
        if (sizeMb > storage.freeMb) {
            return Result.failure(IllegalStateException("STORAGE_FULL"))
        }
        dao.insertPhoto(VirtualPhotoEntity(uriString = uriString, title = title, sizeMb = sizeMb))
        return Result.success(Unit)
    }

    suspend fun deletePhoto(id: Int) = dao.deletePhotoById(id)

    suspend fun factoryResetPhone2() {
        dao.nukeApps()
        dao.nukeFiles()
        dao.nukeContacts()
        dao.nukeMessages()
        dao.nukeCallLogs()
        dao.nukeNotes()
        dao.nukePhotos()
        dao.clearRootLogs()
        prefs.resetAll()
        seedIfNeeded()
    }

    private fun defaultVirtualApps(): List<VirtualAppEntity> = listOf(
        // Dock & Core Phone Apps
        VirtualAppEntity(
            packageId = "com.yvan.phone",
            nameEn = "Phone",
            nameRw = "Telefoni",
            descriptionEn = "Dialer, virtual contacts & call logs for Phone 2",
            descriptionRw = "Hamagara, abantu bawe n'amateka ya telefoni",
            category = "CORE",
            iconKey = "phone",
            colorHex = 0xFF00E676,
            sizeMb = 64f,
            versionName = "15.2.0",
            isSystemApp = true,
            isInstalled = true,
            hasRootAccess = false,
            isPinnedToDock = true
        ),
        VirtualAppEntity(
            packageId = "com.yvan.messages",
            nameEn = "Messages",
            nameRw = "Ubutumwa",
            descriptionEn = "Phone 2 virtual SMS & encrypted message threads",
            descriptionRw = "Ohereza kandi ubike ubutumwa muri Phone 2",
            category = "CORE",
            iconKey = "messages",
            colorHex = 0xFF29B6F6,
            sizeMb = 58f,
            versionName = "15.1.4",
            isSystemApp = true,
            isInstalled = true,
            hasRootAccess = false,
            isPinnedToDock = true
        ),
        VirtualAppEntity(
            packageId = "com.yvan.root",
            nameEn = "YvanSU Root",
            nameRw = "YvanSU Root",
            descriptionEn = "Superuser manager, kernel tuner, su logs & permissions",
            descriptionRw = "Cunga Root (# su), CPU, RAM na permissions",
            category = "ROOT",
            iconKey = "root",
            colorHex = 0xFF00E5FF,
            sizeMb = 128f,
            versionName = "4.2-su",
            isSystemApp = true,
            isInstalled = true,
            hasRootAccess = true,
            isPinnedToDock = true
        ),
        VirtualAppEntity(
            packageId = "com.yvan.store",
            nameEn = "Yvan Store",
            nameRw = "Yvan Store",
            descriptionEn = "Install or sideload apps into your 16 GB partition",
            descriptionRw = "Shyira app nshya cyangwa uzikure muri 16 GB",
            category = "CORE",
            iconKey = "store",
            colorHex = 0xFFFFAB40,
            sizeMb = 96f,
            versionName = "3.8.0",
            isSystemApp = true,
            isInstalled = true,
            hasRootAccess = false,
            isPinnedToDock = true
        ),
        // Home Screen & Utility Apps
        VirtualAppEntity(
            packageId = "com.yvan.terminal",
            nameEn = "Terminal #",
            nameRw = "Terminal #",
            descriptionEn = "Root shell console (su, df, getprop, pm, reboot)",
            descriptionRw = "Andika amategeko ya Root shell muri Phone 2",
            category = "ROOT",
            iconKey = "terminal",
            colorHex = 0xFF1DE9B6,
            sizeMb = 45f,
            versionName = "2.9.1",
            isSystemApp = true,
            isInstalled = true,
            hasRootAccess = true
        ),
        VirtualAppEntity(
            packageId = "com.yvan.files",
            nameEn = "Root Files",
            nameRw = "Ububiko / Files",
            descriptionEn = "Browse /system, /etc, /sdcard inside 16 GB storage",
            descriptionRw = "Genzura amadosiye ya /system na /sdcard (16 GB)",
            category = "ROOT",
            iconKey = "files",
            colorHex = 0xFFFFCA28,
            sizeMb = 72f,
            versionName = "5.0.3",
            isSystemApp = true,
            isInstalled = true,
            hasRootAccess = true
        ),
        VirtualAppEntity(
            packageId = "com.yvan.settings",
            nameEn = "OS Settings",
            nameRw = "Igenamiterere",
            descriptionEn = "Customize Yvan android name, theme, PIN & storage",
            descriptionRw = "Hindura izina, theme, PIN, ururimi na sisitemu",
            category = "CORE",
            iconKey = "settings",
            colorHex = 0xFF7C4DFF,
            sizeMb = 84f,
            versionName = "15.0",
            isSystemApp = true,
            isInstalled = true,
            hasRootAccess = true
        ),
        VirtualAppEntity(
            packageId = "com.yvan.gallery",
            nameEn = "Gallery",
            nameRw = "Amafoto",
            descriptionEn = "Phone 2 private photo vault inside 16 GB storage",
            descriptionRw = "Bika amafoto muri Phone 2 mu bubiko bwa 16 GB",
            category = "CORE",
            iconKey = "gallery",
            colorHex = 0xFFFF4081,
            sizeMb = 112f,
            versionName = "4.4",
            isSystemApp = true,
            isInstalled = true,
            hasRootAccess = false
        ),
        VirtualAppEntity(
            packageId = "com.yvan.notes",
            nameEn = "Notes",
            nameRw = "Inyandiko",
            descriptionEn = "Quick notes & root-locked private memos",
            descriptionRw = "Andika inyandiko n'amabanga afungiye muri Root",
            category = "UTILITY",
            iconKey = "notes",
            colorHex = 0xFFFFD54F,
            sizeMb = 34f,
            versionName = "2.1",
            isSystemApp = false,
            isInstalled = true,
            hasRootAccess = false
        ),
        VirtualAppEntity(
            packageId = "com.yvan.clock",
            nameEn = "Clock",
            nameRw = "Isaha",
            descriptionEn = "World clock, precision stopwatch & countdown timer",
            descriptionRw = "Isaha, stopwatch na timer yo kubara iminota",
            category = "UTILITY",
            iconKey = "clock",
            colorHex = 0xFF40C4FF,
            sizeMb = 28f,
            versionName = "3.0",
            isSystemApp = false,
            isInstalled = true,
            hasRootAccess = false
        ),
        VirtualAppEntity(
            packageId = "com.yvan.calc",
            nameEn = "Calculator",
            nameRw = "Kubara",
            descriptionEn = "Fast scientific & developer expression calculator",
            descriptionRw = "Imashini ibara vuba imibare yose",
            category = "UTILITY",
            iconKey = "calc",
            colorHex = 0xFF69F0AE,
            sizeMb = 22f,
            versionName = "2.4",
            isSystemApp = false,
            isInstalled = true,
            hasRootAccess = false
        ),
        VirtualAppEntity(
            packageId = "com.yvan.sensors",
            nameEn = "Sensors",
            nameRw = "Ibipimo",
            descriptionEn = "Live hardware telemetry, accelerometer & compass",
            descriptionRw = "Ibipimo bya telefoni, compass na accelerometer",
            category = "UTILITY",
            iconKey = "sensors",
            colorHex = 0xFF00BFA5,
            sizeMb = 38f,
            versionName = "1.8",
            isSystemApp = false,
            isInstalled = true,
            hasRootAccess = false
        ),
        VirtualAppEntity(
            packageId = "com.yvan.browser",
            nameEn = "Browser",
            nameRw = "Urubuga",
            descriptionEn = "Phone 2 fast web portal & developer bookmarks",
            descriptionRw = "Fungura imbuga za internet n'ibyo ukunda",
            category = "CORE",
            iconKey = "browser",
            colorHex = 0xFF448AFF,
            sizeMb = 104f,
            versionName = "6.2",
            isSystemApp = false,
            isInstalled = true,
            hasRootAccess = false
        ),
        // Store Catalog Apps (Not yet installed — user can install into 16 GB!)
        VirtualAppEntity(
            packageId = "com.yvan.kernel.tuner",
            nameEn = "Kernel Tuner Pro",
            nameRw = "Kernel Tuner Pro",
            descriptionEn = "Overclock CPU profiles, I/O scheduler & thermal controls",
            descriptionRw = "Ongera umuvuduko wa CPU n'imikorere ya kernel",
            category = "ROOT",
            iconKey = "root",
            colorHex = 0xFFFF5252,
            sizeMb = 185f,
            versionName = "2.5-root",
            isSystemApp = false,
            isInstalled = false,
            hasRootAccess = true
        ),
        VirtualAppEntity(
            packageId = "com.yvan.ram.booster",
            nameEn = "ZRAM Turbo",
            nameRw = "ZRAM Turbo",
            descriptionEn = "Compress virtual memory swap & trim /cache partition",
            descriptionRw = "Tunganya RAM na cache kugira Phone 2 yihute",
            category = "ROOT",
            iconKey = "ram",
            colorHex = 0xFF00E676,
            sizeMb = 92f,
            versionName = "1.9",
            isSystemApp = false,
            isInstalled = false,
            hasRootAccess = true
        ),
        VirtualAppEntity(
            packageId = "com.yvan.dev.inspector",
            nameEn = "Build.prop Studio",
            nameRw = "Build.prop Studio",
            descriptionEn = "Direct system property editor & root config injector",
            descriptionRw = "Hindura build.prop ya sisitemu mu buryo bworoshye",
            category = "ROOT",
            iconKey = "terminal",
            colorHex = 0xFF7C4DFF,
            sizeMb = 110f,
            versionName = "3.1",
            isSystemApp = false,
            isInstalled = false,
            hasRootAccess = true
        ),
        VirtualAppEntity(
            packageId = "com.yvan.vault.notes",
            nameEn = "Root Secret Vault",
            nameRw = "Ububiko bw'Ibanga",
            descriptionEn = "Zero-trace encrypted locker protected by su & PIN",
            descriptionRw = "Bika amabanga yawe arinzwe na Root na PIN",
            category = "UTILITY",
            iconKey = "vault",
            colorHex = 0xFFFFAB40,
            sizeMb = 240f,
            versionName = "4.0",
            isSystemApp = false,
            isInstalled = false,
            hasRootAccess = true
        ),
        VirtualAppEntity(
            packageId = "com.yvan.game.cyber",
            nameEn = "Cyber Reflex",
            nameRw = "Umukino Cyber",
            descriptionEn = "Fast neon reaction mini-game running inside Phone 2",
            descriptionRw = "Umukino wihuta wo kwishimisha muri Phone 2",
            category = "UTILITY",
            iconKey = "game",
            colorHex = 0xFFE040FB,
            sizeMb = 410f,
            versionName = "1.4",
            isSystemApp = false,
            isInstalled = false,
            hasRootAccess = false
        ),
        VirtualAppEntity(
            packageId = "com.yvan.backup.rom",
            nameEn = "ROM Snapshot",
            nameRw = "ROM Backup",
            descriptionEn = "Export & audit your 16 GB virtual partition status",
            descriptionRw = "Reba imiterere y'ububiko bwa 16 GB na backup",
            category = "ROOT",
            iconKey = "files",
            colorHex = 0xFF00B0FF,
            sizeMb = 560f,
            versionName = "2.0",
            isSystemApp = false,
            isInstalled = false,
            hasRootAccess = true
        )
    )

    private fun defaultVirtualFiles(): List<VirtualFileEntity> = listOf(
        VirtualFileEntity(parentPath = "/", name = "system", isDirectory = true, sizeKb = 4L, requiresRoot = true),
        VirtualFileEntity(parentPath = "/", name = "etc", isDirectory = true, sizeKb = 4L, requiresRoot = true),
        VirtualFileEntity(parentPath = "/", name = "data", isDirectory = true, sizeKb = 4L, requiresRoot = true),
        VirtualFileEntity(parentPath = "/", name = "sdcard", isDirectory = true, sizeKb = 4L, requiresRoot = false),
        VirtualFileEntity(
            parentPath = "/system",
            name = "build.prop",
            isDirectory = false,
            sizeKb = 18L,
            requiresRoot = true,
            content = """
                # Yvan android System Properties
                ro.product.brand=Yvan
                ro.product.model=Yvan Root Pro 16G
                ro.build.display.id=YvanOS_15.0_ROOT
                ro.yvan.storage.total=16GB
                ro.yvan.root.su=enabled
                persist.sys.timezone=Africa/Kigali
                dalvik.vm.heapsize=512m
            """.trimIndent()
        ),
        VirtualFileEntity(
            parentPath = "/system",
            name = "xbin_su_readme.txt",
            isDirectory = false,
            sizeKb = 6L,
            requiresRoot = true,
            content = "YvanSU binary bridge v4.2 active. UID=0 (root) available for authorized virtual apps."
        ),
        VirtualFileEntity(
            parentPath = "/etc",
            name = "hosts",
            isDirectory = false,
            sizeKb = 4L,
            requiresRoot = true,
            content = "127.0.0.1 localhost\n::1 ip6-localhost\n10.0.2.2 yvan-phone2.local"
        ),
        VirtualFileEntity(parentPath = "/data", name = "local", isDirectory = true, sizeKb = 4L, requiresRoot = true),
        VirtualFileEntity(parentPath = "/sdcard", name = "Download", isDirectory = true, sizeKb = 4L, requiresRoot = false),
        VirtualFileEntity(parentPath = "/sdcard", name = "DCIM", isDirectory = true, sizeKb = 4L, requiresRoot = false),
        VirtualFileEntity(
            parentPath = "/sdcard",
            name = "Ikaze_Yvan_Android.txt",
            isDirectory = false,
            sizeKb = 12L,
            requiresRoot = false,
            content = "Murakaza neza muri Yvan android (Phone 2)!\nIyi ni telefoni yawe ya kabiri ifite ububiko bwa 16 GB na Root access.\nUshobora gushyiramo app, guhindura amadosiye ya /system, gukoresha Terminal #, no guhindura byose muri Settings."
        ),
        VirtualFileEntity(
            parentPath = "/sdcard/Download",
            name = "yvan_root_guide.md",
            isDirectory = false,
            sizeKb = 24L,
            requiresRoot = false,
            content = "# Yvan android Root Guide\n- Run `whoami` in Terminal #\n- Run `df -h` to inspect 16 GB partition\n- Toggle per-app su permissions in YvanSU Root"
        )
    )

    private fun defaultContacts(): List<VirtualContactEntity> = listOf(
        VirtualContactEntity(name = "Yvan Support", phoneNumber = "+250 788 000 101", label = "YvanOS"),
        VirtualContactEntity(name = "Aline Tech", phoneNumber = "+250 788 123 456", label = "Mobile"),
        VirtualContactEntity(name = "Eric Dev", phoneNumber = "+250 722 987 654", label = "Work")
    )

    private fun defaultMessages(): List<VirtualMessageEntity> = listOf(
        VirtualMessageEntity(
            contactName = "Yvan Support",
            phoneNumber = "+250 788 000 101",
            body = "Murakaza neza muri Yvan android! Telefoni yawe ya 2 ifite Root na 16 GB yiteguye.",
            isIncoming = true
        ),
        VirtualMessageEntity(
            contactName = "Aline Tech",
            phoneNumber = "+250 788 123 456",
            body = "Ese wagenzuye Terminal # na YvanSU Root muri Phone 2?",
            isIncoming = true
        )
    )

    private fun defaultNotes(): List<VirtualNoteEntity> = listOf(
        VirtualNoteEntity(
            title = "Gahunda ya Phone 2",
            content = "1. Fungura YvanSU Root\n2. Genzura ububiko bwa 16 GB\n3. Shyiramo app nshya muri Yvan Store",
            isRootProtected = false
        ),
        VirtualNoteEntity(
            title = "Amabanga ya Root #",
            content = "UID 0 (root) ifunguye. Ushobora guhindura /system/build.prop muri Root Files.",
            isRootProtected = true
        )
    )
}
