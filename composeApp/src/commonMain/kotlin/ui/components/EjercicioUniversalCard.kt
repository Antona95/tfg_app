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
    // esta funcion me sirve para transformar una lista plana de ejercicios
    // en una lista de grupos.
    //
    // el objetivo es que luego la ui pueda pintar:
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
    // empiezo en -1 como valor de control para indicar "ningun bloque real".
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
    // por eso hago esta comprobacion final.
    if (grupoActual.isNotEmpty()) {
        grupos.add(grupoActual)
    }

    // devuelvo la lista de grupos ya preparada para que la ui la pinte.
    return grupos
}

fun obtenerColorBloqueUniversal(numeroBloque: Int, isDarkMode: Boolean): Color {
    // esta funcion ahora ya no decide colores por su cuenta.
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
    // aqui calculo el color de fondo de la tarjeta según el bloque y el tema.
    val colorFondo = obtenerColorBloqueUniversal(numeroBloque, isDarkMode)

    // aqui saco el color principal del texto desde ColoresApp.
    // asi no repito la logica del modo oscuro en cada archivo.
    val colorTexto = ColoresApp.textoPrincipal(isDarkMode)

    Card(
        modifier = modifier.fillMaxWidth(),

        // con cardcolors personalizo tanto el fondo como el color de contenido de la tarjeta.
        colors = CardDefaults.cardColors(
            containerColor = colorFondo,
            contentColor = colorTexto
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // esta primera fila contiene:
            // - a la izquierda, el nombre del ejercicio
            // - a la derecha, una especie de etiqueta con la letra del bloque
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    // si el ejercicio no tiene nombre, muestro "Sin nombre" para evitar vacíos en pantalla.
                    text = ejercicio.nombre ?: "Sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,

                    // weight hace que este texto ocupe todo el espacio posible
                    // y empuje la etiqueta del bloque hacia la derecha.
                    modifier = Modifier.weight(1f),
                    color = colorTexto
                )

                Surface(
                    // esta surface pequeña funciona como una etiqueta o badge del bloque.
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        // en horizontal enseño solo la letra para ahorrar espacio.
                        // en vertical escribo "Bloque A", "Bloque B", etc.
                        text = if (isLandscape) "$letraBloque" else "Bloque $letraBloque",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,

                        // aqui mantengo blanco fijo porque encima del color primario
                        // suele funcionar muy bien y se lee claro.
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // esta segunda fila muestra los datos cortos del ejercicio:
            // series, repeticiones y peso.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // aqui reutilizo el composable DatoUniversal para no repetir codigo.
                //
                // si estoy en horizontal uso etiquetas cortas: S, R, Kg
                // porque hay menos espacio.
                //
                // si estoy en vertical uso etiquetas completas.
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

                // aqui preparo el texto del peso.
                //
                // si el peso existe y es mayor que 0, lo muestro.
                // si no, enseño "--" para indicar que no hay peso informado.
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
    //
    // lo separo en una funcion propia porque asi reutilizo el mismo diseño
    // y mantengo coherencia visual.

    // aqui saco desde ColoresApp el color principal y el secundario.
    // asi no tengo que jugar con opacidades manuales en cada archivo.
    val colorValor = ColoresApp.textoPrincipal(isDarkMode)
    val colorLabel = ColoresApp.textoSecundario(isDarkMode)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // esta es la etiqueta pequeña de arriba.
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colorLabel
        )

        // este es el valor importante del dato.
        // lo muestro en negrita para que destaque.
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = colorValor
        )
    }
}