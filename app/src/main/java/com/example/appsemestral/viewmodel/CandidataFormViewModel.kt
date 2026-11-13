package com.example.appsemestral.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.data.local.CandidataEntity
import com.example.appsemestral.data.repository.CandidataRepository
import com.example.appsemestral.domain.model.CampoState
import com.example.appsemestral.domain.model.CandidataInput
import com.example.appsemestral.domain.validation.CandidataValidator
import com.example.appsemestral.nativo.ArchivoHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FormularioCandidataState(
    val nombre: CampoState<String> = CampoState(""),
    val correo: CampoState<String> = CampoState(""),
    val telefono: CampoState<String> = CampoState(CandidataValidator.TELEFONO_PREFIJO),
    val area: CampoState<String> = CampoState(""),
    val anios: CampoState<String> = CampoState(""),
    val resumen: CampoState<String> = CampoState(""),
    val linkedin: CampoState<String> = CampoState(""),
    val cvUri: CampoState<String> = CampoState(""),
    val cvNombre: String = "",
    val cvTamanoBytes: Long = 0L,
    val cvCargando: Boolean = false
)

class CandidataFormViewModel(private val context: Context, private val repository: CandidataRepository) : ViewModel() {
    private val _estado = MutableStateFlow(FormularioCandidataState())
    val estado: StateFlow<FormularioCandidataState> = _estado
    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje
    private val _guardado = MutableStateFlow(false)
    val guardado: StateFlow<Boolean> = _guardado

    fun cambiarNombre(v: String) = actualizar { it.copy(nombre = it.nombre.con(v, CandidataValidator.validarNombre(v))) }
    fun cambiarCorreo(v: String) = actualizar { it.copy(correo = it.correo.con(v, CandidataValidator.validarCorreo(v))) }
    fun cambiarTelefono(v: String) {
        val formateado = CandidataValidator.formatearTelefonoChile(v)
        val error = if (formateado == CandidataValidator.TELEFONO_PREFIJO) null
            else CandidataValidator.validarTelefono(formateado)
        actualizar { it.copy(telefono = it.telefono.con(formateado, error)) }
    }
    fun cambiarArea(v: String) = actualizar { it.copy(area = it.area.con(v, CandidataValidator.validarArea(v))) }
    fun cambiarAnios(v: String) = actualizar { it.copy(anios = it.anios.con(v, if (v.isBlank()) null else CandidataValidator.validarAnios(v.toIntOrNull()))) }
    fun cambiarResumen(v: String) = actualizar { it.copy(resumen = it.resumen.con(v, CandidataValidator.validarResumen(v))) }
    fun cambiarLinkedin(v: String) = actualizar { it.copy(linkedin = it.linkedin.con(v, CandidataValidator.validarLinkedin(v))) }
    fun seleccionarCv(uri: Uri) {
        if (_estado.value.cvCargando) return
        actualizar { it.copy(cvCargando = true, cvUri = it.cvUri.copy(error = null)) }
        _mensaje.value = ""
        viewModelScope.launch {
            try {
                val pdf = withContext(Dispatchers.IO) { ArchivoHelper.importarPdf(context, uri) }
                val error = CandidataValidator.validarCv(pdf.uri.toString(), pdf.nombre, pdf.tamanoBytes)
                actualizar {
                    it.copy(
                        cvUri = it.cvUri.con(pdf.uri.toString(), error),
                        cvNombre = pdf.nombre,
                        cvTamanoBytes = pdf.tamanoBytes,
                        cvCargando = false
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                val mensajeError = error.message ?: "No se pudo importar el PDF. Intenta seleccionar el archivo nuevamente."
                actualizar {
                    it.copy(
                        cvUri = CampoState("", mensajeError),
                        cvNombre = "",
                        cvTamanoBytes = 0L,
                        cvCargando = false
                    )
                }
                _mensaje.value = mensajeError
                _guardado.value = false
            }
        }
    }
    private fun actualizar(block: (FormularioCandidataState) -> FormularioCandidataState) { _estado.value = block(_estado.value) }

    fun guardar(registradaPor: String) {
        val s = _estado.value
        if (s.cvCargando) {
            _mensaje.value = "Espera a que termine la importación del currículum antes de guardar."
            _guardado.value = false
            return
        }
        val input = CandidataInput(s.nombre.valor, s.correo.valor, s.telefono.valor, s.area.valor, s.anios.valor.toIntOrNull(), s.resumen.valor, s.linkedin.valor, s.cvUri.valor, s.cvNombre, s.cvTamanoBytes)
        val errores = CandidataValidator.validar(input)
        if (errores.isNotEmpty()) {
            _estado.value = s.copy(
                nombre = s.nombre.copy(error = errores["nombre"]), correo = s.correo.copy(error = errores["correo"]),
                telefono = s.telefono.copy(error = errores["telefono"]), area = s.area.copy(error = errores["area"]),
                anios = s.anios.copy(error = errores["anios"]), resumen = s.resumen.copy(error = errores["resumen"]),
                linkedin = s.linkedin.copy(error = errores["linkedin"]), cvUri = s.cvUri.copy(error = errores["cv"])
            )
            _mensaje.value = "Revisa los campos marcados antes de guardar."
            _guardado.value = false
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.registrar(CandidataEntity(
                    nombre = input.nombre.trim(), correo = input.correo.trim().lowercase(), telefono = input.telefono.trim(),
                    area = input.area.trim(), aniosExperiencia = input.aniosExperiencia!!, resumen = input.resumen.trim(),
                    linkedin = input.linkedin.trim(), cvUri = input.cvUri, registradaPor = registradaPor
                ))
            }.onSuccess {
                _mensaje.value = "Candidata guardada correctamente."
                _guardado.value = true
                _estado.value = FormularioCandidataState()
            }.onFailure {
                _mensaje.value = it.message ?: "No se pudo guardar la candidata."
                _guardado.value = false
            }
        }
    }
    fun limpiarMensaje() { _mensaje.value = "" }
}
