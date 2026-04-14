package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.SesionEntrenamiento
import ui.theme.ColoresApp

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

    // aqui saco los colores de estado desde ColoresApp.
    // de esta manera dejo de tener colores "duros" metidos en el componente.
    val colorEstadoFinalizada = ColoresApp.estadoExito(isDarkMode)
    val colorEstadoPendiente = ColoresApp.estadoPendiente(isDarkMode)

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
                // "Sesión de Entrenamiento"
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
                        tint = colorEstadoFinalizada
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        // si está finalizada, muestro este texto.
                        text = "FINALIZADA",

                        // uso el color centralizado de estado de exito.
                        color = colorEstadoFinalizada,

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
                        tint = colorEstadoPendiente
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        // si no está finalizada, muestro pendiente.
                        text = "PENDIENTE",

                        // uso el color centralizado de estado pendiente.
                        color = colorEstadoPendiente,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}