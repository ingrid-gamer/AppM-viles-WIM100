package com.example.appsemestral.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.appsemestral.domain.calculation.CalculadoraPuntaje
import com.example.appsemestral.domain.model.CriterioPuntuacion
import com.example.appsemestral.domain.model.EstadoCandidata
import com.example.appsemestral.domain.model.NotaCriterio
import com.example.appsemestral.nativo.ArchivoHelper

object DatosSemilla {
    suspend fun cargarSiNecesario(context: Context, db: WimDatabase) {
        if (db.candidataDao().cantidad() > 0) return
        db.withTransaction {
            val criterios = listOf(
                CriterioEntity(nombre = "Experiencia técnica", descripcion = "Experiencia demostrable y conocimientos técnicos del área.", peso = 25, orden = 1),
                CriterioEntity(nombre = "Liderazgo", descripcion = "Capacidad de guiar equipos e impulsar iniciativas.", peso = 25, orden = 2),
                CriterioEntity(nombre = "Impacto", descripcion = "Resultados y contribución a su organización o comunidad.", peso = 20, orden = 3),
                CriterioEntity(nombre = "Innovación", descripcion = "Creatividad y mejora continua en su trabajo.", peso = 15, orden = 4),
                CriterioEntity(nombre = "Proyección", descripcion = "Potencial de crecimiento y aporte futuro.", peso = 15, orden = 5)
            )
            val criterioIds = db.criterioDao().insertarVarios(criterios)
            val datos = listOf(
                Triple("Camila Rojas", "camila.rojas@example.cl", "Ciberseguridad"),
                Triple("Valentina Soto", "valentina.soto@example.cl", "Desarrollo de software"),
                Triple("Martina Pérez", "martina.perez@example.cl", "Infraestructura"),
                Triple("Javiera Muñoz", "javiera.munoz@example.cl", "Datos"),
                Triple("Antonia Díaz", "antonia.diaz@example.cl", "Gestión tecnológica"),
                Triple("Fernanda Silva", "fernanda.silva@example.cl", "Ciberseguridad")
            )
            val ids = mutableListOf<Long>()
            datos.forEachIndexed { i, d ->
                val uri = ArchivoHelper.generarCvPrueba(context, d.first, d.second, d.third)
                val estado = when (i) { 0 -> EstadoCandidata.Seleccionada; 1, 2, 3 -> EstadoCandidata.Evaluada; else -> EstadoCandidata.Postulada }
                ids += db.candidataDao().insertar(CandidataEntity(
                    nombre = d.first, correo = d.second, telefono = "+56 9 1234 ${"%04d".format(1000 + i)}", area = d.third,
                    aniosExperiencia = 3 + i,
                    resumen = "Profesional de tecnología con experiencia en proyectos colaborativos, mejora continua y desarrollo de soluciones con impacto medible.",
                    linkedin = "https://www.linkedin.com/in/candidata-demo-${i + 1}", cvUri = uri.toString(), estado = estado, registradaPor = "Datos de demostración"
                ))
            }
            val notasDePrueba = listOf(listOf(5, 5, 4, 5, 4), listOf(4, 4, 4, 4, 4), listOf(4, 3, 4, 3, 4), listOf(3, 4, 3, 3, 4))
            notasDePrueba.forEachIndexed { i, notas ->
                val resultado = CalculadoraPuntaje.calcular(
                    notas.mapIndexed { j, n -> NotaCriterio(criterioIds[j], n, "Evaluación ficticia de demostración") },
                    criterios.mapIndexed { j, c -> CriterioPuntuacion(criterioIds[j], c.peso) }
                )
                val evaluacionId = db.evaluacionDao().insertar(EvaluacionEntity(
                    candidataId = ids[i], evaluadoraClave = "semilla-demo-$i", evaluadoraNombre = "Evaluadora de prueba",
                    puntajeTotal = resultado.total, categoria = resultado.categoria,
                    observacionGeneral = "Evaluación de demostración para comprobar el ranking inicial de la aplicación."
                ))
                db.evaluacionDao().insertarPuntajes(notas.mapIndexed { j, nota ->
                    PuntajeCriterioEntity(evaluacionId = evaluacionId, criterioId = criterioIds[j], puntaje = nota, observacion = "Comentario de prueba del criterio ${j + 1}.")
                })
            }
            db.historialDao().insertar(RegistroHistorialEntity(usuario = "Sistema", accion = "SEED", detalle = "Carga inicial de 6 candidatas, 5 criterios y evaluaciones de demostración."))
        }
    }
}
