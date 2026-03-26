package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// esta clase representa a una persona dentro del sistema.
// la uso tanto para el coach como para el alumno.
@Serializable
data class Persona(

    // aqui indico que en el json el campo se llama "id".
    // lo guardo como string porque en la app me resulta mas comodo manejar los ids asi.
    @SerialName("id") val id: String,

    // nickname con el que el usuario inicia sesion y se identifica en la aplicacion.
    val nickname: String,

    // nombre real de la persona.
    val nombre: String,

    // apellidos de la persona.
    val apellidos: String,

    // rol del usuario dentro del sistema.
    // normalmente sera "entrenador" o "usuario".
    // este campo es importante porque segun su valor redirijo a una interfaz u otra.
    val rol: String, // "ENTRENADOR" o "USUARIO"

    // dejo este campo preparado por si mas adelante quiero trabajar con autenticacion por token.
    // ahora mismo lo tengo opcional para no obligar a que venga siempre del backend.
    val token: String? = null
)

// este enum me sirve para representar de forma mas controlada los roles posibles.
// aunque en la clase persona uso string porque asi coincide con lo que llega de la api,
// este enum me puede venir bien para comparaciones o futuras mejoras.
enum class RolUsuario {

    // rol del entrenador, que tiene acceso a gestion de alumnos y sesiones.
    ENTRENADOR,

    // rol del alumno, que tiene acceso a su entrenamiento y su historial.
    USUARIO
}