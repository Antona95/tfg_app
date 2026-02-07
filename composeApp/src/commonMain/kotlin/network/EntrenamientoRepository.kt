package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post // Importante: POST
import io.ktor.client.request.setBody // Para meter datos en la petición
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import model.Persona
import model.SesionEntrenamiento

// Definimos la estructura de datos para el Login aquí mismo
@Serializable
data class LoginRequest(
    val nickname: String,
    val contrasena: String //sin Ñ para que coincida con la API
)

@Serializable
data class RegistroRequest(
    val nickname: String,
    val contrasena: String,
    val nombre: String,
    val apellidos: String,
    val rol: String = "USUARIO" // Por defecto siempre será USUARIO desde la app
)


class EntrenamientoRepository(private val client: HttpClient) {

    // URL base de tu API (esto cambiará cuando despliegues la API real)
    // Nota: Para el emulador de Android, "localhost" es "10.0.2.2"
    private val baseUrl = "http://10.0.2.2:8080"
    // private val baseUrl = "http://192.168.1.XX:3000" // Si pruebas en un móvil físico, usa la IP de tu PC

    // Función de Login
    suspend fun login(nickname: String, pass: String): Persona? {
        return try {
            // Empaquetamos los datos
            val datosLogin = LoginRequest(nickname = nickname, contrasena = pass)

            // Enviamos la petición POST
            // ESTA ES LA RUTA CORRECTA (Casi seguro):
            val respuesta = client.post("$baseUrl/api/usuarios/login") { // <--- Fíjate en el /usuarios
                contentType(ContentType.Application.Json)
                setBody(datosLogin)
            }

            // Verificamos si la respuesta es exitosa (200 OK o 201 Created)
            if (respuesta.status.value == 200 || respuesta.status.value == 201) {
                respuesta.body<Persona>()
            } else {
                println("REPO: Login fallido. Código: ${respuesta.status}")
                null
            }
        } catch (e: Exception) {
            println("REPO: Error técnico: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun registrarUsuario(nickname: String, pass: String, nombre: String, apellidos: String): Boolean {
        return try {
            val datosRegistro = RegistroRequest(
                nickname = nickname,
                contrasena = pass,
                nombre = nombre,
                apellidos = apellidos
            )

            val respuesta = client.post("$baseUrl/api/usuarios") { // Ruta de crear usuario
                contentType(ContentType.Application.Json)
                setBody(datosRegistro)
            }

            // Si devuelve 200 o 201, es que se creó bien
            respuesta.status.value == 200 || respuesta.status.value == 201
        } catch (e: Exception) {
            println("Error al registrar: ${e.message}")
            false
        }
    }

    // Función para obtener la sesión de hoy de un usuario
    // AHORA ESTÁ DENTRO DE LA CLASE (antes estaba fuera y daba error)
    suspend fun obtenerSesionHoy(idUsuario: String): SesionEntrenamiento? {
        return try {
            // Hacemos la petición GET a tu API
            // Ejemplo: GET http://10.0.2.2:8080/api/sesiones/hoy/usuario123
            // Asegúrate de que esta ruta coincida con tu API Express (quizás sea /api/sesiones/...)
            val respuesta = client.get("$baseUrl/api/sesiones/hoy/$idUsuario")

            // Convertimos el JSON de respuesta a tu objeto Kotlin automáticamente
            respuesta.body<SesionEntrenamiento>()
        } catch (e: Exception) {
            // Si falla (no hay internet, error de servidor), imprimimos el error
            println("Error al obtener sesión: ${e.message}")
            null
        }
    }
}