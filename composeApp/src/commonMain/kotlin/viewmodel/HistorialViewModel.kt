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

    fun limpiarHistorialParaRecargar() {
        _sesiones.value = emptyList() // Al estar vacía, el "if" del cargar permitirá la recarga
    }
    fun cargarHistorial(idUsuario: String) {
        //  Si ya tenemos datos, no hacemos la petición (ahorra batería y datos)
        if (_sesiones.value.isNotEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val lista = repository.obtenerHistorialSesiones(idUsuario)

                // LOG PARA DEPURAR: Abre el Logcat y busca "DEBUG_HISTORIAL"
                println("DEBUG_HISTORIAL: El servidor ha devuelto ${lista.size} sesiones")

                if (lista.isNotEmpty()) {
                    _sesiones.value = lista
                } else {
                    // Si llega vacío pero sospechamos que hay datos, el problema está en el Backend
                    println("DEBUG_HISTORIAL: ¡Ojo! El servidor devolvió una lista vacía.")
                }
            } catch (e: Exception) {
                println("DEBUG_HISTORIAL: Error de conexión -> ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}