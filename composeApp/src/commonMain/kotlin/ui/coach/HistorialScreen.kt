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
import androidx.compose.ui.unit.dp
import model.SesionEntrenamiento
import network.EntrenamientoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    idUsuario: String,
    repository: EntrenamientoRepository,
    onBack: () -> Unit,
    onSesionClick: (SesionEntrenamiento) -> Unit
) {
    var sesiones by remember { mutableStateOf<List<SesionEntrenamiento>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(idUsuario) {
        sesiones = repository.obtenerHistorialSesiones(idUsuario)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Sesiones") },
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
                Text(
                    text = "No hay sesiones guardadas",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sesiones) { sesion ->
                        // Tarjeta interactiva
                        Card(
                            onClick = { onSesionClick(sesion) }, // <--- DETECTAMOS EL CLICK
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = sesion.fechaProgramada,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if(sesion.finalizada) {
                                        Text("✅", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${sesion.ejercicios.size} ejercicios planificados",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}