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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    // aqui uso boxwithconstraints para saber el tamaño disponible de la pantalla.
    // esto me permite adaptar el diseño si el móvil está en vertical o en horizontal.
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        // si el ancho es mayor que el alto, considero que la pantalla está en horizontal.
        val isLandscape = maxWidth > maxHeight

        // ahora saco estos colores desde ColoresApp.
        val colorTarjetaHoy = ColoresApp.tarjetaHoy(isDarkMode)
        val colorTextoTarjetaHoy = ColoresApp.tarjetaHoyTexto(isDarkMode)
        val colorTarjetaHistorial = ColoresApp.tarjetaHistorial(isDarkMode)
        val colorTextoTarjetaHistorial = ColoresApp.tarjetaHistorialTexto(isDarkMode)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "usuario",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    "Hola, ${usuario.nombre}",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                "Vamos a por todas",
                                style = MaterialTheme.typography.labelMedium,
                                color = ColoresApp.textoSecundario(isDarkMode)
                            )
                        }
                    },
                    actions = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onThemeToggle() },
                            modifier = Modifier.padding(end = 8.dp),
                            thumbContent = {
                                if (isDarkMode) {
                                    Icon(
                                        Icons.Default.DarkMode,
                                        "Modo Oscuro",
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.LightMode,
                                        "Modo Claro",
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            }
                        )

                        IconButton(onClick = onLogout) {
                            Icon(
                                Icons.Default.ExitToApp,
                                "Cerrar Sesión",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        ) { padding ->

            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isLandscape) 2 else 1),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(vertical = if (!isLandscape) 32.dp else 0.dp)
            ) {

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
    colorFondo: androidx.compose.ui.graphics.Color,
    colorTexto: androidx.compose.ui.graphics.Color,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    // esta función representa una tarjeta reutilizable del menú del alumno.

    // aqui preparo un color un poco más suave para el subtitulo.
    val colorSubtitulo =
        if (isDarkMode) colorTexto.copy(alpha = 0.88f) else colorTexto.copy(alpha = 0.8f)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorSubtitulo
                )
            }

            Icon(
                icono,
                contentDescription = null,
                tint = colorTexto,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}