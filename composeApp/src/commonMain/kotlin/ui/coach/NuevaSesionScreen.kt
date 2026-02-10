package ui.coach // <--- CAMBIO IMPORTANTE: Ahora está en tu carpeta coach

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import network.EntrenamientoRepository
import viewmodel.SesionUiState
import viewmodel.SesionViewModel
import viewmodel.SesionViewModelFactory
import model.EjercicioDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaSesionScreen(
    idUsuario: String,
    repository: EntrenamientoRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SesionViewModel = viewModel(factory = SesionViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    var tituloSesion by remember { mutableStateOf("") }
    val listaEjercicios = remember { mutableStateListOf<EjercicioDraft>() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is SesionUiState.Success -> {
                Toast.makeText(context, " ¡Sesión guardada!", Toast.LENGTH_LONG).show()
                viewModel.resetState()
                onNavigateBack()
            }
            is SesionUiState.Error -> {
                Toast.makeText(context, " ${(uiState as SesionUiState.Error).mensaje}", Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Sesión") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Volver") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                OutlinedTextField(
                    value = tituloSesion,
                    onValueChange = { tituloSesion = it },
                    label = { Text("Título (ej: Pierna Hipertrofia)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Ejercicios", style = MaterialTheme.typography.titleMedium)

                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(listaEjercicios) { index, ej ->
                        EjercicioItemCard(ej, { listaEjercicios.removeAt(index) }, { listaEjercicios[index] = it })
                    }
                    item {
                        Button(onClick = { listaEjercicios.add(EjercicioDraft("", "3", "10", "")) }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Añadir Ejercicio")
                        }
                    }
                }

                Button(
                    onClick = {
                        if (tituloSesion.isNotBlank() && listaEjercicios.isNotEmpty()) {
                            viewModel.guardarSesion(idUsuario, tituloSesion, "2026-02-10", listaEjercicios)
                        } else {
                            Toast.makeText(context, "Pon título y ejercicios", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = uiState !is SesionUiState.Loading
                ) {
                    if (uiState is SesionUiState.Loading) Text("Guardando...") else Text("GUARDAR SESIÓN")
                }
            }
        }
    }
}

@Composable
fun EjercicioItemCard(ejercicio: EjercicioDraft, onDelete: () -> Unit, onUpdate: (EjercicioDraft) -> Unit) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = ejercicio.nombre, onValueChange = { onUpdate(ejercicio.copy(nombre = it)) }, label = { Text("Ejercicio") }, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Borrar", tint = Color.Red) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = ejercicio.series, onValueChange = { onUpdate(ejercicio.copy(series = it)) }, label = { Text("Series") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = ejercicio.repeticiones, onValueChange = { onUpdate(ejercicio.copy(repeticiones = it)) }, label = { Text("Repes") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = ejercicio.peso, onValueChange = { onUpdate(ejercicio.copy(peso = it)) }, label = { Text("kg") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
    }
}