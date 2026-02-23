package model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val nickname: String,
    val pass: String
)

@Serializable
data class RegistroRequest(
    val nickname: String,
    val pass: String,
    val nombre: String,
    val apellidos: String,
    val rol: String = "USUARIO"
)