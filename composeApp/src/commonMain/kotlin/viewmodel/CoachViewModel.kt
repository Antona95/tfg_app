package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Persona
import network.EntrenamientoRepository

class CoachViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    // Lista original sin filtrar
    private val _todosLosAlumnos = MutableStateFlow<List<Persona>>(emptyList())
    
    // Lista filtrada que se muestra en la UI
    private val _alumnosFiltrados = MutableStateFlow<List<Persona>>(emptyList())
    val alumnos = _alumnosFiltrados.asStateFlow()

    // Texto de búsqueda
    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda = _textoBusqueda.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        cargarAlumnos()
    }

    fun cargarAlumnos() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val lista = repository.obtenerTodosLosUsuarios()
                if (lista.isNotEmpty()) {
                    val soloAlumnos = lista.filter { it.rol == "USUARIO" && it.nickname != "MasterCoach" }
                    _todosLosAlumnos.value = soloAlumnos
                    aplicarFiltro()
                }
            } catch (e: Exception) {
                println("Error cargando alumnos: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Funcion para actualizar el texto y filtrar
    fun buscar(nuevoTexto: String) {
        _textoBusqueda.value = nuevoTexto
        aplicarFiltro()
    }
    fun eliminarAlumno(nickname: String) {
        // SEGURIDAD: No permitimos que el Coach se borre a sí mismo
        if (nickname.equals("MasterCoach", ignoreCase = true)) {
            println("Acción bloqueada: No puedes borrar al administrador")
            return
        }

        viewModelScope.launch {
            val exito = repository.eliminarAlumno(nickname)
            if (exito) {
                cargarAlumnos()
            }
        }
    }

    fun crearNuevoAlumno(nickname: String, pass: String, nombre: String, apellidos: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val exito = repository.crearAlumno(nickname, pass, nombre, apellidos)
            if (exito) {
                // Volvemos a pedir todos los alumnos al servidor
                cargarAlumnos()
            } else {
                println("El servidor rechazó la creación del alumno")
            }
            _isLoading.value = false
        }
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
