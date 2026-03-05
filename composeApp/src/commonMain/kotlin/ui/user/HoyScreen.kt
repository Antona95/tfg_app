package ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewmodel.HoyUiState
import viewmodel.HoyViewModel
import model.SesionEntrenamiento
import ui.components.EjercicioUniversalCard
import ui.components.agruparEjercicios

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
        BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight

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
                    ContenidoEntreno(state.sesion, isDarkMode, isLandscape) {
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
fun ContenidoEntreno(sesion: SesionEntrenamiento, isDarkMode: Boolean, isLandscape: Boolean, onFinalizar: () -> Unit) {
    // Usamos la misma función de agrupar del Coach
    val gruposDeEjercicios = remember(sesion.ejercicios) { agruparEjercicios(sesion.ejercicios) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight().widthIn(max = 900.dp).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("Rutina: ${sesion.titulo ?: "Sin título"}", style = MaterialTheme.typography.titleMedium)
                        Text("Sesión de Entrenamiento", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (sesion.finalizada) {
                            Text("✅ COMPLETADO", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }

            itemsIndexed(gruposDeEjercicios) { indexGrupo, grupo ->
                val numeroBloque = indexGrupo + 1
                val letraBloque = (numeroBloque + 64).toChar()

                if (isLandscape) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (ejercicio in grupo) {
                            Box(modifier = Modifier.weight(1f)) {
                                EjercicioUniversalCard(ejercicio, isDarkMode, isLandscape = true, letraBloque, numeroBloque)
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (ejercicio in grupo) {
                            EjercicioUniversalCard(ejercicio, isDarkMode, isLandscape = false, letraBloque, numeroBloque)
                        }
                    }
                }
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