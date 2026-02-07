package com.example.app_tfg

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import network.createHttpClient       // Asegúrate de importar esto
import network.EntrenamientoRepository // Asegúrate de importar esto
import viewmodel.LoginViewModel
import ui.home.HomeScreen
import ui.login.LoginScreen

@Composable
fun App() {
    MaterialTheme {
        // ---------------------------------------------------------
        // PASO 0: PREPARAR LAS HERRAMIENTAS (LO QUE TE FALTABA)
        // ---------------------------------------------------------

        // 1. Creamos el cliente HTTP con la configuración "flexible" que acabas de hacer
        val client = remember { createHttpClient() }

        // 2. Creamos el Repositorio y le damos el cliente
        val repository = remember { EntrenamientoRepository(client) }


        // ---------------------------------------------------------
        // PASO 1: CREAR EL CEREBRO (VIEWMODEL)
        // ---------------------------------------------------------
        val loginViewModel = getViewModel(
            key = "login-screen",
            factory = viewModelFactory {
                // AQUI ESTA LA CLAVE: Le pasamos el 'repository' al ViewModel
                LoginViewModel(repository)
            }
        )

        // ---------------------------------------------------------
        // PASO 2: UI (PANTALLA)
        // ---------------------------------------------------------
        val state by loginViewModel.uiState.collectAsState()

        if (state.usuarioLogueado != null) {
            HomeScreen(
                usuario = state.usuarioLogueado!!,
                onLogoutClick = {
                    println("APP: El usuario quiere salir")
                    // loginViewModel.cerrarSesion()
                }
            )
        } else {
            // SI NO HEMOS ENTRADO (LoginScreen)
            LoginScreen(
                // 1. Lo que pasa cuando pulsan "Iniciar Sesión"
                onLoginClick = { nickname, pass ->
                    loginViewModel.onLoginClick(nickname, pass)
                },

                // 2. Lo que pasa cuando pulsan "Registrarse"
                onRegistroClick = { nickname, pass, nombre, apellidos ->
                    loginViewModel.onRegistroClick(nickname, pass, nombre, apellidos)
                },

                // 3. Le pasamos el mensaje de éxito si existe en el estado
                mensajeExito = state.mensajeExito
            )
        }
    }
}