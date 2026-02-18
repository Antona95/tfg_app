package ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import network.EntrenamientoRepository
import viewmodel.HoyUiState
import viewmodel.HoyViewModel
import model.DetalleSesion
import model.SesionEntrenamiento

private val coloresBloquesAlumno = listOf(
    Color(0xFFFFFFFF), Color(0xFFE3F2FD), Color(0xFFE8F5E9),
    Color(0xFFFFF3E0), Color(0xFFF3E5F5), Color(0xFFEFEBE9)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoyScreen(
    idUsuario: String,
    repository: EntrenamientoRepository,
    onNavigateBack: () -> Unit
) {
    // Uso correcto de Moko MVVM en Compose Multiplatform
    val viewModel = getViewModel(
        key = "hoy-screen-$idUsuario",
        factory = viewModelFactory { HoyViewModel(repository) }
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idUsuario) {
        viewModel.cargarEntrenamiento(idUsuario)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Entreno de Hoy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is HoyUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is HoyUiState.Empty -> {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💤", style = MaterialTheme.typography.displayLarge)
                        Text("Hoy toca descanso", style = MaterialTheme.typography.headlineSmall)
                    }
                }
                is HoyUiState.Error -> Text("Error: ${state.mensaje}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                is HoyUiState.Success -> {
                    ContenidoEntreno(state.sesion) {
                        viewModel.finalizarEntrenamiento(state.sesion.idSesion, idUsuario)
                    }
                }
            }
        }
    }
}

@Composable
fun ContenidoEntreno(sesion: SesionEntrenamiento, onFinalizar: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("📅 ${sesion.fechaProgramada}", style = MaterialTheme.typography.labelLarge)
                    Text("Rutina del Día", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (sesion.finalizada) {
                        Text("✅ ¡COMPLETADA!", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    }
                }
            }
        }

        itemsIndexed(sesion.ejercicios) { index, ej ->
            val anterior = sesion.ejercicios.getOrNull(index - 1)
            val siguiente = sesion.ejercicios.getOrNull(index + 1)
            val unidoArriba = ej.bloque != 0 && ej.bloque == anterior?.bloque
            val unidoAbajo = ej.bloque != 0 && ej.bloque == siguiente?.bloque
            EjercicioAlumnoCard(ej, unidoArriba, unidoAbajo)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            if (!sesion.finalizada) {
                Button(onClick = onFinalizar, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("TERMINAR ENTRENAMIENTO", fontWeight = FontWeight.Bold)
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Text("Has cumplido por hoy 💪", modifier = Modifier.padding(24.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun EjercicioAlumnoCard(ejercicio: DetalleSesion, unidoArriba: Boolean, unidoAbajo: Boolean) {
    val shape = RoundedCornerShape(
        topStart = if (unidoArriba) 0.dp else 12.dp, topEnd = if (unidoArriba) 0.dp else 12.dp,
        bottomStart = if (unidoAbajo) 0.dp else 12.dp, bottomEnd = if (unidoAbajo) 0.dp else 12.dp
    )
    Card(
        shape = shape,
        modifier = Modifier.fillMaxWidth().padding(top = if (unidoArriba) 0.dp else 8.dp),
        colors = CardDefaults.cardColors(containerColor = if (ejercicio.bloque == 0) MaterialTheme.colorScheme.surfaceVariant else coloresBloquesAlumno[ejercicio.bloque % coloresBloquesAlumno.size])
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ejercicio.bloque != 0 && !unidoArriba) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(end = 8.dp)) {
                        Text("${(ejercicio.bloque + 64).toChar()}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Text(ejercicio.nombre ?: "Ejercicio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DatoAlumno("Series", "${ejercicio.series}")
                DatoAlumno("Reps", ejercicio.repeticiones)
                DatoAlumno("Peso", if ((ejercicio.peso ?: 0.0) > 0.0) "${ejercicio.peso}kg" else "--")
            }
        }
    }
}

@Composable
fun DatoAlumno(label: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}