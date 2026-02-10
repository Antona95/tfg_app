package model

import kotlinx.serialization.Serializable
import kotlin.random.Random

/**
 * Utilizo esta clase para empaquetar los datos de la sesión completa
 * que voy a enviar al servidor.
 */
@Serializable
data class CrearSesionRequest(
    val idUsuario: String,       // El ID de MongoDB del alumno
    val titulo: String,          // El nombre que le puse (ej: "Pierna Hipertrofia")
    val fechaProgramada: String, // La fecha en formato String (ej: "2026-02-10")
    val ejercicios: List<CrearEjercicioRequest> // La lista de ejercicios
)

/**
 * Utilizo esta clase para definir cada ejercicio individual dentro de la sesión.
 * Es una versión simplificada de DetalleSesion, sin IDs ni objetos complejos.
 */
@Serializable
data class CrearEjercicioRequest(
    val nombreEjercicio: String, // Solo envío el nombre, el backend buscará si existe
    val seriesObjetivo: Int,     // El servidor espera un número entero
    val repeticionesObjetivo: String, // String por si pongo rangos (ej: "10-12")
    val pesoObjetivo: Double? = null, // Puede ser nulo si no asigno peso
    val notas: String? = null
)
/**
 * Clase auxiliar para la Interfaz de Usuario (UI).
 * Representa una fila temporal del formulario de "Crear Sesión".
 *
 * No lleva @Serializable porque nunca se envía al servidor directamente;
 * el Repositorio la convierte primero a CrearEjercicioRequest.
 */
data class EjercicioDraft(
// Esto genera un número aleatorio único para usar como clave en la lista.
    val id: String = Random.nextLong().toString(),

    // Uso var (variables) porque el usuario va a escribir y modificar estos datos.
    // Uso String en todo porque los campos de texto (TextField) devuelven texto.
    // Luego el repositorio se encarga de pasar "10" (texto) a 10 (entero).
    var nombre: String = "",
    var series: String = "",
    var repeticiones: String = "",
    var peso: String = ""
)

/**
 * CrearSesionRequest (Para enviar a la API).
 * CrearEjercicioRequest (Para enviar a la API).
 * EjercicioDraft (Para manejar el formulario en la pantalla).
 */