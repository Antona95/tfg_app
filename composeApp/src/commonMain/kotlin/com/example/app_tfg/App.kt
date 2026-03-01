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
    // 1. ESTADO GLOBAL (State Hoisting) - Usamos rememberSaveable para que sobreviva a rotaciones
    var isDarkMode by rememberSaveable { mutableStateOf(false) }

    // 2. CONFIGURACIÓN DEL TEMA
    val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

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
                // SECCIÓN ENTRENADOR (Persistente)
                // =================================================
                val coachViewModel = getViewModel(
                    key = "coach-screen",
                    factory = viewModelFactory { CoachViewModel(repository) }
                )

                // Añadimos el HistorialViewModel para el Coach en el nivel superior para que persista
                val historialCoachVM = getViewModel(
                    key = "historial-coach-vm",
                    factory = viewModelFactory { HistorialViewModel(repository) }
                )

                // Usamos rememberSaveable para que no se pierda el alumno al rotar
                var usuarioSeleccionado by remember { mutableStateOf<Persona?>(null) }
                var creandoSesion by rememberSaveable { mutableStateOf(false) }
                var viendoHistorial by rememberSaveable { mutableStateOf(false) }

                var sesionSeleccionada by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }
                var sesionParaDuplicar by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                when {
                    sesionSeleccionada != null -> {
                        DetalleSesionScreen(
                            sesion = sesionSeleccionada!!,
                            isDarkMode = isDarkMode,
                            onBack = { sesionSeleccionada = null }
                        )
                    }
                    creandoSesion && usuarioSeleccionado != null -> {
                        NuevaSesionScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            repository = repository,
                            isDarkMode = isDarkMode,
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
                            viewModel = historialCoachVM,
                            onBack = { viendoHistorial = false },
                            onSesionClick = { sesion -> sesionSeleccionada = sesion }
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
                        CoachScreen(
                            viewModel = coachViewModel,
                            onLogout = { loginViewModel.cerrarSesion() },
                            onAlumnoClick = { alumno -> usuarioSeleccionado = alumno },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                    }
                }

            } else {
                // =================================================
                // SECCIÓN ALUMNO (Persistente)
                // =================================================
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
                    "MENU" -> {
                        AlumnoHomeScreen(
                            usuario = usuario,
                            onVerHoy = { pantallaAlumno = "HOY" },
                            onVerHistorial = { pantallaAlumno = "HISTORIAL" },
                            onLogout = { loginViewModel.cerrarSesion() },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                    }
                    "HOY" -> {
                        HoyScreen(
                            idUsuario = usuario.id,
                            viewModel = hoyViewModel,
                            isDarkMode = isDarkMode,
                            onNavigateBack = { pantallaAlumno = "MENU" }
                        )
                    }
                    "HISTORIAL" -> {
                        HistorialScreen(
                            idUsuario = usuario.id,
                            repository = repository,
                            viewModel = historialViewModel,
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
                onLoginClick = { nick, pass -> loginViewModel.onLoginClick(nick, pass) },
                onRegistroClick = { nick, pass, nom, ape -> loginViewModel.onRegistroClick(nick, pass, nom, ape) },
                mensajeExito = state.mensajeExito,
                isDarkMode = isDarkMode,
                onThemeToggle = { isDarkMode = !isDarkMode }
            )
        }
    }
}