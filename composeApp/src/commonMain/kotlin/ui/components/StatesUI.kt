package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ui.theme.AppColors

@Composable
fun PantallaCargando(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    // este composable me sirve para mostrar una pantalla de carga reutilizable.
    //
    // lo uso cuando una pantalla todavía está esperando respuesta del backend
    // y quiero enseñar un indicador visual simple al usuario.

    // aqui saco un color de apoyo desde la paleta centralizada.
    // no es obligatorio, pero asi dejo la carga visualmente alineada
    // con el resto de la app en claro y en oscuro.
    val colorIndicador = AppColors.textoSecundario(isDarkMode)

    Box(
        // aqui hago que el box ocupe todo el tamaño disponible.
        modifier = modifier.fillMaxSize(),

        // centro el contenido dentro del box.
        contentAlignment = Alignment.Center
    ) {
        // este indicador circular representa que hay una operación en curso.
        CircularProgressIndicator(color = colorIndicador)
    }
}

@Composable
fun PantallaVacia(
    icono: ImageVector,
    mensaje: String,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    // este composable me sirve para mostrar estados vacíos de forma visual.
    //
    // por ejemplo:
    // - no hay sesiones
    // - no hay resultados
    // - hoy toca descanso
    //
    // ahora uso un pictograma real en vez de un emoji o texto.

    // aqui saco colores desde ColoresApp para que el estado vacío
    // tenga el mismo criterio visual que el resto de la app.
    val colorIcono = AppColors.textoSecundario(isDarkMode)
    val colorTexto = AppColors.textoPrincipal(isDarkMode)

    Column(
        modifier = modifier.fillMaxSize(),

        // coloco el contenido centrado en vertical.
        verticalArrangement = Arrangement.Center,

        // y también centrado en horizontal.
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // aqui muestro el pictograma del estado vacío.
        Icon(
            imageVector = icono,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = colorIcono
        )

        Spacer(modifier = Modifier.height(16.dp))

        // aqui muestro el mensaje principal del estado vacío.
        Text(
            text = mensaje,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colorTexto
        )
    }
}

@Composable
fun PantallaError(
    mensaje: String,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    // este composable me sirve para mostrar un error simple en pantalla completa.
    //
    // lo uso cuando una operación falla y quiero enseñarlo de forma directa.

    // dejo el parámetro isDarkMode porque mantiene la firma consistente
    // con el resto de estados reutilizables.
    //
    // aunque aqui no lo use directamente ahora mismo, me viene bien
    // por si más adelante quiero ajustar iconos o textos secundarios.
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            // aqui concateno la palabra "Error" con el mensaje real recibido.
            //
            // uso el mensaje entero en color de error para que el usuario
            // identifique rápido que ha ocurrido un fallo.
            text = "Error: $mensaje",
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
    }
}

// este composable reutilizable me sirve para mostrar cuadros de alerta
// en cualquier parte de la aplicación.
@Composable
fun DialogoAlerta(
    mostrarDialogo: Boolean,
    titulo: String,
    mensaje: String,
    onDismiss: () -> Unit,
    isDarkMode: Boolean = false
) {
    // aqui hago una comprobación sencilla:
    // solo construyo el dialogo si mostrarDialogo vale true.
    if (mostrarDialogo) {
        AlertDialog(
            // onDismissRequest es la acción que ejecuto cuando el dialogo se cierra.
            //
            // por ejemplo, puede dispararse si el usuario toca fuera del cuadro
            // o si el propio componente se descarta.
            //
            // yo aquí no escribo la lógica directamente, sino que recibo una función
            // llamada onDismiss y la ejecuto.
            onDismissRequest = onDismiss,

            title = {
                // aqui personalizo el encabezado del dialogo con icono + texto.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alerta",

                        // pinto el icono con color de error para reforzar visualmente que es un aviso.
                        tint = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = titulo,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textoPrincipal(isDarkMode)
                    )
                }
            },

            // este es el cuerpo del dialogo con el mensaje explicativo.
            text = {
                Text(
                    text = mensaje,
                    color = AppColors.textoPrincipal(isDarkMode)
                )
            },

            confirmButton = {
                // este botón confirma o simplemente cierra el diálogo.
                //
                // en este caso, al pulsarlo ejecuto onDismiss,
                // o sea, la función que me pasan desde fuera para ocultar el dialogo.
                TextButton(onClick = onDismiss) {
                    Text("Entendido")
                }
            }
        )
    }
}