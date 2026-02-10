package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import model.Persona
import model.SesionEntrenamiento
import model.EjercicioDraft // Asegúrate de que esta clase existe en tu paquete model

// --- MODELOS DE PETICIÓN (AUTH) ---

@Serializable
data class LoginRequest(
    val nickname: String,
    val contrasena: String
)

@Serializable
data class RegistroRequest(
    val nickname: String,
    val contrasena: String,
    val nombre: String,
    val apellidos: String,
    val rol: String = "USUARIO"
)

// --- CLASE PRINCIPAL ---

class EntrenamientoRepository(private val client: HttpClient) {

    // URL base de la API. Recuerda: 10.0.2.2 es el localhost del emulador Android.
    private val baseUrl = "http://10.0.2.2:3005"

    // --- FUNCIONES DE AUTENTICACIÓN ---

    suspend fun login(nickname: String, pass: String): Persona? {
        return try {
            val datosLogin = LoginRequest(nickname = nickname, contrasena = pass)

            val respuesta = client.post("$baseUrl/api/usuarios/login") {
                contentType(ContentType.Application.Json)
                setBody(datosLogin)
            }

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

            val respuesta = client.post("$baseUrl/api/usuarios") {
                contentType(ContentType.Application.Json)
                setBody(datosRegistro)
            }
            println("Respuesta Registro: ${respuesta.status}")

            respuesta.status.value == 200 || respuesta.status.value == 201
        } catch (e: Exception) {
            println("Error al registrar: ${e.message}")
            false
        }
    }

    // --- FUNCIONES DE LECTURA DE DATOS ---

    suspend fun obtenerTodosLosUsuarios(): List<Persona> {
        return try {
            val respuesta = client.get("$baseUrl/api/usuarios").body<List<Persona>>()
            // Filtro la lista para excluir a los entrenadores y ver solo alumnos
            respuesta.filter { it.rol != "ENTRENADOR" }
        } catch (e: Exception) {
            println("Error obteniendo usuarios: ${e.message}")
            emptyList()
        }
    }

    suspend fun obtenerSesionHoy(idUsuario: String): SesionEntrenamiento? {
        return try {
            val respuesta = client.get("$baseUrl/api/sesiones/hoy/$idUsuario")
            respuesta.body<SesionEntrenamiento>()
        } catch (e: Exception) {
            println("Error al obtener sesión: ${e.message}")
            null
        }
    }

    // --- FUNCIONES DE CREACIÓN (ENTRENADOR) ---

    /*
     * Esta es la función nueva encargada de crear una sesión.
     * Recibo el ID del alumno, el título de la sesión y la lista de borradores (Draft)
     * que vienen de la pantalla de creación.
     */
    suspend fun crearSesion(
        idUsuario: String,
        titulo: String,
        listaDraft: List<EjercicioDraft>
    ): Boolean {
        return try {
            // 1. Transformación de datos
            // Recorro la lista de borradores (Strings) y la convierto en una lista de objetos
            // preparados para la API (enteros y dobles), evitando errores de conversión.
            val ejerciciosParaEnviar = listaDraft.map { borrador ->
                CrearEjercicioRequest(
                    nombreEjercicio = borrador.nombre,
                    // Si el usuario deja vacío o escribe texto, asigno 0 por seguridad
                    seriesObjetivo = borrador.series.toIntOrNull() ?: 0,
                    repeticionesObjetivo = borrador.repeticiones,
                    // Convierto el peso a Double, o null si está vacío
                    pesoObjetivo = borrador.peso.toDoubleOrNull()
                )
            }

            // 2. Preparación del paquete
            // Empaqueto toda la información en el objeto de solicitud principal.
            val peticion = CrearSesionRequest(
                idUsuario = idUsuario,
                titulo = titulo,
                fechaProgramada = "2026-02-10", // De momento uso una fecha fija o la actual
                ejercicios = ejerciciosParaEnviar
            )

            // 3. Envío al servidor
            val respuesta = client.post("$baseUrl/api/sesiones") {
                contentType(ContentType.Application.Json)
                setBody(peticion)
            }

            // 4. Verificación
            val esExito = respuesta.status.value == 200 || respuesta.status.value == 201

            if (esExito) {
                println("REPO: Sesión creada con éxito.")
            } else {
                println("REPO: Error al crear sesión. Código: ${respuesta.status}")
            }

            return esExito

        } catch (e: Exception) {
            println("REPO: Error técnico al crear sesión: ${e.message}")
            return false
        }
    }
}

// --- MODELOS DE PETICIÓN NUEVOS (DTOs) ---
// Los coloco aquí para que estén accesibles dentro del repositorio

@Serializable
data class CrearSesionRequest(
    val idUsuario: String,
    val titulo: String,
    val fechaProgramada: String,
    val ejercicios: List<CrearEjercicioRequest>
)

@Serializable
data class CrearEjercicioRequest(
    val nombreEjercicio: String,
    val seriesObjetivo: Int,
    val repeticionesObjetivo: String,
    val pesoObjetivo: Double? = null,
    val notas: String? = null
)