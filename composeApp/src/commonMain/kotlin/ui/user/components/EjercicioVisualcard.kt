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

// Paleta de colores para identificar visualmente las biseries/triseries
private val coloresBloques = listOf(
    Color(0xFFFFFFFF), // Bloque 0: Blanco (Normal)
    Color(0xFFE3F2FD), // Bloque 1: Azul claro
    Color(0xFFF1F8E9), // Bloque 2: Verde claro
    Color(0xFFFFF3E0), // Bloque 3: Naranja claro
    Color(0xFFF3E5F5), // Bloque 4: Morado claro
    Color(0xFFEFEBE9)  // Bloque 5: Gris claro
)

@Composable
fun EjercicioVisualCard(
    ejercicio: DetalleSesion,
    unidoArriba: Boolean,
    unidoAbajo: Boolean
) {
    // 1. Lógica de esquinas: si está unido, la esquina es recta (0.dp)
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
            // Si está unido arriba, quitamos el margen para que las tarjetas se peguen
            .padding(top = if (unidoArriba) 0.dp else 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ejercicio.bloque == 0) MaterialTheme.colorScheme.surfaceVariant
            else coloresBloques[ejercicio.bloque % coloresBloques.size]
        ),
        elevation = CardDefaults.cardElevation(if (unidoArriba) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 2. Indicador de Bloque (Letras A, B, C...)
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
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            // 3. Información del ejercicio
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ejercicio.nombre ?: "Ejercicio sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${ejercicio.series} x ${ejercicio.repeticiones}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                // 4. Observaciones (si el coach escribió algo)
                if (!ejercicio.observaciones.isNullOrBlank()) {
                    Text(
                        text = ejercicio.observaciones!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}