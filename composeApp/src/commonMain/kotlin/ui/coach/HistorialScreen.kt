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
import model.SesionEntrenamiento // Asegúrate de que esta ruta sea correcta
import network.EntrenamientoRepository // Asegúrate de que esta ruta sea correcta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    idUsuario: String,
    repository: EntrenamientoRepository,
    onBack: () -> Unit
) {
    // Estados para la carga de datos
    var sesiones by remember { mutableStateOf<List<SesionEntrenamiento>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // LaunchedEffect ejecuta la carga cuando la pantalla se muestra
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
                    text = "No hay sesiones creadas todavía",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sesiones) { sesion ->
                        SesionCard(sesion)
                    }
                }
            }
        }
    }
}

@Composable
fun SesionCard(sesion: SesionEntrenamiento) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = sesion.fechaProgramada,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            // Usamos un título por defecto si no existe o el id
            Text(
                text = "Sesión de Entrenamiento",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "${sesion.ejercicios.size} ejercicios programados",
                style = MaterialTheme.typography.bodyMedium
            )

            if (sesion.finalizada) {
                Spacer(modifier = Modifier.height(8.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text("Completada") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        labelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}