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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import network.EntrenamientoRepository
import viewmodel.SesionUiState
import viewmodel.SesionViewModel
import model.EjercicioDraft
import model.SesionEntrenamiento
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun obtenerInfoVisualBloque(lista: List<EjercicioDraft>, indexActual: Int): Pair<Char, Int> {
    var currentBlock = 1
    var lastDbBlock = -1
    for (i in 0..indexActual) {
        val ej = lista.getOrNull(i) ?: continue
        if (ej.bloque == 0) {
            if (i > 0 && lastDbBlock != -1) currentBlock++
            else if (i > 0 && lista[i-1].bloque == 0) currentBlock++
            lastDbBlock = -1
        } else {
            if (i > 0 && ej.bloque != lastDbBlock) currentBlock++
            lastDbBlock = ej.bloque
        }
    }
    return Pair((currentBlock + 64).toChar(), currentBlock)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaSesionScreen(
    idUsuario: String,
    viewModel: SesionViewModel,
    isDarkMode: Boolean,
    onNavigateBack: () -> Unit,
    sesionBase: SesionEntrenamiento? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val listaEjercicios by viewModel.listaEjercicios.collectAsState()

    var tituloSesion by rememberSaveable {
        mutableStateOf(if (sesionBase != null) "Copia de ${sesionBase.fechaProgramada}" else "")
    }

    LaunchedEffect(Unit) {
        viewModel.inicializarConSesionBase(sesionBase)
    }

    LaunchedEffect(uiState) {
        if (uiState is SesionUiState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (sesionBase != null) "Duplicar Sesión" else "Nueva Sesión", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Volver") } }
                )
            },
            bottomBar = {
                if (!isLandscape) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Button(
                            onClick = {
                                val fechaHoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                viewModel.guardarSesion(idUsuario, tituloSesion, fechaHoy)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = uiState !is SesionUiState.Loading
                        ) {
                            if (uiState is SesionUiState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("GUARDAR RUTINA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { padding ->
            if (isLandscape) {
                Row(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                    // Panel Lateral Izquierdo
                    Column(modifier = Modifier.weight(0.35f).fillMaxHeight().padding(end = 16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            OutlinedTextField(value = tituloSesion, onValueChange = { tituloSesion = it }, label = { Text("Nombre del entrenamiento") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // BOTÓN COPIAR ÚLTIMA (Landscape)
                            OutlinedButton(
                                onClick = { viewModel.copiarUltimaSesion(idUsuario) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.ContentCopy, null)
                                Spacer(Modifier.width(8.dp))
                                Text("CARGAR ÚLTIMA")
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Agrupar últimos:", style = MaterialTheme.typography.labelSmall)
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.agruparUltimos(2) }, modifier = Modifier.weight(1f)) { Text("Biserie") }
                                Button(onClick = { viewModel.agruparUltimos(3) }, modifier = Modifier.weight(1f)) { Text("Triserie") }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { viewModel.agregarEjercicio() },
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Añadir Ejercicio") }
                        }
                        Button(
                            onClick = {
                                val fechaHoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                viewModel.guardarSesion(idUsuario, tituloSesion, fechaHoy)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = uiState !is SesionUiState.Loading
                        ) { Text("GUARDAR", fontWeight = FontWeight.Bold) }
                    }
                    // Lista Derecha
                    LazyColumn(modifier = Modifier.weight(0.65f).fillMaxHeight()) {
                        itemsIndexed(listaEjercicios) { index, ej ->
                            val (letra, numBloque) = obtenerInfoVisualBloque(listaEjercicios, index)
                            EjercicioItemCard(
                                ejercicio = ej,
                                unidoArriba = ej.bloque != 0 && ej.bloque == listaEjercicios.getOrNull(index - 1)?.bloque,
                                unidoAbajo = ej.bloque != 0 && ej.bloque == listaEjercicios.getOrNull(index + 1)?.bloque,
                                isDarkMode = isDarkMode, letraBloque = letra, numeroBloque = numBloque,
                                onDelete = { viewModel.eliminarEjercicio(index) },
                                onUpdate = { nuevo -> viewModel.actualizarEjercicio(index, nuevo) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            } else {
                // Vista Vertical (Portrait)
                Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
                    OutlinedTextField(value = tituloSesion, onValueChange = { tituloSesion = it }, label = { Text("Nombre del entrenamiento") }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    // BOTÓN COPIAR ÚLTIMA (Portrait)
                    OutlinedButton(
                        onClick = { viewModel.copiarUltimaSesion(idUsuario) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.ContentCopy, null)
                        Spacer(Modifier.width(8.dp))
                        Text("CARGAR ÚLTIMA SESIÓN")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.agruparUltimos(2) }, modifier = Modifier.weight(1f)) { Text("Biserie") }
                        Button(onClick = { viewModel.agruparUltimos(3) }, modifier = Modifier.weight(1f)) { Text("Triserie") }
                    }
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(listaEjercicios) { index, ej ->
                            val (letra, numBloque) = obtenerInfoVisualBloque(listaEjercicios, index)
                            EjercicioItemCard(
                                ejercicio = ej,
                                unidoArriba = ej.bloque != 0 && ej.bloque == listaEjercicios.getOrNull(index - 1)?.bloque,
                                unidoAbajo = ej.bloque != 0 && ej.bloque == listaEjercicios.getOrNull(index + 1)?.bloque,
                                isDarkMode = isDarkMode, letraBloque = letra, numeroBloque = numBloque,
                                onDelete = { viewModel.eliminarEjercicio(index) },
                                onUpdate = { nuevo -> viewModel.actualizarEjercicio(index, nuevo) }
                            )
                        }
                        item {
                            TextButton(onClick = { viewModel.agregarEjercicio() }, modifier = Modifier.padding(vertical = 16.dp)) {
                                Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Añadir ejercicio")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun obtenerColorBloqueForm(numeroBloque: Int, isDarkMode: Boolean): Color {
    val indexColor = ((numeroBloque - 1) % 5) + 1
    return if (isDarkMode) {
        when (indexColor) {
            1 -> Color(0xFF0D47A1)
            2 -> Color(0xFF1B5E20)
            3 -> Color(0xFFB71C1C)
            4 -> Color(0xFF4A148C)
            5 -> Color(0xFFE65100)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    } else {
        when (indexColor) {
            1 -> Color(0xFFE3F2FD)
            2 -> Color(0xFFE8F5E9)
            3 -> Color(0xFFFFF3E0)
            4 -> Color(0xFFF3E5F5)
            5 -> Color(0xFFEFEBE9)
            else -> Color.White
        }
    }
}

@Composable
fun EjercicioItemCard(
    ejercicio: EjercicioDraft, unidoArriba: Boolean, unidoAbajo: Boolean, isDarkMode: Boolean,
    letraBloque: Char, numeroBloque: Int, onDelete: () -> Unit, onUpdate: (EjercicioDraft) -> Unit
) {
    val colorFondo = obtenerColorBloqueForm(numeroBloque, isDarkMode)
    val colorTexto = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface

    val shape = RoundedCornerShape(
        topStart = if (unidoArriba) 0.dp else 12.dp, topEnd = if (unidoArriba) 0.dp else 12.dp,
        bottomStart = if (unidoAbajo) 0.dp else 12.dp, bottomEnd = if (unidoAbajo) 0.dp else 12.dp
    )

    Card(
        shape = shape,
        modifier = Modifier.fillMaxWidth().padding(top = if (unidoArriba) 0.dp else 8.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo, contentColor = colorTexto)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                    Text("$letraBloque", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorTexto, unfocusedTextColor = colorTexto,
                    focusedBorderColor = colorTexto.copy(alpha = 0.8f), unfocusedBorderColor = colorTexto.copy(alpha = 0.4f),
                    focusedLabelColor = colorTexto, unfocusedLabelColor = colorTexto.copy(alpha = 0.7f), cursorColor = colorTexto
                )
                OutlinedTextField(value = ejercicio.nombre, onValueChange = { onUpdate(ejercicio.copy(nombre = it)) }, label = { Text("Ejercicio") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = colorTexto.copy(alpha = 0.6f)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorTexto, unfocusedTextColor = colorTexto,
                    focusedBorderColor = colorTexto.copy(alpha = 0.8f), unfocusedBorderColor = colorTexto.copy(alpha = 0.4f),
                    focusedLabelColor = colorTexto, unfocusedLabelColor = colorTexto.copy(alpha = 0.7f), cursorColor = colorTexto
                )
                OutlinedTextField(value = ejercicio.series, onValueChange = { onUpdate(ejercicio.copy(series = it)) }, label = { Text("S") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors)
                OutlinedTextField(value = ejercicio.repeticiones, onValueChange = { onUpdate(ejercicio.copy(repeticiones = it)) }, label = { Text("R") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                OutlinedTextField(value = ejercicio.peso, onValueChange = { onUpdate(ejercicio.copy(peso = it)) }, label = { Text("kg") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors)
            }
        }
    }
}
