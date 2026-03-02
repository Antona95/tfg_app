package ui.coach

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.* // Importante para remember y getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.Persona
import viewmodel.CoachViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachScreen(
    viewModel: CoachViewModel,
    onAlumnoClick: (Persona) -> Unit,
    onLogoutClick: () -> Unit,
    onBibliotecaClick: () -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    val alumnos by viewModel.alumnos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val textoBusqueda by viewModel.textoBusqueda.collectAsState()

    // Estado para controlar la visibilidad del diálogo
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    // Mostrar el diálogo si el estado es true
    if (mostrarDialogoCrear) {
        DialogoCrearAlumno(
            onConfirmar = { nick, pass, nom, ape ->
                viewModel.crearNuevoAlumno(nick, pass, nom, ape)
                mostrarDialogoCrear = false
            },
            onDescartar = { mostrarDialogoCrear = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Entrenador", fontWeight = FontWeight.Bold) },
                actions = {
                    // BOTÓN PARA AÑADIR NUEVO ALUMNO
                    IconButton(onClick = { mostrarDialogoCrear = true }) {
                        Icon(Icons.Default.PersonAdd, "Nuevo Alumno", tint = MaterialTheme.colorScheme.primary)
                    }

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

                    IconButton(onClick = { viewModel.cargarAlumnos() }) {
                        Icon(Icons.Default.Refresh, "Recargar")
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onBibliotecaClick,
                icon = { Icon(Icons.Default.List, contentDescription = null) },
                text = { Text("Biblioteca") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // --- BUSCADOR ---
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { viewModel.buscar(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar alumno...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.buscar("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // --- LISTA ---
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    if (alumnos.isEmpty()) {
                        Text(
                            text = if (textoBusqueda.isEmpty()) "No hay alumnos registrados." else "Sin resultados.",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(alumnos) { alumno ->
                                AlumnoItem(
                                    alumno = alumno,
                                    onClick = { onAlumnoClick(alumno) },
                                    onDelete = { viewModel.eliminarAlumno(alumno.nickname) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlumnoItem(alumno: Persona, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = alumno.nombre.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "${alumno.nombre} ${alumno.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${alumno.nickname}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // BOTÓN ELIMINAR
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Eliminar Alumno", tint = MaterialTheme.colorScheme.error)
            }

            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
fun DialogoCrearAlumno(
    onConfirmar: (String, String, String, String) -> Unit,
    onDescartar: () -> Unit
) {
    var nick by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDescartar,
        title = { Text("Nuevo Alumno") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") })
                OutlinedTextField(value = nick, onValueChange = { nick = it }, label = { Text("Nickname") })
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Añadimos la misma lógica que tu Zod
                    if(nick.length >= 3 && pass.length >= 4 && nombre.isNotBlank()) {
                        onConfirmar(nick, pass, nombre, apellidos)
                    } else {
                        // Aquí podrías poner un toast o un texto de error
                        println("Datos inválidos para el backend")
                    }
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDescartar) { Text("Cancelar") }
        }
    )
}