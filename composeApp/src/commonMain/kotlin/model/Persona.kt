package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Persona(
    // Mapeamos "_id" de Mongo a "id" de Kotlin automáticamente
    @SerialName("_id")
    val id: String? = null,

    val nickname: String,
    val nombre: String,
    val apellidos: String,
    val rol: String, // "ENTRENADOR" o "USUARIO"

    // Solo si el login devuelve el token dentro del objeto usuario (opcional)
    val token: String? = null
)

// Si usas el Enum en la UI para lógica, déjalo aquí
enum class RolUsuario {
    ENTRENADOR,
    DEPORTISTA
}