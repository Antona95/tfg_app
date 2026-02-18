package com.example.app_tfg

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import network.createHttpClient
import network.EntrenamientoRepository
import viewmodel.LoginViewModel
import ui.login.LoginScreen
import viewmodel.CoachViewModel
import ui.coach.CoachScreen
import ui.coach.UserOptionsScreen
import ui.coach.NuevaSesionScreen
import ui.coach.HistorialScreen // IMPORTANTE: Asegúrate de tener este import
import ui.user.HoyScreen
import model.Persona

@Composable
fun App() {
    MaterialTheme {
        val client = remember { createHttpClient() }
        val repository = remember { EntrenamientoRepository(client) }

        val loginViewModel = getViewModel(
            key = "login-screen",
            factory = viewModelFactory { LoginViewModel(repository) }
        )

        val state by loginViewModel.uiState.collectAsState()

        if (state.usuarioLogueado != null) {
            val usuario = state.usuarioLogueado!!

            if (usuario.rol == "ENTRENADOR") {
                var usuarioSeleccionado by remember { mutableStateOf<Persona?>(null) }
                var creandoSesion by remember { mutableStateOf(false) }
                var viendoHistorial by remember { mutableStateOf(false) }

                // --- NUEVO ESTADO PARA NAVEGACIÓN A DETALLE ---
                var sesionSeleccionada by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                when {
                    // 1. NIVEL MÁS ALTO: DETALLE DE SESIÓN
                    sesionSeleccionada != null -> {
                        ui.coach.DetalleSesionScreen(
                            sesion = sesionSeleccionada!!,
                            onBack = { sesionSeleccionada = null } // Al volver, limpiamos la selección
                        )
                    }

                    // 2. FORMULARIO DE CREACIÓN
                    creandoSesion && usuarioSeleccionado != null -> {
                        NuevaSesionScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            repository = repository,
                            onNavigateBack = { creandoSesion = false }
                        )
                    }

                    // 3. LISTADO DE HISTORIAL
                    viendoHistorial && usuarioSeleccionado != null -> {
                        ui.coach.HistorialScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            repository = repository,
                            onBack = { viendoHistorial = false },
                            onSesionClick = { sesion ->
                                // AQUÍ CAPTURAMOS EL CLICK Y CAMBIAMOS DE ESTADO
                                sesionSeleccionada = sesion
                            }
                        )
                    }

                    // 4. MENÚ DE OPCIONES DEL ALUMNO
                    usuarioSeleccionado != null -> {
                        UserOptionsScreen(
                            usuario = usuarioSeleccionado!!,
                            onBack = { usuarioSeleccionado = null },
                            onNuevaSesion = { creandoSesion = true },
                            onDuplicarSesion = { /* TODO */ },
                            onVerHistorial = { viendoHistorial = true }
                        )
                    }

                    // 5. LISTA DE ALUMNOS (Default)
                    else -> {
                        // ... tu CoachScreen ...
                        val coachViewModel = getViewModel(
                            key = "coach-screen",
                            factory = viewModelFactory { CoachViewModel(repository) }
                        )
                        CoachScreen(
                            viewModel = coachViewModel,
                            onLogoutClick = { loginViewModel.cerrarSesion() },
                            onAlumnoClick = { alumno ->
                                usuarioSeleccionado = alumno
                            }
                        )
                    }
                }

            } else {
                // CASO ALUMNO: Pantalla de entrenamiento diario
                HoyScreen(
                    idUsuario = usuario.id,
                    repository = repository,
                    onNavigateBack = { loginViewModel.cerrarSesion() }
                )
            }

        } else {
            // LOGIN / REGISTRO
            LoginScreen(
                onLoginClick = { nick, pass -> loginViewModel.onLoginClick(nick, pass) },
                onRegistroClick = { nick, pass, nom, ape -> loginViewModel.onRegistroClick(nick, pass, nom, ape) },
                mensajeExito = state.mensajeExito
            )
        }
    }
}