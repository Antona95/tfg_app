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
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import model.SesionEntrenamiento
import network.EntrenamientoRepository
import viewmodel.HistorialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    idUsuario: String,
    repository: EntrenamientoRepository,
    viewModel: HistorialViewModel? = null,
    onBack: () -> Unit,
    onSesionClick: (SesionEntrenamiento) -> Unit
) {
    // 1. Obtenemos el ViewModel de forma segura.
    // Usamos una clave que incluya el idUsuario para que los datos sean específicos de ese alumno.
    val historialVM = viewModel ?: getViewModel(
        key = "historial-$idUsuario",
        factory = viewModelFactory { HistorialViewModel(repository) }
    )

    // 2. Observamos el estado del ViewModel (Fuente de verdad única)
    val sesiones by historialVM.sesiones.collectAsState()
    val isLoading by historialVM.isLoading.collectAsState()

    // 3. LaunchedEffect corregido: Forzamos la carga al entrar para que se actualice tras guardar
    // Pero el ViewModel se encargará de no borrar lo que ya tiene mientras carga lo nuevo.
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
            // Si está cargando Y no hay sesiones previas, mostramos el círculo
            if (isLoading && sesiones.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // Si terminó de cargar y sigue vacío
            else if (!isLoading && sesiones.isEmpty()) {
                Text("No hay sesiones registradas", modifier = Modifier.align(Alignment.Center))
            }
            // Si hay datos (o hay datos viejos mientras cargamos los nuevos)
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(),
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
                                        text = sesion.titulo ?: "Rutina",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if(sesion.finalizada) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Finalizada", tint = MaterialTheme.colorScheme.primary)
                                    } else {
                                        Icon(Icons.Default.PendingActions, contentDescription = "Pendiente")
                                    }
                                }
                                Text(
                                    text = "Rutina programada",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${sesion.ejercicios.size} ejercicios planificados",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}