package ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Link
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
                title = { Text("Mi Entrenamiento de Hoy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver al menu principal")
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
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No hay entrenamientos programados", style = MaterialTheme.typography.headlineSmall)
                        Text("Hoy es dia de descanso", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is HoyUiState.Error -> Text("Error en la carga: ${state.mensaje}", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
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
                    Text("Fecha: ${sesion.fechaProgramada}", style = MaterialTheme.typography.labelLarge)
                    Text("Rutina Actual", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (sesion.finalizada) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                            Spacer(Modifier.width(8.dp))
                            Text("SESION COMPLETADA", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
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
                Button(
                    onClick = onFinalizar,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("FINALIZAR ENTRENAMIENTO", fontWeight = FontWeight.Bold)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        "Objetivo cumplido por hoy",
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
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
        colors = CardDefaults.cardColors(
            containerColor = if (ejercicio.bloque == 0) MaterialTheme.colorScheme.surfaceVariant else coloresBloquesAlumno[ejercicio.bloque % coloresBloquesAlumno.size]
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ejercicio.bloque != 0 && !unidoArriba) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${(ejercicio.bloque + 64).toChar()}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(ejercicio.nombre ?: "Ejercicio sin nombre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DatoAlumno("Series", "${ejercicio.series}")
                DatoAlumno("Repeticiones", ejercicio.repeticiones)
                DatoAlumno("Peso", if ((ejercicio.peso ?: 0.0) > 0.0) "${ejercicio.peso}kg" else "Sin peso")
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