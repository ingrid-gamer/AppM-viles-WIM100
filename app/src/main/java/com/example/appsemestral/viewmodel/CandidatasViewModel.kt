package com.example.appsemestral.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.data.local.CandidataEntity
import com.example.appsemestral.data.repository.CandidataRepository
import com.example.appsemestral.domain.model.EstadoCandidata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CandidatasViewModel(private val repository: CandidataRepository) : ViewModel() {
    private val _candidatas = MutableStateFlow<List<CandidataEntity>>(emptyList())
    val candidatas: StateFlow<List<CandidataEntity>> = _candidatas
    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje
    init { viewModelScope.launch { repository.observarTodas().collect { _candidatas.value = it } } }
    fun resolver(id: Long, estado: EstadoCandidata, usuario: String) {
        viewModelScope.launch {
            runCatching { repository.resolver(id, estado, usuario) }
                .onSuccess { _mensaje.value = "Decisión registrada: ${estado.etiqueta}." }
                .onFailure { _mensaje.value = it.message ?: "No se pudo registrar la decisión." }
        }
    }
    fun limpiarMensaje() { _mensaje.value = "" }
}
