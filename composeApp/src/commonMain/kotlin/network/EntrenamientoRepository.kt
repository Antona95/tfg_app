package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import model.SesionEntrenamiento

class EntrenamientoRepository(private val client: HttpClient) {

    // URL base de tu API (esto cambiará cuando despliegues la API real)
    // Nota: Para el emulador de Android, "localhost" es "10.0.2.2"
    private val baseUrl = "http://10.0.2.2:8080"

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