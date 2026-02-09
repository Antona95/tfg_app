package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Persona
import network.EntrenamientoRepository

class CoachViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    // 1. ESTADO: La lista de alumnos (empieza vacía)
    private val _alumnos = MutableStateFlow<List<Persona>>(emptyList())
    val alumnos = _alumnos.asStateFlow() // Esto es lo que lee la pantalla

    // 2. ESTADO: ¿Estamos cargando? (Para mostrar la ruedecita)
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 3. AL NACER: Cargamos la lista automáticamente
    init {
        cargarAlumnos()
    }

    // 4. FUNCIÓN: Pedir datos al servidor
    fun cargarAlumnos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val lista = repository.obtenerTodosLosUsuarios()

                // CAMBIO AQUÍ: Filtramos para ver SOLO a los alumnos
                _alumnos.value = lista.filter { it.rol == "USUARIO" }

            } catch (e: Exception) {
                println("Error cargando alumnos: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}