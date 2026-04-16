package com.example.app_tfg

import androidx.compose.runtime.*
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import repository.AuthRepository
import repository.SessionRepository
import repository.UserRepository
import ui.components.DialogoCerrarSesion
import ui.login.LoginScreen
import ui.navigation.AlumnoFlow
import ui.navigation.CoachFlow
import viewmodel.LoginViewModel

@Composable
fun AppContent(
    authRepository: AuthRepository,
    usuarioRepository: UserRepository,
    sesionRepository: SessionRepository,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    var mostrarDialogoSalir by remember { mutableStateOf(false) }

    val loginViewModel = getViewModel(
        key = "login-screen",
        factory = viewModelFactory { LoginViewModel(authRepository) }
    )

    val state by loginViewModel.uiState.collectAsState()

    DialogoCerrarSesion(
        mostrarDialogo = mostrarDialogoSalir,
        onConfirmar = {
            mostrarDialogoSalir = false
            loginViewModel.cerrarSesion()
        },
        onCancelar = {
            mostrarDialogoSalir = false
        }
    )

    if (state.usuarioLogueado == null) {
        LoginScreen(
            isLoading = state.isLoading,
            onLoginClick = { nick, pass -> loginViewModel.onLoginClick(nick, pass) },
            onRegistroClick = { nick, pass, nom, ape ->
                loginViewModel.onRegistroClick(nick, pass, nom, ape)
            },
            mensajeExito = state.mensajeExito,
            errorBackend = state.error,
            isDarkMode = isDarkMode,
            onThemeToggle = onThemeToggle
        )
        return
    }

    val usuario = state.usuarioLogueado!!

    if (usuario.rol == "ENTRENADOR") {
        CoachFlow(
            usuario = usuario,
            usuarioRepository = usuarioRepository,
            sesionRepository = sesionRepository,
            isDarkMode = isDarkMode,
            onThemeToggle = onThemeToggle,
            onLogoutRequest = { mostrarDialogoSalir = true }
        )
    } else {
        AlumnoFlow(
            usuario = usuario,
            sesionRepository = sesionRepository,
            isDarkMode = isDarkMode,
            onThemeToggle = onThemeToggle,
            onLogoutRequest = { mostrarDialogoSalir = true }
        )
    }
}