package com.example.appsemestral.domain.calculation

import com.example.appsemestral.domain.model.EvaluacionInput
import com.example.appsemestral.domain.model.NotaCriterio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComparadorEvaluacionTest {
    private val criterios = listOf(1L to "Técnica", 2L to "Comunicación")
    private val base = EvaluacionInput(
        listOf(NotaCriterio(1, 4, ""), NotaCriterio(2, 3, "Comunica con claridad")),
        "Buen desempeño general en la entrevista."
    )

    @Test fun evaluacionIgualNoGeneraCambios() {
        assertTrue(ComparadorEvaluacion.comparar(criterios, base, base.copy()).isEmpty())
    }

    @Test fun espaciosAlInicioOFinalNoCuentanComoCambio() {
        val ahora = base.copy(observacionGeneral = "  Buen desempeño general en la entrevista.  ")
        assertTrue(ComparadorEvaluacion.comparar(criterios, base, ahora).isEmpty())
    }

    @Test fun detectaCambioDePuntaje() {
        val ahora = base.copy(notas = listOf(NotaCriterio(1, 5, ""), NotaCriterio(2, 3, "Comunica con claridad")))
        val cambios = ComparadorEvaluacion.comparar(criterios, base, ahora)
        assertEquals(1, cambios.size)
        assertEquals("Técnica", cambios[0].campo)
        assertEquals("4", cambios[0].antes)
        assertEquals("5", cambios[0].ahora)
    }

    @Test fun detectaCambioDeObservacionGeneralYDeCriterio() {
        val ahora = EvaluacionInput(
            listOf(NotaCriterio(1, 4, ""), NotaCriterio(2, 3, "Comunica con mucha claridad")),
            "Excelente desempeño general en la entrevista."
        )
        val campos = ComparadorEvaluacion.comparar(criterios, base, ahora).map { it.campo }
        assertEquals(listOf("Comunicación (observación)", "Observación general"), campos)
    }

    @Test fun resumenIncluyeCandidataEditorYFlecha() {
        val ahora = base.copy(notas = listOf(NotaCriterio(1, 5, ""), NotaCriterio(2, 4, "Comunica con claridad")))
        val cambios = ComparadorEvaluacion.comparar(criterios, base, ahora)
        val texto = ComparadorEvaluacion.resumen("María González", "Ingrid", cambios, 60.0, 80.0)
        assertTrue(texto.contains("EVALUACIÓN MODIFICADA"))
        assertTrue(texto.contains("Candidata: María González"))
        assertTrue(texto.contains("- Técnica: 4 → 5"))
        assertTrue(texto.contains("- Comunicación: 3 → 4"))
        assertTrue(texto.contains("Último editor: Ingrid"))
    }

    @Test fun resumenRecortaObservacionesLargas() {
        val larga = "x".repeat(200)
        val cambios = ComparadorEvaluacion.comparar(criterios, base, base.copy(observacionGeneral = larga))
        val texto = ComparadorEvaluacion.resumen("Ana", "Luis", cambios, 50.0, 50.0, limiteTexto = 30)
        assertTrue(texto.contains("x".repeat(30) + "…"))
        assertTrue(!texto.contains("x".repeat(31)))
    }
}
