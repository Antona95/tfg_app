package ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.Persona
import ui.theme.ColoresApp

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
    // aqui uso BoxWithConstraints para saber el tamaño disponible de la pantalla.
    // esto me permite adaptar el diseño si el móvil está en vertical o en horizontal.
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        // si el ancho es mayor que el alto, considero que la pantalla está en horizontal.
        val isLandscape = maxWidth > maxHeight

        // aqui fuerzo colores muy visibles directamente en la pantalla
        // para comprobar sin ninguna duda si el home esta aplicando cambios.
        //
        // no dependo de ColoresApp en esta prueba.
        // asi, si ahora si cambia, sabré que el problema no era la card,
        // sino la forma de conectar o percibir esos colores.
        val colorTarjetaHoy =
            if (isDarkMode) {
                androidx.compose.ui.graphics.Color(0xFF0D47A1)
            } else {
                ColoresApp.tarjetaHoy(isDarkMode)
            }

        val colorTextoTarjetaHoy =
            if (isDarkMode) {
                androidx.compose.ui.graphics.Color.White
            } else {
                ColoresApp.tarjetaHoyTexto(isDarkMode)
            }

        val colorTarjetaHistorial =
            if (isDarkMode) {
                androidx.compose.ui.graphics.Color(0xFF4A148C)
            } else {
                ColoresApp.tarjetaHistorial(isDarkMode)
            }

        val colorTextoTarjetaHistorial =
            if (isDarkMode) {
                androidx.compose.ui.graphics.Color.White
            } else {
                ColoresApp.tarjetaHistorialTexto(isDarkMode)
            }
        // scaffold me sirve como estructura base de la pantalla.
        // aquí coloco la barra superior y debajo el contenido principal.
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        // en el título muestro un pequeño bloque con icono, saludo y subtítulo.
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "usuario",
                                    modifier = Modifier.size(20.dp),

                                    // aqui uso un color centralizado para no mezclar
                                    // unos textos con el tema y otros con ColoresApp.
                                    tint = ColoresApp.textoPrincipal(isDarkMode)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Hola, ${usuario.nombre}",
                                    fontWeight = FontWeight.Bold,

                                    // tambien saco este color desde ColoresApp
                                    // para mantener el mismo criterio visual.
                                    color = ColoresApp.textoPrincipal(isDarkMode)
                                )
                            }

                            Text(
                                text = "Vamos a por todas",
                                style = MaterialTheme.typography.labelMedium,
                                color = ColoresApp.textoSecundario(isDarkMode)
                            )
                        }
                    },
                    actions = {
                        // este switch me permite cambiar entre modo claro y oscuro.
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onThemeToggle() },
                            modifier = Modifier.padding(end = 8.dp),
                            thumbContent = {
                                if (isDarkMode) {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = "Modo oscuro",
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.LightMode,
                                        contentDescription = "Modo claro",
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            }
                        )

                        // este botón cierra la sesión del usuario.
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        ) { padding ->

            // aqui construyo el menú principal del alumno en forma de cuadrícula.
            // si está en horizontal uso 2 columnas; si no, 1.
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isLandscape) 2 else 1),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(
                    vertical = if (!isLandscape) 32.dp else 0.dp
                )
            ) {

                // tarjeta para ver el entrenamiento de hoy.
                item {
                    AlumnoMenuCard(
                        titulo = "Entrenamiento de Hoy",
                        subtitulo = "Ver tu rutina y registrar pesos",
                        icono = Icons.Default.SportsGymnastics,
                        colorFondo = colorTarjetaHoy,
                        colorTexto = colorTextoTarjetaHoy,
                        isDarkMode = isDarkMode,
                        onClick = onVerHoy
                    )
                }

                // tarjeta para ver el historial.
                item {
                    AlumnoMenuCard(
                        titulo = "Historial de Sesiones",
                        subtitulo = "Consulta tus entrenos pasados",
                        icono = Icons.Default.History,
                        colorFondo = colorTarjetaHistorial,
                        colorTexto = colorTextoTarjetaHistorial,
                        isDarkMode = isDarkMode,
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
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    // esta función representa una tarjeta reutilizable del menú del alumno.
    // la separo para no repetir diseño y mantener consistencia visual.

    // preparo un color algo más suave para el subtítulo.
    val colorSubtitulo =
        if (isDarkMode) {
            colorTexto.copy(alpha = 0.92f)
        } else {
            colorTexto.copy(alpha = 0.8f)
        }
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // a la izquierda muestro título y subtítulo.
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorSubtitulo
                )
            }

            // a la derecha muestro el pictograma principal de la tarjeta.
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorTexto,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}