package com.example.appsemestral.domain.model

enum class EstadoCandidata(val etiqueta: String) {
    Postulada("Postulada"), EnEvaluacion("En evaluación"), Evaluada("Evaluada"),
    Seleccionada("Seleccionada"), NoSeleccionada("No seleccionada")
}
enum class RolUsuario(
    val etiqueta: String,
    val puedeRegistrarCandidatas: Boolean,
    val puedeEvaluar: Boolean,
    val puedeResolver: Boolean,
    val puedeConsultarResultados: Boolean,
    val puedeConsultarHistorial: Boolean
) {
    Evaluadora(
        etiqueta = "Evaluadora",
        puedeRegistrarCandidatas = true,
        puedeEvaluar = true,
        puedeResolver = false,
        puedeConsultarResultados = false,
        puedeConsultarHistorial = false
    ),
    Direccion(
        etiqueta = "Dirección",
        puedeRegistrarCandidatas = false,
        puedeEvaluar = false,
        puedeResolver = true,
        puedeConsultarResultados = true,
        puedeConsultarHistorial = true
    )
}

data class CampoState<T>(val valor: T, val error: String? = null) {
    fun con(nuevoValor: T, resultado: String?): CampoState<T> = copy(valor = nuevoValor, error = resultado)
}
data class NotaCriterio(val criterioId: Long, val puntaje: Int, val observacion: String)
data class CriterioPuntuacion(val id: Long, val peso: Int)
data class ResultadoPuntaje(val total: Double, val categoria: String)
data class CandidataInput(
    val nombre: String, val correo: String, val telefono: String, val area: String,
    val aniosExperiencia: Int?, val resumen: String, val linkedin: String,
    val cvUri: String, val cvNombre: String, val cvTamanoBytes: Long
)
data class EvaluacionInput(val notas: List<NotaCriterio>, val observacionGeneral: String)
