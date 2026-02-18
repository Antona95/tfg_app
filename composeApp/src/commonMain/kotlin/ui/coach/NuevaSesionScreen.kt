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
import model.SesionEntrenamiento

// ✅ IMPORTS DE FECHA CORRECTOS
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val coloresBloques = listOf(
    Color(0xFFFFFFFF), Color(0xFFE3F2FD), Color(0xFFF1F8E9),
    Color(0xFFFFF3E0), Color(0xFFF3E5F5), Color(0xFFEFEBE9)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaSesionScreen(
    idUsuario: String,
    repository: EntrenamientoRepository,
    onNavigateBack: () -> Unit,
    sesionBase: SesionEntrenamiento? = null
) {
    val viewModel: SesionViewModel = viewModel(factory = SesionViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var tituloSesion by remember {
        mutableStateOf(if (sesionBase != null) "Copia de ${sesionBase.fechaProgramada}" else "")
    }

    var listaEjercicios by remember {
        mutableStateOf(
            if (sesionBase != null) {
                sesionBase.ejercicios.map { detalle ->
                    EjercicioDraft(
                        nombre = detalle.nombre ?: "",
                        series = detalle.series.toString(),
                        repeticiones = detalle.repeticiones,
                        peso = detalle.peso?.toString() ?: "",
                        bloque = detalle.bloque
                    )
                }
            } else {
                listOf<EjercicioDraft>()
            }
        )
    }

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
                title = {
                    Text(
                        if (sesionBase != null) "Duplicar Sesión" else "Nueva Sesión",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {

            OutlinedTextField(
                value = tituloSesion,
                onValueChange = { tituloSesion = it },
                label = { Text("Título de la sesión") },
                placeholder = { Text("Ej: Empuje - Hipertrofia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Agrupar últimos ejercicios:", style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                itemsIndexed(listaEjercicios) { index, ej ->
                    val anterior = listaEjercicios.getOrNull(index - 1)
                    val siguiente = listaEjercicios.getOrNull(index + 1)
                    val unidoArriba = ej.bloque != 0 && ej.bloque == anterior?.bloque
                    val unidoAbajo = ej.bloque != 0 && ej.bloque == siguiente?.bloque

                    EjercicioItemCard(
                        ejercicio = ej,
                        unidoArriba = unidoArriba,
                        unidoAbajo = unidoAbajo,
                        onDelete = { listaEjercicios = listaEjercicios.toMutableList().apply { removeAt(index) } },
                        onUpdate = { nuevo -> listaEjercicios = listaEjercicios.toMutableList().apply { set(index, nuevo) } }
                    )
                }

                item {
                    TextButton(
                        onClick = {
                            listaEjercicios = listaEjercicios + EjercicioDraft(nombre = "", series = "3", repeticiones = "10", peso = "", bloque = 0)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Añadir ejercicio")
                    }
                }
            }

            Button(
                onClick = {
                    if (tituloSesion.isNotBlank() && listaEjercicios.isNotEmpty()) {

                        // ✅ CÓDIGO CORRECTO PARA FECHA REAL EN KOTLIN MULTIPLATFORM
                        val fechaHoy = Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                            .toString()

                        viewModel.guardarSesion(idUsuario, tituloSesion, fechaHoy, listaEjercicios)
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
        modifier = Modifier.fillMaxWidth().padding(top = if (unidoArriba) 0.dp else 8.dp),
        colors = CardDefaults.cardColors(containerColor = if (ejercicio.bloque == 0) Color.White else coloresBloques[ejercicio.bloque % coloresBloques.size]),
        elevation = CardDefaults.cardElevation(if (unidoArriba) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ejercicio.bloque != 0) {
                    val letra = (ejercicio.bloque + 64).toChar()
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(end = 8.dp)) {
                        Text("$letra", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                }
                OutlinedTextField(value = ejercicio.nombre, onValueChange = { onUpdate(ejercicio.copy(nombre = it)) }, label = { Text("Ejercicio") }, modifier = Modifier.weight(1f), singleLine = true)
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Borrar", tint = Color.LightGray) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(value = ejercicio.series, onValueChange = { onUpdate(ejercicio.copy(series = it)) }, label = { Text("S") }, modifier = Modifier.weight(0.7f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = ejercicio.repeticiones, onValueChange = { onUpdate(ejercicio.copy(repeticiones = it)) }, label = { Text("R") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = ejercicio.peso, onValueChange = { onUpdate(ejercicio.copy(peso = it)) }, label = { Text("kg") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
    }
}