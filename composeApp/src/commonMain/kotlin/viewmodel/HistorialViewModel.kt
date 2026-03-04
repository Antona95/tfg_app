package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.SesionEntrenamiento
import network.EntrenamientoRepository

class HistorialViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    private val _sesiones = MutableStateFlow<List<SesionEntrenamiento>>(emptyList())
    val sesiones = _sesiones.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Variable para evitar peticiones infinitas si falla
    private var ultimoUsuarioCargado: String? = null

    fun cargarHistorial(idUsuario: String, forzarRecarga: Boolean = false) {
        // Si forzarRecarga es true, saltamos el 'return' y vamos al servidor
        if (!forzarRecarga && ultimoUsuarioCargado == idUsuario && _sesiones.value.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true // Mostramos carga para confirmar que algo pasa
            try {
                // Esto pide los datos frescos de MongoDB
                val lista = repository.obtenerHistorialSesiones(idUsuario)
                _sesiones.value = lista
                ultimoUsuarioCargado = idUsuario
                println("HISTORIAL CARGADO: Se han encontrado ${lista.size} sesiones")
            } catch (e: Exception) {
                println("Error en historial: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpia el historial actual para asegurar que al cambiar de alumno se carguen datos frescos.
     */
    fun limpiarHistorial() {
        _sesiones.value = emptyList()
        ultimoUsuarioCargado = null
    }

    fun marcarComoFinalizada(idSesion: String, idUsuario: String) {
        viewModelScope.launch {
            // 1. Buscamos la sesión en tu lista de _sesiones
            val sesionEncontrada = _sesiones.value.find { it.idSesion == idSesion }

            if (sesionEncontrada != null) {
                // 2. CONVERSIÓN MANUAL: Creamos la lista que el Repo espera
                // Pasamos de 'DetalleSesion' a 'CrearEjercicioRequest'
                val ejerciciosParaEnviar = sesionEncontrada.ejercicios.map { detalle ->
                    model.CrearEjercicioRequest(
                        nombre = detalle.nombre ?: "",
                        series = detalle.series,
                        repeticiones = detalle.repeticiones,
                        peso = detalle.peso ?: 0.0,
                        bloque = detalle.bloque
                    )
                }

                // 3. Ahora el Repo NO se quejará porque le pasas lo que pide
                val exito = repository.finalizarSesion(idSesion, ejerciciosParaEnviar)

                if (exito) {
                    cargarHistorial(idUsuario, forzarRecarga = true)
                }
            }
        }
    }
}