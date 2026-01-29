package com.example.app_tfg

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ui.login.LoginScreen // Importamos la pantalla de login
import ui.home.HomeScreen
import androidx.compose.runtime.collectAsState // Importante
import androidx.compose.runtime.getValue
import dev.icerock.moko.mvvm.compose.getViewModel // Importante (Moko)
import dev.icerock.moko.mvvm.compose.viewModelFactory // Importante (Moko)
import viewmodel.LoginViewModel


@Composable
fun App() {
    MaterialTheme {
        // 1. CREAMOS EL VIEWMODEL
        // Esto crea el cerebro y hace que sobreviva si giras la pantalla
        val loginViewModel = getViewModel(
            key = "login-screen",
            factory = viewModelFactory { LoginViewModel() }
        )

        // 2. ESCUCHAMOS EL ESTADO
        // 'state' se actualizará automáticamente cuando cambie algo en el ViewModel
        val state by loginViewModel.uiState.collectAsState()

        if (state.usuarioLogueado != null) {
            // SI YA HEMOS ENTRADO (state tiene datos) -> Mostramos la pantalla HOME
            // El '!!' es seguro aquí porque el if ya comprobó que no es null
            HomeScreen(usuario = state.usuarioLogueado!!)

        } else {
            // SI NO HEMOS ENTRADO (es null) -> Mostramos la pantalla LOGIN
            LoginScreen(
                onLoginClick = { dni, pass ->
                    loginViewModel.onLoginClick(dni, pass)
                }
            )

            // ver errores por consola mientras pruebas
            if (state.error != null) {
                println("Error de Login: ${state.error}")
            }
        }
    }
}

