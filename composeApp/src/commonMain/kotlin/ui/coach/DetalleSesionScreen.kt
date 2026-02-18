package ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.SesionEntrenamiento
import model.DetalleSesion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSesionScreen(
    sesion: SesionEntrenamiento,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Detalle de Sesión", style = MaterialTheme.typography.titleMedium)
                        Text(sesion.fechaProgramada, style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Ejercicios Realizados",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(sesion.ejercicios) { ejercicio ->
                ExerciseDetailCard(ejercicio)
            }
        }
    }
}

@Composable
fun ExerciseDetailCard(ejercicio: DetalleSesion) {
    // 1. OBTENEMOS EL COLOR SEGÚN EL BLOQUE
    val colorFondo = obtenerColorBloque(ejercicio.bloque)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // 2. APLICAMOS EL COLOR AL CONTENEDOR
        colors = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // CABECERA: Nombre y Etiqueta de Bloque
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = ejercicio.nombre ?: "Ejercicio sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Si tiene bloque asignado (>0), mostramos una etiqueta visual
                if (ejercicio.bloque > 0) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Bloque ${ejercicio.bloque}") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(alpha = 0.5f),
                            labelColor = Color.Black
                        ),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // DATOS TÉCNICOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoBadge("Series", "${ejercicio.series}")
                InfoBadge("Reps", ejercicio.repeticiones)
                val pesoTexto = if (ejercicio.peso != null && ejercicio.peso > 0) "${ejercicio.peso} kg" else "--"
                InfoBadge("Peso", pesoTexto)
            }

            // OBSERVACIONES
            if (!ejercicio.observaciones.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.Black.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = " ${ejercicio.observaciones}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f) // Aseguramos contraste
                )
            }
        }
    }
}

@Composable
fun InfoBadge(label: String, value: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Black.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

// --- FUNCIÓN PARA COLOREAR LOS BLOQUES ---
fun obtenerColorBloque(bloque: Int): Color {
    return when (bloque) {
        1 -> Color(0xFFBBDEFB) // Azul suave (Blue 100)
        2 -> Color(0xFFFFCCBC) // Naranja suave (Deep Orange 100)
        3 -> Color(0xFFC8E6C9) // Verde suave (Green 100)
        4 -> Color(0xFFE1BEE7) // Violeta suave (Purple 100)
        5 -> Color(0xFFFFF9C4) // Amarillo suave
        else -> Color(0xFFF5F5F5) // Gris muy claro por defecto (o bloque 0)
    }
}