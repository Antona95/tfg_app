package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.Persona
import network.EntrenamientoRepository

// Representación del estado de la pantalla de acceso
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensajeExito: String? = null,
    val usuarioLogueado: Persona? = null
)

class LoginViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    // Gestión de la validación de credenciales
    fun onLoginClick(nickname: String, pass: String) {
        // 1. LIMPIEZA: Eliminamos espacios accidentales que el teclado móvil suele añadir
        val nickLimpio = nickname.trim()
        val passLimpia = pass.trim()

        // Iniciamos carga y limpiamos errores previos en la UI
        _uiState.update { it.copy(isLoading = true, error = null, mensajeExito = null) }

        viewModelScope.launch {
            try {
                // 2. Pedimos los datos al repositorio
                val persona = repository.login(nickLimpio, passLimpia)

                if (persona != null) {
                    // Login exitoso: guardamos el usuario
                    _uiState.update { it.copy(usuarioLogueado = persona, isLoading = false) }
                } else {
                    // Si el repo devuelve null sin lanzar excepción, es que el backend
                    // devolvió un error de credenciales (HTTP 401/404).
                    _uiState.update { it.copy(error = "Nickname o contraseña incorrectos", isLoading = false) }
                }
            } catch (e: Exception) {
                // =======================================================
                // APUNTE DE CLASE: CAPTURA DEL FALLO DE RED
                // 3. Si salta a este catch, es porque el Repositorio detectó que no
                // hay Internet y lanzó un "throw Exception". Extraemos su mensaje
                // (e.message) y se lo mandamos a la UI para que lo pinte de rojo.
                // =======================================================
                _uiState.update { it.copy(error = e.message ?: "Error desconocido de red", isLoading = false) }
            }
        }
    }

    // Gestión de la creación de nuevas cuentas
    fun onRegistroClick(nickname: String, pass: String, nombre: String, apellidos: String) {
        val nickLimpio = nickname.trim()
        val passLimpia = pass.trim()

        // Iniciamos carga y limpiamos estados anteriores
        _uiState.update { it.copy(isLoading = true, error = null, mensajeExito = null) }

        viewModelScope.launch {
            try {
                val exito = repository.registrarUsuario(nickLimpio, passLimpia, nombre, apellidos)
                if (exito) {
                    // Registro exitoso
                    _uiState.update {
                        it.copy(
                            mensajeExito = "Cuenta creada con éxito. Ahora puedes iniciar sesión.",
                            isLoading = false
                        )
                    }
                } else {
                    // Fallo en la creación (HTTP 409 - posible duplicidad u otros)
                    _uiState.update { it.copy(error = "No se pudo crear la cuenta. Revisa los datos.", isLoading = false) }
                }
            } catch (e: Exception) {
                // Captura el fallo de red del repositorio ("No hay conexión con el servidor...")
                _uiState.update { it.copy(error = e.message ?: "Error de red al procesar registro", isLoading = false) }
            }
        }
    }

    // Restablece el estado completo (útil para Logout)
    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }

    // Limpia los mensajes de error y éxito sin afectar al usuario logueado
    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensajeExito = null) }
    }
}