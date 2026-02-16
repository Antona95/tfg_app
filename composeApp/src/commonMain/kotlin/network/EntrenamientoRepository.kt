package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.patch // AÑADIDO
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import model.Persona
import model.SesionEntrenamiento
import model.LoginRequest
import model.RegistroRequest
import model.CrearSesionRequest

class EntrenamientoRepository(
    private val client: HttpClient,
    private val baseUrl: String = "http://10.0.2.2:3005"
) {
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
            e.printStackTrace()
            false
        }
    }

    // --- LECTURA ---
    suspend fun obtenerTodosLosUsuarios(): List<Persona> {
        return try {
            val respuesta = client.get("$baseUrl/api/usuarios")
            if (respuesta.status.value in 200..299) {
                respuesta.body<List<Persona>>().filter { it.rol != "ENTRENADOR" }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obtenerSesionHoy(idUsuario: String): SesionEntrenamiento? {
        return try {
            val respuesta = client.get("$baseUrl/api/sesiones/hoy/$idUsuario")
            if (respuesta.status.value in 200..299) {
                respuesta.body<SesionEntrenamiento>()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun obtenerHistorialSesiones(idUsuario: String): List<SesionEntrenamiento> {
        return try {
            val respuesta = client.get("$baseUrl/api/sesiones/usuario/$idUsuario")
            val lista = respuesta.body<List<SesionEntrenamiento>>()
            println("DEBUG: Sesiones recibidas -> ${lista.size}")
            lista
        } catch (e: Exception) {
            println("DEBUG: Error en historial -> ${e.message}")
            emptyList()
        }
    }

    // --- CREACIÓN ---
    suspend fun crearSesion(request: CrearSesionRequest): Boolean {
        return try {
            val respuesta = client.post("$baseUrl/api/sesiones/app") { // CORREGIDO: minúscula
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun finalizarSesion(idSesion: String): Boolean {
        return try {
            val respuesta = client.patch("$baseUrl/api/sesiones/$idSesion/finalizar") {
                contentType(ContentType.Application.Json)
            }
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
