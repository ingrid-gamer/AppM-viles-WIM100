package com.example.appsemestral.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.example.appsemestral.data.local.CandidataDao
import com.example.appsemestral.data.local.CriterioDao
import com.example.appsemestral.data.local.EvaluacionDao
import com.example.appsemestral.data.local.EvaluacionEntity
import com.example.appsemestral.data.local.PuntajeCriterioEntity
import com.example.appsemestral.data.local.WimDatabase
import com.example.appsemestral.domain.calculation.CalculadoraPuntaje
import com.example.appsemestral.domain.calculation.ComparadorEvaluacion
import com.example.appsemestral.domain.model.CriterioPuntuacion
import com.example.appsemestral.domain.model.EstadoCandidata
import com.example.appsemestral.domain.model.EvaluacionCargada
import com.example.appsemestral.domain.model.EvaluacionInput
import com.example.appsemestral.domain.model.NotaCriterio
import com.example.appsemestral.domain.model.ResultadoEdicion
import com.example.appsemestral.domain.model.ResultadoPuntaje
import com.example.appsemestral.domain.validation.EvaluacionValidator
import kotlinx.coroutines.flow.Flow

class EvaluacionRepository(private val db: WimDatabase, private val candidataDao: CandidataDao, private val criterioDao: CriterioDao, private val evaluacionDao: EvaluacionDao, private val historial: HistorialRepository) {
    fun observarTodas(): Flow<List<EvaluacionEntity>> = evaluacionDao.observarTodas()
    fun observarPorCandidata(id: Long): Flow<List<EvaluacionEntity>> = evaluacionDao.observarPorCandidata(id)
    suspend fun criterios() = criterioDao.listarTodos()

    suspend fun marcarEnEvaluacion(candidataId: Long, evaluadoraNombre: String) {
        db.withTransaction {
            val candidata = candidataDao.obtenerPorId(candidataId) ?: throw IllegalArgumentException("La candidata ya no existe.")
            require(candidata.estado != EstadoCandidata.Seleccionada && candidata.estado != EstadoCandidata.NoSeleccionada) {
                "La candidatura ya tiene una resolución final."
            }
            if (candidata.estado == EstadoCandidata.Postulada) {
                candidataDao.actualizar(candidata.copy(estado = EstadoCandidata.EnEvaluacion))
                historial.registrar(evaluadoraNombre, "EVALUACION_INICIADA", "Se inició la evaluación de ${candidata.nombre}.")
            }
        }
    }

    suspend fun guardar(candidataId: Long, evaluadoraClave: String, evaluadoraNombre: String, input: EvaluacionInput): ResultadoPuntaje {
        val errores = EvaluacionValidator.validar(input)
        require(errores.isEmpty()) { errores.values.firstOrNull() ?: "La evaluación contiene errores." }
        val clave = evaluadoraClave.trim().lowercase()
        try {
            return db.withTransaction {
                if (evaluacionDao.buscarEvaluacion(candidataId, clave) != null) {
                    throw EvaluacionDuplicadaException("Esta evaluadora ya calificó a la candidata. El duplicado quedó registrado.")
                }
                val candidata = candidataDao.obtenerPorId(candidataId) ?: throw IllegalArgumentException("La candidata ya no existe.")
                require(candidata.estado != EstadoCandidata.Seleccionada && candidata.estado != EstadoCandidata.NoSeleccionada) {
                    "No se puede evaluar una candidatura con resolución final."
                }
                val criterios = criterioDao.listarTodos()
                val resultado = CalculadoraPuntaje.calcular(input.notas, criterios.map { CriterioPuntuacion(it.id, it.peso) })
                val evaluacionId = evaluacionDao.insertar(EvaluacionEntity(
                    candidataId = candidataId, evaluadoraClave = clave, evaluadoraNombre = evaluadoraNombre,
                    puntajeTotal = resultado.total, categoria = resultado.categoria, observacionGeneral = input.observacionGeneral.trim()
                ))
                evaluacionDao.insertarPuntajes(input.notas.map { PuntajeCriterioEntity(evaluacionId = evaluacionId, criterioId = it.criterioId, puntaje = it.puntaje, observacion = it.observacion.trim()) })
                candidataDao.actualizar(candidata.copy(estado = EstadoCandidata.Evaluada))
                historial.registrar(evaluadoraNombre, "EVALUACION_GUARDADA", "Evaluación #$evaluacionId guardada para ${candidata.nombre}: ${"%.2f".format(resultado.total)} puntos.")
                resultado
            }
        } catch (e: EvaluacionDuplicadaException) {
            // Se registra fuera de la transacción fallida para que el evento no se revierta.
            historial.registrar(evaluadoraNombre, "INTENTO_DUPLICADO", "Se bloqueó una segunda evaluación para candidata #$candidataId.")
            throw e
        } catch (e: SQLiteConstraintException) {
            historial.registrar(evaluadoraNombre, "INTENTO_DUPLICADO", "La restricción única bloqueó una evaluación concurrente para candidata #$candidataId.")
            throw EvaluacionDuplicadaException("La base de datos bloqueó una evaluación duplicada.")
        }
    }

