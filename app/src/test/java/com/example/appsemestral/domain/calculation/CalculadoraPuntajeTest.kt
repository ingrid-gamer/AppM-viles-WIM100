package com.example.appsemestral.domain.calculation

import com.example.appsemestral.domain.model.CriterioPuntuacion
import com.example.appsemestral.domain.model.NotaCriterio
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculadoraPuntajeTest {
    private val criterios = (1L..5L).map { CriterioPuntuacion(it, 20) }
    private fun notas(vararg valores: Int) = valores.mapIndexed { i, v -> NotaCriterio(i + 1L, v, "Observación suficientemente descriptiva") }
    @Test fun todasLasNotasMaximasDanCienPuntos() {
        val r = CalculadoraPuntaje.calcular(notas(5, 5, 5, 5, 5), criterios)
        assertEquals(100.0, r.total, 0.001); assertEquals("Sobresaliente", r.categoria)
    }
    @Test fun todasLasNotasMinimasDanCeroPuntos() {
        val r = CalculadoraPuntaje.calcular(notas(1, 1, 1, 1, 1), criterios)
        assertEquals(0.0, r.total, 0.001); assertEquals("Bajo el umbral", r.categoria)
    }
    @Test fun puntajeUsaPromedioPonderado() {
        val pesosPrograma = listOf(CriterioPuntuacion(1, 25), CriterioPuntuacion(2, 25), CriterioPuntuacion(3, 20), CriterioPuntuacion(4, 15), CriterioPuntuacion(5, 15))
        val r = CalculadoraPuntaje.calcular(notas(5, 1, 1, 1, 1), pesosPrograma)
        assertEquals(25.0, r.total, 0.001)
    }
    @Test fun clasificaCorrectamenteLosUmbrales() {
        assertEquals("Sobresaliente", CalculadoraPuntaje.calcular(notas(5, 5, 4, 4, 4), criterios).categoria) // 85
        assertEquals("Sólida", CalculadoraPuntaje.calcular(notas(4, 4, 4, 4, 4), criterios).categoria) // 75
        assertEquals("En desarrollo", CalculadoraPuntaje.calcular(notas(3, 3, 3, 3, 3), criterios).categoria) // 50
    }
}
