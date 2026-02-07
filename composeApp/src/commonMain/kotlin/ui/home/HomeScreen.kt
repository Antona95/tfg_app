package ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.Persona

// ---------------------------------------------------------
// 1. EL CEREBRO (Decide qué pantalla mostrar)
// ---------------------------------------------------------
@Composable
fun HomeScreen(
    usuario: Persona,
    onLogoutClick: () -> Unit
) {
    // Normalizamos el rol a mayúsculas para evitar errores (ej: "Entrenador" vs "ENTRENADOR")
    val rol = usuario.rol?.uppercase() ?: "USUARIO"

    if (rol == "ENTRENADOR") {
        VistaEntrenador(usuario, onLogoutClick)
    } else {
        VistaCliente(usuario, onLogoutClick)
    }
}

// ---------------------------------------------------------
// 2. VISTA CLIENTE (Tu código original)
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaCliente(usuario: Persona, onLogoutClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gym Híbrido") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera
            item {
                Text(
                    text = "Hola, ${usuario.nombre} 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Tu objetivo está cerca.", style = MaterialTheme.typography.bodyMedium)
            }

            // Tarjetas de Cliente
            item {
                DashboardCard("Mi Rutina", "Ver ejercicios de hoy", Icons.Default.DateRange) { /* TODO */ }
            }
            item {
                DashboardCard("Mi Perfil", "Datos físicos y progresos", Icons.Default.Person) { /* TODO */ }
            }
        }
    }
}

// ---------------------------------------------------------
// 3. VISTA ENTRENADOR (Nueva pantalla para el Jefe)
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaEntrenador(usuario: Persona, onLogoutClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Entrenador") }, // Título diferente
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer, // Color diferente para diferenciar
                    titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera Entrenador
            item {
                Text(
                    text = "Coach ${usuario.nombre} 💪",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Gestión de gimnasio", style = MaterialTheme.typography.bodyMedium)
            }

            // Tarjetas de Entrenador (Diferentes a las del cliente)
            item {
                DashboardCard(
                    titulo = "Mis Alumnos",
                    subtitulo = "Gestionar usuarios y asignar rutinas",
                    icono = Icons.Default.Group, // Icono de grupo
                    onClick = { /* Aquí navegaremos a la lista de alumnos */ }
                )
            }
            item {
                DashboardCard(
                    titulo = "Crear Rutina",
                    subtitulo = "Diseñar nuevos entrenamientos",
                    icono = Icons.Default.Edit,
                    onClick = { /* Aquí crearemos rutinas */ }
                )
            }
        }
    }
}

// ---------------------------------------------------------
// 4. COMPONENTE COMPARTIDO (Reutilizable para ambos)
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCard(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitulo, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}