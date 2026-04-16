package repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import model.Persona
import model.RegistroRequest
import network.ApiConfig

// este repositorio se encarga de la gestion de usuarios y alumnos.
// aqui meto listado, creacion y borrado de alumnos.
class UserRepository(
    private val client: HttpClient
) {
    private val baseUrl = ApiConfig.BASE_URL

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

    suspend fun eliminarAlumno(nickname: String): Boolean {
        return try {
            val respuesta = client.delete("$baseUrl/api/usuarios/$nickname")
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            throw Exception("Error de red al intentar eliminar el alumno.")
        }
    }

    suspend fun crearAlumno(
        nickname: String,
        pass: String,
        nombre: String,
        apellidos: String
    ): Boolean {
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