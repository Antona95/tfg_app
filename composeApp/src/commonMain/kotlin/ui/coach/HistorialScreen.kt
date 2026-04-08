package ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import model.SesionEntrenamiento
import repository.SesionRepository
import viewmodel.HistorialViewModel
import ui.components.PantallaCargando
import ui.components.PantallaVacia
import ui.components.SesionResumenCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    idUsuario: String,
    repository: SesionRepository,
    viewModel: HistorialViewModel? = null,
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onSesionClick: (SesionEntrenamiento) -> Unit
) {
    // aqui preparo el viewmodel del historial.
    // si me lo pasan desde fuera, uso ese.
    // si no, lo creo aqui con moko mvvm.
    val historialVM = viewModel ?: getViewModel(
        key = "historial-$idUsuario",
        factory = viewModelFactory { HistorialViewModel(repository) }
    )

    // observo la lista de sesiones del historial.
    val sesiones by historialVM.sesiones.collectAsState()

    // observo si hay carga activa.
    val isLoading by historialVM.isLoading.collectAsState()

    // observo si ha ocurrido un error de red o del backend.
    val error by historialVM.error.collectAsState()

    // cada vez que cambia el id del usuario, recargo el historial.
    // esto me asegura que si entro a otro alumno, no me quede con datos viejos.
    LaunchedEffect(idUsuario) {
        historialVM.cargarHistorial(idUsuario, forzarRecarga = true)
    }

    // scaffold me da la estructura base con topbar y contenido principal.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Sesiones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    // este boton me devuelve a la pantalla anterior.
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        // este box ocupa todo el contenido bajo la barra superior.
        Box(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {

            // si hay error, doy prioridad a mostrar una pantalla de error amigable.
            if (error != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // icono visual para indicar fallo de conexion o problema de red.
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Sin Red",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // muestro el mensaje de error exacto que llega del viewmodel.
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // este boton permite reintentar la carga sin salir de la pantalla.
                    Button(onClick = { historialVM.cargarHistorial(idUsuario, true) }) {
                        Text("Reintentar conexión")
                    }
                }
            }

            // si esta cargando y aun no tengo sesiones, muestro la pantalla de carga.
            else if (isLoading && sesiones.isEmpty()) {
                PantallaCargando()
            }

            // si ya no carga y no hay sesiones, muestro una pantalla vacia.
            else if (!isLoading && sesiones.isEmpty()) {
                PantallaVacia(icono = "📭", mensaje = "No hay sesiones registradas")
            }

            // si no hay error y si tengo sesiones, muestro la lista del historial.
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // recorro todas las sesiones y pinto una tarjeta resumen para cada una.
                    items(sesiones) { sesion ->
                        SesionResumenCard(
                            sesion = sesion,

                            // paso el modo oscuro porque la tarjeta adapta algunos colores segun el tema.
                            isDarkMode = isDarkMode,

                            // al pulsar una sesion, aviso a la pantalla padre para ir al detalle.
                            onClick = { onSesionClick(sesion) }
                        )
                    }
                }
            }
        }
    }
}