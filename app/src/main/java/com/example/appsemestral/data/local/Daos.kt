package com.example.appsemestral.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao interface CandidataDao {
    @Query("SELECT * FROM candidatas ORDER BY fechaRegistro DESC") fun observarTodas(): Flow<List<CandidataEntity>>
    @Query("SELECT * FROM candidatas WHERE id = :id LIMIT 1") fun observarPorId(id: Long): Flow<CandidataEntity?>
    @Query("SELECT * FROM candidatas WHERE id = :id LIMIT 1") suspend fun obtenerPorId(id: Long): CandidataEntity?
    @Query("SELECT * FROM candidatas WHERE lower(correo) = lower(:correo) LIMIT 1") suspend fun buscarPorCorreo(correo: String): CandidataEntity?
    @Query("SELECT COUNT(*) FROM candidatas") suspend fun cantidad(): Int
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertar(candidata: CandidataEntity): Long
    @Update suspend fun actualizar(candidata: CandidataEntity)
}
@Dao interface CriterioDao {
    @Query("SELECT * FROM criterios ORDER BY orden ASC") fun observarTodos(): Flow<List<CriterioEntity>>
    @Query("SELECT * FROM criterios ORDER BY orden ASC") suspend fun listarTodos(): List<CriterioEntity>
    @Query("SELECT COUNT(*) FROM criterios") suspend fun cantidad(): Int
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertar(criterio: CriterioEntity): Long
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertarVarios(criterios: List<CriterioEntity>): List<Long>
}
@Dao interface EvaluacionDao {
    @Query("SELECT * FROM evaluaciones ORDER BY puntajeTotal DESC, fecha ASC") fun observarTodas(): Flow<List<EvaluacionEntity>>
    @Query("SELECT * FROM evaluaciones ORDER BY puntajeTotal DESC, fecha ASC") suspend fun listarTodas(): List<EvaluacionEntity>
    @Query("SELECT * FROM evaluaciones WHERE candidataId = :candidataId ORDER BY fecha DESC") fun observarPorCandidata(candidataId: Long): Flow<List<EvaluacionEntity>>
    @Query("SELECT * FROM evaluaciones WHERE candidataId = :candidataId AND evaluadoraClave = :clave LIMIT 1") suspend fun buscarEvaluacion(candidataId: Long, clave: String): EvaluacionEntity?
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertar(evaluacion: EvaluacionEntity): Long
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertarPuntajes(puntajes: List<PuntajeCriterioEntity>)
}
@Dao interface PuntajeCriterioDao {
    @Query("SELECT * FROM puntajes_criterio WHERE evaluacionId = :evaluacionId ORDER BY id ASC") fun observarPorEvaluacion(evaluacionId: Long): Flow<List<PuntajeCriterioEntity>>
    @Query("SELECT * FROM puntajes_criterio WHERE evaluacionId = :evaluacionId ORDER BY id ASC") suspend fun listarPorEvaluacion(evaluacionId: Long): List<PuntajeCriterioEntity>
}
@Dao interface HistorialDao {
    @Query("SELECT * FROM registro_historial ORDER BY fecha DESC") fun observarTodo(): Flow<List<RegistroHistorialEntity>>
    @Insert suspend fun insertar(registro: RegistroHistorialEntity): Long
}
