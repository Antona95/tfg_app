package ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.DarkMode // Icono Luna (poblacion autista)
import androidx.compose.material.icons.filled.LightMode // Icono Sol (poblacion autista)
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${usuario.nombre} 👋", fontWeight = FontWeight.Bold)
                        Text("Vamos a por todas", style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = {
                    // INTERRUPTOR CON PICTOGRAMAS INCRUSTADOS (Accesibilidad Cognitiva y Visual)
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onThemeToggle() },
                        modifier = Modifier.padding(end = 8.dp),
                        thumbContent = {
                            if (isDarkMode) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = "Modo Oscuro Activado",
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = "Modo Claro Activado",
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        }
                    )

                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, "Cerrar Sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlumnoMenuCard(
                titulo = "Entrenamiento de Hoy",
                subtitulo = "Ver tu rutina y registrar pesos",
                icono = Icons.Default.SportsGymnastics,
                colorFondo = MaterialTheme.colorScheme.primary,
                colorTexto = Color.White,
                onClick = onVerHoy
            )

            Spacer(modifier = Modifier.height(24.dp))

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

@Composable
fun AlumnoMenuCard(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorFondo: Color,
    colorTexto: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colorTexto)
                Spacer(modifier = Modifier.height(8.dp))
                Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = colorTexto.copy(alpha = 0.8f))
            }
            Icon(icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(40.dp))
        }
    }
}