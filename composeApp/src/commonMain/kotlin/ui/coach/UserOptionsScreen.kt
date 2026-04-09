package ui.coach

// importo layouts para organizar los elementos de la pantalla
import androidx.compose.foundation.layout.*

// importo la rejilla vertical para colocar las opciones del menú
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

// importo iconos de material design
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History

// importo componentes de material 3
import androidx.compose.material3.*

// importo estado compose para usar remember y mutableStateOf
import androidx.compose.runtime.*

// importo clases de interfaz
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// importo el modelo persona
import model.Persona

// indico que uso una api experimental de material3
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserOptionsScreen(
    usuario: Persona,
    tieneSesiones: Boolean,
    onBack: () -> Unit,
    onNuevaSesion: () -> Unit,
    onDuplicarSesion: () -> Unit,
    onVerHistorial: () -> Unit
) {
    // creo una variable de estado para controlar si muestro el aviso
    var mostrarAvisoSinSesiones by remember { mutableStateOf(false) }

    // uso boxwithconstraints para saber el tamaño disponible en pantalla
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        // compruebo si la pantalla está en horizontal
        val isLandscape = maxWidth > maxHeight

        // scaffold me da una estructura general con barra superior y contenido
        Scaffold(
            topBar = {
                TopAppBar(
                    // muestro el nombre completo del usuario seleccionado
                    title = {
                        Text(
                            usuario.nombre + " " + usuario.apellidos,
                            fontWeight = FontWeight.Bold
                        )
                    },

                    // pongo el botón de volver atrás
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "volver")
                        }
                    }
                )
            }
        ) { padding ->

            // creo una rejilla vertical para mostrar las opciones
            LazyVerticalGrid(
                // si está en horizontal uso 2 columnas, si no solo 1
                columns = GridCells.Fixed(if (isLandscape) 2 else 1),

                // aplico padding general y margen lateral
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),

                // separo los elementos entre sí
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),

                // dejo espacio arriba y abajo
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {

                // este item ocupa todo el ancho de la rejilla
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        // muestro el título principal
                        Text(
                            text = "Gestión de Usuario",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // muestro un subtítulo de ayuda
                        Text(
                            text = "¿Qué quieres hacer hoy?",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // botón para crear una sesión nueva desde cero
                item {
                    MenuButton(
                        text = "Nueva Sesión (Desde Cero)",
                        icon = Icons.Default.Add,
                        onClick = onNuevaSesion
                    )
                }

                // botón para copiar la última sesión
                item {
                    MenuButton(
                        text = "Copiar Última Sesión",
                        icon = Icons.Default.ContentCopy,
                        onClick = {
                            // si el usuario tiene sesiones, dejo duplicar
                            if (tieneSesiones) {
                                onDuplicarSesion()
                            } else {
                                // si no tiene sesiones, muestro aviso
                                mostrarAvisoSinSesiones = true
                            }
                        }
                    )
                }

                // botón para ver historial de sesiones anteriores
                item {
                    MenuButton(
                        text = "Ver Historial / Antiguas",
                        icon = Icons.Default.History,
                        onClick = onVerHistorial
                    )
                }
            }
        }

        // si no hay sesiones previas y se intenta copiar, muestro un diálogo de aviso
        if (mostrarAvisoSinSesiones) {
            AlertDialog(
                onDismissRequest = { mostrarAvisoSinSesiones = false },
                confirmButton = {
                    TextButton(onClick = { mostrarAvisoSinSesiones = false }) {
                        Text("aceptar")
                    }
                },
                title = {
                    Text("¡Ojo!")
                },
                text = {
                    Text("Primero debes crear una sesión.")
                }
            )
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    // creo un botón reutilizable para no repetir diseño
    Button(
        onClick = onClick,

        // hago que ocupe todo el ancho y tenga altura fija
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),

        // aplico forma media del tema
        shape = MaterialTheme.shapes.medium,

        // personalizo colores del botón
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        // muestro el icono
        Icon(icon, contentDescription = null)

        // dejo separación entre icono y texto
        Spacer(modifier = Modifier.width(12.dp))

        // muestro el texto del botón
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}