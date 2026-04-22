package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.DetalleSesion
import ui.theme.ColoresApp

fun agruparEjercicios(ejercicios: List<DetalleSesion>): List<List<DetalleSesion>> {
    // esta función me sirve para transformar una lista plana de ejercicios
    // en una lista de grupos.
    //
    // el objetivo es que luego la UI pueda pintar:
    // - ejercicios sueltos
    // - biseries
    // - triseries
    // como bloques separados.

    // aqui voy guardando el resultado final.
    // cada elemento de "grupos" es a su vez una lista de ejercicios.
    val grupos = mutableListOf<List<DetalleSesion>>()

    // aqui guardo temporalmente el grupo que estoy construyendo en este momento.
    var grupoActual = mutableListOf<DetalleSesion>()

    // aqui recuerdo el bloque del grupo actual.
    // empiezo en -1 como valor de control para indicar "ningún bloque real".
    var bloqueActual = -1

    // recorro todos los ejercicios en el orden en el que vienen.
    for (ej in ejercicios) {

        // si bloque vale 0, interpreto que es un ejercicio normal,
        // es decir, que no pertenece a ninguna biserie ni triserie.
        if (ej.bloque == 0) {

            // si antes estaba montando un grupo, lo cierro y lo añado al resultado.
            if (grupoActual.isNotEmpty()) {
                grupos.add(grupoActual)
                grupoActual = mutableListOf()
            }

            // como este ejercicio va solo, lo añado como una lista de un único elemento.
            grupos.add(listOf(ej))

            // vuelvo a dejar el bloque actual en -1 porque ya no estoy dentro de ningún grupo.
            bloqueActual = -1
        } else {

            // si el bloque del ejercicio coincide con el bloque actual,
            // significa que sigue perteneciendo al mismo grupo.
            if (ej.bloque == bloqueActual) {
                grupoActual.add(ej)
            } else {

                // si cambia de bloque, primero cierro el grupo anterior si existía.
                if (grupoActual.isNotEmpty()) {
                    grupos.add(grupoActual)
                }

                // ahora empiezo un grupo nuevo con este ejercicio.
                grupoActual = mutableListOf(ej)

                // actualizo el bloque actual al nuevo bloque.
                bloqueActual = ej.bloque
            }
        }
    }

    // al terminar el bucle, puede quedar un grupo sin añadir todavía.
    // por eso hago esta comprobación final.
    if (grupoActual.isNotEmpty()) {
        grupos.add(grupoActual)
    }

    // devuelvo la lista de grupos ya preparada para que la UI la pinte.
    return grupos
}

fun obtenerColorBloqueUniversal(numeroBloque: Int, isDarkMode: Boolean): Color {
    // esta función ya no decide colores por su cuenta.
    //
    // en vez de dejar aqui todos los Color(...),
    // delego la responsabilidad al archivo central ColoresApp.
    //
    // asi, si mañana quiero cambiar la paleta de bloques,
    // solo lo hago en un sitio.
    return ColoresApp.colorBloque(numeroBloque, isDarkMode)
}

@Composable
fun EjercicioUniversalCard(
    ejercicio: DetalleSesion,
    isDarkMode: Boolean,
    isLandscape: Boolean,
    letraBloque: Char,
    numeroBloque: Int,
    modifier: Modifier = Modifier
) {
    // calculo el color de fondo de la tarjeta según el bloque y el tema.
    val colorFondo = obtenerColorBloqueUniversal(numeroBloque, isDarkMode)

    // saco el color principal del texto desde ColoresApp.
    val colorTexto = ColoresApp.textoPrincipal(isDarkMode)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorFondo,
            contentColor = colorTexto
        )
    ) {
        Column(
            // aqui hago que la columna ocupe toda la altura disponible de la tarjeta.
            //
            // esto me permite empujar la fila inferior de datos hacia abajo
            // y conseguir que S, R, Kg queden alineados entre tarjetas del mismo grupo.
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // esta primera fila contiene:
            // - a la izquierda, el nombre del ejercicio
            // - a la derecha, una etiqueta con la letra del bloque
            Row(
                // aqui alineo arriba para que, si el nombre ocupa varias lineas,
                // la etiqueta del bloque no quede visualmente descentrada.
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = ejercicio.nombre ?: "Sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = colorTexto
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
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

            // este spacer con peso es la parte importante de la corrección.
            //
            // lo uso para ocupar el espacio sobrante entre el nombre del ejercicio
            // y la fila final de datos.
            //
            // asi, aunque un ejercicio tenga un nombre mas largo que otro,
            // la fila de S, R y Kg queda a la misma altura en todas las tarjetas.
            Spacer(modifier = Modifier.weight(1f))

            // esta segunda fila muestra los datos cortos del ejercicio:
            // series, repeticiones y peso.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DatoUniversal(
                    label = if (isLandscape) "S" else "Series",
                    value = "${ejercicio.series}",
                    isDarkMode = isDarkMode
                )

                DatoUniversal(
                    label = if (isLandscape) "R" else "Reps",
                    value = "${ejercicio.repeticiones}",
                    isDarkMode = isDarkMode
                )

                // preparo el texto del peso.
                val pesoText =
                    if (ejercicio.peso != null && ejercicio.peso > 0) {
                        "${ejercicio.peso}"
                    } else {
                        "--"
                    }

                DatoUniversal(
                    label = if (isLandscape) "Kg" else "Peso",
                    value = pesoText,
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

@Composable
fun DatoUniversal(
    label: String,
    value: String,
    isDarkMode: Boolean
) {
    // este composable pequeño me sirve para pintar cada dato resumen del ejercicio.
    //
    // por ejemplo:
    // series -> 3
    // reps -> 10
    // peso -> 20

    // saco desde ColoresApp el color principal y el secundario.
    val colorValor = ColoresApp.textoPrincipal(isDarkMode)
    val colorLabel =
        if (isDarkMode) {
            ColoresApp.textoPrincipal(isDarkMode).copy(alpha = 0.78f)
        } else {
            ColoresApp.textoSecundario(isDarkMode)
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colorLabel
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = colorValor
        )
    }
}