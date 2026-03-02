package com.example.app_tfg

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import network.createHttpClient
import network.EntrenamientoRepository
import viewmodel.LoginViewModel
import ui.login.LoginScreen
import viewmodel.CoachViewModel
import viewmodel.BibliotecaViewModel
import viewmodel.HoyViewModel
import ui.coach.CoachScreen
import ui.coach.UserOptionsScreen
import ui.coach.NuevaSesionScreen
import ui.coach.BibliotecaScreen
import ui.user.HoyScreen
import model.Persona
import ui.coach.HistorialScreen
import ui.user.AlumnoHomeScreen
import viewmodel.HistorialViewModel

@Composable
fun App() {
    // 1. Estado global del modo oscuro con rememberSaveable para sobrevivir a la rotación
    var isDarkMode by rememberSaveable { mutableStateOf(false) }

    // 2. Definimos los colores dependiendo del estado
    val colores = if (isDarkMode) darkColorScheme() else lightColorScheme()

    // 3. Aplicamos los colores al MaterialTheme para que toda la app reaccione
    MaterialTheme(colorScheme = colores) {

        // --- CONFIGURACIÓN DE CONEXIÓN ---
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
                var viendoBiblioteca by remember { mutableStateOf(false) }

                // 1. ELEVAMOS LOS VIEWMODELS DEL ENTRENADOR
                // Al estar aquí arriba, sobreviven a la navegación y a la rotación
                val coachViewModel = getViewModel(
                    key = "coach-screen",
                    factory = viewModelFactory { CoachViewModel(repository) }
                )

                val biblioViewModel = getViewModel(
                    key = "biblioteca-screen",
                    factory = viewModelFactory { BibliotecaViewModel(repository) }
                )

                when {
                    viendoBiblioteca -> {
                        BibliotecaScreen(
                            viewModel = biblioViewModel, // Usamos el ViewModel elevado
                            onBack = { viendoBiblioteca = false }
                        )
                    }

                    creandoSesion && usuarioSeleccionado != null -> {
                        NuevaSesionScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            repository = repository,
                            isDarkMode = isDarkMode,
                            onNavigateBack = { creandoSesion = false },
                            sesionBase = null
                        )
                    }

                    usuarioSeleccionado != null -> {
                        UserOptionsScreen(
                            usuario = usuarioSeleccionado!!,
                            onBack = { usuarioSeleccionado = null },
                            onNuevaSesion = { creandoSesion = true },
                            onDuplicarSesion = { /* TODO */ },
                            onVerHistorial = { /* TODO */ }
                        )
                    }

                    else -> {
                        CoachScreen(
                            viewModel = coachViewModel, // Usamos el ViewModel elevado
                            onLogoutClick = { loginViewModel.cerrarSesion() },
                            onAlumnoClick = { alumno -> usuarioSeleccionado = alumno },
                            onBibliotecaClick = { viendoBiblioteca = true },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                    }
                }

            } else {
                // 1. Estado de navegación para el Alumno (sobrevive a rotaciones)
                var pantallaAlumno by rememberSaveable { mutableStateOf("HOME") }

                //  2. ELEVAMOS LOS VIEWMODELS DEL ALUMNO
                // Los datos ya no se borrarán al ir de 'Hoy' al 'Home'
                val hoyViewModel = getViewModel(
                    key = "hoy-screen-vm",
                    factory = viewModelFactory { HoyViewModel(repository) }
                )

                val historialViewModel = getViewModel(
                    key = "historial-screen-vm",
                    factory = viewModelFactory { HistorialViewModel(repository) }
                )

                // 3. Control de pantallas
                when (pantallaAlumno) {
                    "HOME" -> {
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
                            viewModel = hoyViewModel, // Usamos el ViewModel elevado
                            isDarkMode = isDarkMode,
                            onNavigateBack = { pantallaAlumno = "HOME" }
                        )
                    }

                    "HISTORIAL" -> {
                        HistorialScreen(
                            idUsuario = usuario.id,
                            repository = repository,
                            viewModel = historialViewModel, // Usamos el ViewModel elevado
                            onBack = { pantallaAlumno = "HOME" },
                            onSesionClick = { /* TODO: Navegar al detalle si es necesario */ }
                        )
                    }
                }
            }

        } else {
            // Pantalla de Login con los parámetros del modo oscuro integrados
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