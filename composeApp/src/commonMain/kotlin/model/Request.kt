package model

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class CrearSesionRequest(
    val idUsuario: String,
    val titulo: String,
    val ejercicios: List<CrearEjercicioRequest>
)

@Serializable
data class CrearEjercicioRequest(
    val nombre: String,
    val series: Int,
    val repeticiones: String,
    val peso: Double? = null,
    val bloque: Int = 0,
)

data class EjercicioDraft(
    val id: String = Random.nextLong().toString(),
    var nombre: String = "",
    var series: String = "",
    var repeticiones: String = "",
    var peso: String = "",
    val bloque: Int = 0
)