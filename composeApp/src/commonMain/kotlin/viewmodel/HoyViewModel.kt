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
            val estado = _uiState.value
            if (estado is HoyUiState.Success) {

                // 1. CONVERSIÓN: Mapeamos de 'DetalleSesion' a 'CrearEjercicioRequest'
                // Esto es lo que "limpia" el rojo, porque ahora los tipos coinciden con el Repo
                val ejerciciosParaEnviar = estado.sesion.ejercicios.map { detalle ->
                    model.CrearEjercicioRequest(
                        nombre = detalle.nombre ?: "",
                        series = detalle.series,
                        repeticiones = detalle.repeticiones,
                        peso = detalle.peso ?: 0.0,
                        bloque = detalle.bloque
                    )
                }

                // 2. Ahora llamamos al repo con la lista convertida 'ejerciciosParaEnviar'
                val exito = repository.finalizarSesion(idSesion, ejerciciosParaEnviar)

                if (exito) {
                    // Forzamos la recarga para ver el estado 'finalizada = true'
                    // Nota: He quitado el IF de cargarEntrenamiento para que fuerce la recarga
                    _uiState.value = HoyUiState.Loading // Opcional: mostrar carga un segundo
                    try {
                        val sesionNueva = repository.obtenerSesionHoy(idUsuario)
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
