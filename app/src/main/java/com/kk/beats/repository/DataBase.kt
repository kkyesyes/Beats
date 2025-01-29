package com.kk.beats.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kk.beats.repository.dao.ArgsGroupDao
import com.kk.beats.repository.entity.ArgsGroup

@Database(
    version = 1,
    entities = [ArgsGroup::class],
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var db: AppDatabase? = null
        private val name = "app_db"

        fun getDB(context: Context) = if (db == null) {
            Room.databaseBuilder(context, AppDatabase::class.java, name)
                .fallbackToDestructiveMigration() // 删除旧数据库并创建新表
                .build()
                .apply {
                    db = this
                }
        } else {
            db!!
        }
    }

    abstract fun getArgsGroupDao(): ArgsGroupDao
}