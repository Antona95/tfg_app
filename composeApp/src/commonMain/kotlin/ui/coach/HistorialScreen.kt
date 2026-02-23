package ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.SesionEntrenamiento
import network.EntrenamientoRepository
import viewmodel.HistorialViewModel // Importa tu ViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    idUsuario: String,
    repository: EntrenamientoRepository,
    viewModel: HistorialViewModel? = null, // PARÁMETRO NUEVO OPCIONAL
    onBack: () -> Unit,
    onSesionClick: (SesionEntrenamiento) -> Unit
) {
    // Si pasamos un ViewModel (Alumnos), usamos su estado. Si no (Coach), usamos estado local.
    val sesionesState = viewModel?.sesiones?.collectAsState()
    val isLoadingState = viewModel?.isLoading?.collectAsState()

    var sesionesLocal by remember { mutableStateOf<List<SesionEntrenamiento>>(emptyList()) }
    var isLoadingLocal by remember { mutableStateOf(true) }

    val sesiones = sesionesState?.value ?: sesionesLocal
    val isLoading = isLoadingState?.value ?: isLoadingLocal

    LaunchedEffect(idUsuario) {
        if (viewModel != null) {
            viewModel.cargarHistorial(idUsuario)
        } else {
            sesionesLocal = repository.obtenerHistorialSesiones(idUsuario)
            isLoadingLocal = false
        }
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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (sesiones.isEmpty()) {
                Text("No hay sesiones registradas", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sesiones) { sesion ->
                        Card(
                            onClick = { onSesionClick(sesion) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sesion.fechaProgramada,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if(sesion.finalizada) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    } else {
                                        Icon(Icons.Default.PendingActions, contentDescription = "Pendiente")
                                    }
                                }
                                Text("${sesion.ejercicios.size} ejercicios planificados")
                            }
                        }
                    }
                }
            }
        }
    }
}