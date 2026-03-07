package ui.coach

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.Persona
import viewmodel.CoachViewModel

import ui.components.DialogoAlerta
import ui.components.Validaciones
import ui.components.CamposRegistro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachScreen(
    viewModel: CoachViewModel,
    onAlumnoClick: (Persona) -> Unit,
    onLogoutClick: () -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    val alumnos by viewModel.alumnos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val textoBusqueda by viewModel.textoBusqueda.collectAsState()

    val errorRegistro by viewModel.errorRegistro.collectAsState()
    val registroExitoso by viewModel.registroExitoso.collectAsState()

    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var alumnoAEliminar by remember { mutableStateOf<Persona?>(null) }

    LaunchedEffect(registroExitoso) {
        if (registroExitoso) {
            mostrarDialogoCrear = false
            viewModel.resetRegistroState()
        }
    }

    if (mostrarDialogoCrear) {
        DialogoCrearAlumno(
            errorServidor = errorRegistro,
            isLoading = isLoading, // <--- APUNTE: Le pasamos el estado de carga para bloquear el formulario
            onConfirmar = { nick, pass, nom, ape ->
                viewModel.crearNuevoAlumno(nick, pass, nom, ape)
            },
            onDescartar = {
                mostrarDialogoCrear = false
                viewModel.resetRegistroState()
            }
        )
    }

    if (alumnoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { alumnoAEliminar = null },
            title = { Text("Confirmar eliminación", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que quieres eliminar a ${alumnoAEliminar!!.nombre} ${alumnoAEliminar!!.apellidos}? Se borrará todo su historial y rutinas de la base de datos de forma permanente.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarAlumno(alumnoAEliminar!!.nickname)
                        alumnoAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar definitivamente") }
            },
            dismissButton = {
                TextButton(onClick = { alumnoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Entrenador", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { mostrarDialogoCrear = true }) {
                        Icon(Icons.Default.PersonAdd, "Nuevo Alumno", tint = MaterialTheme.colorScheme.primary)
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onThemeToggle() },
                        modifier = Modifier.padding(end = 8.dp),
                        thumbContent = {
                            if (isDarkMode) { Icon(Icons.Default.DarkMode, "Modo Oscuro", modifier = Modifier.size(SwitchDefaults.IconSize)) }
                            else { Icon(Icons.Default.LightMode, "Modo Claro", modifier = Modifier.size(SwitchDefaults.IconSize)) }
                        }
                    )

                    IconButton(onClick = { viewModel.cargarAlumnos() }) { Icon(Icons.Default.Refresh, "Recargar") }
                    IconButton(onClick = onLogoutClick) { Icon(Icons.Default.ExitToApp, "Cerrar Sesión", tint = MaterialTheme.colorScheme.error) }
                }
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { viewModel.buscar(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar alumno...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.buscar("") }) { Icon(Icons.Default.Clear, "Limpiar") }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (isLoading && alumnos.isEmpty()) {
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
                                    onDelete = { alumnoAEliminar = alumno }
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
            Surface(modifier = Modifier.size(40.dp), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = alumno.nombre.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "${alumno.nombre} ${alumno.apellidos}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "@${alumno.nickname}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar Alumno", tint = MaterialTheme.colorScheme.error) }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
        }
    }
}

// =========================================================================
// APUNTE DE CLASE: PROTECCIÓN ANTI-DOBLE CLIC
// Hemos modificado los botones de "Crear" y "Cancelar" para que se desactiven
// cuando isLoading es true. Además, impedimos que se cierre pulsando fuera.
// =========================================================================
@Composable
fun DialogoCrearAlumno(
    errorServidor: String?,
    isLoading: Boolean, // <--- NUEVO
    onConfirmar: (String, String, String, String) -> Unit,
    onDescartar: () -> Unit
) {
    var nick by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var mostrarErrorValidacion by remember { mutableStateOf(false) }
    var mensajeErrorValidacion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            // Si está cargando, bloqueamos que el usuario cierre el cuadro tocando la pantalla gris
            if (!isLoading) onDescartar()
        },
        title = { Text("Nuevo Alumno") },
        text = {
            Column {
                CamposRegistro(
                    nombre = nombre, onNombreChange = { nombre = it },
                    apellidos = apellidos, onApellidosChange = { apellidos = it },
                    nickname = nick, onNicknameChange = { nick = it },
                    password = pass, onPasswordChange = { pass = it },
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                )
                if (errorServidor != null) {
                    Text(
                        text = errorServidor,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val error = Validaciones.validarRegistro(nick, pass, nombre, apellidos)
                    if (error != null) {
                        mensajeErrorValidacion = error
                        mostrarErrorValidacion = true
                    } else {
                        onConfirmar(nick, pass, nombre, apellidos)
                    }
                },
                enabled = !isLoading // <--- BLOQUEO ANTI-DOBLE CLIC
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Crear")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDescartar,
                enabled = !isLoading // <--- BLOQUEAMOS TAMBIÉN EL BOTÓN CANCELAR
            ) { Text("Cancelar") }
        }
    )

    DialogoAlerta(
        mostrarDialogo = mostrarErrorValidacion,
        titulo = "Revisa los datos",
        mensaje = mensajeErrorValidacion,
        onDismiss = { mostrarErrorValidacion = false }
    )
}