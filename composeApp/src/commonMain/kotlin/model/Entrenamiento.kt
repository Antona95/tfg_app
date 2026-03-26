package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// esta clase representa un ejercicio base de la aplicacion.
// la uso como modelo cuando recibo o envio ejercicios por la api.
@Serializable
data class Ejercicio(

    // aqui indico que en el json el campo real se llama "_id",
    // pero en kotlin yo lo quiero llamar idEjercicio para que se entienda mejor.
    // lo pongo nullable porque a veces puede no venir informado en algunos contextos.
    @SerialName("_id") val idEjercicio: String? = null,

    // nombre visible del ejercicio, por ejemplo "sentadilla" o "press banca".
    val nombre: String
)

// esta clase representa una sesion de entrenamiento completa.
// es el objeto principal que usa tanto el coach como el alumno para ver una rutina.
@Serializable
data class SesionEntrenamiento(

    // igual que antes, en el json del backend el identificador viene como "_id".
    // aqui lo renombro a idSesion para tener un nombre mas claro en la app.
    @SerialName("_id") val idSesion: String,

    // titulo de la sesion, por ejemplo "pierna" o "25 de marzo".
    // lo dejo nullable y con valor por defecto para evitar errores si alguna sesion antigua no lo trae.
    val titulo: String? = "",

    // este booleano me dice si la sesion esta pendiente o ya se ha finalizado.
    // por defecto lo dejo en false para que una sesion nueva empiece sin finalizar.
    val finalizada: Boolean = false,

    // aqui guardo todos los ejercicios que forman parte de la sesion.
    // lo dejo como lista vacia por defecto para evitar nulos y simplificar la ui.
    val ejercicios: List<DetalleSesion> = emptyList()
)

// esta clase representa cada ejercicio concreto dentro de una sesion.
// no es solo el ejercicio base, sino el detalle real con series, repeticiones, peso y bloque.
@Serializable
data class DetalleSesion(

    // este seria el id interno del detalle si el backend lo devuelve.
    // tambien lo renombro desde "_id".
    @SerialName("_id") val idDetalle: String? = null,

    // nombre visible del ejercicio dentro de la sesion.
    // lo dejo nullable porque a veces puedo depender de lo que venga del backend.
    val nombre: String? = null,

    // id del ejercicio base al que hace referencia este detalle.
    // lo uso para vincular la sesion con la biblioteca de ejercicios.
    val idEjercicio: String? = null,

    // numero de series que hay que realizar.
    val series: Int,

    // repeticiones que hay que hacer.
    // aqui lo guardo como string porque me viene mejor para casos como "10-12".
    val repeticiones: String,

    // peso a mover en ese ejercicio.
    // lo dejo nullable porque puede haber ejercicios sin carga o datos no informados.
    val peso: Double? = null,

    // este campo me sirve para agrupar ejercicios en biseries o triseries.
    // por defecto vale 0, que significa ejercicio normal sin agrupar.
    val bloque: Int = 0
)

// este enum lo uso para representar el tipo de agrupacion de ejercicios.
// me sirve para dar significado semantico a las agrupaciones.
@Serializable
enum class TipoAgrupacion {
    // ejercicio suelto, sin relacion con otros.
    SERIE_NORMAL,
    // dos ejercicios agrupados en el mismo bloque.
    BISERIE,
    // tres ejercicios agrupados en el mismo bloque.
    TRISERIE
}