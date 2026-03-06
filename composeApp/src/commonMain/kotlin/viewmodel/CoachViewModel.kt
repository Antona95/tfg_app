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

    // NUEVOS ESTADOS PARA CONTROLAR EL REGISTRO Y ERRORES DEL BACKEND
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
            val exito = repository.eliminarAlumno(nickname)
            if (exito) cargarAlumnos()
        }
    }

    // MODIFICADO: Ahora controlamos los estados de éxito/error
    fun crearNuevoAlumno(nickname: String, pass: String, nombre: String, apellidos: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorRegistro.value = null // Limpiamos errores previos
            _registroExitoso.value = false

            // Intentamos crear
            val resultado = repository.crearAlumno(nickname, pass, nombre, apellidos)

            // Si el repositorio devuelve un String, asumimos que es un error (ej: "El nickname ya existe")
            // Si devuelve un Boolean true, es que fue éxito. (Ajusta esto según cómo funcione tu repository)
            if (resultado == true) {
                _registroExitoso.value = true
                cargarAlumnos()
            } else {
                // Si tu repository.crearAlumno devuelve un boolean (false) cuando falla,
                // ponemos un mensaje genérico. Si tu repo te permite devolver el error del server, ponlo aquí.
                _errorRegistro.value = "Error al crear el alumno. Asegúrate de que el nickname no esté ya en uso."
            }
            _isLoading.value = false
        }
    }

    // Función para limpiar los estados cuando el usuario cierra el cuadro a mano
    fun resetRegistroState() {
        _errorRegistro.value = null
        _registroExitoso.value = false
    }

    private fun aplicarFiltro() {
        val texto = _textoBusqueda.value.lowercase()
        if (texto.isEmpty()) {
            _alumnosFiltrados.value = _todosLosAlumnos.value
        } else {
            _alumnosFiltrados.value = _todosLosAlumnos.value.filter {
                it.nickname.lowercase().contains(texto) ||
                        it.nombre.lowercase().contains(texto) ||
                        it.apellidos.lowercase().contains(texto)
            }
        }
    }
}