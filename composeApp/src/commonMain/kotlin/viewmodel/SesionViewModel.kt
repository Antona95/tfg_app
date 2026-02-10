package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.EntrenamientoRepository
import network.CrearSesionRequest
import network.CrearEjercicioRequest
import model.EjercicioDraft // Asegúrate de importar tu clase de borrador

// 1. DEFINIMOS LOS ESTADOS DE LA PANTALLA
// Esto nos ayuda a saber si mostrar el formulario, un spinner de carga o un mensaje de éxito.
sealed class SesionUiState {
    object Idle : SesionUiState()      // Estado inicial (esperando)
    object Loading : SesionUiState()   // Cargando (enviando datos)
    object Success : SesionUiState()   // ¡Guardado con éxito!
    data class Error(val mensaje: String) : SesionUiState() // Hubo un problema
}

class SesionViewModel(
    private val repository: EntrenamientoRepository
) : ViewModel() {

    // Estado observable para la UI (Compose)
    private val _uiState = MutableStateFlow<SesionUiState>(SesionUiState.Idle)
    val uiState: StateFlow<SesionUiState> = _uiState

    /**
     * Función principal que llama el botón "Guardar".
     * Recibe los datos crudos de la UI y los prepara para el servidor.
     */
    fun guardarSesion(
        idUsuario: String,
        titulo: String,
        fecha: String,
        listaDrafts: List<EjercicioDraft>
    ) {
        // Lanzamos una corrutina para no bloquear la pantalla principal
        viewModelScope.launch {
            try {
                // 1. Ponemos estado de carga
                _uiState.value = SesionUiState.Loading

                // 2. MAPEO DE DATOS (Transformación)
                // Convertimos la lista de "Borradores" (UI) a "Peticiones" (API)
                val ejerciciosParaEnviar = listaDrafts.map { borrador ->
                    CrearEjercicioRequest(
                        nombreEjercicio = borrador.nombre,

                        // Convertimos String -> Int. Si falla o es vacío, ponemos 0.
                        seriesObjetivo = borrador.series.toIntOrNull() ?: 0,

                        // Repeticiones se queda como String ("10-12")
                        repeticionesObjetivo = borrador.repeticiones,

                        // Convertimos String -> Double. Si falla, es null.
                        pesoObjetivo = borrador.peso.toDoubleOrNull(),

                        notas = null // O borrador.notas si lo tienes en el draft
                    )
                }

                // 3. Creamos el objeto final
                val request = CrearSesionRequest(
                    idUsuario = idUsuario,
                    titulo = titulo,
                    fechaProgramada = fecha,
                    ejercicios = ejerciciosParaEnviar
                )

                // 4. Llamamos al repositorio
                val exito = repository.crearSesion(request)

                // 5. Actualizamos el estado según el resultado
                if (exito) {
                    _uiState.value = SesionUiState.Success
                } else {
                    _uiState.value = SesionUiState.Error("No se pudo guardar la sesión. Revisa la conexión.")
                }

            } catch (e: Exception) {
                // Capturamos errores inesperados
                _uiState.value = SesionUiState.Error("Error técnico: ${e.message}")
            }
        }
    }

    // Función para reiniciar el estado (útil si quieres crear otra sesión seguida)
    fun resetState() {
        _uiState.value = SesionUiState.Idle
    }
}

// --- FACTORY (Necesario si no usas Hilt/Koin) ---
// Copia esto también al final del archivo. Sirve para poder pasarle el repositorio al ViewModel.
class SesionViewModelFactory(private val repository: EntrenamientoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SesionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SesionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}