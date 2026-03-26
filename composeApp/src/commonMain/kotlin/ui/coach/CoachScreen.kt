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
    // aqui observo la lista de alumnos que me da el viewmodel.
    // al usar collectasstate, la pantalla se actualiza sola si cambia la lista.
    val alumnos by viewModel.alumnos.collectAsState()

    // este estado me indica si hay una carga en curso.
    // por ejemplo, cuando pido alumnos al backend o creo un alumno nuevo.
    val isLoading by viewModel.isLoading.collectAsState()

    // aqui guardo el texto actual del buscador.
    val textoBusqueda by viewModel.textoBusqueda.collectAsState()

    // este estado me devuelve un posible error al registrar un alumno.
    val errorRegistro by viewModel.errorRegistro.collectAsState()

    // este booleano me dice si el registro del alumno ha ido bien.
    val registroExitoso by viewModel.registroExitoso.collectAsState()

    // este estado local controla si muestro el dialogo para crear alumno.
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    // aqui guardo temporalmente el alumno que quiero eliminar.
    // si no es null, muestro el dialogo de confirmacion.
    var alumnoAEliminar by remember { mutableStateOf<Persona?>(null) }

    // cuando el registro es exitoso, cierro el dialogo de crear alumno
    // y reseteo el estado del viewmodel para limpiar mensajes viejos.
    LaunchedEffect(registroExitoso) {
        if (registroExitoso) {
            mostrarDialogoCrear = false
            viewModel.resetRegistroState()
        }
    }

    // si el usuario ha pulsado en crear alumno, muestro el dialogo correspondiente.
    if (mostrarDialogoCrear) {
        DialogoCrearAlumno(
            errorServidor = errorRegistro,

            // le paso isloading para bloquear el formulario si hay una operacion en curso.
            isLoading = isLoading,
            onConfirmar = { nick, pass, nom, ape ->

                // al confirmar, llamo al viewmodel para crear el nuevo alumno.
                viewModel.crearNuevoAlumno(nick, pass, nom, ape)
            },
            onDescartar = {

                // si cancelo, cierro el dialogo y limpio estados de error o exito.
                mostrarDialogoCrear = false
                viewModel.resetRegistroState()
            }
        )
    }

    // si hay un alumno marcado para eliminar, muestro un cuadro de confirmacion.
    if (alumnoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { alumnoAEliminar = null },
            title = { Text("Confirmar eliminación", fontWeight = FontWeight.Bold) },

            // aqui construyo el mensaje dinamicamente con el nombre del alumno.
            text = { Text("¿Estás seguro de que quieres eliminar a ${alumnoAEliminar!!.nombre} ${alumnoAEliminar!!.apellidos}? Se borrará todo su historial y rutinas de la base de datos de forma permanente.") },
            confirmButton = {
                Button(
                    onClick = {

                        // si confirmo, pido al viewmodel que elimine al alumno por nickname.
                        viewModel.eliminarAlumno(alumnoAEliminar!!.nickname)

                        // luego limpio el estado para cerrar el dialogo.
                        alumnoAEliminar = null
                    },

                    // pongo el boton en color de error para reforzar visualmente la accion destructiva.
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar definitivamente") }
            },
            dismissButton = {

                // si cancelo, simplemente cierro el dialogo.
                TextButton(onClick = { alumnoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    // scaffold me da una estructura base con topbar y contenido principal.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Entrenador", fontWeight = FontWeight.Bold) },
                actions = {

                    // este boton abre el dialogo de crear alumno.
                    IconButton(onClick = { mostrarDialogoCrear = true }) {
                        Icon(Icons.Default.PersonAdd, "Nuevo Alumno", tint = MaterialTheme.colorScheme.primary)
                    }

                    // este switch cambia entre modo claro y oscuro.
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onThemeToggle() },
                        modifier = Modifier.padding(end = 8.dp),
                        thumbContent = {

                            // aqui cambio el icono del switch segun el tema actual.
                            if (isDarkMode) { Icon(Icons.Default.DarkMode, "Modo Oscuro", modifier = Modifier.size(SwitchDefaults.IconSize)) }
                            else { Icon(Icons.Default.LightMode, "Modo Claro", modifier = Modifier.size(SwitchDefaults.IconSize)) }
                        }
                    )

                    // este boton fuerza la recarga manual de alumnos.
                    IconButton(onClick = { viewModel.cargarAlumnos() }) { Icon(Icons.Default.Refresh, "Recargar") }

                    // este boton lanza la accion de salir de la sesion.
                    IconButton(onClick = onLogoutClick) { Icon(Icons.Default.ExitToApp, "Cerrar Sesión", tint = MaterialTheme.colorScheme.error) }
                }
            )
        },
    ) { padding ->

        // aqui coloco todo el contenido debajo de la barra superior.
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // este textfield es el buscador de alumnos.
            OutlinedTextField(
                value = textoBusqueda,

                // cada vez que escribo algo, llamo al viewmodel para aplicar el filtro.
                onValueChange = { viewModel.buscar(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar alumno...") },

                // icono de buscar a la izquierda.
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },

                trailingIcon = {

                    // si hay texto escrito, muestro el boton de limpiar.
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.buscar("") }) { Icon(Icons.Default.Clear, "Limpiar") }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // este box ocupa el resto de la pantalla.
            // dentro pinto o bien el loading, o el mensaje vacio, o la lista.
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {

                // si esta cargando y no hay alumnos todavia, muestro el progreso en el centro.
                if (isLoading && alumnos.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {

                    // si no hay alumnos, muestro un mensaje distinto segun haya filtro o no.
                    if (alumnos.isEmpty()) {
                        Text(
                            text = if (textoBusqueda.isEmpty()) "No hay alumnos registrados." else "Sin resultados.",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Gray
                        )
                    } else {

                        // si si hay alumnos, los pinto en una lista vertical.
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(alumnos) { alumno ->
                                AlumnoItem(
                                    alumno = alumno,

                                    // al pulsar sobre el alumno, aviso a la pantalla padre para navegar.
                                    onClick = { onAlumnoClick(alumno) },

                                    // si pulso borrar, guardo ese alumno para mostrar la confirmacion.
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

    // esta tarjeta representa un alumno en la lista del coach.
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // este surface pequeño funciona como avatar con la inicial del nombre.
            Surface(modifier = Modifier.size(40.dp), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = alumno.nombre.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // aqui muestro nombre completo y nickname del alumno.
            Column {
                Text(text = "${alumno.nombre} ${alumno.apellidos}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "@${alumno.nickname}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }

            Spacer(modifier = Modifier.weight(1f))

            // este boton rojo sirve para abrir la eliminacion del alumno.
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar Alumno", tint = MaterialTheme.colorScheme.error) }

            // esta flecha indica visualmente que la tarjeta se puede abrir.
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
        }
    }
}

// aqui tengo un dialogo reutilizable para crear alumnos.
// lo separo de coachscreen para que el codigo principal quede mas limpio.
@Composable
fun DialogoCrearAlumno(
    errorServidor: String?,
    isLoading: Boolean,
    onConfirmar: (String, String, String, String) -> Unit,
    onDescartar: () -> Unit
) {

    // estos estados locales guardan lo que escribo en el formulario.
    var nick by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }

    // este booleano controla si la contraseña se ve o no.
    var passwordVisible by remember { mutableStateOf(false) }

    // estos estados sirven para mostrar un dialogo de validacion si faltan datos.
    var mostrarErrorValidacion by remember { mutableStateOf(false) }
    var mensajeErrorValidacion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {

            // si hay una carga en curso, no dejo cerrar tocando fuera del dialogo.
            if (!isLoading) onDescartar()
        },
        title = { Text("Nuevo Alumno") },
        text = {
            Column {

                // aqui reutilizo el componente comun de campos de registro.
                CamposRegistro(
                    nombre = nombre, onNombreChange = { nombre = it },
                    apellidos = apellidos, onApellidosChange = { apellidos = it },
                    nickname = nick, onNicknameChange = { nick = it },
                    password = pass, onPasswordChange = { pass = it },
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                )

                // si el backend ha devuelto un error, lo muestro debajo del formulario.
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

                    // antes de enviar, valido localmente los campos.
                    val error = Validaciones.validarRegistro(nick, pass, nombre, apellidos)

                    if (error != null) {

                        // si hay un error, preparo el dialogo de alerta.
                        mensajeErrorValidacion = error
                        mostrarErrorValidacion = true
                    } else {

                        // si todo esta bien, lanzo la accion de confirmar hacia fuera.
                        onConfirmar(nick, pass, nombre, apellidos)
                    }
                },

                // si esta cargando, desactivo el boton para evitar doble clic.
                enabled = !isLoading
            ) {

                // si hay carga, muestro un spinner dentro del boton.
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

                // tambien desactivo cancelar mientras hay carga.
                enabled = !isLoading
            ) { Text("Cancelar") }
        }
    )

    // este dialogo extra muestra errores de validacion del formulario.
    DialogoAlerta(
        mostrarDialogo = mostrarErrorValidacion,
        titulo = "Revisa los datos",
        mensaje = mensajeErrorValidacion,
        onDismiss = { mostrarErrorValidacion = false }
    )
}