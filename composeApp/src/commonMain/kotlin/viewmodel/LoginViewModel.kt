package viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.Persona
import network.EntrenamientoRepository

// esta data class representa todo el estado de la pantalla de login.
// la hago asi para tener en un solo objeto:
// carga, error, mensaje de exito y usuario logueado.
data class LoginUiState(

    // este booleano me dice si hay una operacion en curso.
    // por ejemplo, al hacer login o al registrar una cuenta.
    val isLoading: Boolean = false,

    // aqui guardo un posible error para mostrarlo en pantalla.
    val error: String? = null,

    // aqui guardo un mensaje de exito, por ejemplo cuando el registro sale bien.
    val mensajeExito: String? = null,

    // si el login es correcto, aqui guardo la persona devuelta por el backend.
    val usuarioLogueado: Persona? = null
)

// este viewmodel controla toda la logica de la pantalla de login y registro.
class LoginViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    // aqui guardo el estado interno de la pantalla.
    // empiezo con un estado vacio por defecto.
    private val _uiState = MutableStateFlow(LoginUiState())

    // expongo el estado como solo lectura para que la ui lo observe.
    val uiState = _uiState.asStateFlow()

    // este metodo se ejecuta cuando el usuario pulsa el boton de entrar.
    fun onLoginClick(nickname: String, pass: String) {

        // limpio espacios al principio y al final para evitar errores por escribir mal.
        val nickLimpio = nickname.trim()
        val passLimpia = pass.trim()

        // antes de lanzar la peticion, pongo el estado en carga
        // y limpio errores, mensajes y usuario previo.
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
                // pido al repositorio que intente hacer login.
                val persona = repository.login(nickLimpio, passLimpia)

                if (persona != null) {

                    // si la api devuelve persona, el login ha salido bien
                    // y guardo el usuario en el estado.
                    _uiState.update { it.copy(usuarioLogueado = persona, isLoading = false) }
                } else {

                    // si no devuelve persona, considero que nickname o contraseña son incorrectos.
                    _uiState.update {
                        it.copy(
                            error = "Nickname o contraseña incorrectos",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {

                // si hay fallo de red o del servidor, guardo el mensaje de error.
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Error desconocido de red",
                        isLoading = false
                    )
                }
            }
        }
    }

    // este metodo se ejecuta cuando el usuario pulsa crear cuenta.
    fun onRegistroClick(nickname: String, pass: String, nombre: String, apellidos: String) {

        // igual que en login, limpio espacios para evitar problemas innecesarios.
        val nickLimpio = nickname.trim()
        val passLimpia = pass.trim()

        // antes de empezar, activo carga y limpio estados anteriores.
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
                // pido al repositorio que registre al nuevo usuario.
                val exito = repository.registrarUsuario(nickLimpio, passLimpia, nombre, apellidos)

                if (exito) {

                    // si sale bien, guardo un mensaje de exito.
                    // no guardo usuario logueado porque despues del registro quiero volver al login.
                    _uiState.update {
                        it.copy(
                            mensajeExito = "Cuenta creada con éxito. Ahora puedes iniciar sesión.",
                            isLoading = false
                        )
                    }
                } else {

                    // si el backend responde pero no registra, muestro error generico.
                    _uiState.update {
                        it.copy(
                            error = "No se pudo crear la cuenta. Revisa los datos.",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {

                // si hay problema de red o servidor, lo reflejo tambien en pantalla.
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Error de red al procesar registro",
                        isLoading = false
                    )
                }
            }
        }
    }

    // este metodo sirve para cerrar la sesion.
    // al hacerlo, reseteo todo el estado como si la app acabara de arrancar.
    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }

    // este metodo me sirve para limpiar mensajes de error o exito
    // sin borrar al usuario logueado ni tocar otros datos.
    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensajeExito = null) }
    }
}