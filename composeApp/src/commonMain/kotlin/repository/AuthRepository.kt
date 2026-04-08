package repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import model.LoginRequest
import model.Persona
import model.RegistroRequest
import network.ApiConfig

// este repositorio se encarga solo de la parte de autenticacion.
// aqui meto login y registro general de usuario.
class AuthRepository(
    private val client: HttpClient
) {
    private val baseUrl = ApiConfig.BASE_URL

    suspend fun login(nickname: String, pass: String): Persona? {
        return try {
            val respuesta = client.post("$baseUrl/api/usuarios/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(nickname, pass))
            }

            if (respuesta.status.value in 200..299) {
                respuesta.body<Persona>()
            } else {
                println("ERROR HTTP [login]: ${respuesta.status.value}")
                null
            }
        } catch (e: Exception) {
            throw Exception("No hay conexión con el servidor. Revisa tu Internet.")
        }
    }

    suspend fun registrarUsuario(
        nickname: String,
        pass: String,
        nombre: String,
        apellidos: String
    ): Boolean {
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
}