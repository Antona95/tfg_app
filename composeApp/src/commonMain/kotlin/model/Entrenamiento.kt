package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ejercicio(
    @SerialName("_id") val idEjercicio: String? = null,
    val nombre: String
)

@Serializable
data class SesionEntrenamiento(
    @SerialName("_id") val idSesion: String,
    val titulo: String? = "",
    val finalizada: Boolean = false,
    val ejercicios: List<DetalleSesion> = emptyList()
)

@Serializable
data class DetalleSesion(
    @SerialName("_id") val idDetalle: String? = null,
    val nombre: String? = null,
    val idEjercicio: String? = null,
    val series: Int,
    val repeticiones: String,
    val peso: Double? = null,
    val bloque: Int = 0
)

@Serializable
enum class TipoAgrupacion {
    SERIE_NORMAL,
    BISERIE,
    TRISERIE
}
