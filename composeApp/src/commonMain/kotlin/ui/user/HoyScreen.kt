package ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewmodel.HoyUiState
import viewmodel.HoyViewModel
import model.DetalleSesion
import model.SesionEntrenamiento

@Composable
fun obtenerColoresAdaptativos(isDarkMode: Boolean): List<Color> {
    return if (isDarkMode) {
        listOf(
            MaterialTheme.colorScheme.surfaceVariant, // Bloque 0
            Color(0xFF0D47A1), // Azul Marino
            Color(0xFF1B5E20), // Verde Bosque
            Color(0xFFB71C1C), // Rojo Oscuro
            Color(0xFF4A148C), // Púrpura
            Color(0xFFE65100)  // Naranja Quemado
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            Color(0xFFE3F2FD),
            Color(0xFFE8F5E9),
            Color(0xFFFFF3E0),
            Color(0xFFF3E5F5),
            Color(0xFFEFEBE9)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoyScreen(
    idUsuario: String,
    viewModel: HoyViewModel,
    isDarkMode: Boolean,
    onNavigateBack: () -> Unit
) {
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💤", fontSize = 100.sp)
                        Text("Hoy toca descanso", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
                is HoyUiState.Error -> Text("Error: ${state.mensaje}", modifier = Modifier.align(Alignment.Center))
                is HoyUiState.Success -> {
                    ContenidoEntreno(state.sesion, isDarkMode) {
                        viewModel.finalizarEntrenamiento(state.sesion.idSesion, idUsuario) {
                            println("Finalizado")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContenidoEntreno(sesion: SesionEntrenamiento, isDarkMode: Boolean, onFinalizar: () -> Unit) {
    // 1. EL ENVOLTORIO MÁGICO QUE CENTRA LA LISTA
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            // 2. EL TOPE DE ANCHURA Y FILLMAXWIDTH PARA QUE NO SE ESTIRE NI DESAPAREZCA
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 600.dp)
                .fillMaxWidth() ,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("Fecha: ${sesion.fechaProgramada}", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = sesion.titulo,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (sesion.finalizada) {
                            Text("✅ COMPLETADO", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }

            itemsIndexed(sesion.ejercicios) { index, ej ->
                val anterior = sesion.ejercicios.getOrNull(index - 1)
                val siguiente = sesion.ejercicios.getOrNull(index + 1)
                EjercicioAlumnoCard(
                    ejercicio = ej,
                    unidoArriba = ej.bloque != 0 && ej.bloque == anterior?.bloque,
                    unidoAbajo = ej.bloque != 0 && ej.bloque == siguiente?.bloque,
                    isDarkMode = isDarkMode
                )
            }

            item {
                if (!sesion.finalizada) {
                    Button(
                        onClick = onFinalizar,
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp)
                    ) {
                        Text("FINALIZAR ENTRENAMIENTO", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EjercicioAlumnoCard(ejercicio: DetalleSesion, unidoArriba: Boolean, unidoAbajo: Boolean, isDarkMode: Boolean) {
    val misColores = obtenerColoresAdaptativos(isDarkMode)

    val fondo = if (ejercicio.bloque == 0) MaterialTheme.colorScheme.surfaceVariant
    else misColores[ejercicio.bloque % misColores.size]

    val colorTexto = if (isDarkMode && ejercicio.bloque != 0) Color.White
    else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        shape = RoundedCornerShape(
            topStart = if (unidoArriba) 0.dp else 12.dp, topEnd = if (unidoArriba) 0.dp else 12.dp,
            bottomStart = if (unidoAbajo) 0.dp else 12.dp, bottomEnd = if (unidoAbajo) 0.dp else 12.dp
        ),
        modifier = Modifier.fillMaxWidth().padding(top = if (unidoArriba) 0.dp else 8.dp),
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = colorTexto)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = ejercicio.nombre ?: "Ejercicio", fontWeight = FontWeight.Bold, color = colorTexto)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DatoAlumno("SERIES", "${ejercicio.series}", colorTexto)
                DatoAlumno("REPS", ejercicio.repeticiones, colorTexto)
                DatoAlumno("PESO", "${ejercicio.peso ?: 0.0}kg", colorTexto)
            }
        }
    }
}

@Composable
fun DatoAlumno(label: String, valor: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
        Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
    }
}