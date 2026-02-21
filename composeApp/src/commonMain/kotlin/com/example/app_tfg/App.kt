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

@Composable
fun App() {
    // 1. GUARDAMOS EL ESTADO DEL MODO OSCURO
    var isDarkMode by remember { mutableStateOf(false) }

    // 2. ELEGIMOS LA PALETA DE COLORES
    val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

    // 3. APLICAMOS EL TEMA A TODA LA APP
    MaterialTheme(colorScheme = colorScheme) {
        val client = remember { createHttpClient() }
        val repository = remember { EntrenamientoRepository(client) }
        val scope = rememberCoroutineScope()

        val loginViewModel = getViewModel(
            key = "login-screen",
            factory = viewModelFactory { LoginViewModel(repository) }
        )

        val state by loginViewModel.uiState.collectAsState()

        if (state.usuarioLogueado != null) {
            val usuario = state.usuarioLogueado!!

            if (usuario.rol == "ENTRENADOR") {
                // =================================================
                // SECCIÓN ENTRENADOR
                // =================================================
                var usuarioSeleccionado by remember { mutableStateOf<Persona?>(null) }
                var creandoSesion by remember { mutableStateOf(false) }
                var viendoHistorial by remember { mutableStateOf(false) }
                var sesionSeleccionada by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }
                var sesionParaDuplicar by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                when {
                    sesionSeleccionada != null -> {
                        DetalleSesionScreen(
                            sesion = sesionSeleccionada!!,
                            onBack = { sesionSeleccionada = null }
                        )
                    }

                    creandoSesion && usuarioSeleccionado != null -> {
                        NuevaSesionScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            repository = repository,
                            sesionBase = sesionParaDuplicar,
                            onNavigateBack = {
                                creandoSesion = false
                                sesionParaDuplicar = null
                            }
                        )
                    }

                    viendoHistorial && usuarioSeleccionado != null -> {
                        HistorialScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            repository = repository,
                            onBack = { viendoHistorial = false },
                            onSesionClick = { sesion ->
                                sesionSeleccionada = sesion
                            }
                        )
                    }

                    usuarioSeleccionado != null -> {
                        UserOptionsScreen(
                            usuario = usuarioSeleccionado!!,
                            onBack = { usuarioSeleccionado = null },
                            onNuevaSesion = {
                                sesionParaDuplicar = null
                                creandoSesion = true
                            },
                            onDuplicarSesion = {
                                scope.launch {
                                    val historial = repository.obtenerHistorialSesiones(usuarioSeleccionado!!.id)
                                    if (historial.isNotEmpty()) {
                                        sesionParaDuplicar = historial.first()
                                        creandoSesion = true
                                    }
                                }
                            },
                            onVerHistorial = { viendoHistorial = true }
                        )
                    }

                    else -> {
                        val coachViewModel = getViewModel(
                            key = "coach-screen",
                            factory = viewModelFactory { CoachViewModel(repository) }
                        )
                        CoachScreen(
                            viewModel = coachViewModel,
                            onLogout = { loginViewModel.cerrarSesion() },
                            onAlumnoClick = { alumno ->
                                usuarioSeleccionado = alumno
                            },
                            // 🌙 PASAMOS EL TEMA A LA PANTALLA DEL COACH
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                    }
                }

            } else {
                // =================================================
                // SECCIÓN ALUMNO
                // =================================================
                var pantallaAlumno by remember { mutableStateOf("MENU") }
                var sesionDetalleAlumno by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                when (pantallaAlumno) {
                    "MENU" -> {
                        AlumnoHomeScreen(
                            usuario = usuario,
                            onVerHoy = { pantallaAlumno = "HOY" },
                            onVerHistorial = { pantallaAlumno = "HISTORIAL" },
                            onLogout = { loginViewModel.cerrarSesion() },
                            // 🌙 PASAMOS EL TEMA A LA PANTALLA DEL ALUMNO
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                    }

                    "HOY" -> {
                        HoyScreen(
                            idUsuario = usuario.id,
                            repository = repository,
                            onNavigateBack = { pantallaAlumno = "MENU" }
                        )
                    }

                    "HISTORIAL" -> {
                        HistorialScreen(
                            idUsuario = usuario.id,
                            repository = repository,
                            onBack = { pantallaAlumno = "MENU" },
                            onSesionClick = { sesion ->
                                sesionDetalleAlumno = sesion
                                pantallaAlumno = "DETALLE"
                            }
                        )
                    }

                    "DETALLE" -> {
                        if (sesionDetalleAlumno != null) {
                            DetalleSesionScreen(
                                sesion = sesionDetalleAlumno!!,
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
            // =================================================
            // PANTALLA DE ACCESO (Login/Registro)
            // =================================================
            LoginScreen(
                onLoginClick = { nick, pass -> loginViewModel.onLoginClick(nick, pass) },
                onRegistroClick = { nick, pass, nom, ape -> loginViewModel.onRegistroClick(nick, pass, nom, ape) },
                mensajeExito = state.mensajeExito
            )
        }
    }
}