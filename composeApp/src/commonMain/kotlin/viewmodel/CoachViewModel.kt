package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Persona
import network.EntrenamientoRepository

class CoachViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    private val _todosLosAlumnos = MutableStateFlow<List<Persona>>(emptyList())
    private val _alumnosFiltrados = MutableStateFlow<List<Persona>>(emptyList())
    val alumnos = _alumnosFiltrados.asStateFlow()

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda = _textoBusqueda.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorRegistro = MutableStateFlow<String?>(null)
    val errorRegistro = _errorRegistro.asStateFlow()

    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso = _registroExitoso.asStateFlow()

    init {
        cargarAlumnos()
    }

    fun cargarAlumnos() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val lista = repository.obtenerTodosLosUsuarios()
                _todosLosAlumnos.value = lista
                aplicarFiltro()
            } catch (e: Exception) {
                println("Error cargando alumnos: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun buscar(nuevoTexto: String) {
        _textoBusqueda.value = nuevoTexto
        aplicarFiltro()
    }

    fun eliminarAlumno(nickname: String) {
        if (nickname.equals("MasterCoach", ignoreCase = true)) {
            println("Acción bloqueada: No puedes borrar al administrador")
            return
        }

        viewModelScope.launch {
            try {
                val exito = repository.eliminarAlumno(nickname)
                if (exito) {
                    cargarAlumnos()
                }
            } catch (e: Exception) {
                println("Error al eliminar alumno: ${e.message}")
            }
        }
    }

    fun crearNuevoAlumno(nickname: String, pass: String, nombre: String, apellidos: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorRegistro.value = null
            _registroExitoso.value = false

            try {
                val resultado = repository.crearAlumno(nickname, pass, nombre, apellidos)

                if (resultado) {
                    _registroExitoso.value = true
                    _isLoading.value = false
                    cargarAlumnos()
                } else {
                    _errorRegistro.value =
                        "Error al crear el alumno. Asegúrate de que el nickname no esté ya en uso."
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorRegistro.value = e.message ?: "Error de red al crear el alumno."
                _isLoading.value = false
            }
        }
    }

    fun resetRegistroState() {
        _errorRegistro.value = null
        _registroExitoso.value = false
    }

    private fun aplicarFiltro() {
        val texto = _textoBusqueda.value.lowercase()
        _alumnosFiltrados.value =
            if (texto.isEmpty()) {
                _todosLosAlumnos.value
            } else {
                _todosLosAlumnos.value.filter {
                    it.nickname.lowercase().contains(texto) ||
                            it.nombre.lowercase().contains(texto) ||
                            it.apellidos.lowercase().contains(texto)
                }
            }
    }
}