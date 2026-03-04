package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.EntrenamientoRepository
import model.CrearSesionRequest
import model.CrearEjercicioRequest
import model.EjercicioDraft
import model.SesionEntrenamiento

sealed class SesionUiState {
    object Idle : SesionUiState()
    object Loading : SesionUiState()
    object Success : SesionUiState()
    data class Error(val mensaje: String) : SesionUiState()
}

class SesionViewModel(
    private val repository: EntrenamientoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SesionUiState>(SesionUiState.Idle)
    val uiState: StateFlow<SesionUiState> = _uiState

    private val _listaEjercicios = MutableStateFlow<List<EjercicioDraft>>(emptyList())
    val listaEjercicios: StateFlow<List<EjercicioDraft>> = _listaEjercicios

    private var ultimoBloqueId = 0

    fun inicializarConSesionBase(sesionBase: SesionEntrenamiento?, forzar: Boolean = false) {
        if (!forzar && _listaEjercicios.value.isNotEmpty()) return

        _listaEjercicios.value = sesionBase?.ejercicios?.map { detalle ->
            EjercicioDraft(
                nombre = detalle.nombre ?: "",
                series = detalle.series.toString(),
                repeticiones = detalle.repeticiones,
                peso = detalle.peso?.toString() ?: "0.0",
                bloque = detalle.bloque
            )
        } ?: listOf(EjercicioDraft(nombre = "", series = "3", repeticiones = "10", peso = "0.0", bloque = 0))
    }

    fun agregarEjercicio() {
        _listaEjercicios.value = _listaEjercicios.value + EjercicioDraft(nombre = "", series = "3", repeticiones = "10", peso = "0.0", bloque = 0)
    }

    fun eliminarEjercicio(index: Int) {
        val listaMutable = _listaEjercicios.value.toMutableList()
        if (index in listaMutable.indices) {
            listaMutable.removeAt(index)
            _listaEjercicios.value = listaMutable
        }
    }

    fun actualizarEjercicio(index: Int, nuevo: EjercicioDraft) {
        val listaMutable = _listaEjercicios.value.toMutableList()
        if (index in listaMutable.indices) {
            listaMutable[index] = nuevo
            _listaEjercicios.value = listaMutable
        }
    }

    fun agruparUltimos(cantidad: Int) {
        if (_listaEjercicios.value.size < cantidad) return
        ultimoBloqueId++
        _listaEjercicios.value = _listaEjercicios.value.mapIndexed { index, draft ->
            if (index >= _listaEjercicios.value.size - cantidad) {
                draft.copy(bloque = ultimoBloqueId)
            } else {
                draft
            }
        }
    }

    fun guardarSesion(idUsuario: String, titulo: String, fecha: String) {
        viewModelScope.launch {
            try {
                _uiState.value = SesionUiState.Loading
                
                if (_listaEjercicios.value.isEmpty()) {
                    _uiState.value = SesionUiState.Error("Añade al menos un ejercicio")
                    return@launch
                }

                val ejerciciosParaEnviar = _listaEjercicios.value.map { borrador ->
                    CrearEjercicioRequest(
                        nombre = borrador.nombre,
                        series = borrador.series.toIntOrNull() ?: 0,
                        repeticiones = borrador.repeticiones,
                        peso = borrador.peso.toDoubleOrNull() ?: 0.0,
                        bloque = borrador.bloque,
                    )
                }

                val request = CrearSesionRequest(idUsuario, titulo, fecha, ejerciciosParaEnviar)
                if (repository.crearSesion(request)) {
                    _uiState.value = SesionUiState.Success
                } else {
                    _uiState.value = SesionUiState.Error("No se pudo guardar la sesión.")
                }
            } catch (e: Exception) {
                _uiState.value = SesionUiState.Error("Error técnico: ${e.message}")
            }
        }
    }

    /**
     * Lógica delegada desde App.kt para obtener la última sesión y prepararla para duplicar.
     */
    fun prepararDuplicado(idUsuario: String, onExito: (SesionEntrenamiento) -> Unit) {
        viewModelScope.launch {
            try {
                val historial = repository.obtenerHistorialSesiones(idUsuario)
                if (historial.isNotEmpty()) {
                    val ultima = historial.maxByOrNull { it.fechaProgramada } ?: historial.last()
                    onExito(ultima)
                }
            } catch (e: Exception) {
                println("Error al preparar duplicado: ${e.message}")
            }
        }
    }

    fun finalizarEntrenamiento(idSesion: String) {
        viewModelScope.launch {
            try {
                _uiState.value = SesionUiState.Loading

                val ejerciciosFinales = _listaEjercicios.value.map { borrador ->
                    CrearEjercicioRequest(
                        nombre = borrador.nombre,
                        series = borrador.series.toIntOrNull() ?: 0,
                        repeticiones = borrador.repeticiones,
                        peso = borrador.peso.toDoubleOrNull() ?: 0.0,
                        bloque = borrador.bloque
                    )
                }

                if (repository.finalizarSesion(idSesion, ejerciciosFinales)) {
                    _uiState.value = SesionUiState.Success
                } else {
                    _uiState.value = SesionUiState.Error("No se pudo guardar el progreso.")
                }
            } catch (e: Exception) {
                _uiState.value = SesionUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun copiarUltimaSesion(idUsuario: String) {
        viewModelScope.launch {
            try {
                val ultima = repository.obtenerUltimaSesion(idUsuario)
                if (ultima != null) {
                    inicializarConSesionBase(ultima, forzar = true)
                }
            } catch (e: Exception) {
                println("Error al copiar sesión: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = SesionUiState.Idle
        _listaEjercicios.value = emptyList()
        ultimoBloqueId = 0
    }
}
