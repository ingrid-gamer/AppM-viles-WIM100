package com.example.appsemestral.domain.validation

import com.example.appsemestral.domain.model.EvaluacionInput

object EvaluacionValidator {
    fun validar(input: EvaluacionInput): Map<String, String> {
        val e = linkedMapOf<String, String>()
        if (input.notas.isEmpty()) e["notas"] = "Debes evaluar los criterios."
        input.notas.forEach { n ->
            if (n.puntaje !in 1..5) e["criterio_${n.criterioId}"] = "El puntaje debe estar entre 1 y 5."
            if (n.puntaje in 1..2 && n.observacion.trim().length < 10) e["justificacion_${n.criterioId}"] = "Justifica los puntajes 1 o 2 con al menos 10 caracteres."
        }
        if (input.observacionGeneral.trim().length !in 20..400) e["observacionGeneral"] = "La observación general debe tener entre 20 y 400 caracteres."
        return e
    }
}
