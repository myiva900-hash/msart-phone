package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface YvanDao {
    // Virtual Apps
    @Query("SELECT * FROM virtual_apps ORDER BY isPinnedToDock DESC, isSystemApp DESC, nameEn ASC")
    fun observeAllApps(): Flow<List<VirtualAppEntity>>

    @Query("SELECT COUNT(*) FROM virtual_apps")
    suspend fun getAppsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApps(apps: List<VirtualAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApp(app: VirtualAppEntity)

    @Query("DELETE FROM virtual_apps WHERE packageId = :packageId")
    suspend fun deleteAppById(packageId: String)

    // Virtual Files
    @Query("SELECT * FROM virtual_files ORDER BY isDirectory DESC, name ASC")
    fun observeAllFiles(): Flow<List<VirtualFileEntity>>

    @Query("SELECT COUNT(*) FROM virtual_files")
    suspend fun getFilesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<VirtualFileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VirtualFileEntity)

    @Update
    suspend fun updateFile(file: VirtualFileEntity)

    @Query("DELETE FROM virtual_files WHERE id = :id")
    suspend fun deleteFileById(id: Int)

    // Contacts
    @Query("SELECT * FROM virtual_contacts ORDER BY name ASC")
    fun observeContacts(): Flow<List<VirtualContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<VirtualContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: VirtualContactEntity)

    @Query("DELETE FROM virtual_contacts WHERE id = :id")
    suspend fun deleteContactById(id: Int)

    // Messages
    @Query("SELECT * FROM virtual_messages ORDER BY timestamp DESC")
    fun observeMessages(): Flow<List<VirtualMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<VirtualMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: VirtualMessageEntity)

    @Query("DELETE FROM virtual_messages WHERE phoneNumber = :phoneNumber")
    suspend fun deleteThread(phoneNumber: String)

    // Call Logs
    @Query("SELECT * FROM virtual_call_logs ORDER BY timestamp DESC")
    fun observeCallLogs(): Flow<List<VirtualCallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(log: VirtualCallLogEntity)

    // Notes
    @Query("SELECT * FROM virtual_notes ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<VirtualNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<VirtualNoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: VirtualNoteEntity)

    @Query("DELETE FROM virtual_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    // Photos
    @Query("SELECT * FROM virtual_photos ORDER BY addedAt DESC")
    fun observePhotos(): Flow<List<VirtualPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: VirtualPhotoEntity)

    @Query("DELETE FROM virtual_photos WHERE id = :id")
    suspend fun deletePhotoById(id: Int)

    // Root Logs
    @Query("SELECT * FROM root_logs ORDER BY timestamp DESC LIMIT 100")
    fun observeRootLogs(): Flow<List<RootLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRootLog(log: RootLogEntity)

    @Query("DELETE FROM root_logs")
    suspend fun clearRootLogs()

    // Full reset helper
    @Query("DELETE FROM virtual_apps")
    suspend fun nukeApps()

    @Query("DELETE FROM virtual_files")
    suspend fun nukeFiles()

    @Query("DELETE FROM virtual_contacts")
    suspend fun nukeContacts()

    @Query("DELETE FROM virtual_messages")
    suspend fun nukeMessages()

    @Query("DELETE FROM virtual_call_logs")
    suspend fun nukeCallLogs()

    @Query("DELETE FROM virtual_notes")
    suspend fun nukeNotes()

    @Query("DELETE FROM virtual_photos")
    suspend fun nukePhotos()
}
