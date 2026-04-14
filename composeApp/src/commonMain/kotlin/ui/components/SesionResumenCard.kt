package ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.SesionEntrenamiento

@Composable
fun SesionResumenCard(
    sesion: SesionEntrenamiento,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    // este composable me sirve para mostrar una tarjeta resumen de una sesion.
    //
    // la uso sobre todo en pantallas de historial, donde necesito listar muchas sesiones
    // y al pulsar una de ellas entrar al detalle.
    //
    // la idea es enseñar de forma compacta:
    // - el titulo
    // - si esta finalizada o pendiente
    // - una flecha para indicar que se puede abrir

    Card(
        // redondeo las esquinas para que la tarjeta tenga un aspecto mas moderno y limpio.
        shape = RoundedCornerShape(12.dp),

        modifier = Modifier
            .fillMaxWidth()

            // clickable hace que toda la tarjeta responda al toque.
            // cuando pulso, ejecuto la funcion onclick que me llega por parametro.
            .clickable { onClick() },

        colors = CardDefaults.cardColors(
            // uso un color secundario del tema para diferenciarla del fondo
            // pero sin destacar demasiado.
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),

        // añado una elevacion pequeña para separar visualmente la tarjeta del fondo.
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            // centro verticalmente el contenido de la fila.
            verticalAlignment = Alignment.CenterVertically,

            // spacebetween coloca el bloque de texto a la izquierda
            // y la flecha a la derecha.
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                // con weight hago que esta columna ocupe el espacio principal de la fila
                // y deje la flecha al extremo derecho.
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    // aqui muestro el titulo de la sesion.
                    // si por algun motivo no tiene titulo, enseño un texto por defecto.
                    text = sesion.titulo ?: "Sesión sin título",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,

                    // uso el color de texto asociado a surfacevariant para que combine bien con la tarjeta.
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // dejo una pequeña separacion entre el titulo y el estado.
                Spacer(modifier = Modifier.height(6.dp))

                // aqui muestro el estado de la sesion.
                if (sesion.finalizada) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "finalizada",
                            tint = Color(0xFF2E7D32)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            "Finalizada",

                            // si está finalizada uso verde porque transmite visualmente "hecho" o "correcto".
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "pendiente",
                            tint = if (isDarkMode) Color(0xFFFFB74D) else Color(0xFFE65100)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            "Pendiente",

                            // si esta pendiente cambio el color segun el tema.
                            //
                            // en oscuro uso un naranja mas claro para que tenga contraste.
                            // en claro uso un naranja mas intenso.
                            color = if (isDarkMode) Color(0xFFFFB74D) else Color(0xFFE65100),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // esta flecha indica que la tarjeta se puede abrir o navegar.
            //
            // uso automirrored para que si algun dia hubiera soporte rtl
            // la flecha se adapte automaticamente a la direccion correcta.
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver detalles",

                // uso el color primario del tema para que la flecha destaque un poco.
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}