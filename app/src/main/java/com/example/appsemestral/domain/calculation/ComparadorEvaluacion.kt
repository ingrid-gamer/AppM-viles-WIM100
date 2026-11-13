package com.example.appsemestral.domain.calculation

import com.example.appsemestral.domain.model.CambioEvaluacion
import com.example.appsemestral.domain.model.EvaluacionInput

/** Compara dos versiones de una evaluación. Kotlin puro: no depende de Android ni de Room. */
object ComparadorEvaluacion {
    /** @param criterios pares (id, nombre) en el orden en que deben mostrarse. */
    fun comparar(criterios: List<Pair<Long, String>>, antes: EvaluacionInput, ahora: EvaluacionInput): List<CambioEvaluacion> {
        val cambios = mutableListOf<CambioEvaluacion>()
        val notasAntes = antes.notas.associateBy { it.criterioId }
        val notasAhora = ahora.notas.associateBy { it.criterioId }
        criterios.forEach { (id, nombre) ->
            val a = notasAntes[id]
            val b = notasAhora[id]
            if (a?.puntaje != b?.puntaje) {
                cambios += CambioEvaluacion(nombre, a?.puntaje?.toString() ?: "—", b?.puntaje?.toString() ?: "—")
            }
            val obsA = a?.observacion.orEmpty().trim()
            val obsB = b?.observacion.orEmpty().trim()
            if (obsA != obsB) {
                cambios += CambioEvaluacion("$nombre (observación)", obsA, obsB, esTexto = true)
            }
        }
        val generalA = antes.observacionGeneral.trim()
        val generalB = ahora.observacionGeneral.trim()
        if (generalA != generalB) cambios += CambioEvaluacion("Observación general", generalA, generalB, esTexto = true)
        return cambios
    }

    /** Texto para la notificación y el historial. [limiteTexto] recorta observaciones largas. */
    fun resumen(
        candidata: String,
        editor: String,
        cambios: List<CambioEvaluacion>,
        totalAntes: Double,
        totalAhora: Double,
        limiteTexto: Int = 80
    ): String = buildString {
        appendLine("EVALUACIÓN MODIFICADA")
        appendLine()
        appendLine("Candidata: $candidata")
        appendLine()
        appendLine("Cambios realizados:")
        cambios.forEach { c ->
            if (c.esTexto) {
                appendLine("- ${c.campo}:")
                appendLine("  Antes: ${recortar(c.antes, limiteTexto)}")
                appendLine("  Ahora: ${recortar(c.ahora, limiteTexto)}")
            } else {
                appendLine("- ${c.campo}: ${c.antes} → ${c.ahora}")
            }
        }
        appendLine()
        appendLine("Puntaje total: ${"%.2f".format(totalAntes)} → ${"%.2f".format(totalAhora)}")
        append("Último editor: $editor")
    }

    private fun recortar(texto: String, limite: Int): String = when {
        texto.isBlank() -> "(sin texto)"
        texto.length <= limite -> texto
        else -> texto.take(limite).trimEnd() + "…"
    }
}
