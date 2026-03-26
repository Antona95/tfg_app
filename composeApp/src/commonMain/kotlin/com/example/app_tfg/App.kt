package com.example.app_tfg

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import ui.components.BackHandler
import androidx.compose.runtime.*
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import network.createHttpClient
import network.EntrenamientoRepository
import viewmodel.LoginViewModel
import viewmodel.CoachViewModel
import viewmodel.HoyViewModel
import viewmodel.HistorialViewModel
import viewmodel.SesionViewModel
import ui.login.LoginScreen
import ui.coach.CoachScreen
import ui.coach.UserOptionsScreen
import ui.coach.NuevaSesionScreen
import ui.coach.HistorialScreen
import ui.coach.DetalleSesionScreen
import ui.user.HoyScreen
import ui.user.AlumnoHomeScreen
import model.Persona
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun App() {
    // aqui guardo si la app esta en modo oscuro o claro.
    // uso remembersaveable para que no se pierda el estado si rota la pantalla.
    var isDarkMode by rememberSaveable { mutableStateOf(false) }

    // este estado me sirve para mostrar o no el cuadro de confirmacion al salir.
    var mostrarDialogoSalir by remember { mutableStateOf(false) }

    // segun el booleano anterior, aplico una paleta de colores u otra.
    val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

    // materialtheme aplica el tema general a toda la interfaz de la app.
    MaterialTheme(colorScheme = colorScheme) {

        // creo el cliente http una sola vez para no reconstruirlo en cada recomposicion.
        val client = remember { createHttpClient() }

        // creo el repositorio central una sola vez.
        // este repositorio es el que habla con la api.
        val repository = remember { EntrenamientoRepository(client) }

        // obtengo el viewmodel del login.
        // lo hago con moko mvvm porque estoy en kotlin multiplatform.
        val loginViewModel = getViewModel(
            key = "login-screen",
            factory = viewModelFactory { LoginViewModel(repository) }
        )

        // observo el estado del login de forma reactiva.
        // cuando cambia, compose recompone la ui.
        val state by loginViewModel.uiState.collectAsState()

        // si el usuario pulsa salir, muestro un dialogo de confirmacion.
        if (mostrarDialogoSalir) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoSalir = false },
                title = {
                    Text("Confirmar salida")
                },
                text = {
                    Text("¿Seguro que quieres salir?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // si confirma, cierro el dialogo y cierro sesion.
                            mostrarDialogoSalir = false
                            loginViewModel.cerrarSesion()
                        }
                    ) {
                        Text("Sí, salir")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { mostrarDialogoSalir = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // si hay un usuario logueado, entro en la app.
        // si no lo hay, me quedo en la pantalla de login.
        if (state.usuarioLogueado != null) {

            // saco el usuario del estado.
            val usuario = state.usuarioLogueado!!

            // aqui separo el flujo segun el rol.
            // si es entrenador, muestro el flujo del coach.
            if (usuario.rol == "ENTRENADOR") {

                // viewmodel principal del coach.
                val coachViewModel = getViewModel(
                    key = "coach-screen",
                    factory = viewModelFactory { CoachViewModel(repository) }
                )

                // viewmodel para el historial del alumno seleccionado.
                val historialCoachVM = getViewModel(
                    key = "historial-coach-vm",
                    factory = viewModelFactory { HistorialViewModel(repository) }
                )

                // viewmodel para crear o duplicar sesiones.
                val sesionVM = getViewModel(
                    key = "sesion-vm",
                    factory = viewModelFactory { SesionViewModel(repository) }
                )

                // observo las sesiones del alumno seleccionado.
                // esto me sirve para saber si tiene historial o no.
                val sesionesAlumno by historialCoachVM.sesiones.collectAsState()

                // estas variables controlan mi navegacion manual.
                // no estoy usando navhost, sino cambios de estado.
                var usuarioSeleccionado by remember { mutableStateOf<Persona?>(null) }
                var creandoSesion by rememberSaveable { mutableStateOf(false) }
                var viendoHistorial by rememberSaveable { mutableStateOf(false) }
                var sesionSeleccionada by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }
                var sesionParaDuplicar by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                // cuando selecciono un alumno, limpio historial previo y cargo el suyo.
                // asi evito mezclar datos de un alumno con otro.
                LaunchedEffect(usuarioSeleccionado) {
                    if (usuarioSeleccionado != null) {
                        historialCoachVM.limpiarHistorial()
                        historialCoachVM.cargarHistorial(usuarioSeleccionado!!.id, forzarRecarga = true)
                    }
                }

                // aqui hago una especie de maquina de estados para navegar entre pantallas del coach.
                when {

                    // si hay una sesion concreta seleccionada, entro al detalle.
                    sesionSeleccionada != null -> {

                        // capturo el boton fisico de atras para volver atras dentro de mi flujo.
                        BackHandler { sesionSeleccionada = null }

                        DetalleSesionScreen(
                            sesion = sesionSeleccionada!!,
                            isDarkMode = isDarkMode,
                            onBack = { sesionSeleccionada = null }
                        )
                    }

                    // si estoy creando sesion, entro a la pantalla de nueva sesion.
                    creandoSesion && usuarioSeleccionado != null -> {

                        // si el usuario pulsa atras, salgo de crear sesion y limpio la sesion base duplicada.
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
                                // al volver, cierro la pantalla de creacion
                                // y recargo historial por si se ha creado una sesion nueva.
                                creandoSesion = false
                                sesionParaDuplicar = null
                                historialCoachVM.cargarHistorial(
                                    usuarioSeleccionado!!.id,
                                    forzarRecarga = true
                                )
                            }
                        )
                    }

                    // si estoy viendo historial, muestro el historial del alumno seleccionado.
                    viendoHistorial && usuarioSeleccionado != null -> {

                        // el boton atras vuelve al menu de opciones del alumno.
                        BackHandler { viendoHistorial = false }

                        HistorialScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            repository = repository,
                            viewModel = historialCoachVM,
                            isDarkMode = isDarkMode,
                            onBack = { viendoHistorial = false },
                            onSesionClick = { sesion -> sesionSeleccionada = sesion }
                        )
                    }

                    // si hay un alumno seleccionado pero no estoy en detalle, ni creando, ni en historial,
                    // muestro la pantalla intermedia de opciones de ese alumno.
                    usuarioSeleccionado != null -> {

                        // el boton atras quita el alumno seleccionado y me devuelve a la lista.
                        BackHandler { usuarioSeleccionado = null }

                        UserOptionsScreen(
                            usuario = usuarioSeleccionado!!,
                            tieneSesiones = sesionesAlumno.isNotEmpty(),
                            onBack = { usuarioSeleccionado = null },
                            onNuevaSesion = {
                                // si creo una sesion desde cero, limpio la base de duplicado.
                                sesionParaDuplicar = null
                                creandoSesion = true
                            },
                            onDuplicarSesion = {
                                // aqui pido al viewmodel la ultima sesion para usarla como plantilla.
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

                    // si no estoy en ninguna de las pantallas anteriores,
                    // muestro la principal del coach.
                    else -> {
                        CoachScreen(
                            viewModel = coachViewModel,
                            onLogoutClick = { mostrarDialogoSalir = true },
                            onAlumnoClick = { alumno -> usuarioSeleccionado = alumno },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                    }
                }

            } else {

                // si no es entrenador, entro al flujo del alumno.
                val hoyViewModel = getViewModel(
                    key = "hoy-screen-vm",
                    factory = viewModelFactory { HoyViewModel(repository) }
                )

                val historialViewModel = getViewModel(
                    key = "historial-screen-vm",
                    factory = viewModelFactory { HistorialViewModel(repository) }
                )

                // aqui uso un string para controlar la pantalla actual del alumno.
                var pantallaAlumno by rememberSaveable { mutableStateOf("MENU") }

                // aqui guardo la sesion elegida cuando entra al detalle desde historial.
                var sesionDetalleAlumno by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                when (pantallaAlumno) {

                    // pantalla principal del alumno.
                    "MENU" -> AlumnoHomeScreen(
                        usuario = usuario,
                        onVerHoy = { pantallaAlumno = "HOY" },
                        onVerHistorial = { pantallaAlumno = "HISTORIAL" },
                        onLogout = { mostrarDialogoSalir = true },
                        isDarkMode = isDarkMode,
                        onThemeToggle = { isDarkMode = !isDarkMode }
                    )

                    // pantalla del entrenamiento actual del alumno.
                    "HOY" -> {

                        // al pulsar atras vuelvo al menu del alumno.
                        BackHandler { pantallaAlumno = "MENU" }

                        HoyScreen(
                            idUsuario = usuario.id,
                            viewModel = hoyViewModel,
                            isDarkMode = isDarkMode,
                            onNavigateBack = { pantallaAlumno = "MENU" }
                        )
                    }

                    // pantalla del historial del alumno.
                    "HISTORIAL" -> {

                        // al pulsar atras vuelvo al menu.
                        BackHandler { pantallaAlumno = "MENU" }

                        HistorialScreen(
                            idUsuario = usuario.id,
                            repository = repository,
                            viewModel = historialViewModel,
                            isDarkMode = isDarkMode,
                            onBack = { pantallaAlumno = "MENU" },
                            onSesionClick = { sesion ->
                                // guardo la sesion pulsada y navego a detalle.
                                sesionDetalleAlumno = sesion
                                pantallaAlumno = "DETALLE"
                            }
                        )
                    }

                    // pantalla de detalle de una sesion del alumno.
                    "DETALLE" -> {

                        // al pulsar atras desde detalle, vuelvo al historial.
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
                            // esto lo pongo por seguridad.
                            // si por alguna razon el detalle es nulo, vuelvo al menu.
                            pantallaAlumno = "MENU"
                        }
                    }
                }
            }
        } else {

            // si no hay usuario logueado, muestro login y registro.
            LoginScreen(
                isLoading = state.isLoading,
                onLoginClick = { nick, pass -> loginViewModel.onLoginClick(nick, pass) },
                onRegistroClick = { nick, pass, nom, ape ->
                    loginViewModel.onRegistroClick(nick, pass, nom, ape)
                },
                mensajeExito = state.mensajeExito,
                errorBackend = state.error,
                isDarkMode = isDarkMode,
                onThemeToggle = { isDarkMode = !isDarkMode }
            )
        }
    }
}