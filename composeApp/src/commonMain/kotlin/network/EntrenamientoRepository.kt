package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.Persona
import model.SesionEntrenamiento

// --- MODELOS DE PETICIÓN (DTOs) ---
// Estos modelos definen CÓMO viajan los datos por internet.
// Usamos @SerialName para asegurar que coinciden con el Backend.

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

@Serializable
data class CrearSesionRequest(
    val idUsuario: String,
    val titulo: String,
    val fechaProgramada: String,
    val ejercicios: List<CrearEjercicioRequest>
)

@Serializable
data class CrearEjercicioRequest(
    val nombreEjercicio: String,

    // IMPORTANTE: @SerialName traduce "seriesObjetivo" a "series" para el JSON
    @SerialName("series")
    val seriesObjetivo: Int,

    @SerialName("repeticiones")
    val repeticionesObjetivo: String, // String para permitir "10-12"

    @SerialName("peso")
    val pesoObjetivo: Double? = null,

    val notas: String? = null
)

// --- CLASE REPOSITORIO ---

class EntrenamientoRepository(private val client: HttpClient) {

    // Asegúrate de que este puerto es el correcto de tu backend (3000, 3005, etc.)
    private val baseUrl = "http://10.0.2.2:3005"

    // --- AUTENTICACIÓN ---

    suspend fun login(nickname: String, pass: String): Persona? {
        return try {
            val respuesta = client.post("$baseUrl/api/usuarios/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(nickname, pass))
            }
            if (respuesta.status.value in 200..299) respuesta.body<Persona>() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun registrarUsuario(nickname: String, pass: String, nombre: String, apellidos: String): Boolean {
        return try {
            val respuesta = client.post("$baseUrl/api/usuarios") {
                contentType(ContentType.Application.Json)
                setBody(RegistroRequest(nickname, pass, nombre, apellidos))
            }
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    // --- LECTURA ---

    suspend fun obtenerTodosLosUsuarios(): List<Persona> {
        return try {
            client.get("$baseUrl/api/usuarios").body<List<Persona>>()
                .filter { it.rol != "ENTRENADOR" }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerSesionHoy(idUsuario: String): SesionEntrenamiento? {
        return try {
            client.get("$baseUrl/api/sesiones/hoy/$idUsuario").body<SesionEntrenamiento>()
        } catch (e: Exception) {
            null
        }
    }

    // --- CREACIÓN (LO QUE HEMOS CAMBIADO) ---

    /**
     * Envía la petición de creación al servidor.
     * Recibe el objeto YA PREPARADO (CrearSesionRequest).
     * La conversión de datos la haremos en el ViewModel.
     */
    suspend fun crearSesion(request: CrearSesionRequest): Boolean {
        return try {
            val respuesta = client.post("$baseUrl/api/sesiones") {
                contentType(ContentType.Application.Json)
                setBody(request) // Ktor convierte esto a JSON automáticamente
            }

            println("REPO: Respuesta servidor: ${respuesta.status}")

            // Devolvemos true si el código es 200 o 201
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            println("REPO: Error al crear sesión: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}