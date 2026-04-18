package ui.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import model.Persona
import model.SesionEntrenamiento
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
    // aqui creo el viewmodel principal del coach.
    //
    // le pongo una key con el id del usuario logueado para evitar
    // reutilizar estado si cambiara el usuario de sesión.
    val coachViewModel = getViewModel(
        key = "coach-screen-${usuario.id}",
        factory = viewModelFactory { CoachViewModel(usuarioRepository) }
    )

    // aqui creo el viewmodel del historial que uso dentro del flujo del coach.
    //
    // también lo asocio al id del usuario actual para mantener consistencia.
    val historialCoachVM = getViewModel(
        key = "historial-coach-vm-${usuario.id}",
        factory = viewModelFactory { HistorialViewModel(sesionRepository) }
    )

    // este viewmodel controla la creación y edición de sesiones.
    val sesionVM = getViewModel(
        key = "sesion-vm-${usuario.id}",
        factory = viewModelFactory { SesionViewModel(sesionRepository) }
    )

    // aqui observo las sesiones del alumno seleccionado.
    val sesionesAlumno by historialCoachVM.sesiones.collectAsState()

    // aqui guardo el alumno que el coach ha seleccionado.
    var usuarioSeleccionado by remember { mutableStateOf<Persona?>(null) }

    // este booleano me dice si estoy en la pantalla de crear sesión.
    var creandoSesion by rememberSaveable { mutableStateOf(false) }

    // este booleano me dice si estoy viendo el historial del alumno.
    var viendoHistorial by rememberSaveable { mutableStateOf(false) }

    // aqui guardo la sesión concreta que quiero abrir en detalle.
    var sesionSeleccionada by remember { mutableStateOf<SesionEntrenamiento?>(null) }

    // aqui guardo una posible sesión base para duplicarla.
    var sesionParaDuplicar by remember { mutableStateOf<SesionEntrenamiento?>(null) }

    // cada vez que cambia el alumno seleccionado, recargo su historial.
    //
    // antes limpio el historial para evitar que se vean datos del alumno anterior
    // durante un instante.
    LaunchedEffect(usuarioSeleccionado) {
        if (usuarioSeleccionado != null) {
            historialCoachVM.limpiarHistorial()
            historialCoachVM.cargarHistorial(
                usuarioSeleccionado!!.id,
                forzarRecarga = true
            )
        }
    }

    when {
        sesionSeleccionada != null -> {
            // si estoy en detalle de sesión, el botón atrás cierra el detalle.
            BackHandler {
                sesionSeleccionada = null
            }

            DetalleSesionScreen(
                sesion = sesionSeleccionada!!,
                isDarkMode = isDarkMode,
                onBack = { sesionSeleccionada = null }
            )
        }

        creandoSesion && usuarioSeleccionado != null -> {
            // si estoy creando una sesión, el botón atrás cancela esta pantalla
            // y limpia la posible sesión base de duplicado.
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
                    // al volver atrás, cierro la pantalla de creación,
                    // limpio la sesión base y reseteo el viewmodel.
                    creandoSesion = false
                    sesionParaDuplicar = null
                    sesionVM.resetState()

                    // además recargo el historial por si se ha creado o duplicado una sesión.
                    historialCoachVM.cargarHistorial(
                        usuarioSeleccionado!!.id,
                        forzarRecarga = true
                    )
                }
            )
        }

        viendoHistorial && usuarioSeleccionado != null -> {
            // si estoy viendo el historial, el botón atrás me devuelve
            // a la pantalla de opciones del usuario.
            BackHandler {
                viendoHistorial = false
            }

            HistorialScreen(
                idUsuario = usuarioSeleccionado!!.id,
                repository = sesionRepository,
                viewModel = historialCoachVM,
                isDarkMode = isDarkMode,
                onBack = { viendoHistorial = false },
                onSesionClick = { sesion ->
                    sesionSeleccionada = sesion
                }
            )
        }

        usuarioSeleccionado != null -> {
            // si estoy en la pantalla de opciones de un alumno,
            // el botón atrás me devuelve al listado general del coach.
            BackHandler {
                usuarioSeleccionado = null
            }

            UserOptionsScreen(
                usuario = usuarioSeleccionado!!,
                tieneSesiones = sesionesAlumno.isNotEmpty(),
                onBack = { usuarioSeleccionado = null },
                onNuevaSesion = {
                    // cuando creo una sesión desde cero,
                    // limpio el estado anterior del viewmodel y elimino sesión base.
                    sesionVM.resetState()
                    sesionParaDuplicar = null
                    creandoSesion = true
                },
                onDuplicarSesion = {
                    // aqui pido preparar el duplicado de la última sesión del alumno.
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
            // esta es la pantalla principal del coach.
            CoachScreen(
                viewModel = coachViewModel,
                onLogoutClick = onLogoutRequest,
                onAlumnoClick = { alumno ->
                    usuarioSeleccionado = alumno
                },
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle
            )
        }
    }
}