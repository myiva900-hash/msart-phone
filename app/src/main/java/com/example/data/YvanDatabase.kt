package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VirtualAppEntity::class,
        VirtualFileEntity::class,
        VirtualContactEntity::class,
        VirtualMessageEntity::class,
        VirtualCallLogEntity::class,
        VirtualNoteEntity::class,
        VirtualPhotoEntity::class,
        RootLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class YvanDatabase : RoomDatabase() {
    abstract fun yvanDao(): YvanDao

    companion object {
        @Volatile
        private var INSTANCE: YvanDatabase? = null

        fun getInstance(context: Context): YvanDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    YvanDatabase::class.java,
                    "yvan_android_phone2.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
