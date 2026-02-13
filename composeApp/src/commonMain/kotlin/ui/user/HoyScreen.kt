package ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import network.EntrenamientoRepository
import viewmodel.HoyUiState
import viewmodel.HoyViewModel
import viewmodel.SesionViewModelFactory
import model.DetalleSesion

private val coloresBloques = listOf(
    Color(0xFFFFFFFF), // Bloque 0: Blanco
    Color(0xFFE3F2FD), // Bloque 1: Azul
    Color(0xFFF1F8E9), // Bloque 2: Verde
    Color(0xFFFFF3E0), // Bloque 3: Naranja
    Color(0xFFF3E5F5), // Bloque 4: Morado
    Color(0xFFEFEBE9)  // Bloque 5: Gris
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoyScreen(
    idUsuario: String,
    repository: EntrenamientoRepository,
    onNavigateBack: () -> Unit
) {
    val viewModel: HoyViewModel = viewModel(factory = SesionViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idUsuario) {
        viewModel.cargarEntrenamiento(idUsuario)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Entreno de Hoy", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is HoyUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HoyUiState.Empty -> {
                    Text("Hoy toca descanso.", modifier = Modifier.align(Alignment.Center))
                }
                is HoyUiState.Error -> {
                    Text(state.mensaje, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                is HoyUiState.Success -> {
                    val sesion = state.sesion
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        // Título y Fecha
                        item {
                            Text(
                                text = "Sesión: ${sesion.fechaProgramada}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Lista de Ejercicios
                        itemsIndexed(sesion.ejercicios) { index, ej ->
                            val anterior = sesion.ejercicios.getOrNull(index - 1)
                            val siguiente = sesion.ejercicios.getOrNull(index + 1)

                            val unidoArriba = ej.bloque != 0 && ej.bloque == anterior?.bloque
                            val unidoAbajo = ej.bloque != 0 && ej.bloque == siguiente?.bloque

                            EjercicioVisualCard(ej, unidoArriba, unidoAbajo)
                        }

                        // --- PUNTO 3: BOTÓN DE FINALIZAR O ESTADO COMPLETADO ---
                        item {
                            Spacer(modifier = Modifier.height(32.dp))

                            if (!sesion.finalizada) {
                                Button(
                                    onClick = { viewModel.finalizarEntrenamiento(sesion.idSesion, idUsuario) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        "TERMINAR ENTRENAMIENTO",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            } else {
                                // Estado cuando la sesión ya está finalizada
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "✅ ¡ENTRENAMIENTO COMPLETADO!",
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            // Espacio final para que el scroll no quede pegado
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EjercicioVisualCard(
    ejercicio: DetalleSesion,
    unidoArriba: Boolean,
    unidoAbajo: Boolean
) {
    val shape = RoundedCornerShape(
        topStart = if (unidoArriba) 0.dp else 12.dp,
        topEnd = if (unidoArriba) 0.dp else 12.dp,
        bottomStart = if (unidoAbajo) 0.dp else 12.dp,
        bottomEnd = if (unidoAbajo) 0.dp else 12.dp
    )

    Card(
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (unidoArriba) 0.dp else 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ejercicio.bloque == 0) MaterialTheme.colorScheme.surfaceVariant
            else coloresBloques[ejercicio.bloque % coloresBloques.size]
        ),
        elevation = CardDefaults.cardElevation(if (unidoArriba) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (ejercicio.bloque != 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${(ejercicio.bloque + 64).toChar()}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column {
                Text(
                    text = ejercicio.nombre ?: "Sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${ejercicio.series} x ${ejercicio.repeticiones} — ${ejercicio.peso ?: 0}kg",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}