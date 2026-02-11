package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ejercicio(
    @SerialName("_id") val idEjercicio: String,
    val nombre: String,
)

@Serializable
data class SesionEntrenamiento(
    @SerialName("_id") val idSesion: String,
    val idPlan: String,
    val fechaProgramada: String,
    val finalizada: Boolean = false,
    val ejercicios: List<DetalleSesion> = emptyList()
)

@Serializable
data class DetalleSesion(
    val idDetalle: String? = null,
    val nombre: String? = null, // Para guardar el nombre directamente
    val id_ejercicio: String? = null,
    val series: Int,
    val repeticiones: String,
    val peso: Double? = null,
    val bloque: Int = 0, // <--- AÑADE ESTO: 0 = normal, 1...N = grupos
    val observaciones: String? = null
)
@Serializable
enum class TipoAgrupacion {
    SERIE_NORMAL,
    BISERIE,
    TRISERIE,
    CIRCUITO
}