package com.example.appsemestral.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.data.local.CriterioEntity
import com.example.appsemestral.data.repository.EvaluacionRepository
import com.example.appsemestral.domain.calculation.CalculadoraPuntaje
import com.example.appsemestral.domain.model.CriterioPuntuacion
import com.example.appsemestral.domain.model.EvaluacionInput
import com.example.appsemestral.domain.model.NotaCriterio
import com.example.appsemestral.domain.validation.EvaluacionValidator
import com.example.appsemestral.nativo.NotificacionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EvaluacionViewModel(private val repository: EvaluacionRepository, private val context: Context) : ViewModel() {
    private val _criterios = MutableStateFlow<List<CriterioEntity>>(emptyList())
    val criterios: StateFlow<List<CriterioEntity>> = _criterios
    private val _puntajes = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val puntajes: StateFlow<Map<Long, Int>> = _puntajes
    private val _observaciones = MutableStateFlow<Map<Long, String>>(emptyMap())
    val observaciones: StateFlow<Map<Long, String>> = _observaciones
    private val _errores = MutableStateFlow<Map<String, String>>(emptyMap())
    val errores: StateFlow<Map<String, String>> = _errores
    private val _puntajeTotal = MutableStateFlow<Double?>(null)
    val puntajeTotal: StateFlow<Double?> = _puntajeTotal
    private val _categoria = MutableStateFlow("")
    val categoria: StateFlow<String> = _categoria
    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje
    private val _guardado = MutableStateFlow(false)
    val guardado: StateFlow<Boolean> = _guardado
    private val _intentosInvalidos = MutableStateFlow(0)
    val intentosInvalidos: StateFlow<Int> = _intentosInvalidos
    private val _observacionGeneral = MutableStateFlow("")
    val observacionGeneral: StateFlow<String> = _observacionGeneral
    private var candidataId: Long = -1
    private var evaluadoraClave = ""
    private var evaluadoraNombre = ""
    private var intentoGuardar = false

    fun iniciar(candidata: Long, clave: String, nombre: String) {
        if (candidataId == candidata && _criterios.value.isNotEmpty()) return
        candidataId = candidata; evaluadoraClave = clave; evaluadoraNombre = nombre; intentoGuardar = false
        _puntajes.value = emptyMap(); _observaciones.value = emptyMap(); _errores.value = emptyMap()
        _observacionGeneral.value = ""; _puntajeTotal.value = null; _categoria.value = ""; _mensaje.value = ""; _guardado.value = false
        viewModelScope.launch {
            runCatching { repository.marcarEnEvaluacion(candidata, nombre) }
                .onFailure { _mensaje.value = it.message ?: "No se pudo iniciar la evaluación." }
            _criterios.value = repository.criterios()
            recalcular()
        }
    }

    fun cambiarPuntaje(criterioId: Long, puntaje: Int) {
        _puntajes.value = _puntajes.value + (criterioId to puntaje)
        // Antes del primer guardado solo se valida el criterio que se acaba de editar.
        if (puntaje in 1..2 && _observaciones.value[criterioId].orEmpty().trim().length < 10) {
            _errores.value = _errores.value + ("justificacion_$criterioId" to "Justifica el puntaje bajo con al menos 10 caracteres.")
        } else {
            _errores.value = _errores.value - "justificacion_$criterioId" - "criterio_$criterioId"
        }
        if (intentoGuardar) validarTodo()
        recalcular()
    }
    fun cambiarObservacion(criterioId: Long, texto: String) {
        _observaciones.value = _observaciones.value + (criterioId to texto)
        if (_puntajes.value[criterioId] in 1..2 && texto.trim().length < 10) {
            _errores.value = _errores.value + ("justificacion_$criterioId" to "Justifica el puntaje bajo con al menos 10 caracteres.")
        } else _errores.value = _errores.value - "justificacion_$criterioId"
        if (intentoGuardar) validarTodo()
    }
    fun cambiarObservacionGeneral(texto: String) {
        _observacionGeneral.value = texto
        if (intentoGuardar) validarTodo()
    }
    private fun inputActual() = EvaluacionInput(
        notas = _criterios.value.mapNotNull { c -> _puntajes.value[c.id]?.let { NotaCriterio(c.id, it, _observaciones.value[c.id].orEmpty()) } },
        observacionGeneral = _observacionGeneral.value
    )
    private fun validarTodo(): Boolean {
        val errores = EvaluacionValidator.validar(inputActual()).toMutableMap()
        _criterios.value.filter { _puntajes.value[it.id] == null }.forEach { errores["criterio_${it.id}"] = "Selecciona una nota de 1 a 5." }
        _errores.value = errores
        return errores.isEmpty() && _puntajes.value.size == _criterios.value.size
    }
    private fun recalcular() {
        val criterios = _criterios.value
        val puntajes = _puntajes.value
        if (criterios.isNotEmpty() && puntajes.keys.containsAll(criterios.map { it.id })) {
            runCatching {
                CalculadoraPuntaje.calcular(
                    criterios.map { c -> NotaCriterio(c.id, puntajes.getValue(c.id), _observaciones.value[c.id].orEmpty()) },
                    criterios.map { CriterioPuntuacion(it.id, it.peso) }
                )
            }.onSuccess { _puntajeTotal.value = it.total; _categoria.value = it.categoria }
        } else { _puntajeTotal.value = null; _categoria.value = "" }
    }
    fun guardar() {
        intentoGuardar = true
        if (!validarTodo()) {
            _intentosInvalidos.value += 1
            _mensaje.value = "Completa los criterios y corrige los errores señalados."
            _guardado.value = false
            return
        }
        viewModelScope.launch {
            runCatching { repository.guardar(candidataId, evaluadoraClave, evaluadoraNombre, inputActual()) }
                .onSuccess {
                    _mensaje.value = "Evaluación guardada: ${"%.2f".format(it.total)} puntos · ${it.categoria}."
                    _guardado.value = true
                    NotificacionHelper.mostrarEvaluacion(context, _mensaje.value)
                }
                .onFailure { _mensaje.value = it.message ?: "No se pudo guardar la evaluación."; _guardado.value = false }
        }
    }
    fun limpiarMensaje() { _mensaje.value = "" }
}
