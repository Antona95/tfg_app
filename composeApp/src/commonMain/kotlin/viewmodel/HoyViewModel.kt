package viewmodel

package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import model.SesionEntrenamiento
import network.EntrenamientoRepository

sealed class HoyUiState {
    object Loading : HoyUiState()
    data class Success(val sesion: SesionEntrenamiento) : HoyUiState()
    object Empty : HoyUiState() // No hay entreno para hoy
    data class Error(val mensaje: String) : HoyUiState()
}

class HoyViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<HoyUiState>(HoyUiState.Loading)
    val uiState: StateFlow<HoyUiState> = _uiState

    fun cargarEntrenamiento(idUsuario: String) {
        viewModelScope.launch {
            _uiState.value = HoyUiState.Loading
            try {
                val sesion = repository.obtenerSesionHoy(idUsuario)
                if (sesion != null) {
                    _uiState.value = HoyUiState.Success(sesion)
                } else {
                    _uiState.value = HoyUiState.Empty
                }
            } catch (e: Exception) {
                _uiState.value = HoyUiState.Error("Error al conectar: ${e.message}")
            }
        }
    }
}