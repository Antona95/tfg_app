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
    val idDetalle: String,
    val ejercicio: Ejercicio, // Objeto anidado
    val seriesObjetivo: Int,
    val repeticionesObjetivo: String,
    val pesoObjetivo: Double? = null,

    // Lógica avanzada (Biseries, etc.)
    val tipoAgrupacion: TipoAgrupacion = TipoAgrupacion.SERIE_NORMAL,

    // Inputs reales del usuario
    val rirReal: Int? = null,
    val rpeReal: Int? = null
)

@Serializable
enum class TipoAgrupacion {
    SERIE_NORMAL,
    BISERIE,
    TRISERIE,
    CIRCUITO
}