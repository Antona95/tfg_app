package com.example.app_tfg

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import ui.components.BackHandler
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
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun App() {
    var isDarkMode by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoSalir by remember { mutableStateOf(false) }

    val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        val client = remember { createHttpClient() }
        val repository = remember { EntrenamientoRepository(client) }

        val loginViewModel = getViewModel(
            key = "login-screen",
            factory = viewModelFactory { LoginViewModel(repository) }
        )

        val state by loginViewModel.uiState.collectAsState()

        if (mostrarDialogoSalir) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoSalir = false },
                title = {
                    Text("Confirmar salida")
                },
                text = {
                    Text("¿Seguro que quieres salir?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarDialogoSalir = false
                            loginViewModel.cerrarSesion()
                        }
                    ) {
                        Text("Sí, salir")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { mostrarDialogoSalir = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (state.usuarioLogueado != null) {
            val usuario = state.usuarioLogueado!!

            if (usuario.rol == "ENTRENADOR") {
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

                val sesionesAlumno by historialCoachVM.sesiones.collectAsState()

                var usuarioSeleccionado by remember { mutableStateOf<Persona?>(null) }
                var creandoSesion by rememberSaveable { mutableStateOf(false) }
                var viendoHistorial by rememberSaveable { mutableStateOf(false) }
                var sesionSeleccionada by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }
                var sesionParaDuplicar by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                LaunchedEffect(usuarioSeleccionado) {
                    if (usuarioSeleccionado != null) {
                        historialCoachVM.limpiarHistorial()
                        historialCoachVM.cargarHistorial(usuarioSeleccionado!!.id, forzarRecarga = true)
                    }
                }

                when {
                    sesionSeleccionada != null -> {
                        BackHandler { sesionSeleccionada = null }
                        DetalleSesionScreen(
                            sesion = sesionSeleccionada!!,
                            isDarkMode = isDarkMode,
                            onBack = { sesionSeleccionada = null }
                        )
                    }

                    creandoSesion && usuarioSeleccionado != null -> {
                        BackHandler {
                            creandoSesion = false
                            sesionParaDuplicar = null
                        }
                        NuevaSesionScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            viewModel = sesionVM,
                            isDarkMode = isDarkMode,
                            sesionBase = sesionParaDuplicar,
                            onNavigateBack = {
                                creandoSesion = false
                                sesionParaDuplicar = null
                                historialCoachVM.cargarHistorial(
                                    usuarioSeleccionado!!.id,
                                    forzarRecarga = true
                                )
                            }
                        )
                    }

                    viendoHistorial && usuarioSeleccionado != null -> {
                        BackHandler { viendoHistorial = false }
                        HistorialScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            repository = repository,
                            viewModel = historialCoachVM,
                            isDarkMode = isDarkMode,
                            onBack = { viendoHistorial = false },
                            onSesionClick = { sesion -> sesionSeleccionada = sesion }
                        )
                    }

                    usuarioSeleccionado != null -> {
                        BackHandler { usuarioSeleccionado = null }
                        UserOptionsScreen(
                            usuario = usuarioSeleccionado!!,
                            tieneSesiones = sesionesAlumno.isNotEmpty(),
                            onBack = { usuarioSeleccionado = null },
                            onNuevaSesion = {
                                sesionParaDuplicar = null
                                creandoSesion = true
                            },
                            onDuplicarSesion = {
                                sesionVM.prepararDuplicado(usuarioSeleccionado!!.id) { sesion ->
                                    if (sesion != null) {
                                        sesionParaDuplicar = sesion
                                        creandoSesion = true
                                    }
                                }
                            },
                            onVerHistorial = { viendoHistorial = true }
                        )
                    }

                    else -> {
                        CoachScreen(
                            viewModel = coachViewModel,
                            onLogoutClick = { mostrarDialogoSalir = true },
                            onAlumnoClick = { alumno -> usuarioSeleccionado = alumno },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                    }
                }
            } else {
                val hoyViewModel = getViewModel(
                    key = "hoy-screen-vm",
                    factory = viewModelFactory { HoyViewModel(repository) }
                )
                val historialViewModel = getViewModel(
                    key = "historial-screen-vm",
                    factory = viewModelFactory { HistorialViewModel(repository) }
                )

                var pantallaAlumno by rememberSaveable { mutableStateOf("MENU") }
                var sesionDetalleAlumno by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                when (pantallaAlumno) {
                    "MENU" -> AlumnoHomeScreen(
                        usuario = usuario,
                        onVerHoy = { pantallaAlumno = "HOY" },
                        onVerHistorial = { pantallaAlumno = "HISTORIAL" },
                        onLogout = { mostrarDialogoSalir = true },
                        isDarkMode = isDarkMode,
                        onThemeToggle = { isDarkMode = !isDarkMode }
                    )

                    "HOY" -> {
                        BackHandler { pantallaAlumno = "MENU" }
                        HoyScreen(
                            idUsuario = usuario.id,
                            viewModel = hoyViewModel,
                            isDarkMode = isDarkMode,
                            onNavigateBack = { pantallaAlumno = "MENU" }
                        )
                    }

                    "HISTORIAL" -> {
                        BackHandler { pantallaAlumno = "MENU" }
                        HistorialScreen(
                            idUsuario = usuario.id,
                            repository = repository,
                            viewModel = historialViewModel,
                            isDarkMode = isDarkMode,
                            onBack = { pantallaAlumno = "MENU" },
                            onSesionClick = { sesion ->
                                sesionDetalleAlumno = sesion
                                pantallaAlumno = "DETALLE"
                            }
                        )
                    }

                    "DETALLE" -> {
                        BackHandler {
                            pantallaAlumno = "HISTORIAL"
                            sesionDetalleAlumno = null
                        }

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
                            pantallaAlumno = "MENU"
                        }
                    }
                }
            }
        } else {
            LoginScreen(
                isLoading = state.isLoading,
                onLoginClick = { nick, pass -> loginViewModel.onLoginClick(nick, pass) },
                onRegistroClick = { nick, pass, nom, ape ->
                    loginViewModel.onRegistroClick(nick, pass, nom, ape)
                },
                mensajeExito = state.mensajeExito,
                errorBackend = state.error,
                isDarkMode = isDarkMode,
                onThemeToggle = { isDarkMode = !isDarkMode }
            )
        }
    }
}