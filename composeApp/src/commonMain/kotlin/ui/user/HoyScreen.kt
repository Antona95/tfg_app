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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import network.EntrenamientoRepository
import viewmodel.HoyUiState
import viewmodel.HoyViewModel
import viewmodel.SesionViewModelFactory
import model.DetalleSesion

// Volvemos a definir los colores aquí (o impórtalos si los tienes en un archivo de constantes)
private val coloresBloques = listOf(
    Color(0xFFFFFFFF), // Bloque 0
    Color(0xFFE3F2FD), // Bloque 1
    Color(0xFFF1F8E9), // Bloque 2
    Color(0xFFFFF3E0), // Bloque 3
    Color(0xFFF3E5F5), // Bloque 4
    Color(0xFFEFEBE9)  // Bloque 5
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoyScreen(
    idUsuario: String,
    repository: EntrenamientoRepository,
    onNavigateBack: () -> Unit
) {
    // Usamos la Factory para inyectar el repositorio
    val viewModel: HoyViewModel = viewModel(factory = SesionViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    // Carga inicial
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
                    Text(
                        "Hoy toca descanso. ¡Recupera bien!",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is HoyUiState.Error -> {
                    Text(
                        state.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is HoyUiState.Success -> {
                    val sesion = state.sesion

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp) // El espacio lo controla el Card
                    ) {
                        item {
                            Text(
                                text = sesion.titulo ?: "Sesión sin título",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(20.dp))
                        }

                        itemsIndexed(sesion.ejercicios) { index, ej ->
                            val anterior = sesion.ejercicios.getOrNull(index - 1)
                            val siguiente = sesion.ejercicios.getOrNull(index + 1)

                            val unidoArriba = ej.bloque != 0 && ej.bloque == anterior?.bloque
                            val unidoAbajo = ej.bloque != 0 && ej.bloque == siguiente?.bloque

                            EjercicioVisualCard(ej, unidoArriba, unidoAbajo)
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
            .padding(top = if (unidoArriba) 0.dp else 12.dp), // Separación si no es el mismo bloque
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
            // Letra del Bloque (A, B, C...)
            if (ejercicio.bloque != 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${(ejercicio.bloque + 64).toChar()}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
                Spacer(Modifier.width(16.dp))
            }

            Column {
                // Accedemos a ejercicio.ejercicio.nombre porque DetalleSesion tiene el objeto Ejercicio dentro
                Text(
                    text = ejercicio.ejercicio.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${ejercicio.seriesObjetivo} x ${ejercicio.repeticionesObjetivo} — ${ejercicio.pesoObjetivo ?: 0}kg",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}