    /** Devuelve la evaluación previa de esta evaluadora (con todos sus puntajes) o null si todavía no evaluó. */
    suspend fun cargarExistente(candidataId: Long, evaluadoraClave: String): EvaluacionCargada? {
        val evaluacion = evaluacionDao.buscarEvaluacion(candidataId, evaluadoraClave.trim().lowercase()) ?: return null
        val notas = evaluacionDao.listarPuntajes(evaluacion.id).map { NotaCriterio(it.criterioId, it.puntaje, it.observacion) }
        return EvaluacionCargada(evaluacion.observacionGeneral, notas, evaluacion.puntajeTotal, evaluacion.categoria, evaluacion.ultimoEditorNombre)
    }

    /**
     * Edita la evaluación existente conservando su ID (el índice único candidata+evaluadora sigue intacto).
     * Todo ocurre en una transacción: evaluación principal, puntajes por criterio e historial.
     * Si no hay diferencias no se escribe nada y [ResultadoEdicion.huboCambios] es false.
     */
    suspend fun actualizar(candidataId: Long, evaluadoraClave: String, editorNombre: String, input: EvaluacionInput): ResultadoEdicion {
        val errores = EvaluacionValidator.validar(input)
        require(errores.isEmpty()) { errores.values.firstOrNull() ?: "La evaluación contiene errores." }
        val clave = evaluadoraClave.trim().lowercase()
        return db.withTransaction {
            val existente = evaluacionDao.buscarEvaluacion(candidataId, clave)
                ?: throw IllegalArgumentException("No existe una evaluación previa que editar.")
            val candidata = candidataDao.obtenerPorId(candidataId) ?: throw IllegalArgumentException("La candidata ya no existe.")
            require(candidata.estado != EstadoCandidata.Seleccionada && candidata.estado != EstadoCandidata.NoSeleccionada) {
                "No se puede modificar una evaluación con resolución final."
            }
            val criterios = criterioDao.listarTodos()
            val resultado = CalculadoraPuntaje.calcular(input.notas, criterios.map { CriterioPuntuacion(it.id, it.peso) })
            val anterior = EvaluacionInput(
                notas = evaluacionDao.listarPuntajes(existente.id).map { NotaCriterio(it.criterioId, it.puntaje, it.observacion) },
                observacionGeneral = existente.observacionGeneral
            )
            val cambios = ComparadorEvaluacion.comparar(criterios.map { it.id to it.nombre }, anterior, input)
            val editor = editorNombre.trim()
            val salida = ResultadoEdicion(resultado.total, resultado.categoria, existente.puntajeTotal, candidata.nombre, editor, cambios)
            if (cambios.isEmpty()) return@withTransaction salida

            evaluacionDao.actualizar(existente.copy(
                puntajeTotal = resultado.total, categoria = resultado.categoria,
                observacionGeneral = input.observacionGeneral.trim(), ultimoEditorNombre = editor
            ))
            evaluacionDao.eliminarPuntajes(existente.id)
            evaluacionDao.insertarPuntajes(input.notas.map {
                PuntajeCriterioEntity(evaluacionId = existente.id, criterioId = it.criterioId, puntaje = it.puntaje, observacion = it.observacion.trim())
            })
            historial.registrar(
                editor, "EVALUACION_MODIFICADA",
                "Evaluación #${existente.id} de ${candidata.nombre} modificada por $editor. " +
                    ComparadorEvaluacion.resumen(candidata.nombre, editor, cambios, existente.puntajeTotal, resultado.total, limiteTexto = 60)
                        .substringAfter("Cambios realizados:").trim().replace("\n", " | ")
            )
            salida
        }
    }

}

class EvaluacionDuplicadaException(message: String) : IllegalStateException(message)
