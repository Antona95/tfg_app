package com.example.app_tfg

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import network.createHttpClient
import network.EntrenamientoRepository
import viewmodel.LoginViewModel
import ui.home.HomeScreen
import ui.login.LoginScreen
import viewmodel.CoachViewModel
import ui.coach.CoachScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ui.coach.UserOptionsScreen
import ui.coach.NuevaSesionScreen // <--- ASEGÚRATE DE IMPORTAR ESTO

@Composable
fun App() {
    MaterialTheme {
        // 1. CONFIGURACIÓN (Igual que antes)
        val client = remember { createHttpClient() }
        val repository = remember { EntrenamientoRepository(client) }

        // 2. VIEWMODEL LOGIN (Igual que antes)
        val loginViewModel = getViewModel(
            key = "login-screen",
            factory = viewModelFactory { LoginViewModel(repository) }
        )

        // 3. ESTADO (Igual que antes)
        val state by loginViewModel.uiState.collectAsState()

        // 4. LÓGICA DE NAVEGACIÓN
        if (state.usuarioLogueado != null) {
            val usuario = state.usuarioLogueado!!

            // CASO ENTRENADOR
            if (usuario.rol == "ENTRENADOR") {

                // VAR 1: ¿Qué alumno hemos elegido?
                var usuarioSeleccionado by remember { mutableStateOf<model.Persona?>(null) }

                // VAR 2 (NUEVO): ¿Estamos en la pantalla de crear sesión?
                var creandoSesion by remember { mutableStateOf(false) }

                // --- LÓGICA DE PANTALLAS (ESCALERA DE IFs) ---

                if (creandoSesion && usuarioSeleccionado != null) {
                    // PANTALLA 3: FORMULARIO DE NUEVA SESIÓN
                    // (Se muestra si le dimos al botón y tenemos un usuario)
                    NuevaSesionScreen(
                        idUsuario = usuarioSeleccionado!!.id ?: "", // Pasamos el ID del alumno
                        repository = repository,
                        onNavigateBack = {
                            // AL VOLVER: Apagamos el interruptor y volvemos al menú de opciones
                            creandoSesion = false
                        }
                    )

                } else if (usuarioSeleccionado != null) {
                    // PANTALLA 2: OPCIONES DEL USUARIO (Menú de 3 botones)
                    UserOptionsScreen(
                        usuario = usuarioSeleccionado!!,
                        onBack = { usuarioSeleccionado = null }, // Volver a la lista

                        onNuevaSesion = {
                            // AQUÍ ESTÁ LA CLAVE: Activamos el interruptor
                            creandoSesion = true
                        },

                        onDuplicarSesion = { /* TODO */ },
                        onVerHistorial = { /* TODO */ }
                    )

                } else {
                    // PANTALLA 1: LISTA DE ALUMNOS (Por defecto)
                    val coachViewModel = getViewModel(
                        key = "coach-screen",
                        factory = viewModelFactory { CoachViewModel(repository) }
                    )
                    CoachScreen(
                        viewModel = coachViewModel,
                        onLogoutClick = { /* loginViewModel.cerrarSesion() */ },
                        onAlumnoClick = { alumno ->
                            usuarioSeleccionado = alumno
                        }
                    )
                }

            } else {
                // CASO USUARIO NORMAL (Igual que antes)
                HomeScreen(
                    usuario = usuario,
                    repository = repository,
                    onLogoutClick = { /* logout */ }
                )
            }

        } else {
            // CASO LOGIN (Igual que antes)
            LoginScreen(
                onLoginClick = { nick, pass -> loginViewModel.onLoginClick(nick, pass) },
                onRegistroClick = { nick, pass, nom, ape -> loginViewModel.onRegistroClick(nick, pass, nom, ape) },
                mensajeExito = state.mensajeExito
            )
        }
    }
}