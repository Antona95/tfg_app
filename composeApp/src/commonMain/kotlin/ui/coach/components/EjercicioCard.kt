package ui.coach.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.EjercicioDraft

// Paleta de colores suaves para las agrupaciones
val coloresBloques = listOf(
    Color(0xFFFFFFFF), // Bloque 0: Blanco (Normal)
    Color(0xFFE3F2FD), // Bloque 1: Azul
    Color(0xFFF1F8E9), // Bloque 2: Verde
    Color(0xFFFFF3E0), // Bloque 3: Naranja
    Color(0xFFF3E5F5), // Bloque 4: Morado
    Color(0xFFEFEBE9)  // Bloque 5: Marrón claro
)

@Composable
fun EjercicioCard(
    ejercicio: EjercicioDraft,
    unidoArriba: Boolean,
    unidoAbajo: Boolean
) {
    // Si el bloque es 0, usamos 0. Si no, rotamos colores.
    val colorFondo = if (ejercicio.bloque == 0) Color.White
    else coloresBloques[ejercicio.bloque % coloresBloques.size]

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
            .padding(horizontal = 16.dp)
            .padding(top = if (unidoArriba) 0.dp else 8.dp), // Sin hueco si está unido
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = ejercicio.nombre, style = MaterialTheme.typography.titleMedium)
            Text(text = "${ejercicio.series} x ${ejercicio.repeticiones} - ${ejercicio.peso}kg")
        }
    }
}