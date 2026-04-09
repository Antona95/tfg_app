package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.CrearEjercicioRequest
import model.CrearSesionRequest
import model.EjercicioDraft
import model.SesionEntrenamiento
import repository.SesionRepository

// esta sealed class representa los posibles estados de la pantalla de creacion de sesion.
// me sirve para que la ui sepa si esta en reposo, cargando, si ha salido bien o si hay error.
sealed class SesionUiState {
    object Idle : SesionUiState()
    object Loading : SesionUiState()
    object Success : SesionUiState()
    data class Error(val mensaje: String) : SesionUiState()
}

class SesionViewModel(
    // inyecto el repositorio para no meter aqui directamente la logica de red.
    private val repository: SesionRepository
) : ViewModel() {

    // aqui guardo el estado general de la pantalla.
    // empiezo en idle porque al abrir la pantalla todavia no he hecho ninguna accion.
    private val _uiState = MutableStateFlow<SesionUiState>(SesionUiState.Idle)
    val uiState: StateFlow<SesionUiState> = _uiState.asStateFlow()

    // aqui guardo la lista de ejercicios que el coach va montando en el formulario.
    // uso ejerciciosdraft porque en la ui trabajo con texto y no con el modelo final de backend.
    private val _listaEjercicios = MutableStateFlow<List<EjercicioDraft>>(emptyList())
    val listaEjercicios: StateFlow<List<EjercicioDraft>> = _listaEjercicios.asStateFlow()

    // esta variable me sirve para asignar ids de bloque a las biseries o triseries.
    // cada vez que creo una agrupacion nueva, aumento este contador.
    private var ultimoBloqueId = 0

    // este metodo inicializa el formulario.
    // si me pasan una sesion base, la convierto en borradores para poder editarla.
    fun inicializarConSesionBase(sesionBase: SesionEntrenamiento?, forzar: Boolean = false) {

        // si ya tengo ejercicios cargados y no me obligan a forzar, no vuelvo a inicializar.
        // esto evita sobreescribir el formulario si la pantalla recompone.
        if (!forzar && _listaEjercicios.value.isNotEmpty()) return

        _listaEjercicios.value = sesionBase?.ejercicios?.map { detalle ->
            EjercicioDraft(
                // convierto cada detalle de sesion a un borrador editable.
                nombre = detalle.nombre ?: "",
                series = detalle.series.toString(),
                repeticiones = detalle.repeticiones,
                peso = detalle.peso?.toString() ?: "0.0",

                // mantengo el bloque que ya traiga la sesion base.
                bloque = detalle.bloque,

                // cuando duplico o cargo una sesion, ningun ejercicio empieza marcado.
                seleccionado = false
            )
        } ?: listOf(
            // si no hay sesion base, creo un ejercicio por defecto
            // para que el formulario no salga vacio.
            EjercicioDraft(
                nombre = "",
                series = "3",
                repeticiones = "10",
                peso = "0.0",
                bloque = 0,
                seleccionado = false
            )
        )

        // aqui recalculo el ultimo bloque usado.
        // asi, si duplico una sesion que ya tenia bloques 1, 2 o 3,
        // la siguiente agrupacion nueva no reutiliza esos ids.
        ultimoBloqueId = _listaEjercicios.value.maxOfOrNull { it.bloque } ?: 0
    }

    // este metodo añade un ejercicio nuevo al formulario.
    fun agregarEjercicio() {
        _listaEjercicios.value = _listaEjercicios.value + EjercicioDraft(
            nombre = "",
            series = "3",
            repeticiones = "10",
            peso = "0.0",
            bloque = 0,
            seleccionado = false
        )
    }

    // este metodo elimina un ejercicio concreto segun su posicion en la lista.
    //
    // ademas de borrar la fila, tambien reviso si algun bloque se queda roto.
    // por ejemplo:
    // - si una triserie pierde 1 ejercicio, los otros 2 pueden seguir como biserie
    // - si una biserie pierde 1 ejercicio, el que queda pasa a ejercicio unico
    fun eliminarEjercicio(index: Int) {
        val listaMutable = _listaEjercicios.value.toMutableList()

        // compruebo que el indice exista para no provocar errores.
        if (index !in listaMutable.indices) return

        // borro el ejercicio en la posicion indicada.
        listaMutable.removeAt(index)

        // ahora reviso los bloques que siguen existiendo.
        // solo me interesan los que no son 0, porque 0 significa ejercicio unico.
        val bloquesRestantes = listaMutable
            .filter { it.bloque != 0 }
            .groupBy { it.bloque }

        // si un bloque se queda con menos de 2 ejercicios,
        // ya no tiene sentido como agrupacion.
        // por eso esos ejercicios pasan a bloque 0.
        val bloquesQueSeRompen = bloquesRestantes
            .filterValues { ejercicios -> ejercicios.size < 2 }
            .keys

        // reconstruyo la lista final corrigiendo los bloques rotos.
        _listaEjercicios.value = listaMutable.map { draft ->
            if (draft.bloque in bloquesQueSeRompen) {
                draft.copy(bloque = 0)
            } else {
                draft
            }
        }
    }

    // este metodo actualiza un ejercicio concreto cuando el usuario modifica sus campos.
    fun actualizarEjercicio(index: Int, nuevo: EjercicioDraft) {
        val listaMutable = _listaEjercicios.value.toMutableList()

        // otra vez valido que el indice exista antes de modificar.
        if (index in listaMutable.indices) {
            listaMutable[index] = nuevo
            _listaEjercicios.value = listaMutable
        }
    }

    // este metodo marca o desmarca un ejercicio del formulario.
    // lo usare cuando el coach pulse sobre una tarjeta o su checkbox.
    fun toggleSeleccionEjercicio(index: Int) {
        val listaMutable = _listaEjercicios.value.toMutableList()

        if (index in listaMutable.indices) {
            val actual = listaMutable[index]
            listaMutable[index] = actual.copy(seleccionado = !actual.seleccionado)
            _listaEjercicios.value = listaMutable
        }
    }

    // este metodo limpia la seleccion de todos los ejercicios.
    // me sirve despues de agrupar o si quiero cancelar la seleccion.
    fun limpiarSeleccion() {
        _listaEjercicios.value = _listaEjercicios.value.map { it.copy(seleccionado = false) }
    }

    // este metodo agrupa exactamente los ejercicios seleccionados.
    // si la cantidad no coincide, no hago nada y devuelvo false.
    fun agruparSeleccionados(cantidad: Int): Boolean {

        // aqui obtengo las posiciones de los ejercicios marcados.
        val seleccionados = _listaEjercicios.value
            .mapIndexedNotNull { index, ej ->
                if (ej.seleccionado) index else null
            }

        // para biserie necesito 2 y para triserie necesito 3.
        if (seleccionados.size != cantidad) return false

        // creo un bloque nuevo distinto a los anteriores.
        ultimoBloqueId++

        _listaEjercicios.value = _listaEjercicios.value.mapIndexed { index, draft ->
            if (index in seleccionados) {
                draft.copy(
                    // a todos los seleccionados les asigno el mismo bloque.
                    bloque = ultimoBloqueId,

                    // despues de agrupar, dejo de marcarlos.
                    seleccionado = false
                )
            } else {
                draft
            }
        }

        return true
    }

    // este metodo quita la agrupacion de los ejercicios seleccionados.
//
// aqui no solo pongo a 0 los seleccionados,
// sino que tambien reviso si algun bloque se queda "roto".
//
// ejemplos:
// - si una triserie pierde 1 ejercicio, los otros 2 siguen agrupados
// - si una biserie pierde 1 ejercicio, el que queda pasa a unico
// - si una triserie pierde 2 ejercicios, el que queda tambien pasa a unico
    fun desagruparSeleccionados(): Boolean {
        val listaActual = _listaEjercicios.value

        // primero compruebo si hay al menos un ejercicio marcado.
        // si no hay ninguno, no hago nada.
        val haySeleccion = listaActual.any { it.seleccionado }
        if (!haySeleccion) return false

        // aqui convierto en ejercicios unicos todos los seleccionados.
        // ademas les quito la marca de seleccion.
        val listaTrasDesagrupar = listaActual.map { draft ->
            if (draft.seleccionado) {
                draft.copy(
                    bloque = 0,
                    seleccionado = false
                )
            } else {
                draft
            }
        }

        // ahora agrupo los ejercicios que aun siguen teniendo bloque.
        // esto me sirve para ver cuantos quedan en cada grupo despues del cambio.
        val bloquesRestantes = listaTrasDesagrupar
            .filter { it.bloque != 0 }
            .groupBy { it.bloque }

        // si en un bloque queda menos de 2 ejercicios,
        // ese bloque ya no tiene sentido como agrupacion.
        // por eso guardo esos ids de bloque para romperlos tambien.
        val bloquesQueSeRompen = bloquesRestantes
            .filterValues { ejercicios -> ejercicios.size < 2 }
            .keys

        // finalmente recorro la lista y convierto a ejercicio unico
        // cualquier ejercicio que pertenezca a un bloque roto.
        _listaEjercicios.value = listaTrasDesagrupar.map { draft ->
            if (draft.bloque in bloquesQueSeRompen) {
                draft.copy(bloque = 0)
            } else {
                draft
            }
        }

        return true
    }

    // este metodo guarda una sesion nueva en el backend.
    fun guardarSesion(idUsuario: String, titulo: String) {
        viewModelScope.launch {
            try {
                // antes de empezar pongo la ui en loading.
                _uiState.value = SesionUiState.Loading

                // si no hay ejercicios, devuelvo error directamente.
                if (_listaEjercicios.value.isEmpty()) {
                    _uiState.value = SesionUiState.Error("Añade al menos un ejercicio")
                    return@launch
                }

                // aqui convierto los borradores de la ui al formato real que espera la api.
                val ejerciciosParaEnviar = _listaEjercicios.value.map { borrador ->
                    CrearEjercicioRequest(
                        nombre = borrador.nombre,

                        // convierto series de string a int.
                        // si falla la conversion, pongo 0.
                        series = borrador.series.toIntOrNull() ?: 0,

                        repeticiones = borrador.repeticiones,

                        // convierto peso de string a double.
                        // si falla la conversion, pongo 0.0.
                        peso = borrador.peso.toDoubleOrNull() ?: 0.0,

                        // este bloque es el que luego usa la api
                        // para guardar ejercicios sueltos, biseries o triseries.
                        bloque = borrador.bloque
                    )
                }

                // monto la peticion completa con id de usuario, titulo y ejercicios.
                val request = CrearSesionRequest(idUsuario, titulo, ejerciciosParaEnviar)

                // llamo al repositorio para guardar la sesion.
                if (repository.crearSesion(request)) {
                    _uiState.value = SesionUiState.Success
                } else {
                    _uiState.value = SesionUiState.Error("No se pudo guardar la sesión.")
                }
            } catch (e: Exception) {
                // si falla algo de red o backend, paso el mensaje al estado de error.
                _uiState.value = SesionUiState.Error("Error técnico: ${e.message}")
            }
        }
    }

    // este metodo prepara el duplicado de la ultima sesion de un usuario.
    // no navega ni cambia pantalla por si solo, solo devuelve el resultado por callback.
    fun prepararDuplicado(idUsuario: String, onResultado: (SesionEntrenamiento?) -> Unit) {
        viewModelScope.launch {
            try {
                val historial = repository.obtenerHistorialSesiones(idUsuario)

                if (historial.isNotEmpty()) {
                    // como el historial ya viene ordenado, cojo la primera que es la mas reciente.
                    val ultima = historial.first()
                    onResultado(ultima)
                } else {
                    // si no hay sesiones, devuelvo null.
                    onResultado(null)
                }
            } catch (e: Exception) {
                // si falla, tambien devuelvo null para que la pantalla lo gestione.
                println("Error al preparar duplicado: ${e.message}")
                onResultado(null)
            }
        }
    }

    // este metodo finaliza una sesion ya existente.
    // aqui reutilizo la lista actual del formulario como datos finales.
    fun finalizarEntrenamiento(idSesion: String) {
        viewModelScope.launch {
            try {
                _uiState.value = SesionUiState.Loading

                // convierto otra vez los borradores al formato que espera el backend.
                val ejerciciosFinales = _listaEjercicios.value.map { borrador ->
                    CrearEjercicioRequest(
                        nombre = borrador.nombre,
                        series = borrador.series.toIntOrNull() ?: 0,
                        repeticiones = borrador.repeticiones,
                        peso = borrador.peso.toDoubleOrNull() ?: 0.0,
                        bloque = borrador.bloque
                    )
                }

                // llamo al repositorio para enviar la finalizacion.
                if (repository.finalizarSesion(idSesion, ejerciciosFinales)) {
                    _uiState.value = SesionUiState.Success
                } else {
                    _uiState.value = SesionUiState.Error("No se pudo guardar el progreso.")
                }
            } catch (e: Exception) {
                _uiState.value = SesionUiState.Error("Error: ${e.message}")
            }
        }
    }

    // este metodo copia la ultima sesion del usuario directamente dentro del formulario.
    // es una forma rapida de reutilizar una rutina anterior.
    fun copiarUltimaSesion(idUsuario: String) {
        viewModelScope.launch {
            try {
                val ultima = repository.obtenerUltimaSesion(idUsuario)

                if (ultima != null) {
                    // al usar forzar = true, obligo a reemplazar lo que hubiera en el formulario.
                    inicializarConSesionBase(ultima, forzar = true)
                }
            } catch (e: Exception) {
                println("Error al copiar sesión: ${e.message}")
            }
        }
    }

    // este metodo resetea el viewmodel para dejar la pantalla limpia al salir.
    fun resetState() {
        _uiState.value = SesionUiState.Idle
        _listaEjercicios.value = emptyList()
        ultimoBloqueId = 0
    }
}