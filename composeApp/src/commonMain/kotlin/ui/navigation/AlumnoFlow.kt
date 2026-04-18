package ui.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import model.Persona
import model.SesionEntrenamiento
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
    // aqui creo el viewmodel de la pantalla de hoy.
    //
    // le pongo una key con el id del usuario para evitar que,
    // si cambio de usuario o rehago el flujo, se reutilice un estado que no toca.
    val hoyViewModel = getViewModel(
        key = "hoy-screen-vm-${usuario.id}",
        factory = viewModelFactory { HoyViewModel(sesionRepository) }
    )

    // aqui creo el viewmodel del historial del alumno.
    //
    // igual que antes, uso una key con el id del usuario para que el estado
    // quede asociado a ese alumno y no a otro.
    val historialViewModel = getViewModel(
        key = "historial-screen-vm-${usuario.id}",
        factory = viewModelFactory { HistorialViewModel(sesionRepository) }
    )

    // este estado me dice en qué pantalla del flujo del alumno estoy.
    //
    // uso rememberSaveable porque este dato sí me interesa conservarlo
    // si hay recreación de la interfaz.
    var pantallaAlumno by rememberSaveable { mutableStateOf("MENU") }

    // aqui guardo la sesión que el alumno haya pulsado en el historial
    // para poder abrir su detalle.
    //
    // con remember me vale porque es un estado de navegación local sencillo.
    var sesionDetalleAlumno by remember { mutableStateOf<SesionEntrenamiento?>(null) }

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
            // aqui controlo el botón atrás del dispositivo.
            // si lo pulso, vuelvo al menú principal del alumno.
            BackHandler {
                pantallaAlumno = "MENU"
            }

            HoyScreen(
                idUsuario = usuario.id,
                viewModel = hoyViewModel,
                isDarkMode = isDarkMode,
                onNavigateBack = { pantallaAlumno = "MENU" }
            )
        }

        "HISTORIAL" -> {
            // si estoy en historial y pulso atrás, vuelvo al menú.
            BackHandler {
                pantallaAlumno = "MENU"
            }

            HistorialScreen(
                idUsuario = usuario.id,
                repository = sesionRepository,
                viewModel = historialViewModel,
                isDarkMode = isDarkMode,
                onBack = { pantallaAlumno = "MENU" },
                onSesionClick = { sesion ->
                    // cuando pulso una sesión, la guardo y navego al detalle.
                    sesionDetalleAlumno = sesion
                    pantallaAlumno = "DETALLE"
                }
            )
        }

        "DETALLE" -> {
            // si estoy en detalle y pulso atrás, vuelvo al historial
            // y limpio la sesión seleccionada.
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
                // si por algún motivo no tengo sesión seleccionada,
                // vuelvo al menú como ruta segura.
                pantallaAlumno = "MENU"
            }
        }
    }
}