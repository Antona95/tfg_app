package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Persona
import network.EntrenamientoRepository
import network.createHttpClient

// Esto define los 3 estados posibles de tu pantalla
data class LoginUiState(
    val isLoading: Boolean = false,      // ¿Cargando?
    val error: String? = null,           // ¿Hubo error?
    val mensajeExito: String? = null,    // ¿Hubo éxito?
    val usuarioLogueado: Persona? = null // ¿Éxito?
)

class LoginViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    // Estado interno (modificable)
    private val _uiState = MutableStateFlow(LoginUiState())
    // Estado público (solo lectura para la pantalla)
    val uiState = _uiState.asStateFlow()

    fun onLoginClick(nickname: String, pass: String) {
        // 1. Activamos el modo "Cargando" y borramos errores previos
        _uiState.value = LoginUiState(isLoading = true, error = null)

        // 2. Lanzamos la tarea en segundo plano (viewModelScope es de la librería Moko)
        viewModelScope.launch {
            try {
                val persona = repository.login(nickname, pass)

                if (persona != null) {
                    // ¡Éxito! Guardamos la persona
                    _uiState.value = LoginUiState(usuarioLogueado = persona)
                    println("Login correcto: ${persona.nombre}")
                } else {
                    // Fallo: Credenciales mal
                    _uiState.value = LoginUiState(error = "Nickname o contraseña incorrectos", isLoading = false)
                }
            } catch (e: Exception) {
                // Fallo: Error técnico (servidor apagado, sin internet...)
                _uiState.value = LoginUiState(error = "Error de conexión: ${e.message}", isLoading = false)
                e.printStackTrace()
            }
        }
    }
    fun onRegistroClick(nickname: String, pass: String, nombre: String, apellidos: String) {
        _uiState.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val exito = repository.registrarUsuario(nickname, pass, nombre, apellidos)
                if (exito) {
                    // Si se crea bien, avisamos y quitamos el loading
                    _uiState.value = LoginUiState(
                        mensajeExito = "¡Cuenta creada! Ahora inicia sesión.",
                        isLoading = false
                    )
                } else {
                    _uiState.value = LoginUiState(error = "No se pudo crear (¿Nickname repetido?)", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState(error = "Error al registrar", isLoading = false)
            }
        }
    }

    // Función auxiliar para limpiar mensajes al cambiar de pantalla
    fun limpiarErrores() {
        _uiState.value = LoginUiState()
    }
}