package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    fun inicializarConSesionBase(sesionBase: SesionEntrenamiento?) {
        if (_listaEjercicios.value.isNotEmpty()) return

        _listaEjercicios.value = sesionBase?.ejercicios?.map { detalle ->
            EjercicioDraft(
                nombre = detalle.nombre ?: "",
                series = detalle.series.toString(),
                repeticiones = detalle.repeticiones,
                peso = detalle.peso?.toString() ?: "",
                bloque = detalle.bloque
            )
        } ?: listOf(EjercicioDraft(nombre = "", series = "3", repeticiones = "10", peso = "", bloque = 0))
    }

    fun agregarEjercicio() {
        _listaEjercicios.value = _listaEjercicios.value + EjercicioDraft(nombre = "", series = "3", repeticiones = "10", peso = "", bloque = 0)
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
                val ejerciciosParaEnviar = _listaEjercicios.value.map { borrador ->
                    CrearEjercicioRequest(
                        nombre = borrador.nombre,
                        series = borrador.series.toIntOrNull() ?: 0,
                        repeticiones = borrador.repeticiones,
                        peso = borrador.peso.toDoubleOrNull(),
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

    fun resetState() {
        _uiState.value = SesionUiState.Idle
        _listaEjercicios.value = emptyList()
        ultimoBloqueId = 0
    }
}

class SesionViewModelFactory(private val repository: EntrenamientoRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: kotlin.reflect.KClass<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
        return SesionViewModel(repository) as T
    }
}