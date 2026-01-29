package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import model.SesionEntrenamiento
import io.ktor.client.request.post // Importante: POST
import io.ktor.client.request.setBody // Para meter datos en la petición
import io.ktor.http.ContentType
import io.ktor.http.contentType
import model.LoginRequest
import model.Persona

class EntrenamientoRepository(private val client: HttpClient) {

    // URL base de tu API (esto cambiará cuando despliegues la API real)
    // Nota: Para el emulador de Android, "localhost" es "10.0.2.2"
    // Recuerda: 10.0.2.2 es "localhost" desde el emulador de Android
    private val baseUrl = "http://10.0.2.2:8080"
    // private val baseUrl = "http://192.168.1.XX:3000" // Si pruebas en un móvil físico, usa la IP de tu PC

    // Función de Login
    suspend fun login(dni: String, pass: String): Persona? {
        return try {
            // Empaquetamos los datos
            val datosLogin = LoginRequest(dni = dni, contrasena = pass)

            // Enviamos la petición POST
            val respuesta = client.post("$baseUrl/api/login") { // Ajusta la ruta a la de tu API (/api/login, /auth/login...)
                contentType(ContentType.Application.Json) // Avisamos que enviamos JSON
                setBody(datosLogin) // Metemos el sobre dentro
            }

            // Si todo va bien, el servidor devuelve los datos de la Persona
            respuesta.body<Persona>()
        } catch (e: Exception) {
            println("Error en Login: ${e.message}")
            // Aquí podrías diferenciar si es error de contraseña (401) o de servidor (500)
            null
        }
    }
    // Función para obtener la sesión de hoy de un usuario
    suspend fun obtenerSesionHoy(idUsuario: String): SesionEntrenamiento? {
        return try {
            // Hacemos la petición GET a tu API
            // Ejemplo: GET http://10.0.2.2:8080/entrenamientos/hoy/usuario123
            val respuesta = client.get("$baseUrl/entrenamientos/hoy/$idUsuario")

            // Convertimos el JSON de respuesta a tu objeto Kotlin automáticamente
            respuesta.body<SesionEntrenamiento>()
        } catch (e: Exception) {
            // Si falla (no hay internet, error de servidor), imprimimos el error
            println("Error al obtener sesión: ${e.message}")
            null
        }
    }
}