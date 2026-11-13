package com.example.appsemestral.data.repository

import com.example.appsemestral.data.local.HistorialDao
import com.example.appsemestral.data.local.RegistroHistorialEntity
import kotlinx.coroutines.flow.Flow

class HistorialRepository(private val dao: HistorialDao) {
    fun observar(): Flow<List<RegistroHistorialEntity>> = dao.observarTodo()
    suspend fun registrar(usuario: String, accion: String, detalle: String) {
        dao.insertar(RegistroHistorialEntity(usuario = usuario.ifBlank { "Sistema" }, accion = accion, detalle = detalle))
    }
}
