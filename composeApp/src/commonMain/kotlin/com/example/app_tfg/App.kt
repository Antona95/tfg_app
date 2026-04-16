package com.example.app_tfg

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import network.createHttpClient
import repository.AuthRepository
import repository.UserRepository
import repository.SessionRepository

@Composable
fun App() {
    // aqui guardo el estado global del tema de la aplicacion.
    // uso remembersaveable para que no se pierda si rota la pantalla.
    // esta variable vive en la parte mas alta de la app porque el tema afecta
    // a todas las pantallas, no solo a una concreta.
    var isDarkMode by rememberSaveable { mutableStateOf(false) }

    // aqui decido que paleta de colores aplico en funcion del modo actual.
    // si isdarkmode es true uso esquema oscuro.
    // si no, uso esquema claro.
    val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

    // materialtheme envuelve toda la app y aplica colores, tipografias
    // y estilos comunes a todos los composables hijos.
    MaterialTheme(colorScheme = colorScheme) {

        // aqui creo el cliente http una sola vez.
        // uso remember para no reconstruirlo en cada recomposicion.
        val client = remember { createHttpClient() }

        // ahora creo los repositorios por separado, cada uno con su responsabilidad.
        val authRepository = remember { AuthRepository(client) }
        val usuarioRepository = remember { UserRepository(client) }
        val sesionRepository = remember { SessionRepository(client) }

        // aqui delego el resto del trabajo a appcontent.
        AppContent(
            authRepository = authRepository,
            usuarioRepository = usuarioRepository,
            sesionRepository = sesionRepository,
            isDarkMode = isDarkMode,
            onThemeToggle = { isDarkMode = !isDarkMode }
        )
    }
}