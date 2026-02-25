package ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
    onLogoutClick: () -> Unit,
    isDarkMode: Boolean,          // <--- NUEVO PARÁMETRO
    onThemeToggle: () -> Unit     // <--- NUEVO PARÁMETRO
) {
    val rol = usuario.rol?.uppercase() ?: "USUARIO"

    if (rol == "ENTRENADOR") {
        VistaEntrenador(usuario, repository, onLogoutClick, isDarkMode, onThemeToggle)
    } else {
        VistaCliente(usuario, onLogoutClick, isDarkMode, onThemeToggle)
    }
}

// ---------------------------------------------------------
// 2. VISTA ENTRENADOR (CON NAVEGACIÓN)
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaEntrenador(
    usuario: Persona,
    repository: EntrenamientoRepository,
    onLogoutClick: () -> Unit,
    isDarkMode: Boolean,          // <--- NUEVO PARÁMETRO
    onThemeToggle: () -> Unit     // <--- NUEVO PARÁMETRO
) {
    val viewModel = remember { CoachViewModel(repository) }
    val listaAlumnos by viewModel.alumnos.collectAsState()
    val cargando by viewModel.isLoading.collectAsState()

    var alumnoSeleccionado by remember { mutableStateOf<Persona?>(null) }

    if (alumnoSeleccionado != null) {
        VistaDetalleAlumno(
            alumno = alumnoSeleccionado!!,
            onVolver = { alumnoSeleccionado = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Panel Entrenador") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    actions = {
                        // INTERRUPTOR DE MODO OSCURO (ENTRENADOR)
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onThemeToggle() },
                            modifier = Modifier.padding(end = 8.dp),
                            thumbContent = {
                                if (isDarkMode) {
                                    Icon(Icons.Default.DarkMode, "Modo Oscuro", modifier = Modifier.size(SwitchDefaults.IconSize))
                                } else {
                                    Icon(Icons.Default.LightMode, "Modo Claro", modifier = Modifier.size(SwitchDefaults.IconSize))
                                }
                            }
                        )

                        IconButton(onClick = onLogoutClick) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Salir")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
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
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(listaAlumnos) { alumno ->
                                AlumnoCard(
                                    alumno = alumno,
                                    onClick = { alumnoSeleccionado = alumno }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 3. TARJETA DE ALUMNO (CLICABLE)
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnoCard(
    alumno: Persona,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
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
fun VistaCliente(
    usuario: Persona,
    onLogoutClick: () -> Unit,
    isDarkMode: Boolean,          // <--- NUEVO PARÁMETRO
    onThemeToggle: () -> Unit     // <--- NUEVO PARÁMETRO
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gym Híbrido") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // INTERRUPTOR DE MODO OSCURO (CLIENTE)
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onThemeToggle() },
                        modifier = Modifier.padding(end = 8.dp),
                        thumbContent = {
                            if (isDarkMode) {
                                Icon(Icons.Default.DarkMode, "Modo Oscuro", modifier = Modifier.size(SwitchDefaults.IconSize))
                            } else {
                                Icon(Icons.Default.LightMode, "Modo Claro", modifier = Modifier.size(SwitchDefaults.IconSize))
                            }
                        }
                    )

                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 600.dp),
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

// ---------------------------------------------------------
// 6. PANTALLA DE DETALLE DEL ALUMNO
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaDetalleAlumno(
    alumno: Persona,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(alumno.nombre ?: "Alumno") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 600.dp)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (alumno.nombre ?: "?").take(1).uppercase(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${alumno.nombre} ${alumno.apellidos}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Cliente desde 2024", color = Color.Gray)

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { /* TODO: Crear Rutina */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Asignar Nueva Rutina")
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* TODO: Ver estadísticas */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver Progreso")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}