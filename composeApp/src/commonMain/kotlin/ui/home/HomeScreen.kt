package ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.EntrenamientoRepository
import model.Persona
import viewmodel.CoachViewModel

// ---------------------------------------------------------
// 1. EL CEREBRO PRINCIPAL
// ---------------------------------------------------------
@Composable
fun HomeScreen(
    usuario: Persona,
    repository: EntrenamientoRepository,
    onLogoutClick: () -> Unit
) {
    val rol = usuario.rol?.uppercase() ?: "USUARIO"

    if (rol == "ENTRENADOR") {
        VistaEntrenador(usuario, repository, onLogoutClick)
    } else {
        VistaCliente(usuario, onLogoutClick)
    }
}

// ---------------------------------------------------------
// 2. VISTA ENTRENADOR
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaEntrenador(
    usuario: Persona,
    repository: EntrenamientoRepository,
    onLogoutClick: () -> Unit
) {
    val viewModel = remember { CoachViewModel(repository) }
    val listaAlumnos by viewModel.alumnos.collectAsState()
    val cargando by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Entrenador") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Mis Alumnos (${listaAlumnos.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (listaAlumnos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes alumnos asignados aún")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listaAlumnos) { alumno ->
                        AlumnoCard(alumno)
                    }
                }
            }
        }
    }
}
// ---------------------------------------------------------
// 3. TARJETA DE ALUMNO
// ---------------------------------------------------------
@Composable
fun AlumnoCard(alumno: Persona) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val nombreSeguro = alumno.nombre ?: "?"
                val inicial = if (nombreSeguro.isNotEmpty()) nombreSeguro.take(1).uppercase() else "?"

                Text(
                    text = inicial,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${alumno.nombre ?: "Sin Nombre"} ${alumno.apellidos ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${alumno.nickname ?: "anonimo"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Icon(Icons.Default.ChevronRight, contentDescription = "Ver detalles")
        }
    }
}

// ---------------------------------------------------------
// 4. VISTA CLIENTE
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Hola, ${usuario.nombre}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rol: ${usuario.rol}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            item {
                DashboardCard(
                    titulo = "Mi Rutina",
                    subtitulo = "Ver ejercicios de hoy",
                    icono = Icons.Default.DateRange,
                    onClick = { /* Navegar a rutina */ }
                )
            }

            item {
                DashboardCard(
                    titulo = "Mi Perfil",
                    subtitulo = "Datos físicos y progresos",
                    icono = Icons.Default.Person,
                    onClick = { /* Navegar a perfil */ }
                )
            }
        }
    }
}

// ---------------------------------------------------------
// 5. COMPONENTE COMPARTIDO (DASHBOARD)
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}