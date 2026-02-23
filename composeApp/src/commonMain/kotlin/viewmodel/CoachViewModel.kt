package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Persona
import network.EntrenamientoRepository

class CoachViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    // 1. ESTADO: La lista de alumnos
    private val _alumnos = MutableStateFlow<List<Persona>>(emptyList())
    val alumnos = _alumnos.asStateFlow()

    // 2. ESTADO: ¿Estamos cargando?
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        cargarAlumnos()
    }

    fun cargarAlumnos() {
        // MEJORA 1: Si ya está cargando, evitamos lanzar otra petición repetida
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val lista = repository.obtenerTodosLosUsuarios()

                // MEJORA 2: Solo actualizamos la lista si el servidor devuelve datos.
                // Así, si hay un error de red temporal, no le borramos los alumnos de la pantalla.
                if (lista.isNotEmpty()) {
                    _alumnos.value = lista.filter { it.rol == "USUARIO" }
                }

            } catch (e: Exception) {
                println("Error cargando alumnos: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}