package com.example.appsemestral.data.repository

import androidx.room.withTransaction
import com.example.appsemestral.data.local.CandidataDao
import com.example.appsemestral.data.local.CandidataEntity
import com.example.appsemestral.data.local.WimDatabase
import com.example.appsemestral.domain.model.EstadoCandidata
import kotlinx.coroutines.flow.Flow

class CandidataRepository(private val db: WimDatabase, private val dao: CandidataDao, private val historial: HistorialRepository) {
    fun observarTodas(): Flow<List<CandidataEntity>> = dao.observarTodas()
    fun observarPorId(id: Long): Flow<CandidataEntity?> = dao.observarPorId(id)
    suspend fun registrar(candidata: CandidataEntity) {
        db.withTransaction {
            if (dao.buscarPorCorreo(candidata.correo.trim()) != null) throw IllegalArgumentException("Ya existe una candidata con ese correo.")
            val id = dao.insertar(candidata.copy(correo = candidata.correo.trim().lowercase()))
            historial.registrar(candidata.registradaPor, "CANDIDATA_CREADA", "Candidata #$id registrada: ${candidata.nombre}.")
        }
    }
    suspend fun resolver(id: Long, estado: EstadoCandidata, usuario: String) {
        require(estado == EstadoCandidata.Seleccionada || estado == EstadoCandidata.NoSeleccionada) { "Resolución no permitida." }
        db.withTransaction {
            val candidata = dao.obtenerPorId(id) ?: throw IllegalArgumentException("No se encontró la candidata.")
            dao.actualizar(candidata.copy(estado = estado))
            historial.registrar(usuario, "RESOLUCION_FINAL", "${candidata.nombre}: ${estado.etiqueta}.")
        }
    }
}
