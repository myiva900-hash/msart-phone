package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "virtual_apps")
data class VirtualAppEntity(
    @PrimaryKey val packageId: String,
    val nameEn: String,
    val nameRw: String,
    val descriptionEn: String,
    val descriptionRw: String,
    val category: String, // "CORE", "ROOT", "UTILITY", "CUSTOM"
    val iconKey: String,
    val colorHex: Long,
    val sizeMb: Float,
    val versionName: String,
    val isSystemApp: Boolean,
    val isInstalled: Boolean,
    val hasRootAccess: Boolean,
    val isPinnedToDock: Boolean = false,
    val isPinnedToHome: Boolean = true,
    val customPayload: String = "" // URL or custom mini-app script/note
)

@Entity(tableName = "virtual_files")
data class VirtualFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val parentPath: String, // e.g., "/", "/system", "/etc", "/sdcard", "/sdcard/Download"
    val name: String,
    val isDirectory: Boolean,
    val content: String = "",
    val sizeKb: Long = 0L,
    val requiresRoot: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "virtual_contacts")
data class VirtualContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val label: String = "Mobile"
)

@Entity(tableName = "virtual_messages")
data class VirtualMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactName: String,
    val phoneNumber: String,
    val body: String,
    val isIncoming: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "virtual_call_logs")
data class VirtualCallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val number: String,
    val contactName: String,
    val direction: String, // "OUTGOING", "INCOMING"
    val durationSec: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "virtual_notes")
data class VirtualNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val isRootProtected: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "virtual_photos")
data class VirtualPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uriString: String,
    val title: String,
    val sizeMb: Float,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "root_logs")
data class RootLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val packageId: String,
    val command: String,
    val granted: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class StorageBreakdown(
    val totalMb: Float = 16384f, // Exact 16.0 GB (16 * 1024 MB)
    val systemOsMb: Float = 2460f, // ~2.40 GB Rooted System Partition
    val appsMb: Float = 0f,
    val filesAndMediaMb: Float = 0f,
    val cacheMb: Float = 148f
) {
    val usedMb: Float get() = (systemOsMb + appsMb + filesAndMediaMb + cacheMb).coerceAtMost(totalMb)
    val freeMb: Float get() = (totalMb - usedMb).coerceAtLeast(0f)
    val usedGb: Float get() = usedMb / 1024f
    val totalGb: Float get() = totalMb / 1024f
    val freeGb: Float get() = freeMb / 1024f
    val usedFraction: Float get() = (usedMb / totalMb).coerceIn(0f, 1f)
}
