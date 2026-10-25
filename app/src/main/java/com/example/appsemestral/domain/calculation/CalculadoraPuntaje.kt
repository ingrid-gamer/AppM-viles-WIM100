package com.example.appsemestral.domain.calculation

import com.example.appsemestral.domain.model.CriterioPuntuacion
import com.example.appsemestral.domain.model.NotaCriterio
import com.example.appsemestral.domain.model.ResultadoPuntaje

/** Convierte cada nota 1..5 a 0..100 y luego aplica el peso de ese criterio. */
object CalculadoraPuntaje {
    fun calcular(notas: List<NotaCriterio>, criterios: List<CriterioPuntuacion>): ResultadoPuntaje {
        require(criterios.isNotEmpty()) { "No hay criterios configurados." }
        require(criterios.all { it.peso >= 0 } && criterios.sumOf { it.peso } == 100) { "Los pesos deben sumar 100%." }
        require(notas.map { it.criterioId }.toSet().size == notas.size) { "Hay criterios repetidos." }
        require(notas.map { it.criterioId }.toSet() == criterios.map { it.id }.toSet()) { "Debes calificar todos los criterios una sola vez." }
        require(notas.all { it.puntaje in 1..5 }) { "Cada puntaje debe estar entre 1 y 5." }
        val porId = notas.associateBy { it.criterioId }
        val total = criterios.sumOf { c -> ((porId.getValue(c.id).puntaje - 1) / 4.0 * 100.0) * c.peso / 100.0 }.coerceIn(0.0, 100.0)
        return ResultadoPuntaje(total, categorizar(total))
    }
    fun categorizar(puntaje: Double): String = when {
        puntaje >= 85.0 -> "Sobresaliente"
        puntaje >= 70.0 -> "Sólida"
        puntaje >= 50.0 -> "En desarrollo"
        else -> "Bajo el umbral"
    }
}
