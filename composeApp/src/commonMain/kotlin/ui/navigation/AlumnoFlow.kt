package ui.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import model.Persona
import repository.SesionRepository
import ui.coach.DetalleSesionScreen
import ui.coach.HistorialScreen
import ui.components.BackHandler
import ui.user.AlumnoHomeScreen
import ui.user.HoyScreen
import viewmodel.HistorialViewModel
import viewmodel.HoyViewModel

@Composable
fun AlumnoFlow(
    usuario: Persona,
    sesionRepository: SesionRepository,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onLogoutRequest: () -> Unit
) {
    val hoyViewModel = getViewModel(
        key = "hoy-screen-vm",
        factory = viewModelFactory { HoyViewModel(sesionRepository) }
    )

    val historialViewModel = getViewModel(
        key = "historial-screen-vm",
        factory = viewModelFactory { HistorialViewModel(sesionRepository) }
    )

    var pantallaAlumno by rememberSaveable { mutableStateOf("MENU") }
    var sesionDetalleAlumno by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

    when (pantallaAlumno) {
        "MENU" -> {
            AlumnoHomeScreen(
                usuario = usuario,
                onVerHoy = { pantallaAlumno = "HOY" },
                onVerHistorial = { pantallaAlumno = "HISTORIAL" },
                onLogout = onLogoutRequest,
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle
            )
        }

        "HOY" -> {
            BackHandler { pantallaAlumno = "MENU" }

            HoyScreen(
                idUsuario = usuario.id,
                viewModel = hoyViewModel,
                isDarkMode = isDarkMode,
                onNavigateBack = { pantallaAlumno = "MENU" }
            )
        }

        "HISTORIAL" -> {
            BackHandler { pantallaAlumno = "MENU" }

            HistorialScreen(
                idUsuario = usuario.id,
                repository = sesionRepository,
                viewModel = historialViewModel,
                isDarkMode = isDarkMode,
                onBack = { pantallaAlumno = "MENU" },
                onSesionClick = { sesion ->
                    sesionDetalleAlumno = sesion
                    pantallaAlumno = "DETALLE"
                }
            )
        }

        "DETALLE" -> {
            BackHandler {
                pantallaAlumno = "HISTORIAL"
                sesionDetalleAlumno = null
            }

            if (sesionDetalleAlumno != null) {
                DetalleSesionScreen(
                    sesion = sesionDetalleAlumno!!,
                    isDarkMode = isDarkMode,
                    onBack = {
                        pantallaAlumno = "HISTORIAL"
                        sesionDetalleAlumno = null
                    }
                )
            } else {
                pantallaAlumno = "MENU"
            }
        }
    }
}