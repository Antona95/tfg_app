package ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape // IMPORT PARA EL SHAPE
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment // IMPORT PARA ALIGNMENT
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp // IMPORT PARA EL DP
import model.SesionEntrenamiento
import model.DetalleSesion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSesionScreen(
    sesion: SesionEntrenamiento,
    isDarkMode: Boolean,
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
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = sesion.titulo,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (sesion.finalizada) {
                            Text("✅ FINALIZADA", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Ejercicios Realizados",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            itemsIndexed(sesion.ejercicios) { index, ejercicio ->
                ExerciseDetailCard(ejercicio, isDarkMode)
            }
        }
    }
}

@Composable
fun ExerciseDetailCard(ejercicio: DetalleSesion, isDarkMode: Boolean) {
    val colorFondo = obtenerColorBloque(ejercicio.bloque, isDarkMode)
    val colorTexto = if (isDarkMode && ejercicio.bloque > 0) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorFondo,
            contentColor = colorTexto
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ejercicio.nombre ?: "Ejercicio sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = colorTexto
                )

                if (ejercicio.bloque > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp) // 👈 Ahora ya no debería estar en rojo
                    ) {
                        Text(
                            text = "Bloque ${ejercicio.bloque}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoBadge("Series", "${ejercicio.series}", colorTexto)
                InfoBadge("Reps", ejercicio.repeticiones, colorTexto)
                val pesoTexto = if (ejercicio.peso != null && ejercicio.peso > 0) "${ejercicio.peso} kg" else "--"
                InfoBadge("Peso", pesoTexto, colorTexto)
            }

            if (!ejercicio.observaciones.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colorTexto.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ejercicio.observaciones!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorTexto.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun InfoBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

fun obtenerColorBloque(bloque: Int, isDarkMode: Boolean): Color {
    return if (isDarkMode) {
        when (bloque) {
            1 -> Color(0xFF0D47A1)
            2 -> Color(0xFFB71C1C)
            3 -> Color(0xFF1B5E20)
            4 -> Color(0xFF4A148C)
            5 -> Color(0xFFE65100)
            else -> Color(0xFF2C2C2C)
        }
    } else {
        when (bloque) {
            1 -> Color(0xFFBBDEFB)
            2 -> Color(0xFFFFCCBC)
            3 -> Color(0xFFC8E6C9)
            4 -> Color(0xFFE1BEE7)
            5 -> Color(0xFFFFF9C4)
            else -> Color(0xFFF5F5F5)
        }
    }
}