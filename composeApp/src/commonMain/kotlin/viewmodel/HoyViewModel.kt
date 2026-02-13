package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.SesionEntrenamiento
import network.EntrenamientoRepository

// 1. ESTADOS DE LA PANTALLA DE HOY
sealed class HoyUiState {
    object Loading : HoyUiState()
    data class Success(val sesion: SesionEntrenamiento) : HoyUiState()
    object Empty : HoyUiState()
    data class Error(val mensaje: String) : HoyUiState()
}

class HoyViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    // Usamos asStateFlow para que la UI no pueda modificar el estado directamente
    private val _uiState = MutableStateFlow<HoyUiState>(HoyUiState.Loading)
    val uiState: StateFlow<HoyUiState> = _uiState.asStateFlow()

    /**
     * Solicita al repositorio la sesión programada para la fecha actual.
     */
    fun cargarEntrenamiento(idUsuario: String) {
        viewModelScope.launch {
            _uiState.value = HoyUiState.Loading
            try {
                // Llamada al repositorio que conecta con Node.js
                val sesion = repository.obtenerSesionHoy(idUsuario)

                if (sesion != null) {
                    _uiState.value = HoyUiState.Success(sesion)
                } else {
                    _uiState.value = HoyUiState.Empty
                }
            } catch (e: Exception) {
                _uiState.value = HoyUiState.Error("No se pudo conectar con el servidor")
                e.printStackTrace()
            }
        }
    }
    // Añade esto dentro de la clase HoyViewModel
    fun finalizarEntrenamiento(idSesion: String, idUsuario: String) {
        viewModelScope.launch {
            val exito = repository.finalizarSesion(idSesion)
            if (exito) {
                // Recargamos los datos para que la UI sepa que ya está finalizada
                cargarEntrenamiento(idUsuario)
            } else {
                _uiState.value = HoyUiState.Error("No se pudo finalizar la sesión")
            }
        }
    }
}