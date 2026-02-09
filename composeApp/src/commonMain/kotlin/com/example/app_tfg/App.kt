package com.example.app_tfg

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import network.createHttpClient
import network.EntrenamientoRepository
import viewmodel.LoginViewModel
import ui.home.HomeScreen
import ui.login.LoginScreen
// Importamos los componentes del Entrenador que acabamos de crear
import viewmodel.CoachViewModel
import ui.coach.CoachScreen

@Composable
fun App() {
    MaterialTheme {
        // ---------------------------------------------------------
        // 1. CONFIGURACIÓN DE LA CAPA DE DATOS (HERRAMIENTAS)
        // ---------------------------------------------------------

        // Creamos el cliente HTTP una sola vez usando 'remember'.
        // Esto evita que se cree un cliente nuevo cada vez que la pantalla se redibuja.
        val client = remember { createHttpClient() }

        // Inicializamos el Repositorio, que es quien sabe hablar con el Backend.
        // Le pasamos el cliente HTTP para que pueda hacer las peticiones.
        val repository = remember { EntrenamientoRepository(client) }


        // ---------------------------------------------------------
        // 2. VIEWMODEL PRINCIPAL (LOGIN Y ESTADO GLOBAL)
        // ---------------------------------------------------------

        // Usamos getViewModel con una 'factory' para poder pasarle el repositorio
        // al constructor del LoginViewModel.
        val loginViewModel = getViewModel(
            key = "login-screen",
            factory = viewModelFactory {
                LoginViewModel(repository)
            }
        )

        // ---------------------------------------------------------
        // 3. OBSERVAMOS EL ESTADO (UI STATE)
        // ---------------------------------------------------------

        // collectAsState convierte el flujo de datos del ViewModel en un Estado de Compose.
        // Cada vez que 'uiState' cambie, esta función App() se volverá a ejecutar (recomposición)
        // para mostrar la pantalla correcta.
        val state by loginViewModel.uiState.collectAsState()


        // ---------------------------------------------------------
        // 4. LÓGICA DE NAVEGACIÓN Y ROLES
        // ---------------------------------------------------------

        // PRIMERA COMPROBACIÓN: ¿Tenemos un usuario logueado en memoria?
        if (state.usuarioLogueado != null) {

            // Forzamos el desempaquetado (!!) porque ya comprobamos que no es nulo arriba
            val usuario = state.usuarioLogueado!!

            // SEGUNDA COMPROBACIÓN: ¿Qué ROL tiene este usuario?
            if (usuario.rol == "ENTRENADOR") {

                // A) CASO ENTRENADOR: Mostramos el Panel de Gestión

                // 1. Instanciamos el ViewModel específico para el Entrenador.
                // Es importante usar una 'key' diferente ("coach-screen") para que no se mezcle.
                val coachViewModel = getViewModel(
                    key = "coach-screen",
                    factory = viewModelFactory {
                        CoachViewModel(repository)
                    }
                )

                // 2. Mostramos la pantalla visual (CoachScreen).
                // Conectamos los eventos (clics) con la lógica.
                CoachScreen(
                    viewModel = coachViewModel,

                    onLogoutClick = {
                        // Aquí iría la lógica para cerrar sesión (limpiar estado)
                        // loginViewModel.cerrarSesion()
                    },

                    onAlumnoClick = { alumnoSeleccionado ->
                        // Este bloque se ejecuta cuando tocas una tarjeta de alumno.
                        // Imprimimos por consola para verificar que funciona.
                        // En el futuro, aquí navegaremos a la pantalla de "Crear Rutina".
                        println("Navegación: Has seleccionado al alumno ${alumnoSeleccionado.nombre} (ID: ${alumnoSeleccionado.id})")
                    }
                )

            } else {

                // B) CASO USUARIO NORMAL: Mostramos la pantalla de sus rutinas
                HomeScreen(
                    usuario = usuario,
                    repository = repository,
                    onLogoutClick = {
                        println("APP: El usuario quiere salir")
                        // loginViewModel.cerrarSesion()
                    }
                )
            }

        } else {

            // C) CASO NO LOGUEADO: Mostramos la pantalla de Login/Registro
            LoginScreen(
                onLoginClick = { nickname, pass ->
                    // Delegamos la lógica al ViewModel
                    loginViewModel.onLoginClick(nickname, pass)
                },
                onRegistroClick = { nickname, pass, nombre, apellidos ->
                    // Delegamos el registro al ViewModel
                    loginViewModel.onRegistroClick(nickname, pass, nombre, apellidos)
                },
                mensajeExito = state.mensajeExito
            )
        }
    }
}