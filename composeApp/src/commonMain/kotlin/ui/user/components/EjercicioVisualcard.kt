package ui.components

import androidx.compose.foundation.isSystemInDarkTheme
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

@Composable
fun EjercicioVisualCard(
    ejercicio: DetalleSesion,
    unidoArriba: Boolean,
    unidoAbajo: Boolean
) {
    val isDarkMode = isSystemInDarkTheme()

    // Paleta adaptativa
    val colores = if (isDarkMode) {
        listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            Color(0xFF0D47A1), Color(0xFF2E7D32), Color(0xFFE65100), Color(0xFF6A1B9A)
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            Color(0xFFE3F2FD), Color(0xFFF1F8E9), Color(0xFFFFF3E0), Color(0xFFF3E5F5)
        )
    }

    val colorFondo = if (ejercicio.bloque == 0) colores[0]
    else colores[ejercicio.bloque % colores.size]

    // Texto blanco en bloques oscuros, o el color por defecto del tema
    val colorTexto = if (isDarkMode && ejercicio.bloque != 0) Color.White
    else MaterialTheme.colorScheme.onSurface

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
            .padding(top = if (unidoArriba) 0.dp else 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorFondo,
            contentColor = colorTexto
        ),
        elevation = CardDefaults.cardElevation(if (unidoArriba) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (ejercicio.bloque != 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    val letra = (ejercicio.bloque + 64).toChar()
                    Text(
                        text = "$letra",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary, // Contraste automático
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ejercicio.nombre ?: "Ejercicio sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${ejercicio.series} x ${ejercicio.repeticiones}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (ejercicio.peso != null && ejercicio.peso!! > 0.0) {
                        VerticalDivider(modifier = Modifier.height(12.dp).padding(horizontal = 8.dp))
                        Text(
                            text = "${ejercicio.peso} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (!ejercicio.observaciones.isNullOrBlank()) {
                    Text(
                        text = ejercicio.observaciones!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode && ejercicio.bloque != 0) Color.White.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}