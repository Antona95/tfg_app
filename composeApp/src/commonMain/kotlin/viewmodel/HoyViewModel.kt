package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.SesionEntrenamiento
import network.EntrenamientoRepository

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
        if (uiState.value is HoyUiState.Success) return

        viewModelScope.launch {
            _uiState.value = HoyUiState.Loading
            try {
                // CAMBIO CLAVE: En vez de obtenerSesionHoy, pedimos la última sesión creada
                val sesion = repository.obtenerUltimaSesion(idUsuario)

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

    fun finalizarEntrenamiento(idSesion: String, idUsuario: String, onExito: () -> Unit) {
        viewModelScope.launch {
            val estado = _uiState.value
            if (estado is HoyUiState.Success) {

                val ejerciciosParaEnviar = estado.sesion.ejercicios.map { detalle ->
                    model.CrearEjercicioRequest(
                        nombre = detalle.nombre ?: "",
                        series = detalle.series,
                        repeticiones = detalle.repeticiones,
                        peso = detalle.peso ?: 0.0,
                        bloque = detalle.bloque
                    )
                }

                val exito = repository.finalizarSesion(idSesion, ejerciciosParaEnviar)

                if (exito) {
                    _uiState.value = HoyUiState.Loading
                    try {
                       //al refrescar, volvemos a pedir la última sesión
                        val sesionNueva = repository.obtenerUltimaSesion(idUsuario)
                        if (sesionNueva != null) {
                            _uiState.value = HoyUiState.Success(sesionNueva)
                        } else {
                            _uiState.value = HoyUiState.Empty
                        }
                    } catch (e: Exception) {
                        _uiState.value = HoyUiState.Error("Error al refrescar")
                    }
                    onExito()
                }
            }
        }
    }
}