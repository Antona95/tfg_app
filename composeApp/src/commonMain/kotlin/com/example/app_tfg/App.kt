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
import ui.user.HoyScreen // Importamos la pantalla del alumno
import model.Persona

@Composable
fun App() {
    MaterialTheme {
        // 1. CONFIGURACIÓN INICIAL
        val client = remember { createHttpClient() }
        val repository = remember { EntrenamientoRepository(client) }

        // 2. VIEWMODEL LOGIN (Estado global de la sesión)
        val loginViewModel = getViewModel(
            key = "login-screen",
            factory = viewModelFactory { LoginViewModel(repository) }
        )

        val state by loginViewModel.uiState.collectAsState()

        // 3. LÓGICA DE NAVEGACIÓN PRINCIPAL
        if (state.usuarioLogueado != null) {
            val usuario = state.usuarioLogueado!!

            // --- CASO A: EL USUARIO ES UN ENTRENADOR ---
            if (usuario.rol == "ENTRENADOR") {

                // Estado para saber qué alumno estamos gestionando
                var usuarioSeleccionado by remember { mutableStateOf<Persona?>(null) }

                // Estado para saber si estamos dentro del formulario de creación
                var creandoSesion by remember { mutableStateOf(false) }

                when {
                    // PANTALLA: FORMULARIO DE NUEVA SESIÓN
                    creandoSesion && usuarioSeleccionado != null -> {
                        NuevaSesionScreen(
                            idUsuario = usuarioSeleccionado!!.id, // Usamos .id de tu modelo Persona
                            repository = repository,
                            onNavigateBack = { creandoSesion = false }
                        )
                    }

                    // PANTALLA: MENÚ DE OPCIONES DEL ALUMNO (Botonera)
                    usuarioSeleccionado != null -> {
                        UserOptionsScreen(
                            usuario = usuarioSeleccionado!!,
                            onBack = { usuarioSeleccionado = null },
                            onNuevaSesion = { creandoSesion = true },
                            onDuplicarSesion = { /* TODO */ },
                            onVerHistorial = { /* TODO */ }
                        )
                    }

                    // PANTALLA: LISTADO DE TODOS LOS ALUMNOS
                    else -> {
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
                // --- CASO B: EL USUARIO ES UN DEPORTISTA / USUARIO ---
                // Aquí usamos la nueva pantalla HoyScreen
                HoyScreen(
                    idUsuario = usuario.id, // Usamos .id de tu modelo Persona
                    repository = repository,
                    onNavigateBack = { loginViewModel.cerrarSesion() }
                )
            }

        } else {
            // --- CASO C: NADIE LOGUEADO ---
            LoginScreen(
                onLoginClick = { nick, pass -> loginViewModel.onLoginClick(nick, pass) },
                onRegistroClick = { nick, pass, nom, ape -> loginViewModel.onRegistroClick(nick, pass, nom, ape) },
                mensajeExito = state.mensajeExito
            )
        }
    }
}