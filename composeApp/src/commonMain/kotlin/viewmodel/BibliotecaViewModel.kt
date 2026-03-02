package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Ejercicio
import network.EntrenamientoRepository

class BibliotecaViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    private val _ejercicios = MutableStateFlow<List<Ejercicio>>(emptyList())
    val ejercicios = _ejercicios.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        cargarEjercicios()
    }

    fun cargarEjercicios() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val lista = repository.obtenerEjerciciosBiblioteca()
                _ejercicios.value = lista.sortedBy { it.nombre }
            } catch (e: Exception) {
                _error.value = "Error al cargar la biblioteca"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun crearEjercicio(nombre: String, onExito: () -> Unit) {
        if (nombre.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val nuevo = repository.crearEjercicioBiblioteca(nombre)
                if (nuevo != null) {
                    cargarEjercicios() // Recargamos para ver el nuevo
                    onExito()
                } else {
                    _error.value = "No se pudo crear el ejercicio"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
