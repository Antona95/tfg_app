package repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import model.CrearEjercicioRequest
import model.CrearSesionRequest
import model.SesionEntrenamiento
import network.ApiConfig

// este repositorio se encarga de toda la gestion de sesiones.
// aqui meto historial, sesion actual, creacion y finalizacion.
class SesionRepository(
    private val client: HttpClient
) {
    private val baseUrl = ApiConfig.BASE_URL

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

            // aqui no busco "la primera pendiente" del historial,
            // porque eso puede rescatar sesiones antiguas que ya no deben aparecer en hoy.
            //
            // lo que hago es coger solo la sesion mas reciente.
            val ultimaSesion = historial.firstOrNull()

            // si no hay ninguna sesion, devuelvo null.
            if (ultimaSesion == null) {
                null
            }
            // si la ultima sesion ya esta finalizada,
            // en la pantalla de hoy no debo mostrar nada.
            else if (ultimaSesion.finalizada) {
                null
            }
            // si la ultima sesion no esta finalizada,
            // esa si es la que debo mostrar en hoy.
            else {
                ultimaSesion
            }
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

    suspend fun finalizarSesion(
        idSesion: String,
        ejercicios: List<CrearEjercicioRequest>
    ): Boolean {
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
}