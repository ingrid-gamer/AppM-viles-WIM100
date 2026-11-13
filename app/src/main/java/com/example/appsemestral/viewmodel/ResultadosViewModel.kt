package com.example.appsemestral.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.data.local.CandidataEntity
import com.example.appsemestral.data.repository.CandidataRepository
import com.example.appsemestral.data.repository.EvaluacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class FilaRanking(val candidata: CandidataEntity, val promedio: Double, val cantidadEvaluaciones: Int, val categoria: String)
class ResultadosViewModel(candidatas: CandidataRepository, evaluaciones: EvaluacionRepository) : ViewModel() {
    private val _ranking = MutableStateFlow<List<FilaRanking>>(emptyList())
    val ranking: StateFlow<List<FilaRanking>> = _ranking
    init {
        viewModelScope.launch {
            combine(candidatas.observarTodas(), evaluaciones.observarTodas()) { lista, evaluacionesLista ->
                lista.map { candidata ->
                    val evaluacionesCandidata = evaluacionesLista.filter { it.candidataId == candidata.id }
                    val promedio = evaluacionesCandidata.map { it.puntajeTotal }.average().takeIf { it.isFinite() } ?: 0.0
                    val categoria = evaluacionesCandidata.maxByOrNull { it.fecha }?.categoria ?: "Pendiente"
                    FilaRanking(candidata, promedio, evaluacionesCandidata.size, categoria)
                }.sortedWith(compareByDescending<FilaRanking> { it.promedio }.thenBy { it.candidata.nombre })
            }.collect { _ranking.value = it }
        }
    }
}
