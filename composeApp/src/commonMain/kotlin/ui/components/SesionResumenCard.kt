package ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.SesionEntrenamiento
import ui.theme.ColoresApp

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

    // aqui saco algunos colores desde ColoresApp para mejorar el contraste
    // y no dejar colores fijos en este componente.
    val colorEstadoFinalizada = ColoresApp.estadoExito(isDarkMode)
    val colorEstadoPendiente = ColoresApp.estadoPendiente(isDarkMode)
    val colorTextoSecundario = ColoresApp.textoSecundario(isDarkMode)

    Card(
        // redondeo las esquinas para que la tarjeta tenga un aspecto mas moderno y limpio.
        shape = RoundedCornerShape(12.dp),

        modifier = Modifier
            .fillMaxWidth()

            // clickable hace que toda la tarjeta responda al toque.
            // cuando pulso, ejecuto la funcion onClick que me llega por parametro.
            .clickable { onClick() },

        colors = CardDefaults.cardColors(
            // uso un color del tema para diferenciar la tarjeta del fondo.
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

            // spaceBetween coloca el bloque de texto a la izquierda
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

                    // uso el color de texto asociado a surfaceVariant para que combine bien con la tarjeta.
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
                            tint = colorEstadoFinalizada
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            "Finalizada",

                            // uso el color centralizado de exito.
                            color = colorEstadoFinalizada,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "pendiente",
                            tint = colorEstadoPendiente
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            "Pendiente",

                            // uso el color centralizado de pendiente.
                            color = colorEstadoPendiente,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // esta flecha indica que la tarjeta se puede abrir o navegar.
            //
            // uso AutoMirrored para que si algun dia hubiera soporte rtl
            // la flecha se adapte automaticamente a la direccion correcta.
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver detalles",

                // aqui uso un color secundario centralizado para que no resalte demasiado.
                tint = colorTextoSecundario
            )
        }
    }
}