package com.example.appsemestral.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CandidataEntity::class, CriterioEntity::class, EvaluacionEntity::class, PuntajeCriterioEntity::class, RegistroHistorialEntity::class], version = 2, exportSchema = true)
@TypeConverters(WimConverters::class)
abstract class WimDatabase : RoomDatabase() {
    abstract fun candidataDao(): CandidataDao
    abstract fun criterioDao(): CriterioDao
    abstract fun evaluacionDao(): EvaluacionDao
    abstract fun puntajeCriterioDao(): PuntajeCriterioDao
    abstract fun historialDao(): HistorialDao
    companion object {
        /** v1 → v2: agrega el último editor de cada evaluación sin perder los registros existentes. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE evaluaciones ADD COLUMN ultimoEditorNombre TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile private var INSTANCE: WimDatabase? = null
        fun obtener(context: Context): WimDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, WimDatabase::class.java, "wim100.db")
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
        }
    }
}
