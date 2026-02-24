package ui.coach.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.EjercicioDraft

@Composable
fun EjercicioCard(
    ejercicio: EjercicioDraft,
    unidoArriba: Boolean,
    unidoAbajo: Boolean,
    isDarkMode: Boolean // <--- 1. AÑADIMOS EL PARÁMETRO AQUÍ
) {
    // Paleta que cambia según el tema de la App (No del sistema)
    val colores = if (isDarkMode) {
        listOf(
            MaterialTheme.colorScheme.surface, // Bloque 0
            Color(0xFF1A237E), Color(0xFF1B5E20), Color(0xFF4E342E), Color(0xFF4A148C)
        )
    } else {
        listOf(
            Color.White, // Bloque 0
            Color(0xFFE3F2FD), Color(0xFFF1F8E9), Color(0xFFFFF3E0), Color(0xFFF3E5F5)
        )
    }

    val colorFondo = if (ejercicio.bloque == 0) colores[0]
    else colores[ejercicio.bloque % colores.size]

    // Ajustamos el color del texto para que siempre contraste
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
            .padding(horizontal = 16.dp)
            .padding(top = if (unidoArriba) 0.dp else 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorFondo,
            contentColor = colorTexto // Aplica el color a los textos internos
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = ejercicio.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "${ejercicio.series} x ${ejercicio.repeticiones} - ${ejercicio.peso}kg", style = MaterialTheme.typography.bodyMedium)
        }
    }
}