package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
fun CabeceraEstadoSesion(sesion: SesionEntrenamiento, isDarkMode: Boolean) {
    // este composable me sirve para mostrar una cabecera comun en las pantallas
    // donde enseño una sesion.
    //
    // la idea es reutilizar siempre el mismo bloque visual para que:
    // - el titulo de la sesion salga igual en todas partes
    // - el estado finalizada o pendiente tambien salga igual
    //
    // asi evito repetir codigo en varias pantallas.

    Card(
        colors = CardDefaults.cardColors(
            // aqui uso un color del tema para que la cabecera destaque un poco
            // respecto al fondo general de la pantalla.
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            // este modifier hace dos cosas:
            // 1. ocupa todo el ancho disponible
            // 2. deja un padding interior de 16 dp para que el contenido no quede pegado
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            // aqui centro horizontalmente los elementos del column.
            // por eso el titulo y el estado salen centrados.
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                // aqui muestro el titulo de la sesion.
                //
                // si sesion.titulo viene null, uso un texto por defecto:
                // "sesión de entrenamiento"
                //
                // el operador ?: significa "si esto es null, usa esto otro".
                text = sesion.titulo ?: "Sesión de Entrenamiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // este spacer me sirve para dejar un hueco entre el titulo
            // y el bloque visual del estado.
            Spacer(modifier = Modifier.height(8.dp))

            // aqui compruebo si la sesion está finalizada o no.
            if (sesion.finalizada) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // sustituyo el emoticono por un pictograma real.
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "sesión finalizada",
                        tint = Color(0xFF2E7D32)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        // si está finalizada, muestro este texto.
                        text = "FINALIZADA",

                        // uso un verde fijo para reforzar visualmente que la sesion ya está hecha.
                        color = Color(0xFF2E7D32),

                        // le doy un tamaño algo mayor para que destaque.
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // sustituyo el emoticono por un pictograma real.
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "sesión pendiente",
                        tint = if (isDarkMode) Color(0xFFFFB74D) else Color(0xFFE65100)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        // si no está finalizada, muestro pendiente.
                        text = "PENDIENTE",

                        // aqui cambio el color según el modo oscuro o claro.
                        //
                        // lo hago porque el mismo color no siempre se ve igual de bien
                        // en ambos temas.
                        //
                        // si isdarkmode es true uso un naranja más claro.
                        // si no, uso un naranja más oscuro.
                        color = if (isDarkMode) Color(0xFFFFB74D) else Color(0xFFE65100),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}