package ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.Persona

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnoHomeScreen(
    usuario: Persona,
    onVerHoy: () -> Unit,
    onVerHistorial: () -> Unit,
    onLogout: () -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    // aqui uso boxwithconstraints para saber el tamaño disponible de la pantalla.
    // esto me permite adaptar el diseño si el móvil está en vertical o en horizontal.
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        // si el ancho es mayor que el alto, considero que la pantalla está en horizontal.
        val isLandscape = maxWidth > maxHeight

        // scaffold me sirve como estructura base de la pantalla.
        // aquí coloco la barra superior y debajo el contenido principal.
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {

                        // en el título muestro un saludo personalizado con el nombre del usuario.
                        Column {
                            Text("Hola, ${usuario.nombre} 👋", fontWeight = FontWeight.Bold)
                            Text("Vamos a por todas", style = MaterialTheme.typography.labelMedium)
                        }
                    },
                    actions = {

                        // este switch cambia entre modo claro y modo oscuro.
                        // lo dejo en la parte superior porque es un ajuste global de la app.
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onThemeToggle() },
                            modifier = Modifier.padding(end = 8.dp),
                            thumbContent = {

                                // cambio el icono del switch según el tema actual.
                                if (isDarkMode) {
                                    Icon(Icons.Default.DarkMode, "Modo Oscuro", modifier = Modifier.size(SwitchDefaults.IconSize))
                                } else {
                                    Icon(Icons.Default.LightMode, "Modo Claro", modifier = Modifier.size(SwitchDefaults.IconSize))
                                }
                            }
                        )

                        // este botón sirve para cerrar la sesión del usuario.
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.ExitToApp, "Cerrar Sesión")
                        }
                    }
                )
            }
        ) { padding ->

            // aquí construyo el menú principal del alumno en forma de cuadrícula.
            // uso lazyverticalgrid porque quiero que sea adaptable y eficiente.
            LazyVerticalGrid(

                // si estoy en horizontal muestro 2 columnas.
                // si estoy en vertical muestro 1.
                columns = GridCells.Fixed(if (isLandscape) 2 else 1),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),

                // dejo separación vertical y horizontal entre tarjetas para que respire la interfaz.
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),

                // en vertical añado más espacio arriba y abajo para que visualmente quede mejor.
                contentPadding = PaddingValues(vertical = if (!isLandscape) 32.dp else 0.dp)
            ) {

                // esta tarjeta lleva al entrenamiento actual del usuario.
                item {
                    AlumnoMenuCard(
                        titulo = "Entrenamiento de Hoy",
                        subtitulo = "Ver tu rutina y registrar pesos",
                        icono = Icons.Default.SportsGymnastics,
                        colorFondo = MaterialTheme.colorScheme.primary,
                        colorTexto = Color.White,
                        onClick = onVerHoy
                    )
                }

                // esta tarjeta lleva al historial de sesiones del usuario.
                item {
                    AlumnoMenuCard(
                        titulo = "Historial de Sesiones",
                        subtitulo = "Consulta tus entrenos pasados",
                        icono = Icons.Default.History,
                        colorFondo = MaterialTheme.colorScheme.secondaryContainer,
                        colorTexto = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = onVerHistorial
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnoMenuCard(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorFondo: Color,
    colorTexto: Color,
    onClick: () -> Unit
) {
    // esta función representa una tarjeta reutilizable del menú del alumno.
    // la he separado para no repetir código y para que el diseño sea consistente.
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(140.dp),

        // redondeo las esquinas para que la tarjeta tenga un estilo más moderno.
        shape = RoundedCornerShape(16.dp),

        // el color de fondo lo recibo por parámetro para poder reutilizar la misma tarjeta
        // con distintos estilos.
        colors = CardDefaults.cardColors(containerColor = colorFondo),

        // añado una pequeña elevación para que visualmente se separe del fondo.
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // en la parte izquierda muestro el título y el subtítulo.
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )

                Spacer(modifier = Modifier.height(8.dp))

                // el subtítulo lo dejo un poco más suave bajando la opacidad.
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorTexto.copy(alpha = 0.8f)
                )
            }

            // en la parte derecha muestro el icono de la opción.
            Icon(
                icono,
                contentDescription = null,
                tint = colorTexto,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}