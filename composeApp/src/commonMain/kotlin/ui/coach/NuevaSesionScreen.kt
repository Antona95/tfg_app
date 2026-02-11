package ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import network.EntrenamientoRepository
import viewmodel.SesionUiState
import viewmodel.SesionViewModel
import viewmodel.SesionViewModelFactory
import model.EjercicioDraft

// Paleta de colores para los bloques de biseries/triseries
val coloresBloques = listOf(
    Color(0xFFFFFFFF), // Bloque 0: Blanco
    Color(0xFFE3F2FD), // Bloque 1: Azul claro
    Color(0xFFF1F8E9), // Bloque 2: Verde claro
    Color(0xFFFFF3E0), // Bloque 3: Naranja claro
    Color(0xFFF3E5F5), // Bloque 4: Morado claro
    Color(0xFFEFEBE9)  // Bloque 5: Gris claro
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaSesionScreen(
    idUsuario: String,
    repository: EntrenamientoRepository,
    onNavigateBack: () -> Unit
) {
    val viewModel: SesionViewModel = viewModel(factory = SesionViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    // Estado para el Snackbar (sustituye al Toast)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var tituloSesion by remember { mutableStateOf("") }
    var listaEjercicios by remember { mutableStateOf(listOf<EjercicioDraft>()) }

    // Reacción a los cambios de estado (Éxito o Error)
    LaunchedEffect(uiState) {
        when (uiState) {
            is SesionUiState.Success -> {
                snackbarHostState.showSnackbar("¡Sesión guardada con éxito!")
                viewModel.resetState()
                onNavigateBack()
            }
            is SesionUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as SesionUiState.Error).mensaje)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Nueva Sesión", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Título de la sesión
            OutlinedTextField(
                value = tituloSesion,
                onValueChange = { tituloSesion = it },
                label = { Text("Título de la sesión") },
                placeholder = { Text("Ej: Empuje - Hipertrofia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTONES DE AGRUPACIÓN ---
            Text("Agrupar últimos ejercicios:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { listaEjercicios = viewModel.agruparUltimosEnDrafts(listaEjercicios, 2) },
                    enabled = listaEjercicios.size >= 2,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
                ) { Text("Biserie") }

                Button(
                    onClick = { listaEjercicios = viewModel.agruparUltimosEnDrafts(listaEjercicios, 3) },
                    enabled = listaEjercicios.size >= 3,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
                ) { Text("Triserie") }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // --- LISTA DE EJERCICIOS ---
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                itemsIndexed(listaEjercicios) { index, ej ->
                    val anterior = listaEjercicios.getOrNull(index - 1)
                    val siguiente = listaEjercicios.getOrNull(index + 1)

                    val unidoArriba = ej.bloque != 0 && ej.bloque == anterior?.bloque
                    val unidoAbajo = ej.bloque != 0 && ej.bloque == siguiente?.bloque

                    EjercicioItemCard(
                        ejercicio = ej,
                        unidoArriba = unidoArriba,
                        unidoAbajo = unidoAbajo,
                        onDelete = {
                            listaEjercicios = listaEjercicios.toMutableList().apply { removeAt(index) }
                        },
                        onUpdate = { nuevo ->
                            listaEjercicios = listaEjercicios.toMutableList().apply { set(index, nuevo) }
                        }
                    )
                }

                item {
                    TextButton(
                        onClick = {
                            listaEjercicios = listaEjercicios + EjercicioDraft("", "", "10", "", "", 0)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Añadir ejercicio")
                    }
                }
            }

            // --- BOTÓN GUARDAR ---
            Button(
                onClick = {
                    if (tituloSesion.isNotBlank() && listaEjercicios.isNotEmpty()) {
                        viewModel.guardarSesion(idUsuario, tituloSesion, "2026-02-11", listaEjercicios)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState !is SesionUiState.Loading
            ) {
                if (uiState is SesionUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("FINALIZAR Y GUARDAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EjercicioItemCard(
    ejercicio: EjercicioDraft,
    unidoArriba: Boolean,
    unidoAbajo: Boolean,
    onDelete: () -> Unit,
    onUpdate: (EjercicioDraft) -> Unit
) {
    val shape = RoundedCornerShape(
        topStart = if (unidoArriba) 0.dp else 12.dp,
        topEnd = if (unidoArriba) 0.dp else 12.dp,
        bottomStart = if (unidoAbajo) 0.dp else 12.dp,
        bottomEnd = if (unidoAbajo) 0.dp else 12.dp
    )

    Card(
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (unidoArriba) 0.dp else 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ejercicio.bloque == 0) Color.White
            else coloresBloques[ejercicio.bloque % coloresBloques.size]
        ),
        elevation = CardDefaults.cardElevation(if (unidoArriba) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ejercicio.bloque != 0) {
                    val letra = (ejercicio.bloque + 64).toChar()
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "$letra",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                OutlinedTextField(
                    value = ejercicio.nombre,
                    onValueChange = { onUpdate(ejercicio.copy(nombre = it)) },
                    label = { Text("Ejercicio") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Borrar", tint = Color.LightGray)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = ejercicio.series,
                    onValueChange = { onUpdate(ejercicio.copy(series = it)) },
                    label = { Text("S") },
                    modifier = Modifier.weight(0.7f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = ejercicio.repeticiones,
                    onValueChange = { onUpdate(ejercicio.copy(repeticiones = it)) },
                    label = { Text("R") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ejercicio.peso,
                    onValueChange = { onUpdate(ejercicio.copy(peso = it)) },
                    label = { Text("kg") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}