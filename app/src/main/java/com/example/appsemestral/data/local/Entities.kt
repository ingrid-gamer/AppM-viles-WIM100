package com.example.appsemestral.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.appsemestral.domain.model.EstadoCandidata

class WimConverters {
    @TypeConverter fun estadoAString(estado: EstadoCandidata): String = estado.name
    @TypeConverter fun stringAEstado(valor: String): EstadoCandidata = runCatching { EstadoCandidata.valueOf(valor) }.getOrDefault(EstadoCandidata.Postulada)
}

@Entity(tableName = "candidatas", indices = [Index(value = ["correo"], unique = true)])
@TypeConverters(WimConverters::class)
data class CandidataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val area: String,
    val aniosExperiencia: Int,
    val resumen: String,
    val linkedin: String,
    val cvUri: String,
    val estado: EstadoCandidata = EstadoCandidata.Postulada,
    val registradaPor: String,
    val fechaRegistro: Long = System.currentTimeMillis()
)

@Entity(tableName = "criterios")
data class CriterioEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val nombre: String, val descripcion: String, val peso: Int, val orden: Int)

@Entity(
    tableName = "evaluaciones",
    indices = [Index(value = ["candidataId", "evaluadoraClave"], unique = true)],
    foreignKeys = [ForeignKey(entity = CandidataEntity::class, parentColumns = ["id"], childColumns = ["candidataId"], onDelete = ForeignKey.CASCADE)]
)
data class EvaluacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val candidataId: Long,
    val evaluadoraClave: String,
    val evaluadoraNombre: String,
    val puntajeTotal: Double,
    val categoria: String,
    val observacionGeneral: String,
    val fecha: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "puntajes_criterio",
    indices = [Index(value = ["evaluacionId"]), Index(value = ["criterioId"])],
    foreignKeys = [
        ForeignKey(entity = EvaluacionEntity::class, parentColumns = ["id"], childColumns = ["evaluacionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CriterioEntity::class, parentColumns = ["id"], childColumns = ["criterioId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class PuntajeCriterioEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val evaluacionId: Long, val criterioId: Long, val puntaje: Int, val observacion: String)

/** La bitácora no usa claves foráneas para mantener la trazabilidad aunque se borren datos operativos. */
@Entity(tableName = "registro_historial", indices = [Index(value = ["fecha"])])
data class RegistroHistorialEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val usuario: String, val accion: String, val detalle: String, val fecha: Long = System.currentTimeMillis())
