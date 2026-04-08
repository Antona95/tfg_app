package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.SesionEntrenamiento
import repository.SesionRepository

// esta sealed class representa todos los estados posibles de la pantalla "hoy".
// me viene muy bien porque asi la ui no depende de booleanos sueltos,
// sino de un estado claro y cerrado.
sealed class HoyUiState {

    // estado inicial o de carga mientras pido datos al backend.
    object Loading : HoyUiState()

    // estado correcto cuando si tengo una sesion para mostrar.
    // aqui guardo la propia sesion.
    data class Success(val sesion: SesionEntrenamiento) : HoyUiState()

    // estado para cuando no hay ninguna sesion disponible.
    object Empty : HoyUiState()

    // estado de error cuando falla la red o el servidor.
    data class Error(val mensaje: String) : HoyUiState()
}

// este viewmodel controla la logica de la pantalla del entrenamiento del alumno.
class HoyViewModel(private val repository: SesionRepository) : ViewModel() {

    // aqui guardo el estado actual de la pantalla.
    // empiezo en loading porque normalmente al abrir la pantalla voy a pedir datos.
    private val _uiState = MutableStateFlow<HoyUiState>(HoyUiState.Loading)

    // expongo el estado como stateflow de solo lectura para que la ui lo observe.
    val uiState: StateFlow<HoyUiState> = _uiState.asStateFlow()

    // este metodo carga el entrenamiento del usuario.
    fun cargarEntrenamiento(idUsuario: String) {

        // si ya estoy en success, no vuelvo a recargar.
        // esto evita llamadas repetidas innecesarias si la pantalla recompone.
        if (uiState.value is HoyUiState.Success) return

        viewModelScope.launch {

            // antes de empezar, pongo la pantalla en loading.
            _uiState.value = HoyUiState.Loading

            try {
                // pido al repositorio la ultima sesion util del usuario.
                val sesion = repository.obtenerUltimaSesion(idUsuario)

                if (sesion != null) {

                    // si existe una sesion, paso al estado success con sus datos.
                    _uiState.value = HoyUiState.Success(sesion)
                } else {

                    // si no hay sesion, paso a empty para que la ui muestre "hoy toca descanso".
                    _uiState.value = HoyUiState.Empty
                }
            } catch (e: Exception) {

                // si hay fallo de red o de backend, paso al estado error con mensaje.
                _uiState.value = HoyUiState.Error(e.message ?: "Error de red al conectar")
            }
        }
    }

    // este metodo finaliza una sesion ya cargada en pantalla.
    fun finalizarEntrenamiento(idSesion: String, idUsuario: String, onExito: () -> Unit) {
        viewModelScope.launch {

            // primero leo el estado actual.
            val estado = _uiState.value

            // solo puedo finalizar si ahora mismo tengo una sesion cargada correctamente.
            if (estado is HoyUiState.Success) {

                // convierto los ejercicios de la sesion al formato que espera el backend.
                val ejerciciosParaEnviar = estado.sesion.ejercicios.map { detalle ->
                    model.CrearEjercicioRequest(
                        nombre = detalle.nombre ?: "",
                        series = detalle.series,
                        repeticiones = detalle.repeticiones,
                        peso = detalle.peso ?: 0.0,
                        bloque = detalle.bloque
                    )
                }

                try {
                    // llamo al repositorio para marcar la sesion como finalizada.
                    val exito = repository.finalizarSesion(idSesion, ejerciciosParaEnviar)

                    if (exito) {

                        // si el backend responde bien, actualizo la sesion localmente
                        // para que la ui siga mostrando la misma sesion pero ya como finalizada.
                        // esto lo hago asi para no perder la pantalla y no mostrar "hoy toca descanso".
                        val sesionFinalizada = estado.sesion.copy(finalizada = true)
                        _uiState.value = HoyUiState.Success(sesionFinalizada)

                        // llamo al callback por si la pantalla quiere reaccionar.
                        onExito()
                    } else {

                        // si la peticion no ha salido bien, paso a estado error.
                        _uiState.value = HoyUiState.Error("No se pudo finalizar la sesión")
                    }
                } catch (e: Exception) {

                    // si hay fallo de red o del servidor, tambien paso a error.
                    _uiState.value = HoyUiState.Error(e.message ?: "Fallo al finalizar la sesión")
                }
            }
        }
    }
}