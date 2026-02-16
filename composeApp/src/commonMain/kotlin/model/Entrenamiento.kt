package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ejercicio(
    val idEjercicio: String,
    val nombre: String,
)

@Serializable
data class SesionEntrenamiento(
    @SerialName("id") val idSesion: String, // Mongo devuelve 'id' tras el mapper
    @SerialName("id_plan") val idPlan: String,
    @SerialName("fecha") val fechaProgramada: String,
    val finalizada: Boolean = false,
    val ejercicios: List<DetalleSesion> = emptyList()
)

@Serializable
data class DetalleSesion(
    val idDetalle: String? = null,
    val idEjercicio: String? = null,
    @SerialName("nombre") val nombre: String? = null, // De BD 'nombreEjercicio' a Kotlin 'nombre'
    val series: Int,
    val repeticiones: String,
    val peso: Double? = null,
    val bloque: Int = 0,
    val observaciones: String? = null // De BD 'notas' a Kotlin 'observaciones'
)

@Serializable
enum class TipoAgrupacion {
    SERIE_NORMAL,
    BISERIE,
    TRISERIE,
    CIRCUITO
}
