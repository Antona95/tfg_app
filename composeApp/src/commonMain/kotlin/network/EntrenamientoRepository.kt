package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.request.delete
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import model.*

/**
 * ============================================================================
 * FUENTE DE DATOS PRINCIPAL: INTEGRACIÓN CON API REST (NODE.JS + MONGODB)
 * ============================================================================
 * Esta clase actúa como la capa de repositorio remoto de la aplicación.
 * Para garantizar la ligereza del cliente móvil, la app NO almacena bases
 * de datos complejas localmente.
 * Todo el flujo de datos (usuarios, sesiones, historial) se consume de forma
 * dinámica desde una API RESTful propia desplegada en el servidor.
 * Utiliza Ktor Client [HttpClient] para realizar peticiones HTTP asíncronas
 * y transformar las respuestas JSON en Data Classes nativas de Kotlin.
 */


class EntrenamientoRepository(
    private val client: HttpClient
) {
    // Usamos el puerto 8080 como está en tu configuración actual
    private val baseUrl = "http://10.0.2.2:8080"

    // =========================================================================
    // APUNTE DE CLASE: GESTIÓN DE EXCEPCIONES EN LA CAPA DE DATOS
    // Si la librería de red (Ktor) falla (ej: no hay WiFi, Modo Avión),
    // NO devolvemos 'null' o 'false'. Debemos lanzar una excepción clara (throw)
    // para que la capa superior (ViewModel) la intercepte y se la muestre al usuario.
    // =========================================================================

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
                // Si el server responde 401 (No autorizado), aquí sí devolvemos null
                // porque la conexión funcionó, simplemente la contraseña es incorrecta.
                println("ERROR HTTP [login]: ${respuesta.status.value}")
                null
            }
        } catch (e: Exception) {
            // ERROR DE RED: Lanzamos el aviso hacia el ViewModel
            throw Exception("No hay conexión con el servidor. Revisa tu Internet.")
        }
    }

    suspend fun registrarUsuario(nickname: String, pass: String, nombre: String, apellidos: String): Boolean {
        return try {
            val respuesta = client.post("$baseUrl/api/usuarios") {
                contentType(ContentType.Application.Json)
                setBody(RegistroRequest(nickname, pass, nombre, apellidos))
            }
            if (respuesta.status.value in 200..299) {
                true
            } else {
                println("ERROR HTTP [registrarUsuario]: ${respuesta.status.value}")
                false
            }
        } catch (e: Exception) {
            throw Exception("No hay conexión con el servidor. Revisa tu Internet.")
        }
    }

    // --- USUARIOS ---
    suspend fun obtenerTodosLosUsuarios(): List<Persona> {
        return try {
            val respuesta = client.get("$baseUrl/api/usuarios")
            if (respuesta.status.value in 200..299) {
                respuesta.body<List<Persona>>().filter { it.rol != "ENTRENADOR" }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            throw Exception("Fallo de red al cargar la lista de alumnos.")
        }
    }

    // --- SESIONES ---
    suspend fun obtenerSesionHoy(idUsuario: String): SesionEntrenamiento? {
        return try {
            val respuesta = client.get("$baseUrl/api/sesiones/hoy/$idUsuario")
            if (respuesta.status.value in 200..299) {
                respuesta.body<SesionEntrenamiento>()
            } else {
                null
            }
        } catch (e: Exception) {
            throw Exception("Fallo de red al buscar el entrenamiento de hoy.")
        }
    }

    suspend fun obtenerUltimaSesion(idUsuario: String): SesionEntrenamiento? {
        return try {
            val historial = obtenerHistorialSesiones(idUsuario)
            historial.lastOrNull()
        } catch (e: Exception) {
            throw Exception("Fallo de red al buscar la última sesión.")
        }
    }

    suspend fun obtenerHistorialSesiones(idUsuario: String): List<SesionEntrenamiento> {
        return try {
            val respuesta = client.get("$baseUrl/api/sesiones/usuario/$idUsuario")
            if (respuesta.status.value in 200..299) {
                respuesta.body<List<SesionEntrenamiento>>()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            throw Exception("Fallo de red al cargar el historial.")
        }
    }

    suspend fun crearSesion(request: CrearSesionRequest): Boolean {
        return try {
            val respuesta = client.post("$baseUrl/api/sesiones/app") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            throw Exception("No hay conexión. No se pudo guardar la sesión.")
        }
    }

    suspend fun finalizarSesion(idSesion: String, ejercicios: List<CrearEjercicioRequest>): Boolean {
        return try {
            val respuesta = client.patch("$baseUrl/api/sesiones/$idSesion/finalizar") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("ejercicios" to ejercicios))
            }
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            throw Exception("No hay conexión. No se pudo finalizar la sesión.")
        }
    }

    // --- GESTIÓN DE ALUMNOS ---
    suspend fun eliminarAlumno(nickname: String): Boolean {
        return try {
            val respuesta = client.delete("$baseUrl/api/usuarios/$nickname")
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            throw Exception("Error de red al intentar eliminar el alumno.")
        }
    }

    suspend fun crearAlumno(nickname: String, pass: String, nombre: String, apellidos: String): Boolean {
        return try {
            val respuesta = client.post("$baseUrl/api/usuarios") {
                contentType(ContentType.Application.Json)
                setBody(RegistroRequest(nickname, pass, nombre, apellidos, "USUARIO"))
            }
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            throw Exception("Error de red al crear el alumno.")
        }
    }
}