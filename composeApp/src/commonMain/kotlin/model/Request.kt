package model

import kotlinx.serialization.Serializable
import kotlin.random.Random

// esta clase representa la peticion completa que envio al backend
// cuando el coach crea una nueva sesion desde la app.
@Serializable
data class CrearSesionRequest(

    // id del usuario al que se le asigna la sesion.
    // normalmente sera el id del alumno seleccionado por el coach.
    val idUsuario: String,

    // titulo de la sesion, por ejemplo "pierna" o "25 de marzo".
    val titulo: String,

    // lista de ejercicios que forman parte de la sesion.
    // cada elemento de la lista se construye con la clase crearejerciciorequest.
    val ejercicios: List<CrearEjercicioRequest>
)

// esta clase representa cada ejercicio individual que mando al backend
// dentro de una nueva sesion.
@Serializable
data class CrearEjercicioRequest(

    // nombre del ejercicio.
    // en mi proyecto el backend lo usa para construir el detalle de la sesion.
    val nombre: String,

    // numero de series que hay que hacer.
    val series: Int,

    // repeticiones.
    // lo dejo como string porque me interesa permitir formatos como "10" o "10-12".
    val repeticiones: String,

    // peso del ejercicio.
    // lo dejo nullable por si no se informa o si el ejercicio no necesita carga.
    val peso: Double? = null,

    // este campo sirve para agrupar ejercicios en bloques.
    // por defecto vale 0, que seria un ejercicio normal no agrupado.
    val bloque: Int = 0,
)

// esta clase no la uso para enviar directamente al backend,
// sino para manejar los datos del formulario en la interfaz.
data class EjercicioDraft(

    // genero un id aleatorio para poder identificar cada fila del formulario.
    // esto me viene bien al trabajar con listas dinamicas en compose.
    val id: String = Random.nextLong().toString(),

    // nombre que el coach escribe en el formulario.
    var nombre: String = "",

    // aqui guardo las series como string porque vienen de un textfield.
    // luego en el viewmodel o en el repositorio ya las convierto a int.
    var series: String = "",

    // igual que antes, las repeticiones las manejo como texto porque vienen de la ui.
    var repeticiones: String = "",

    // el peso tambien lo manejo como string en el formulario,
    // y despues lo transformo a double cuando preparo la peticion real.
    var peso: String = "",

    // bloque al que pertenece este ejercicio.
    // me sirve para representar biseries o triseries en la interfaz.
    val bloque: Int = 0,

    // este booleano solo lo uso en la interfaz.
    // me permite marcar ejercicios para agruparlos o desagruparlos.
    val seleccionado: Boolean = false
)