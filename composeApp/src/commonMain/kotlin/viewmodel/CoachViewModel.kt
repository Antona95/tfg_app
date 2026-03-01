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
                    val soloAlumnos = lista.filter { it.rol == "USUARIO" }
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
