package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.request.delete
import io.ktor.http.ContentType
import io.ktor.http.contentType
import model.*
import io.ktor.client.statement.bodyAsText

class EntrenamientoRepository(
    private val client: HttpClient
) {
    // Usamos el puerto 8080 como está en tu configuración actual
    private val baseUrl = "http://10.0.2.2:3005"

    // --- AUTENTICACIÓN ---
    suspend fun login(nickname: String, pass: String): Persona? {
        return try {
            val respuesta = client.post("$baseUrl/api/usuarios/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(nickname, pass))
            }
            if (respuesta.status.value in 200..299) {
                respuesta.body<Persona>()
            } else {
                // LEEMOS EL ERROR PARA LIBERAR LA CONEXIÓN
                val errorBody = respuesta.bodyAsText()
                println(" ERROR HTTP LOGIN: ${respuesta.status.value} - Detalle: $errorBody")
                null
            }
        } catch (e: Exception) {
            println(" EXCEPCIÓN KTOR EN LOGIN: ${e.message}")
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

    // --- USUARIOS ---
    suspend fun obtenerTodosLosUsuarios(): List<Persona> {
        return try {
            val respuesta = client.get("$baseUrl/api/usuarios")
            if (respuesta.status.value in 200..299) {
                respuesta.body<List<Persona>>().filter { it.rol != "ENTRENADOR" }
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    // --- SESIONES ---
    suspend fun obtenerSesionHoy(idUsuario: String): SesionEntrenamiento? {
        return try {
            val respuesta = client.get("$baseUrl/api/sesiones/hoy/$idUsuario")
            if (respuesta.status.value in 200..299) respuesta.body<SesionEntrenamiento>() else null
        } catch (e: Exception) { null }
    }

    suspend fun obtenerHistorialSesiones(idUsuario: String): List<SesionEntrenamiento> {
        return try {
            val respuesta = client.get("$baseUrl/api/sesiones/usuario/$idUsuario")
            if (respuesta.status.value in 200..299) respuesta.body<List<SesionEntrenamiento>>() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun crearSesion(request: CrearSesionRequest): Boolean {
        return try {
            val respuesta = client.post("$baseUrl/api/sesiones/app") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            respuesta.status.value in 200..299
        } catch (e: Exception) { false }
    }

    suspend fun finalizarSesion(idSesion: String): Boolean {
        return try {
            val respuesta = client.patch("$baseUrl/api/sesiones/$idSesion/finalizar") {
                contentType(ContentType.Application.Json)
            }
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            println(" Error al finalizar sesión: ${e.message}")
            false
        }
    }

    // --- EJERCICIOS (NUEVA FUNCIONALIDAD) ---
    
    /**
     * Obtiene la lista global de ejercicios disponibles en el sistema.
     */
    suspend fun obtenerEjerciciosBiblioteca(): List<Ejercicio> {
        return try {
            val respuesta = client.get("$baseUrl/api/ejercicios")
            if (respuesta.status.value in 200..299) {
                respuesta.body<List<Ejercicio>>()
            } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Crea un nuevo ejercicio en la base de datos (Biblioteca global).
     */
    suspend fun crearEjercicioBiblioteca(nombre: String): Ejercicio? {
        return try {
            // Enviamos un objeto que coincida con lo que espera tu CrearEjercicioUseCase
            val respuesta = client.post("$baseUrl/api/ejercicios") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("nombre" to nombre)) // O un DTO si tienes más campos
            }
            if (respuesta.status.value in 200..299) {
                respuesta.body<Ejercicio>()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Borrar un alumno usando su nickname
    suspend fun eliminarAlumno(nickname: String): Boolean {
        return try {
            val respuesta = client.delete("$baseUrl/api/usuarios/$nickname")
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    // Crear un alumno (el Coach rellena los datos)
    suspend fun crearAlumno(nickname: String, pass: String, nombre: String, apellidos: String): Boolean {
        return try {
            val respuesta = client.post("$baseUrl/api/usuarios") {
                contentType(ContentType.Application.Json)
                setBody(RegistroRequest(nickname, pass, nombre, apellidos, "USUARIO"))
            }
            if (respuesta.status.value !in 200..299) {
                println(" Error Servidor: ${respuesta.bodyAsText()}")
            }
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }
}
