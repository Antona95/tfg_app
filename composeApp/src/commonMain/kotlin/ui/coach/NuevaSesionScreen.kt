package ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
        if (uiState is SesionUiState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (sesionBase != null) "Duplicar Sesion" else "Nueva Sesion", fontWeight = FontWeight.Bold) },
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
                label = { Text("Nombre del entrenamiento") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Agrupar ultimos ejercicios:", style = MaterialTheme.typography.labelSmall)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { listaEjercicios = viewModel.agruparUltimosEnDrafts(listaEjercicios, 2) }, modifier = Modifier.weight(1f)) {
                    Text("Biserie")
                }
                Button(onClick = { listaEjercicios = viewModel.agruparUltimosEnDrafts(listaEjercicios, 3) }, modifier = Modifier.weight(1f)) {
                    Text("Triserie")
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
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
                    TextButton(onClick = {
                        listaEjercicios = listaEjercicios + EjercicioDraft(nombre = "", series = "3", repeticiones = "10", peso = "", bloque = 0)
                    }) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Anadir ejercicio")
                    }
                }
            }

            Button(
                onClick = {
                    val fechaHoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                    viewModel.guardarSesion(idUsuario, tituloSesion, fechaHoy, listaEjercicios)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("GUARDAR RUTINA", fontWeight = FontWeight.Bold)
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
        topStart = if (unidoArriba) 0.dp else 12.dp, topEnd = if (unidoArriba) 0.dp else 12.dp,
        bottomStart = if (unidoAbajo) 0.dp else 12.dp, bottomEnd = if (unidoAbajo) 0.dp else 12.dp
    )
    Card(
        shape = shape,
        modifier = Modifier.fillMaxWidth().padding(top = if (unidoArriba) 0.dp else 8.dp),
        colors = CardDefaults.cardColors(containerColor = if (ejercicio.bloque == 0) Color.White else coloresBloques[ejercicio.bloque % coloresBloques.size])
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ejercicio.bloque != 0) {
                    // Letra en lugar de numero
                    val letra = (ejercicio.bloque + 64).toChar()
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                        Text("$letra", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                }
                OutlinedTextField(value = ejercicio.nombre, onValueChange = { onUpdate(ejercicio.copy(nombre = it)) }, label = { Text("Ejercicio") }, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Gray) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(value = ejercicio.series, onValueChange = { onUpdate(ejercicio.copy(series = it)) }, label = { Text("S") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = ejercicio.repeticiones, onValueChange = { onUpdate(ejercicio.copy(repeticiones = it)) }, label = { Text("R") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = ejercicio.peso, onValueChange = { onUpdate(ejercicio.copy(peso = it)) }, label = { Text("kg") }, modifier = Modifier.weight(1f))
            }
        }
    }
}