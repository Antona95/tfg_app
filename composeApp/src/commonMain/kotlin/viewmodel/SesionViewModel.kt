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

    // --- NUEVO: Contador para generar IDs de bloque únicos en esta sesión ---
    private var ultimoBloqueId = 0

    /**
     * Envía la sesión al servidor mapeando los borradores a la petición final.
     */
    fun guardarSesion(
        idUsuario: String,
        titulo: String,
        fecha: String,
        listaDrafts: List<EjercicioDraft>
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = SesionUiState.Loading

                val ejerciciosParaEnviar = listaDrafts.map { borrador ->
                    CrearEjercicioRequest(
                        nombre = borrador.nombre,
                        series = borrador.series.toIntOrNull() ?: 0,
                        repeticiones = borrador.repeticiones,
                        peso = borrador.peso.toDoubleOrNull(),
                        bloque = borrador.bloque,
                        observaciones = null
                    )
                }

                val request = CrearSesionRequest(
                    idUsuario = idUsuario,
                    titulo = titulo,
                    fechaProgramada = fecha,
                    ejercicios = ejerciciosParaEnviar
                )

                val exito = repository.crearSesion(request)

                if (exito) {
                    _uiState.value = SesionUiState.Success
                    ultimoBloqueId = 0 // Reiniciamos contador tras éxito
                } else {
                    _uiState.value = SesionUiState.Error("No se pudo guardar la sesión.")
                }

            } catch (e: Exception) {
                _uiState.value = SesionUiState.Error("Error técnico: ${e.message}")
            }
        }
    }

    // --- NUEVA FUNCIÓN: Lógica para agrupar ejercicios en biseries/circuitos ---
    /**
     * Esta función devuelve una NUEVA lista de borradores con los últimos N ejercicios
     * marcados con un mismo ID de bloque.
     * Uso: listaEjercicios = viewModel.agruparUltimosEnDrafts(listaEjercicios, 2)
     */
    fun agruparUltimosEnDrafts(
        listaActual: List<EjercicioDraft>,
        cantidad: Int
    ): List<EjercicioDraft> {
        if (listaActual.size < cantidad) return listaActual

        ultimoBloqueId++ // Cada vez que agrupamos, usamos un ID nuevo para el color

        return listaActual.mapIndexed { index, draft ->
            // Si el ejercicio está dentro de los últimos 'N' de la lista
            if (index >= listaActual.size - cantidad) {
                draft.copy(bloque = ultimoBloqueId)
            } else {
                draft
            }
        }
    }

    fun resetState() {
        _uiState.value = SesionUiState.Idle
        ultimoBloqueId = 0
    }
}

class SesionViewModelFactory(private val repository: EntrenamientoRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: kotlin.reflect.KClass<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
        return SesionViewModel(repository) as T
    }
}
