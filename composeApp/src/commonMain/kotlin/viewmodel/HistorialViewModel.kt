package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.SesionEntrenamiento
import network.EntrenamientoRepository

class HistorialViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    private val _sesiones = MutableStateFlow<List<SesionEntrenamiento>>(emptyList())
    val sesiones = _sesiones.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Variable para evitar peticiones infinitas si falla
    private var ultimoUsuarioCargado: String? = null

    fun cargarHistorial(idUsuario: String, forzarRecarga: Boolean = false) {
        // Si NO forzamos y ya tenemos los datos del usuario actual, salimos
        if (!forzarRecarga && ultimoUsuarioCargado == idUsuario && _sesiones.value.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            // Solo mostramos el circulito de carga si la lista está vacía
            // Así, al recargar, el usuario sigue viendo los datos viejos hasta que lleguen los nuevos (evita parpadeos)
            if (_sesiones.value.isEmpty()) _isLoading.value = true

            try {
                val lista = repository.obtenerHistorialSesiones(idUsuario)
                _sesiones.value = lista
                ultimoUsuarioCargado = idUsuario
            } catch (e: Exception) {
                println("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}