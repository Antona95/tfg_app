package model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val nickname: String,
    val contrasena: String
)

@Serializable
data class RegistroRequest(
    val nickname: String,
    val contrasena: String,
    val nombre: String,
    val apellidos: String,
    val rol: String = "USUARIO"
)