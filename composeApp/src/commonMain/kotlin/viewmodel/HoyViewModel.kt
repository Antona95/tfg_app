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
        // ESCUDO: Si ya tenemos éxito, no hacemos nada.
        // Esto evita que al volver atrás o cambiar modo oscuro se borren los datos.
        if (uiState.value is HoyUiState.Success) return

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

    // Cambia la cabecera de la función para añadir el parámetro 'onExito'
    fun finalizarEntrenamiento(idSesion: String, idUsuario: String, onExito: () -> Unit) {
        viewModelScope.launch {
            // 1. Mostramos estado de carga mientras procesamos
            _uiState.value = HoyUiState.Loading

            try {
                val exito = repository.finalizarSesion(idSesion)

                if (exito) {
                    // 2. En lugar de solo recargar, podemos forzar un pequeño delay
                    // o simplemente confiar en que el Repository ya limpió la caché.
                    val sesionActualizada = repository.obtenerSesionHoy(idUsuario)

                    if (sesionActualizada != null) {
                        _uiState.value = HoyUiState.Success(sesionActualizada)
                    } else {
                        // Si no hay sesión hoy tras finalizar, mostramos estado vacío
                        _uiState.value = HoyUiState.Empty
                    }

                    // 3. Notificamos a la UI (para mostrar un Toast o navegar)
                    onExito()
                } else {
                    _uiState.value = HoyUiState.Error("El servidor no pudo marcar la sesión como finalizada")
                }
            } catch (e: Exception) {
                println(" Error en finalizarEntrenamiento: ${e.message}")
                _uiState.value = HoyUiState.Error("Error de conexión al finalizar sesión")
            }
        }
    }
}
