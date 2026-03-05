package com.example.app_tfg

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import network.createHttpClient
import network.EntrenamientoRepository
import viewmodel.LoginViewModel
import viewmodel.CoachViewModel
import viewmodel.HoyViewModel
import viewmodel.HistorialViewModel
import viewmodel.SesionViewModel
import ui.login.LoginScreen
import ui.coach.CoachScreen
import ui.coach.UserOptionsScreen
import ui.coach.NuevaSesionScreen
import ui.coach.HistorialScreen
import ui.coach.DetalleSesionScreen
import ui.user.HoyScreen
import ui.user.AlumnoHomeScreen
import model.Persona
import kotlinx.coroutines.launch
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun App() {
    // Control del modo oscuro. Usamos rememberSaveable para que el estado sobreviva a
    // rotaciones de pantalla o si el sistema mata la activity por falta de memoria.
    var isDarkMode by rememberSaveable { mutableStateOf(false) }
    val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        // Inicializamos el cliente Ktor y nuestro Repositorio central.
        // Usamos remember para no instanciarlo cada vez que Jetpack Compose repinta la UI (recomposición).
        val client = remember { createHttpClient() }
        val repository = remember { EntrenamientoRepository(client) }

        // Inyectamos el ViewModel del Login usando Moko MVVM para Kotlin Multiplatform
        val loginViewModel = getViewModel(
            key = "login-screen",
            factory = viewModelFactory { LoginViewModel(repository) }
        )

        // Observamos el estado del login (si hay usuario logueado, errores de API, etc.)
        val state by loginViewModel.uiState.collectAsState()

        // Ruteo principal: Si hay un usuario logueado, entramos a la app; si no, al Login.
        if (state.usuarioLogueado != null) {
            val usuario = state.usuarioLogueado!!

            // ==========================================
            // FLUJO DEL ENTRENADOR (MASTERCOACH)
            // ==========================================
            if (usuario.rol == "ENTRENADOR") {
                // Instanciamos los ViewModels necesarios para el flujo del entrenador
                val coachViewModel = getViewModel(
                    key = "coach-screen",
                    factory = viewModelFactory { CoachViewModel(repository) }
                )
                val historialCoachVM = getViewModel(
                    key = "historial-coach-vm",
                    factory = viewModelFactory { HistorialViewModel(repository) }
                )
                val sesionVM = getViewModel(
                    key = "sesion-vm",
                    factory = viewModelFactory { SesionViewModel(repository) }
                )

                // NUEVO: Observamos las sesiones del alumno para saber si la lista está vacía o no.
                // Esto nos servirá luego para bloquear el botón de "Copiar Sesión" si es un alumno nuevo.
                val sesionesAlumno by historialCoachVM.sesiones.collectAsState()

                // Variables de estado para controlar la navegación manual del entrenador sin usar librerías complejas
                var usuarioSeleccionado by remember { mutableStateOf<Persona?>(null) }
                var creandoSesion by rememberSaveable { mutableStateOf(false) }
                var viendoHistorial by rememberSaveable { mutableStateOf(false) }
                var sesionSeleccionada by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }
                var sesionParaDuplicar by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                // TRUCO PARA MEJORAR LA UX: Cuando el coach hace clic en un alumno, lanzamos un efecto.
                // 1. Limpiamos datos viejos para que no vea la sesión de otro alumno por error.
                // 2. Pre-cargamos su historial en segundo plano. Así la lista carga al instante al navegar.
                LaunchedEffect(usuarioSeleccionado) {
                    if (usuarioSeleccionado != null) {
                        historialCoachVM.limpiarHistorial()
                        historialCoachVM.cargarHistorial(usuarioSeleccionado!!.id, forzarRecarga = true)
                    }
                }

                // Máquina de estados rudimentaria pero efectiva para manejar las pantallas del Coach
                when {
                    // 1. Ver detalle de una sesión concreta (viene de la lista del historial)
                    sesionSeleccionada != null -> {
                        DetalleSesionScreen(
                            sesion = sesionSeleccionada!!,
                            isDarkMode = isDarkMode,
                            onBack = { sesionSeleccionada = null }
                        )
                    }
                    // 2. Crear una nueva sesión (desde cero o duplicando una base)
                    creandoSesion && usuarioSeleccionado != null -> {
                        NuevaSesionScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            viewModel = sesionVM,
                            isDarkMode = isDarkMode,
                            sesionBase = sesionParaDuplicar,
                            onNavigateBack = {
                                creandoSesion = false
                                sesionParaDuplicar = null
                                // Forzamos recarga del historial por si el coach acaba de guardar una rutina nueva
                                historialCoachVM.cargarHistorial(usuarioSeleccionado!!.id, forzarRecarga = true)
                            }
                        )
                    }
                    // 3. Ver la lista de entrenamientos pasados (Historial del alumno seleccionado)
                    viendoHistorial && usuarioSeleccionado != null -> {
                        HistorialScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            repository = repository,
                            viewModel = historialCoachVM,
                            onBack = { viendoHistorial = false },
                            onSesionClick = { sesion -> sesionSeleccionada = sesion }
                        )
                    }
                    // 4. Menú de opciones de un alumno (Pantalla puente)
                    usuarioSeleccionado != null -> {
                        UserOptionsScreen(
                            usuario = usuarioSeleccionado!!,
                            // Pasamos el booleano comprobando si la lista de sesiones pre-cargada tiene contenido
                            tieneSesiones = sesionesAlumno.isNotEmpty(),
                            onBack = { usuarioSeleccionado = null },
                            onNuevaSesion = {
                                sesionParaDuplicar = null
                                creandoSesion = true
                            },
                            onDuplicarSesion = {
                                // Llamamos al backend para preparar la última sesión y copiar sus datos
                                sesionVM.prepararDuplicado(usuarioSeleccionado!!.id) { sesion ->
                                    sesionParaDuplicar = sesion
                                    creandoSesion = true
                                }
                            },
                            onVerHistorial = { viendoHistorial = true }
                        )
                    }
                    // 5. Pantalla principal del Coach: Lista del buscador de alumnos
                    else -> {
                        CoachScreen(
                            viewModel = coachViewModel,
                            onLogoutClick = { loginViewModel.cerrarSesion() },
                            onAlumnoClick = { alumno -> usuarioSeleccionado = alumno },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                    }
                }

            } else {
                // ==========================================
                // FLUJO DEL ALUMNO (USUARIO NORMAL)
                // ==========================================

                // Instanciamos los ViewModels específicos para el deportista
                val hoyViewModel = getViewModel(
                    key = "hoy-screen-vm",
                    factory = viewModelFactory { HoyViewModel(repository) }
                )
                val historialViewModel = getViewModel(
                    key = "historial-screen-vm",
                    factory = viewModelFactory { HistorialViewModel(repository) }
                )

                // En el alumno usamos un String como clave de estado para la navegación (otra forma válida de ruteo)
                var pantallaAlumno by rememberSaveable { mutableStateOf("MENU") }
                var sesionDetalleAlumno by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                when (pantallaAlumno) {
                    // Dashboard principal con botones de acceso
                    "MENU" -> AlumnoHomeScreen(
                        usuario = usuario,
                        onVerHoy = { pantallaAlumno = "HOY" },
                        onVerHistorial = { pantallaAlumno = "HISTORIAL" },
                        onLogout = { loginViewModel.cerrarSesion() },
                        isDarkMode = isDarkMode,
                        onThemeToggle = { isDarkMode = !isDarkMode }
                    )
                    // Pantalla para ejecutar el entrenamiento asignado para el día actual
                    "HOY" -> HoyScreen(
                        idUsuario = usuario.id,
                        viewModel = hoyViewModel,
                        isDarkMode = isDarkMode,
                        onNavigateBack = { pantallaAlumno = "MENU" }
                    )
                    // Consulta de rutinas antiguas
                    "HISTORIAL" -> HistorialScreen(
                        idUsuario = usuario.id,
                        repository = repository,
                        viewModel = historialViewModel,
                        onBack = { pantallaAlumno = "MENU" },
                        onSesionClick = { sesion ->
                            sesionDetalleAlumno = sesion
                            pantallaAlumno = "DETALLE"
                        }
                    )
                    // Reutilizamos el componente visual de DetalleSesionScreen para ver el interior del historial
                    "DETALLE" -> {
                        if (sesionDetalleAlumno != null) {
                            DetalleSesionScreen(
                                sesion = sesionDetalleAlumno!!,
                                isDarkMode = isDarkMode,
                                onBack = {
                                    pantallaAlumno = "HISTORIAL"
                                    sesionDetalleAlumno = null
                                }
                            )
                        } else {
                            // Ojo con esto para que no pete: por seguridad, si el detalle llega nulo, lo mandamos al menú
                            pantallaAlumno = "MENU"
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // PANTALLA DE LOGIN Y REGISTRO
            // ==========================================
            LoginScreen(
                onLoginClick = { nick, pass -> loginViewModel.onLoginClick(nick, pass) },
                onRegistroClick = { nick, pass, nom, ape -> loginViewModel.onRegistroClick(nick, pass, nom, ape) },
                mensajeExito = state.mensajeExito,
                errorBackend = state.error,
                isDarkMode = isDarkMode,
                onThemeToggle = { isDarkMode = !isDarkMode }
            )
        }
    }
}