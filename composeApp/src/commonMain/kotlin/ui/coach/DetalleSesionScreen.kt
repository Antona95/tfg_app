package ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.SesionEntrenamiento
import ui.components.EjercicioUniversalCard
import ui.components.agruparEjercicios
import ui.components.CabeceraEstadoSesion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSesionScreen(
    sesion: SesionEntrenamiento,
    isDarkMode: Boolean,
    onBack: () -> Unit
) {
    // aqui agrupo los ejercicios segun su bloque para poder mostrar bien
    // las series normales, biseries o triseries.
    // uso remember para no recalcularlo en cada recomposicion si no cambia la sesion.
    val gruposDeEjercicios = remember(sesion.ejercicios) { agruparEjercicios(sesion.ejercicios) }

    // scaffold me da una estructura base con barra superior y contenido principal.
    Scaffold(
        topBar = {
            TopAppBar(
                title = {

                    // en el titulo pongo dos lineas:
                    // una fija con el nombre de la pantalla
                    // y otra con el titulo real de la sesion.
                    Column {
                        Text("Detalle de Sesión", style = MaterialTheme.typography.titleMedium)
                        Text(sesion.titulo ?: "Sin título", style = MaterialTheme.typography.bodyLarge)
                    }
                },
                navigationIcon = {

                    // este boton sirve para volver a la pantalla anterior.
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        // boxwithconstraints me permite saber si estoy en vertical u horizontal
        // segun el tamaño disponible de la pantalla.
        BoxWithConstraints(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {

            // si el ancho es mayor que el alto, considero que estoy en landscape.
            val isLandscape = maxWidth > maxHeight

            // uso una lazycolumn para que el contenido sea scrollable
            // si la sesion tiene muchos ejercicios.
            LazyColumn(
                modifier = Modifier.fillMaxHeight().widthIn(max = 900.dp).fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // aqui reutilizo una cabecera comun que muestra
                // el nombre de la sesion y su estado finalizada o pendiente.
                item {
                    CabeceraEstadoSesion(sesion = sesion, isDarkMode = isDarkMode)
                }

                // este texto separa visualmente la cabecera de la lista de ejercicios.
                item {
                    Text("Ejercicios Planificados", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }

                // recorro los grupos de ejercicios ya preparados.
                // cada grupo puede contener uno o varios ejercicios segun el bloque.
                itemsIndexed(gruposDeEjercicios) { indexGrupo, grupo ->

                    // calculo el numero de bloque empezando en 1.
                    val numeroBloque = indexGrupo + 1

                    // convierto el numero de bloque en una letra: 1 = a, 2 = b, etc.
                    // esto me sirve para etiquetar visualmente los grupos.
                    val letraBloque = (numeroBloque + 64).toChar()

                    // si estoy en horizontal, coloco los ejercicios del grupo en una fila.
                    if (isLandscape) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                            // recorro los ejercicios del grupo.
                            for (ejercicio in grupo) {

                                // uso weight para que todos ocupen el mismo ancho.
                                Box(modifier = Modifier.weight(1f)) {

                                    // esta tarjeta es un componente reutilizable
                                    // que pinta el ejercicio con su color, bloque y datos.
                                    EjercicioUniversalCard(ejercicio, isDarkMode, isLandscape = true, letraBloque, numeroBloque)
                                }
                            }
                        }
                    } else {

                        // si estoy en vertical, muestro los ejercicios uno debajo de otro.
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (ejercicio in grupo) {
                                EjercicioUniversalCard(ejercicio, isDarkMode, isLandscape = false, letraBloque, numeroBloque)
                            }
                        }
                    }
                }

                // este espacio al final evita que el ultimo elemento quede demasiado pegado al borde.
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}