package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Persona
import repository.UsuarioRepository

// este viewmodel controla toda la logica de la pantalla del entrenador.
// aqui gestiono la lista de alumnos, la busqueda, la creacion y el borrado.
class CoachViewModel(private val repository: UsuarioRepository) : ViewModel() {

    // aqui guardo la lista completa de alumnos tal y como llega del repositorio.
    // esta es la fuente original sobre la que luego aplico filtros.
    private val _todosLosAlumnos = MutableStateFlow<List<Persona>>(emptyList())

    // aqui guardo la lista ya filtrada segun lo que escriba el entrenador en el buscador.
    private val _alumnosFiltrados = MutableStateFlow<List<Persona>>(emptyList())

    // este estado es el que expongo a la ui.
    // la pantalla del coach observa esta lista y la pinta.
    val alumnos = _alumnosFiltrados.asStateFlow()

    // aqui guardo el texto actual del buscador.
    private val _textoBusqueda = MutableStateFlow("")

    // expongo el texto de busqueda para que la ui lo observe y lo muestre.
    val textoBusqueda = _textoBusqueda.asStateFlow()

    // este booleano me dice si hay alguna operacion en curso.
    // lo uso para mostrar carga o bloquear botones.
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // aqui guardo un posible mensaje de error al registrar alumno.
    private val _errorRegistro = MutableStateFlow<String?>(null)
    val errorRegistro = _errorRegistro.asStateFlow()

    // este estado indica si el alta del alumno se ha completado bien.
    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso = _registroExitoso.asStateFlow()

    // en cuanto se crea el viewmodel, cargo los alumnos automaticamente.
    init {
        cargarAlumnos()
    }

    // este metodo pide al repositorio la lista de alumnos.
    fun cargarAlumnos() {

        // si ya estoy cargando, corto para evitar llamadas duplicadas.
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // pido la lista completa al repositorio.
                val lista = repository.obtenerTodosLosUsuarios()

                // actualizo la lista original.
                _todosLosAlumnos.value = lista

                // reaplico el filtro por si habia texto escrito antes.
                aplicarFiltro()
            } catch (e: Exception) {

                // de momento solo saco el error por consola.
                println("Error cargando alumnos: ${e.message}")
            } finally {

                // termine bien o mal, quito el estado de carga.
                _isLoading.value = false
            }
        }
    }

    // este metodo se llama cada vez que cambia el texto del buscador.
    fun buscar(nuevoTexto: String) {

        // guardo el nuevo texto.
        _textoBusqueda.value = nuevoTexto

        // vuelvo a filtrar la lista.
        aplicarFiltro()
    }

    // este metodo elimina un alumno por nickname.
    fun eliminarAlumno(nickname: String) {

        // aqui pongo una proteccion para no borrar al administrador principal.
        if (nickname.equals("MasterCoach", ignoreCase = true)) {
            println("Acción bloqueada: No puedes borrar al administrador")
            return
        }

        viewModelScope.launch {
            try {
                // llamo al repositorio para borrar el alumno.
                val exito = repository.eliminarAlumno(nickname)

                // si ha ido bien, recargo la lista para reflejar el cambio en pantalla.
                if (exito) {
                    cargarAlumnos()
                }
            } catch (e: Exception) {

                // si falla, por ahora solo lo muestro en consola.
                println("Error al eliminar alumno: ${e.message}")
            }
        }
    }

    // este metodo crea un nuevo alumno desde el formulario del coach.
    fun crearNuevoAlumno(nickname: String, pass: String, nombre: String, apellidos: String) {
        viewModelScope.launch {

            // activo la carga y limpio estados anteriores.
            _isLoading.value = true
            _errorRegistro.value = null
            _registroExitoso.value = false

            try {
                // pido al repositorio que cree el alumno.
                val resultado = repository.crearAlumno(nickname, pass, nombre, apellidos)

                if (resultado) {

                    // si ha salido bien, marco el registro como exitoso.
                    _registroExitoso.value = true

                    // quito la carga.
                    _isLoading.value = false

                    // recargo la lista para que aparezca el nuevo alumno.
                    cargarAlumnos()
                } else {

                    // si el repo devuelve false, muestro un mensaje de error generico.
                    _errorRegistro.value =
                        "Error al crear el alumno. Asegúrate de que el nickname no esté ya en uso."
                    _isLoading.value = false
                }
            } catch (e: Exception) {

                // si hay una excepcion de red o backend, guardo el mensaje para enseñarlo en la ui.
                _errorRegistro.value = e.message ?: "Error de red al crear el alumno."
                _isLoading.value = false
            }
        }
    }

    // este metodo limpia los estados relacionados con el dialogo de registro.
    fun resetRegistroState() {
        _errorRegistro.value = null
        _registroExitoso.value = false
    }

    // este metodo aplica el filtro sobre la lista original de alumnos.
    private fun aplicarFiltro() {

        // convierto el texto a minusculas para comparar sin importar mayusculas o minusculas.
        val texto = _textoBusqueda.value.lowercase()

        _alumnosFiltrados.value =
            if (texto.isEmpty()) {

                // si no hay texto escrito, enseño todos los alumnos.
                _todosLosAlumnos.value
            } else {

                // si si hay texto, filtro por nickname, nombre o apellidos.
                _todosLosAlumnos.value.filter {
                    it.nickname.lowercase().contains(texto) ||
                            it.nombre.lowercase().contains(texto) ||
                            it.apellidos.lowercase().contains(texto)
                }
            }
    }
}