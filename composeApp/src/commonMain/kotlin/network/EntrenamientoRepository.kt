package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import model.*

// este repositorio es la capa que uso para comunicar la app con la api.
// aqui concentro todas las peticiones http para no mezclar red con la interfaz.
class EntrenamientoRepository(

    // inyecto el cliente ktor ya configurado para reutilizarlo en todas las llamadas.
    private val client: HttpClient
) {

    // cojo la url base desde apiconfig para no repetirla en cada metodo.
    private val baseUrl = ApiConfig.BASE_URL

    // este metodo sirve para iniciar sesion.
    // envio nickname y password al backend y espero recibir una persona si todo va bien.
    suspend fun login(nickname: String, pass: String): Persona? {
        return try {

            // hago una peticion post al endpoint de login.
            val respuesta = client.post("$baseUrl/api/usuarios/login") {

                // indico que envio json.
                contentType(ContentType.Application.Json)

                // convierto el loginrequest a json y lo mando en el body.
                setBody(LoginRequest(nickname, pass))
            }

            // si el servidor responde con codigo correcto, convierto la respuesta en persona.
            if (respuesta.status.value in 200..299) {
                respuesta.body<Persona>()
            } else {

                // si el error es http pero no de conexion, devuelvo null.
                // esto me permite diferenciar credenciales incorrectas de fallo de red.
                println("ERROR HTTP [login]: ${respuesta.status.value}")
                null
            }
        } catch (e: Exception) {

            // si entra aqui, normalmente es porque no hay conexion o el servidor no responde.
            throw Exception("No hay conexión con el servidor. Revisa tu Internet.")
        }
    }

    // este metodo registra un usuario desde la app.
    // devuelvo boolean porque solo me interesa saber si se ha creado o no.
    suspend fun registrarUsuario(
        nickname: String,
        pass: String,
        nombre: String,
        apellidos: String
    ): Boolean {
        return try {

            // envio los datos del registro al endpoint de usuarios.
            val respuesta = client.post("$baseUrl/api/usuarios") {
                contentType(ContentType.Application.Json)
                setBody(RegistroRequest(nickname, pass, nombre, apellidos))
            }

            // si la api responde bien, considero que el registro ha sido correcto.
            if (respuesta.status.value in 200..299) {
                true
            } else {

                // si hay error http, lo muestro por consola y devuelvo false.
                println("ERROR HTTP [registrarUsuario]: ${respuesta.status.value}")
                false
            }
        } catch (e: Exception) {

            // si falla la conexion, lanzo una excepcion con un mensaje mas claro para la ui.
            throw Exception("No hay conexión con el servidor. Revisa tu Internet.")
        }
    }

    // este metodo obtiene todos los usuarios desde la api.
    // luego filtro para quitar a los entrenadores y quedarme solo con alumnos.
    suspend fun obtenerTodosLosUsuarios(): List<Persona> {
        return try {
            val respuesta = client.get("$baseUrl/api/usuarios")

            if (respuesta.status.value in 200..299) {

                // aqui hago un filtro porque en el panel del coach no quiero mostrar otros entrenadores.
                respuesta.body<List<Persona>>().filter { it.rol != "ENTRENADOR" }
            } else {

                // si el servidor no responde bien pero no hay fallo de red, devuelvo lista vacia.
                emptyList()
            }
        } catch (e: Exception) {
            throw Exception("Fallo de red al cargar la lista de alumnos.")
        }
    }

    // este metodo pide a la api la sesion de hoy de un usuario.
    // aunque en mi proyecto luego he trabajado mas con la ultima sesion no finalizada,
    // este endpoint sigue existiendo en la api.
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

    // este metodo obtiene la ultima sesion util para el alumno.
    // en mi caso no cojo simplemente la mas nueva, sino la primera que no este finalizada.
    suspend fun obtenerUltimaSesion(idUsuario: String): SesionEntrenamiento? {
        return try {

            // primero pido el historial completo.
            val historial = obtenerHistorialSesiones(idUsuario)

            // luego me quedo con la primera sesion no finalizada.
            // esto me sirve para que el alumno vea su sesion activa.
            historial.firstOrNull { !it.finalizada }
        } catch (e: Exception) {
            throw Exception("Fallo de red al buscar la última sesión.")
        }
    }

    // este metodo devuelve el historial completo de sesiones de un usuario.
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

    // este metodo crea una nueva sesion desde la app del coach.
    // recibe ya el objeto preparado y lo manda al backend.
    suspend fun crearSesion(request: CrearSesionRequest): Boolean {
        return try {
            val respuesta = client.post("$baseUrl/api/sesiones/app") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            // devuelvo true o false segun el codigo http.
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            throw Exception("No hay conexión. No se pudo guardar la sesión.")
        }
    }

    // este metodo finaliza una sesion.
    // envio el id de la sesion y la lista de ejercicios actualizada con peso y repeticiones reales.
    suspend fun finalizarSesion(
        idSesion: String,
        ejercicios: List<CrearEjercicioRequest>
    ): Boolean {
        return try {

            // aqui uso patch porque no estoy recreando la sesion completa,
            // sino actualizando solo parte de su informacion.
            val respuesta = client.patch("$baseUrl/api/sesiones/$idSesion/finalizar") {
                contentType(ContentType.Application.Json)

                // envio un map porque el backend espera un objeto con la clave "ejercicios".
                setBody(mapOf("ejercicios" to ejercicios))
            }

            respuesta.status.value in 200..299
        } catch (e: Exception) {
            throw Exception("No hay conexión. No se pudo finalizar la sesión.")
        }
    }

    // este metodo elimina un alumno por nickname.
    // lo usa el coach desde su panel.
    suspend fun eliminarAlumno(nickname: String): Boolean {
        return try {
            val respuesta = client.delete("$baseUrl/api/usuarios/$nickname")
            respuesta.status.value in 200..299
        } catch (e: Exception) {
            throw Exception("Error de red al intentar eliminar el alumno.")
        }
    }

    // este metodo crea un alumno nuevo desde el panel del entrenador.
    // aqui fuerzo el rol "usuario" para asegurar que se cree como alumno.
    suspend fun crearAlumno(
        nickname: String,
        pass: String,
        nombre: String,
        apellidos: String
    ): Boolean {
        return try {
            val respuesta = client.post("$baseUrl/api/usuarios") {
                contentType(ContentType.Application.Json)

                // aqui envio explicitamente el rol usuario.
                setBody(RegistroRequest(nickname, pass, nombre, apellidos, "USUARIO"))
            }

            respuesta.status.value in 200..299
        } catch (e: Exception) {
            throw Exception("Error de red al crear el alumno.")
        }
    }
}