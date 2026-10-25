package com.example.appsemestral.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.data.local.RegistroHistorialEntity
import com.example.appsemestral.data.repository.HistorialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistorialViewModel(repository: HistorialRepository) : ViewModel() {
    private val _registros = MutableStateFlow<List<RegistroHistorialEntity>>(emptyList())
    val registros: StateFlow<List<RegistroHistorialEntity>> = _registros
    init { viewModelScope.launch { repository.observar().collect { _registros.value = it } } }
}
