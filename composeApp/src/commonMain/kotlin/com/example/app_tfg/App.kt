package com.example.app_tfg
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ui.login.LoginScreen // Importamos la pantalla que creaste

@Composable
fun App() {
    MaterialTheme {
        // Llamamos a tu pantalla de Login
        LoginScreen(
            onLoginClick = { dni, password ->
                // Aquí pondremos la lógica de conexión más adelante.
                // De momento, esto nos sirve para saber que el botón funciona.
                println("Usuario pulsó entrar con DNI: $dni")
            }
        )
    }
}