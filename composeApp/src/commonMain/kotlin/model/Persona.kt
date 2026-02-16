package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Persona(
    @SerialName("id") val id: String,
    val nickname: String,
    val nombre: String,
    val apellidos: String,
    val rol: String, // "ENTRENADOR" o "USUARIO"
    val token: String? = null
)

enum class RolUsuario {
    ENTRENADOR,
    USUARIO
}
