package com.example.app_tfg

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import network.createHttpClient
import network.EntrenamientoRepository
import viewmodel.LoginViewModel
import ui.login.LoginScreen
import viewmodel.CoachViewModel
import ui.coach.CoachScreen
import ui.coach.UserOptionsScreen
import ui.coach.NuevaSesionScreen
import ui.coach.HistorialScreen
import ui.user.HoyScreen
import model.Persona
import kotlinx.coroutines.launch // IMPORTANTE: Necesario para lanzar la petición asíncrona
import ui.user.AlumnoHomeScreen
import ui.user.HoyScreen
import ui.coach.HistorialScreen
import ui.coach.DetalleSesionScreen

@Composable
fun App() {
    MaterialTheme {
        val client = remember { createHttpClient() }
        val repository = remember { EntrenamientoRepository(client) }
        val scope = rememberCoroutineScope() // IMPORTANTE: Para llamar al repo desde botones

        val loginViewModel = getViewModel(
            key = "login-screen",
            factory = viewModelFactory { LoginViewModel(repository) }
        )

        val state by loginViewModel.uiState.collectAsState()

        if (state.usuarioLogueado != null) {
            val usuario = state.usuarioLogueado!!

            if (usuario.rol == "ENTRENADOR") {
                var usuarioSeleccionado by remember { mutableStateOf<Persona?>(null) }
                var creandoSesion by remember { mutableStateOf(false) }
                var viendoHistorial by remember { mutableStateOf(false) }

                // Estado para ver detalle (solo lectura)
                var sesionSeleccionada by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                // --- NUEVO ESTADO: Sesión para usar como plantilla (copiar) ---
                var sesionParaDuplicar by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                when {
                    // 1. NIVEL MÁS ALTO: DETALLE DE SESIÓN (Solo lectura)
                    sesionSeleccionada != null -> {
                        ui.coach.DetalleSesionScreen(
                            sesion = sesionSeleccionada!!,
                            onBack = { sesionSeleccionada = null }
                        )
                    }

                    // 2. FORMULARIO DE CREACIÓN (Nueva o Copia)
                    creandoSesion && usuarioSeleccionado != null -> {
                        NuevaSesionScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            repository = repository,
                            // Pasamos la sesión a copiar (será null si es desde cero)
                            sesionBase = sesionParaDuplicar,
                            onNavigateBack = {
                                creandoSesion = false
                                sesionParaDuplicar = null // Limpiamos al salir
                            }
                        )
                    }

                    // 3. LISTADO DE HISTORIAL
                    viendoHistorial && usuarioSeleccionado != null -> {
                        ui.coach.HistorialScreen(
                            idUsuario = usuarioSeleccionado!!.id,
                            repository = repository,
                            onBack = { viendoHistorial = false },
                            onSesionClick = { sesion ->
                                sesionSeleccionada = sesion
                            }
                        )
                    }

                    // 4. MENÚ DE OPCIONES DEL ALUMNO
                    usuarioSeleccionado != null -> {
                        UserOptionsScreen(
                            usuario = usuarioSeleccionado!!,
                            onBack = { usuarioSeleccionado = null },

                            // A) NUEVA SESIÓN VACÍA
                            onNuevaSesion = {
                                sesionParaDuplicar = null
                                creandoSesion = true
                            },

                            // B) COPIAR ÚLTIMA SESIÓN
                            onDuplicarSesion = {
                                scope.launch {
                                    // 1. Pedimos historial al backend
                                    val historial = repository.obtenerHistorialSesiones(usuarioSeleccionado!!.id)

                                    if (historial.isNotEmpty()) {
                                        // 2. Cogemos la primera (la más reciente)
                                        sesionParaDuplicar = historial.first()
                                        // 3. Abrimos la pantalla de creación
                                        creandoSesion = true
                                    } else {
                                        // Aquí podrías poner un snackbar si quisieras avisar
                                        println("No hay sesiones previas para copiar")
                                    }
                                }
                            },

                            onVerHistorial = { viendoHistorial = true }
                        )
                    }

                    // 5. LISTA DE ALUMNOS (Default)
                    else -> {
                        val coachViewModel = getViewModel(
                            key = "coach-screen",
                            factory = viewModelFactory { CoachViewModel(repository) }
                        )
                        CoachScreen(
                            viewModel = coachViewModel,
                            onLogoutClick = { loginViewModel.cerrarSesion() },
                            onAlumnoClick = { alumno ->
                                usuarioSeleccionado = alumno
                            }
                        )
                    }
                }

            } else {
                // CASO ALUMNO: NAVEGACIÓN PROPIA

                // Variables de estado para navegar dentro del perfil de Alumno
                var pantallaAlumno by remember { mutableStateOf("MENU") }
                var sesionDetalleAlumno by remember { mutableStateOf<model.SesionEntrenamiento?>(null) }

                when (pantallaAlumno) {
                    // 1. MENÚ PRINCIPAL
                    "MENU" -> {
                        ui.user.AlumnoHomeScreen(
                            usuario = usuario,
                            onVerHoy = { pantallaAlumno = "HOY" },
                            onVerHistorial = { pantallaAlumno = "HISTORIAL" },
                            onLogout = { loginViewModel.cerrarSesion() }
                        )
                    }

                    // 2. PANTALLA DE HOY
                    "HOY" -> {
                        ui.user.HoyScreen(
                            idUsuario = usuario.id,
                            repository = repository,
                            onNavigateBack = { pantallaAlumno = "MENU" } // Volver al menú
                        )
                    }

                    // 3. HISTORIAL (Reutilizamos la del Coach)
                    "HISTORIAL" -> {
                        ui.coach.HistorialScreen(
                            idUsuario = usuario.id,
                            repository = repository,
                            onBack = { pantallaAlumno = "MENU" },
                            onSesionClick = { sesion ->
                                sesionDetalleAlumno = sesion
                                pantallaAlumno = "DETALLE"
                            }
                        )
                    }

                    // 4. DETALLE DE SESIÓN ANTIGUA (Reutilizamos la del Coach)
                    "DETALLE" -> {
                        if (sesionDetalleAlumno != null) {
                            ui.coach.DetalleSesionScreen(
                                sesion = sesionDetalleAlumno!!,
                                onBack = {
                                    pantallaAlumno = "HISTORIAL"
                                    sesionDetalleAlumno = null
                                }
                            )
                        } else {
                            pantallaAlumno = "MENU" // Fallback por si acaso
                        }
                    }
                }
            }

        } else {
            // PANTALLA DE LOGIN / REGISTRO (Esto sigue igual fuera del if/else principal)
            LoginScreen(
                onLoginClick = { nick, pass -> loginViewModel.onLoginClick(nick, pass) },
                onRegistroClick = { nick, pass, nom, ape -> loginViewModel.onRegistroClick(nick, pass, nom, ape) },
                mensajeExito = state.mensajeExito
            )
        }
    }
}