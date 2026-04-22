package com.example.app_tfg

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import network.createHttpClient
import repository.AuthRepository
import repository.UsuarioRepository
import repository.SesionRepository

@Composable
fun App() {
    // aqui guardo el estado global del tema de la aplicacion.
    // uso rememberSaveable para que no se pierda si rota la pantalla.
    // esta variable vive en la parte mas alta de la app porque el tema afecta
    // a todas las pantallas, no solo a una concreta.
    var isDarkMode by rememberSaveable { mutableStateOf(false) }

    // aqui defino mis propios esquemas de color en vez de usar
    // los que trae material por defecto.
    //
    // esto es importante porque antes estaba llamando a:
    // darkColorScheme() y lightColorScheme() sin pasar colores.
    //
    // eso hacia que gran parte del aspecto real de la app
    // siguiera dependiendo del tema estandar de material 3,
    // y por eso parecia que mis cambios en ColoresApp apenas se notaban.
    val darkScheme = darkColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF8AB4FF),
        onPrimary = androidx.compose.ui.graphics.Color(0xFF0F1B2D),

        secondary = androidx.compose.ui.graphics.Color(0xFFD7D0E0),
        onSecondary = androidx.compose.ui.graphics.Color(0xFF1E1B26),

        background = androidx.compose.ui.graphics.Color(0xFF15121C),
        onBackground = androidx.compose.ui.graphics.Color(0xFFF3F2F7),

        surface = androidx.compose.ui.graphics.Color(0xFF1D1A24),
        onSurface = androidx.compose.ui.graphics.Color(0xFFF3F2F7),

        // este color afecta mucho a tarjetas, topbars y elementos de apoyo.
        // lo separo mejor del fondo para que en oscuro no quede todo "empastado".
        surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2A2633),
        onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFE3DDEA),

        secondaryContainer = androidx.compose.ui.graphics.Color(0xFF312C3D),
        onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFF0EBF6),

        error = androidx.compose.ui.graphics.Color(0xFFFFB4AB),
        onError = androidx.compose.ui.graphics.Color(0xFF690005)
    )

    val lightScheme = lightColorScheme(
        // aqui recupero un primario morado para que el tema claro
        // vuelva a tener una identidad mas parecida a la que te gustaba antes.
        primary = androidx.compose.ui.graphics.Color(0xFF6750A4),
        onPrimary = androidx.compose.ui.graphics.Color.White,

        secondary = androidx.compose.ui.graphics.Color(0xFF7B61A8),
        onSecondary = androidx.compose.ui.graphics.Color.White,

        background = androidx.compose.ui.graphics.Color(0xFFFFFBFE),
        onBackground = androidx.compose.ui.graphics.Color(0xFF1C1B1F),

        surface = androidx.compose.ui.graphics.Color(0xFFFFFBFE),
        onSurface = androidx.compose.ui.graphics.Color(0xFF1C1B1F),

        // este surfaceVariant lo dejo en una gama lila suave
        // para que tarjetas y superficies secundarias no tiren a azul.
        surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE8DEF8),
        onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF49454F),

        secondaryContainer = androidx.compose.ui.graphics.Color(0xFFE8DEF8),
        onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF1D192B),

        error = androidx.compose.ui.graphics.Color(0xFFB3261E),
        onError = androidx.compose.ui.graphics.Color.White
    )

    // aqui decido que paleta de colores aplico en funcion del modo actual.
    // si isDarkMode es true uso esquema oscuro.
    // si no, uso esquema claro.
    val colorScheme = if (isDarkMode) darkScheme else lightScheme

    // materialtheme envuelve toda la app y aplica colores, tipografias
    // y estilos comunes a todos los composables hijos.
    MaterialTheme(colorScheme = colorScheme) {

        // aqui creo el cliente http una sola vez.
        // uso remember para no reconstruirlo en cada recomposicion.
        val client = remember { createHttpClient() }

        // ahora creo los repositorios por separado, cada uno con su responsabilidad.
        val authRepository = remember { AuthRepository(client) }
        val usuarioRepository = remember { UsuarioRepository(client) }
        val sesionRepository = remember { SesionRepository(client) }

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