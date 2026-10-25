package com.example.appsemestral.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CandidataEntity::class, CriterioEntity::class, EvaluacionEntity::class, PuntajeCriterioEntity::class, RegistroHistorialEntity::class], version = 1, exportSchema = true)
@TypeConverters(WimConverters::class)
abstract class WimDatabase : RoomDatabase() {
    abstract fun candidataDao(): CandidataDao
    abstract fun criterioDao(): CriterioDao
    abstract fun evaluacionDao(): EvaluacionDao
    abstract fun puntajeCriterioDao(): PuntajeCriterioDao
    abstract fun historialDao(): HistorialDao
    companion object {
        @Volatile private var INSTANCE: WimDatabase? = null
        fun obtener(context: Context): WimDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, WimDatabase::class.java, "wim100.db").fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}
