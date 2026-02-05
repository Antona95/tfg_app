
package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// @Serializable: Le dice a Kotlin que esta clase puede convertirse a/desde JSON automáticamente.

@Serializable
data class Persona(
    // MongoDB devuelve "_id", pero tu API a veces devuelve "id".
    // Ponemos los dos por si acaso para que no falle.
    @SerialName("_id") val mongoId: String? = null,
    val id: String? = null,

    // Datos del usuario (Todos opcionales para evitar CRASH)
    val dni: String? = null,
    val nombre: String? = null,
    val apellidos: String? = null,
    val rol: String? = null,
    val nombreCompleto: String? = null,

    // Si tu login devuelve un token, recógelo aquí
    val token: String? = null
)

@Serializable
data class Ejercicio(
    @SerialName("_id")
    val idEjercicio: String,
    val nombre: String,
)

@Serializable
data class SesionEntrenamiento(
    @SerialName("_id")
    val idSesion: String,
    val idPlan: String,
    val fechaProgramada: String,
    val finalizada: Boolean = false,
    // Si tu API devuelve los ejercicios ANIDADOS dentro de la sesión, esto está perfecto.
    // Si tu API devuelve solo IDs de ejercicios, tendríamos que cambiar esto.
    val ejercicios: List<DetalleSesion> = emptyList()
)

@Serializable
data class DetalleSesion(
    val idDetalle: String,
    val idSesion: String,
    val ejercicio: Ejercicio,
    val seriesObjetivo: Int,
    val repeticionesObjetivo: String,
    val pesoObjetivo: Double? = null,
    // LOGICA DE AGRUPACIÓN (Biseries/Triseries)
    // Si dos detalles tienen el mismo 'idAgrupacion', se pintan del mismo color/bloque
    val idAgrupacion: String? = null,
    val tipoAgrupacion: TipoAgrupacion = TipoAgrupacion.SERIE_NORMAL,

    // Para el futuro (Inputs del usuario)
    val rirReal: Int? = null,
    val rpeReal: Int? = null
)

// Los Enums también necesitan serializable para que coincidan con el texto de la base de datos
@Serializable
enum class RolUsuario {
    ENTRENADOR,
    DEPORTISTA
}

@Serializable
enum class TipoAgrupacion {
    SERIE_NORMAL,
    BISERIE,
    TRISERIE,
    CIRCUITO
}

// esta clase empaqueta el dni y contraseña del usuario del login p,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,ara enviarlos

@Serializable
data class LoginRequest(
    val dni: String,
    val contrasena: String
)
