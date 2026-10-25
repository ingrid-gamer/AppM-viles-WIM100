package com.example.appsemestral.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.data.local.CriterioDao
import com.example.appsemestral.data.local.EvaluacionEntity
import com.example.appsemestral.data.local.PuntajeCriterioDao
import com.example.appsemestral.data.repository.EvaluacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DetalleNotaEvaluada(val nombreCriterio: String, val puntaje: Int, val observacion: String)
data class DetalleEvaluacion(val evaluacion: EvaluacionEntity, val notas: List<DetalleNotaEvaluada>)

class DetalleEvaluacionesViewModel(
    private val repository: EvaluacionRepository,
    private val criterioDao: CriterioDao,
    private val puntajeDao: PuntajeCriterioDao
) : ViewModel() {
    private val _evaluaciones = MutableStateFlow<List<DetalleEvaluacion>>(emptyList())
    val evaluaciones: StateFlow<List<DetalleEvaluacion>> = _evaluaciones
    private var candidataActual = -1L

    fun cargar(candidataId: Long) {
        if (candidataActual == candidataId) return
        candidataActual = candidataId
        viewModelScope.launch {
            repository.observarPorCandidata(candidataId).collect { evaluaciones ->
                val criterios = criterioDao.listarTodos().associateBy { it.id }
                _evaluaciones.value = evaluaciones.map { evaluacion ->
                    val puntos = puntajeDao.listarPorEvaluacion(evaluacion.id).map { punto ->
                        DetalleNotaEvaluada(criterios[punto.criterioId]?.nombre ?: "Criterio", punto.puntaje, punto.observacion)
                    }
                    DetalleEvaluacion(evaluacion, puntos)
                }
            }
        }
    }
}
