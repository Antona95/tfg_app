package ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import model.SesionEntrenamiento
import network.EntrenamientoRepository
import viewmodel.HistorialViewModel
import ui.components.PantallaCargando
import ui.components.PantallaVacia
import ui.components.SesionResumenCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    idUsuario: String,
    repository: EntrenamientoRepository,
    viewModel: HistorialViewModel? = null,
    isDarkMode: Boolean, // <--- AÑADIDO PARA PASÁRSELO A LA TARJETA
    onBack: () -> Unit,
    onSesionClick: (SesionEntrenamiento) -> Unit
) {
    val historialVM = viewModel ?: getViewModel(
        key = "historial-$idUsuario",
        factory = viewModelFactory { HistorialViewModel(repository) }
    )

    val sesiones by historialVM.sesiones.collectAsState()
    val isLoading by historialVM.isLoading.collectAsState()

    LaunchedEffect(idUsuario) {
        historialVM.cargarHistorial(idUsuario, forzarRecarga = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Sesiones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            // USAMOS LOS NUEVOS COMPONENTES DE ESTADO
            if (isLoading && sesiones.isEmpty()) {
                PantallaCargando()
            } else if (!isLoading && sesiones.isEmpty()) {
                PantallaVacia(icono = "📭", mensaje = "No hay sesiones registradas")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sesiones) { sesion ->
                        // USAMOS LA NUEVA TARJETA REUTILIZABLE
                        SesionResumenCard(
                            sesion = sesion,
                            isDarkMode = isDarkMode,
                            onClick = { onSesionClick(sesion) }
                        )
                    }
                }
            }
        }
    }
}