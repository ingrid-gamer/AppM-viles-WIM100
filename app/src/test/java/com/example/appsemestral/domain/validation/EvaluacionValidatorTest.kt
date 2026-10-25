package com.example.appsemestral.domain.validation

import com.example.appsemestral.domain.model.EvaluacionInput
import com.example.appsemestral.domain.model.NotaCriterio
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluacionValidatorTest {
    @Test fun evaluacionCompletaValidaNoTieneErrores() {
        val input = EvaluacionInput(listOf(NotaCriterio(1, 4, "Buen ejemplo de trabajo"), NotaCriterio(2, 5, "Liderazgo consistente")), "Cumple los objetivos y evidencia aporte profesional relevante.")
        assertTrue(EvaluacionValidator.validar(input).isEmpty())
    }
    @Test fun notaBajaSinJustificacionEsRechazada() {
        val input = EvaluacionInput(listOf(NotaCriterio(1, 2, "corto")), "La evaluación requiere un comentario general completo.")
        assertTrue(EvaluacionValidator.validar(input).containsKey("justificacion_1"))
    }
    @Test fun notaFueraDeRangoYObservacionGeneralCortaFalla() {
        val input = EvaluacionInput(listOf(NotaCriterio(1, 6, "")), "Muy bien")
        val errores = EvaluacionValidator.validar(input)
        assertTrue(errores.containsKey("criterio_1"))
        assertTrue(errores.containsKey("observacionGeneral"))
    }
}
