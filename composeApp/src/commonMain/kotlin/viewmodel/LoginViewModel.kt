package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.Persona
import network.EntrenamientoRepository

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
        val nickLimpio = nickname.trim()
        val passLimpia = pass.trim()

        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                mensajeExito = null,
                usuarioLogueado = null
            )
        }

        viewModelScope.launch {
            try {
                val persona = repository.login(nickLimpio, passLimpia)

                if (persona != null) {
                    _uiState.update { it.copy(usuarioLogueado = persona, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Nickname o contraseña incorrectos",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Error desconocido de red",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onRegistroClick(nickname: String, pass: String, nombre: String, apellidos: String) {
        val nickLimpio = nickname.trim()
        val passLimpia = pass.trim()

        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                mensajeExito = null,
                usuarioLogueado = null
            )
        }

        viewModelScope.launch {
            try {
                val exito = repository.registrarUsuario(nickLimpio, passLimpia, nombre, apellidos)
                if (exito) {
                    _uiState.update {
                        it.copy(
                            mensajeExito = "Cuenta creada con éxito. Ahora puedes iniciar sesión.",
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "No se pudo crear la cuenta. Revisa los datos.",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Error de red al procesar registro",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensajeExito = null) }
    }
}