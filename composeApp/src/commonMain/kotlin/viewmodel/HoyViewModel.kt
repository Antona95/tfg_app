package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.SesionEntrenamiento
import network.EntrenamientoRepository

// Estados de la UI para la pantalla del alumno
sealed class HoyUiState {
    object Loading : HoyUiState()
    data class Success(val sesion: SesionEntrenamiento) : HoyUiState()
    object Empty : HoyUiState()
    data class Error(val mensaje: String) : HoyUiState()
}

class HoyViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<HoyUiState>(HoyUiState.Loading)
    val uiState: StateFlow<HoyUiState> = _uiState.asStateFlow()

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
                _uiState.value = HoyUiState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun finalizarEntrenamiento(idSesion: String, idUsuario: String) {
        viewModelScope.launch {
            val exito = repository.finalizarSesion(idSesion)
            if (exito) {
                // Si el patch en el servidor fue correcto, recargamos los datos
                cargarEntrenamiento(idUsuario)
            }
        }
    }
}