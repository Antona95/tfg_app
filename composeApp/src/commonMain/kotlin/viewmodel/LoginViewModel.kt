package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.Persona
import network.EntrenamientoRepository

// Representacion del estado de la pantalla de acceso
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensajeExito: String? = null,
    val usuarioLogueado: Persona? = null
)

class LoginViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    // Gestion de la validacion de credenciales
    fun onLoginClick(nickname: String, pass: String) {
        // Iniciamos carga y limpiamos errores previos
        _uiState.update { it.copy(isLoading = true, error = null, mensajeExito = null) }

        viewModelScope.launch {
            try {
                // AÑADIMOS ESTE PRINT PARA VER QUÉ ESTAMOS ENVIANDO
                println(" INTENTANDO LOGIN -> Nickname: $nickname, Pass: $pass")

                val persona = repository.login(nickname, pass)

                if (persona != null) {
                    // Login exitoso: guardamos el usuario
                    _uiState.update { it.copy(usuarioLogueado = persona, isLoading = false) }
                } else {
                    // Fallo de credenciales
                    _uiState.update { it.copy(error = "Nickname o contrasena incorrectos", isLoading = false) }
                }
            } catch (e: Exception) {
                // Error de red o servidor
                _uiState.update { it.copy(error = "Error de conexion: ${e.message}", isLoading = false) }
            }
        }
    }

    // Gestion de la creacion de nuevas cuentas
    fun onRegistroClick(nickname: String, pass: String, nombre: String, apellidos: String) {
        // Iniciamos carga y limpiamos estados anteriores
        _uiState.update { it.copy(isLoading = true, error = null, mensajeExito = null) }

        viewModelScope.launch {
            try {
                val exito = repository.registrarUsuario(nickname, pass, nombre, apellidos)
                if (exito) {
                    // Registro exitoso: actualizamos mensajeExito para que la UI cambie al modo Login
                    _uiState.update {
                        it.copy(
                            mensajeExito = "Cuenta creada con exito. Ahora puedes iniciar sesion.",
                            isLoading = false
                        )
                    }
                } else {
                    // Fallo en la creacion (posible duplicidad)
                    _uiState.update { it.copy(error = "No se pudo crear la cuenta. El usuario ya existe.", isLoading = false) }
                }
            } catch (e: Exception) {
                // Error en la comunicacion con la API
                _uiState.update { it.copy(error = "Error al procesar el registro", isLoading = false) }
            }
        }
    }

    // Restablece el estado completo (util para Logout)
    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }

    // Limpia los mensajes de error y exito sin afectar al usuario logueado
    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensajeExito = null) }
    }
}