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

    // NUEVO: Estado para guardar errores de red
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var ultimoUsuarioCargado: String? = null

    fun cargarHistorial(idUsuario: String, forzarRecarga: Boolean = false) {
        if (!forzarRecarga && ultimoUsuarioCargado == idUsuario && _sesiones.value.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null // Limpiamos errores viejos al reintentar
            try {
                val lista = repository.obtenerHistorialSesiones(idUsuario)
                _sesiones.value = lista
                ultimoUsuarioCargado = idUsuario
            } catch (e: Exception) {
                // MODIFICADO: En lugar del println, mandamos el error a la pantalla
                _error.value = e.message ?: "No se pudo cargar el historial."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarHistorial() {
        _sesiones.value = emptyList()
        ultimoUsuarioCargado = null
        _error.value = null
    }

    // (El método marcarComoFinalizada se queda igual, pero le añadimos try/catch)
    fun marcarComoFinalizada(idSesion: String, idUsuario: String) {
        viewModelScope.launch {
            val sesionEncontrada = _sesiones.value.find { it.idSesion == idSesion }
            if (sesionEncontrada != null) {
                val ejerciciosParaEnviar = sesionEncontrada.ejercicios.map { detalle ->
                    model.CrearEjercicioRequest(
                        nombre = detalle.nombre ?: "", series = detalle.series,
                        repeticiones = detalle.repeticiones, peso = detalle.peso ?: 0.0,
                        bloque = detalle.bloque
                    )
                }
                try {
                    val exito = repository.finalizarSesion(idSesion, ejerciciosParaEnviar)
                    if (exito) cargarHistorial(idUsuario, forzarRecarga = true)
                } catch (e: Exception) {
                    _error.value = e.message
                }
            }
        }
    }
}