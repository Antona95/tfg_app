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

class EntrenamientoRepository(
    private val client: HttpClient
) {
    // Usamos el puerto 8080 como está en tu configuración actual
    private val baseUrl = "http://10.0.2.2:8080"

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
                println("ERROR HTTP [login]: ${respuesta.status.value} - ${respuesta.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            println("EXCEPCION KTOR [login]: ${e.message}")
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
            if (respuesta.status.value in 200..299) {
                true
            } else {
                println("ERROR HTTP [registrarUsuario]: ${respuesta.status.value} - ${respuesta.bodyAsText()}")
                false
            }
        } catch (e: Exception) {
            println("EXCEPCION KTOR [registrarUsuario]: ${e.message}")
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
            } else {
                println("ERROR HTTP [obtenerTodosLosUsuarios]: ${respuesta.status.value} - ${respuesta.bodyAsText()}")
                emptyList()
            }
        } catch (e: Exception) {
            println("EXCEPCION KTOR [obtenerTodosLosUsuarios]: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    // --- SESIONES ---
    suspend fun obtenerSesionHoy(idUsuario: String): SesionEntrenamiento? {
        return try {
            val respuesta = client.get("$baseUrl/api/sesiones/hoy/$idUsuario")
            if (respuesta.status.value in 200..299) {
                respuesta.body<SesionEntrenamiento>()
            } else {
                println("ERROR HTTP [obtenerSesionHoy]: ${respuesta.status.value} - ${respuesta.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            println("EXCEPCION KTOR [obtenerSesionHoy]: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun obtenerUltimaSesion(idUsuario: String): SesionEntrenamiento? {
        return try {
            val historial = obtenerHistorialSesiones(idUsuario)
            historial.lastOrNull()
        } catch (e: Exception) {
            println("EXCEPCION KTOR [obtenerUltimaSesion]: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun obtenerHistorialSesiones(idUsuario: String): List<SesionEntrenamiento> {
        return try {
            val respuesta = client.get("$baseUrl/api/sesiones/usuario/$idUsuario")
            if (respuesta.status.value in 200..299) {
                respuesta.body<List<SesionEntrenamiento>>()
            } else {
                println("ERROR HTTP [obtenerHistorialSesiones]: ${respuesta.status.value} - ${respuesta.bodyAsText()}")
                emptyList()
            }
        } catch (e: Exception) {
            println("EXCEPCION KTOR [obtenerHistorialSesiones]: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun crearSesion(request: CrearSesionRequest): Boolean {
        return try {
            val respuesta = client.post("$baseUrl/api/sesiones/app") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (respuesta.status.value in 200..299) {
                true
            } else {
                println("ERROR HTTP [crearSesion]: ${respuesta.status.value} - ${respuesta.bodyAsText()}")
                false
            }
        } catch (e: Exception) {
            println("EXCEPCION KTOR [crearSesion]: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun finalizarSesion(idSesion: String, ejercicios: List<CrearEjercicioRequest>): Boolean {
        return try {
            val respuesta = client.patch("$baseUrl/api/sesiones/$idSesion/finalizar") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("ejercicios" to ejercicios))
            }
            if (respuesta.status.value in 200..299) {
                true
            } else {
                println("ERROR HTTP [finalizarSesion]: ${respuesta.status.value} - ${respuesta.bodyAsText()}")
                false
            }
        } catch (e: Exception) {
            println("EXCEPCION KTOR [finalizarSesion]: ${e.message}")
            e.printStackTrace()
            false
        }
    }


    // --- GESTIÓN DE ALUMNOS ---

    suspend fun eliminarAlumno(nickname: String): Boolean {
        return try {
            val respuesta = client.delete("$baseUrl/api/usuarios/$nickname")
            if (respuesta.status.value in 200..299) {
                true
            } else {
                println("ERROR HTTP [eliminarAlumno]: ${respuesta.status.value} - ${respuesta.bodyAsText()}")
                false
            }
        } catch (e: Exception) {
            println("EXCEPCION KTOR [eliminarAlumno]: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun crearAlumno(nickname: String, pass: String, nombre: String, apellidos: String): Boolean {
        return try {
            val respuesta = client.post("$baseUrl/api/usuarios") {
                contentType(ContentType.Application.Json)
                setBody(RegistroRequest(nickname, pass, nombre, apellidos, "USUARIO"))
            }
            if (respuesta.status.value in 200..299) {
                true
            } else {
                println("ERROR HTTP [crearAlumno]: ${respuesta.status.value} - ${respuesta.bodyAsText()}")
                false
            }
        } catch (e: Exception) {
            println("EXCEPCION KTOR [crearAlumno]: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}