package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.SesionEntrenamiento
import repository.SessionRepository

// este viewmodel se encarga de toda la logica del historial.
// aqui gestiono la carga de sesiones, el estado de carga, los errores y
// algunas acciones como marcar una sesion como finalizada.
class HistoryViewModel(private val repository: SessionRepository) : ViewModel() {

    // aqui guardo la lista de sesiones del historial.
    // uso mutablestateflow porque quiero que la ui reaccione automaticamente a los cambios.
    private val _sesiones = MutableStateFlow<List<SesionEntrenamiento>>(emptyList())

    // expongo la lista como asstateflow para que la ui la pueda leer pero no modificar.
    val sesiones = _sesiones.asStateFlow()

    // este booleano me dice si estoy cargando el historial.
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // aqui guardo un posible error de red o del backend.
    // lo hago asi para poder pintarlo directamente en la interfaz.
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // este campo me sirve para recordar de que usuario he cargado el historial por ultima vez.
    // asi evito recargar datos innecesariamente.
    private var ultimoUsuarioCargado: String? = null

    // este metodo carga el historial de un usuario.
    fun cargarHistorial(idUsuario: String, forzarRecarga: Boolean = false) {

        // si no me obligan a recargar, y ya tengo cargado ese mismo usuario con sesiones,
        // no vuelvo a hacer la llamada.
        if (!forzarRecarga && ultimoUsuarioCargado == idUsuario && _sesiones.value.isNotEmpty()) {
            return
        }

        viewModelScope.launch {

            // antes de empezar la peticion, activo el loading y limpio errores anteriores.
            _isLoading.value = true
            _error.value = null

            try {
                // pido al repositorio la lista de sesiones del usuario.
                val lista = repository.obtenerHistorialSesiones(idUsuario)

                // guardo la lista para que la ui la muestre.
                _sesiones.value = lista

                // actualizo el ultimo usuario cargado.
                ultimoUsuarioCargado = idUsuario
            } catch (e: Exception) {

                // si falla, guardo el mensaje del error para enseñarlo en la pantalla.
                _error.value = e.message ?: "No se pudo cargar el historial."
            } finally {

                // termine bien o mal, quito el estado de carga.
                _isLoading.value = false
            }
        }
    }

    // este metodo limpia completamente el historial guardado en memoria.
    // me viene bien cuando cambio de alumno o quiero evitar mezclar datos.
    fun limpiarHistorial() {
        _sesiones.value = emptyList()
        ultimoUsuarioCargado = null
        _error.value = null
    }

    // este metodo marca una sesion concreta como finalizada.
    // en realidad no solo cambia un booleano, sino que tambien manda los ejercicios finales.
    fun marcarComoFinalizada(idSesion: String, idUsuario: String) {
        viewModelScope.launch {

            // primero busco dentro de la lista la sesion que coincide con ese id.
            val sesionEncontrada = _sesiones.value.find { it.idSesion == idSesion }

            if (sesionEncontrada != null) {

                // aqui preparo los ejercicios en el formato que espera el backend.
                // convierto cada detallesesion en un crearejerciciorequest.
                val ejerciciosParaEnviar = sesionEncontrada.ejercicios.map { detalle ->
                    model.CrearEjercicioRequest(
                        nombre = detalle.nombre ?: "",
                        series = detalle.series,
                        repeticiones = detalle.repeticiones,
                        peso = detalle.peso ?: 0.0,
                        bloque = detalle.bloque
                    )
                }

                try {
                    // llamo al repositorio para finalizar la sesion en el backend.
                    val exito = repository.finalizarSesion(idSesion, ejerciciosParaEnviar)

                    if (exito) {

                        // si sale bien, recargo el historial para que la ui vea el cambio real.
                        cargarHistorial(idUsuario, forzarRecarga = true)
                    } else {

                        // si el backend responde pero no lo da por bueno, guardo un error.
                        _error.value = "No se pudo finalizar la sesión."
                    }
                } catch (e: Exception) {

                    // si hay un fallo de red o del servidor, tambien lo guardo.
                    _error.value = e.message
                }
            }
        }
    }
}