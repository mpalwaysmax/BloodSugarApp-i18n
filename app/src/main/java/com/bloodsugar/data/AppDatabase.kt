package com.bloodsugar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Record::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration 占位：修改 Record 表结构时在这里加 Migration(oldVersion, newVersion)
         * 示例：val MIGRATION_1_2 = object : Migration(1, 2) {
         *     override fun migrate(db: SupportSQLiteDatabase) {
         *         db.execSQL("ALTER TABLE records ADD COLUMN xxx TEXT DEFAULT ''")
         *     }
         * }
         */
        private val ALL_MIGRATIONS = arrayOf<Migration>()

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "blood_sugar.db"
                )
                    .apply {
                        if (ALL_MIGRATIONS.isNotEmpty()) {
                            addMigrations(*ALL_MIGRATIONS)
                        }
                    }
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
