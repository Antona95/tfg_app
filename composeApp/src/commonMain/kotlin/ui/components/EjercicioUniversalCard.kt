package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.DetalleSesion

// 1. Lógica para agrupar biseries/triseries (Extraída de tu CoachScreen)
fun agruparEjercicios(ejercicios: List<DetalleSesion>): List<List<DetalleSesion>> {
    val grupos = mutableListOf<List<DetalleSesion>>()
    var grupoActual = mutableListOf<DetalleSesion>()
    var bloqueActual = -1

    for (ej in ejercicios) {
        if (ej.bloque == 0) {
            if (grupoActual.isNotEmpty()) {
                grupos.add(grupoActual)
                grupoActual = mutableListOf()
            }
            grupos.add(listOf(ej))
            bloqueActual = -1
        } else {
            if (ej.bloque == bloqueActual) {
                grupoActual.add(ej)
            } else {
                if (grupoActual.isNotEmpty()) {
                    grupos.add(grupoActual)
                }
                grupoActual = mutableListOf(ej)
                bloqueActual = ej.bloque
            }
        }
    }
    if (grupoActual.isNotEmpty()) {
        grupos.add(grupoActual)
    }
    return grupos
}

// 2. Lógica universal de colores
fun obtenerColorBloqueUniversal(numeroBloque: Int, isDarkMode: Boolean): Color {
    val indexColor = ((numeroBloque - 1) % 5) + 1
    return if (isDarkMode) {
        when (indexColor) {
            1 -> Color(0xFF0D47A1); 2 -> Color(0xFF1B5E20); 3 -> Color(0xFFB71C1C)
            4 -> Color(0xFF4A148C); 5 -> Color(0xFFE65100); else -> Color(0xFF2C2C2C)
        }
    } else {
        when (indexColor) {
            1 -> Color(0xFFE3F2FD); 2 -> Color(0xFFE8F5E9); 3 -> Color(0xFFFFF3E0)
            4 -> Color(0xFFF3E5F5); 5 -> Color(0xFFEFEBE9); else -> Color(0xFFF5F5F5)
        }
    }
}

// 3. Tarjeta visual universal
@Composable
fun EjercicioUniversalCard(
    ejercicio: DetalleSesion,
    isDarkMode: Boolean,
    isLandscape: Boolean,
    letraBloque: Char,
    numeroBloque: Int
) {
    val colorFondo = obtenerColorBloqueUniversal(numeroBloque, isDarkMode)
    val colorTexto = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondo, contentColor = colorTexto)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ejercicio.nombre ?: "Sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = colorTexto
                )
                Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = if (isLandscape) "$letraBloque" else "Bloque $letraBloque",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DatoUniversal(if (isLandscape) "S" else "Series", "${ejercicio.series}", colorTexto)
                DatoUniversal(if (isLandscape) "R" else "Reps", ejercicio.repeticiones, colorTexto)
                val pesoText = if (ejercicio.peso != null && ejercicio.peso > 0) "${ejercicio.peso}" else "--"
                DatoUniversal(if (isLandscape) "Kg" else "Peso", pesoText, colorTexto)
            }
        }
    }
}

@Composable
fun DatoUniversal(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    }
}