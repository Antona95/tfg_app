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
    // esta funcion decide el color de fondo de cada bloque de ejercicios.
    //
    // la idea es que bloques distintos tengan colores distintos,
    // y que esos colores cambien segun si estoy en modo oscuro o claro.

    // aqui hago un truco para reciclar 5 colores.
    //
    // por ejemplo:
    // bloque 1 -> color 1
    // bloque 2 -> color 2
    // ...
    // bloque 6 -> vuelve a color 1
    //
    // asi no necesito definir infinitos colores.
    val indexColor = ((numeroBloque - 1) % 5) + 1

    return if (isDarkMode) {
        // en modo oscuro uso colores más intensos y profundos
        // para que contrasten bien con texto blanco.
        when (indexColor) {
            1 -> Color(0xFF0D47A1)
            2 -> Color(0xFF1B5E20)
            3 -> Color(0xFFB71C1C)
            4 -> Color(0xFF4A148C)
            5 -> Color(0xFFE65100)

            // este else actua como color de seguridad por si algo falla.
            else -> Color(0xFF2C2C2C)
        }
    } else {
        // en modo claro uso colores pastel o suaves
        // para que no molesten visualmente y sigan diferenciando bloques.
        when (indexColor) {
            1 -> Color(0xFFE3F2FD)
            2 -> Color(0xFFE8F5E9)
            3 -> Color(0xFFFFF3E0)
            4 -> Color(0xFFF3E5F5)
            5 -> Color(0xFFEFEBE9)

            // color de respaldo si no entra en ningún caso.
            else -> Color(0xFFF5F5F5)
        }
    }
}

@Composable
fun EjercicioUniversalCard(
    ejercicio: DetalleSesion,
    isDarkMode: Boolean,
    isLandscape: Boolean,
    letraBloque: Char,
    numeroBloque: Int
) {
    // aqui calculo el color de fondo de la tarjeta según el bloque y el tema.
    val colorFondo = obtenerColorBloqueUniversal(numeroBloque, isDarkMode)

    // aqui decido el color del texto principal.
    // en oscuro uso blanco para que contraste.
    // en claro uso el color de texto normal del tema.
    val colorTexto = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),

        // con cardcolors personalizo tanto el fondo como el color de contenido de la tarjeta.
        colors = CardDefaults.cardColors(containerColor = colorFondo, contentColor = colorTexto)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // esta primera fila contiene:
            // - a la izquierda, el nombre del ejercicio
            // - a la derecha, una especie de etiqueta con la letra del bloque
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    // si el ejercicio no tiene nombre, muestro "sin nombre" para evitar vacíos en pantalla.
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
                        // en vertical escribo "bloque a", "bloque b", etc.
                        text = if (isLandscape) "$letraBloque" else "Bloque $letraBloque",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
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
                // aqui reutilizo el composable datouniversal para no repetir codigo.
                //
                // si estoy en horizontal uso etiquetas cortas: s, r, kg
                // porque hay menos espacio.
                //
                // si estoy en vertical uso etiquetas completas.
                DatoUniversal(if (isLandscape) "S" else "Series", "${ejercicio.series}", colorTexto)
                DatoUniversal(if (isLandscape) "R" else "Reps", "${ejercicio.repeticiones}", colorTexto)

                // aqui preparo el texto del peso.
                //
                // si el peso existe y es mayor que 0, lo muestro.
                // si no, enseño "--" para indicar que no hay peso informado.
                val pesoText =
                    if (ejercicio.peso != null && ejercicio.peso > 0) "${ejercicio.peso}" else "--"

                DatoUniversal(if (isLandscape) "Kg" else "Peso", pesoText, colorTexto)
            }
        }
    }
}

@Composable
fun DatoUniversal(label: String, value: String, color: Color) {
    // este composable pequeño me sirve para pintar cada dato resumen del ejercicio.
    //
    // por ejemplo:
    // series -> 3
    // reps -> 10
    // peso -> 20
    //
    // lo separo en una funcion propia porque asi reutilizo el mismo diseño
    // y mantengo coherencia visual.
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // esta es la etiqueta pequeña de arriba.
        // le bajo un poco la opacidad para que tenga menos importancia visual que el valor.
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.6f)
        )

        // este es el valor importante del dato.
        // lo muestro en negrita para que destaque.
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}