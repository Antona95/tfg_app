package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Persona
import network.EntrenamientoRepository

// Estado de la pantalla
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensajeExito: String? = null,
    val usuarioLogueado: Persona? = null
)

class LoginViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onLoginClick(nickname: String, pass: String) {
        _uiState.value = LoginUiState(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val persona = repository.login(nickname, pass)

                if (persona != null) {
                    _uiState.value = LoginUiState(usuarioLogueado = persona)
                } else {
                    _uiState.value = LoginUiState(error = "Nickname o contraseña incorrectos", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState(error = "Error de conexión: ${e.message}", isLoading = false)
            }
        }
    }

    fun onRegistroClick(nickname: String, pass: String, nombre: String, apellidos: String) {
        _uiState.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val exito = repository.registrarUsuario(nickname, pass, nombre, apellidos)
                if (exito) {
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

    // --- ESTA ES LA FUNCIÓN QUE FALTABA ---
    fun cerrarSesion() {
        // Al instanciar LoginUiState() vacío, todos los valores vuelven a sus
        // valores por defecto (usuarioLogueado = null, isLoading = false, etc.)
        _uiState.value = LoginUiState()
    }

    fun limpiarErrores() {
        _uiState.value = LoginUiState()
    }
}