package ui.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import model.Persona
import repository.SesionRepository
import repository.UsuarioRepository
import ui.coach.CoachScreen
import ui.coach.DetalleSesionScreen
import ui.coach.HistorialScreen
import ui.coach.NuevaSesionScreen
import ui.coach.UserOptionsScreen
import ui.components.BackHandler
import viewmodel.CoachViewModel
import viewmodel.HistorialViewModel
import viewmodel.SesionViewModel

@Composable
fun CoachFlow(
    usuario: Persona,
    usuarioRepository: UsuarioRepository,
    sesionRepository: SesionRepository,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onLogoutRequest: () -> Unit
) {
    val coachViewModel = getViewModel(
        key = "coach-screen",
        factory = viewModelFactory { CoachViewModel(usuarioRepository) }
    )

    val historialCoachVM = getViewModel(
        key = "historial-coach-vm",
        factory = viewModelFactory { HistorialViewModel(sesionRepository) }
    )

    val sesionVM = getViewModel(
        key = "sesion-vm",
        factory = viewModelFactory { SesionViewModel(sesionRepository) }
    )

    val sesionesAlumno by historialCoachVM.sesiones.collectAsState()

    var usuarioSeleccionado by remember { mutableStateOf<Persona?>(null) }
    var creandoSesion by rememberSaveable { mutableStateOf(false) }
    var viendoHistorial by rememberSaveable { mutableStateOf(false) }
    var sesionSeleccionada by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }
    var sesionParaDuplicar by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

    LaunchedEffect(usuarioSeleccionado) {
        if (usuarioSeleccionado != null) {
            historialCoachVM.limpiarHistorial()
            historialCoachVM.cargarHistorial(usuarioSeleccionado!!.id, forzarRecarga = true)
        }
    }

    when {
        sesionSeleccionada != null -> {
            BackHandler { sesionSeleccionada = null }

            DetalleSesionScreen(
                sesion = sesionSeleccionada!!,
                isDarkMode = isDarkMode,
                onBack = { sesionSeleccionada = null }
            )
        }

        creandoSesion && usuarioSeleccionado != null -> {
            BackHandler {
                creandoSesion = false
                sesionParaDuplicar = null
            }

            NuevaSesionScreen(
                idUsuario = usuarioSeleccionado!!.id,
                viewModel = sesionVM,
                isDarkMode = isDarkMode,
                sesionBase = sesionParaDuplicar,
                onNavigateBack = {
                    creandoSesion = false
                    sesionParaDuplicar = null
                    sesionVM.resetState()
                    historialCoachVM.cargarHistorial(
                        usuarioSeleccionado!!.id,
                        forzarRecarga = true
                    )
                }
            )
        }

        viendoHistorial && usuarioSeleccionado != null -> {
            BackHandler { viendoHistorial = false }

            HistorialScreen(
                idUsuario = usuarioSeleccionado!!.id,
                repository = sesionRepository,
                viewModel = historialCoachVM,
                isDarkMode = isDarkMode,
                onBack = { viendoHistorial = false },
                onSesionClick = { sesion -> sesionSeleccionada = sesion }
            )
        }

        usuarioSeleccionado != null -> {
            BackHandler { usuarioSeleccionado = null }

            UserOptionsScreen(
                usuario = usuarioSeleccionado!!,
                tieneSesiones = sesionesAlumno.isNotEmpty(),
                onBack = { usuarioSeleccionado = null },
                onNuevaSesion = {
                    sesionVM.resetState()
                    sesionParaDuplicar = null
                    creandoSesion = true
                },
                onDuplicarSesion = {
                    sesionVM.prepararDuplicado(usuarioSeleccionado!!.id) { sesion ->
                        if (sesion != null) {
                            sesionParaDuplicar = sesion
                            creandoSesion = true
                        }
                    }
                },
                onVerHistorial = { viendoHistorial = true }
            )
        }

        else -> {
            CoachScreen(
                viewModel = coachViewModel,
                onLogoutClick = onLogoutRequest,
                onAlumnoClick = { alumno -> usuarioSeleccionado = alumno },
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle
            )
        }
    }
}