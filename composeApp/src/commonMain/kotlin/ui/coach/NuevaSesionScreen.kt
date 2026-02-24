package ui.coach

import androidx.compose.foundation.isSystemInDarkTheme // Importante para detectar el modo si no se pasa por parametro
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

    // Detectamos el modo oscuro automáticamente (KMP)
    val isDarkMode = isSystemInDarkTheme()

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

    // 1. EL ENVOLTORIO MULTIPLATAFORMA
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (sesionBase != null) "Duplicar Sesión" else "Nueva Sesión", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            },
            // 2. BOTÓN DE GUARDAR FIJO (Solo en Vertical, en Horizontal lo pondremos a la izquierda)
            bottomBar = {
                if (!isLandscape) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
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
        ) { padding ->

            // 3. LA MAGIA DE LA PANTALLA DIVIDIDA
            if (isLandscape) {
                Row(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                    // PANEL IZQUIERDO: Controles (30% del ancho)
                    Column(
                        modifier = Modifier.weight(0.35f).fillMaxHeight().padding(end = 16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            OutlinedTextField(
                                value = tituloSesion,
                                onValueChange = { tituloSesion = it },
                                label = { Text("Nombre del entrenamiento") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Agrupar últimos:", style = MaterialTheme.typography.labelSmall)
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { listaEjercicios = viewModel.agruparUltimosEnDrafts(listaEjercicios, 2) }, modifier = Modifier.weight(1f)) {
                                    Text("Biserie")
                                }
                                Button(onClick = { listaEjercicios = viewModel.agruparUltimosEnDrafts(listaEjercicios, 3) }, modifier = Modifier.weight(1f)) {
                                    Text("Triserie")
                                }
                            }
                        }

                        // Botón de Guardar en la columna izquierda en Horizontal
                        Button(
                            onClick = {
                                val fechaHoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                viewModel.guardarSesion(idUsuario, tituloSesion, fechaHoy, listaEjercicios)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("GUARDAR", fontWeight = FontWeight.Bold)
                        }
                    }

                    // PANEL DERECHO: Lista de Ejercicios (65% del ancho)
                    LazyColumn(modifier = Modifier.weight(0.65f).fillMaxHeight()) {
                        item {
                            TextButton(onClick = {
                                listaEjercicios = listaEjercicios + EjercicioDraft(nombre = "", series = "3", repeticiones = "10", peso = "", bloque = 0)
                            }) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Añadir ejercicio al inicio")
                            }
                        }
                        itemsIndexed(listaEjercicios) { index, ej ->
                            val anterior = listaEjercicios.getOrNull(index - 1)
                            val siguiente = listaEjercicios.getOrNull(index + 1)

                            EjercicioItemCard(
                                ejercicio = ej,
                                unidoArriba = ej.bloque != 0 && ej.bloque == anterior?.bloque,
                                unidoAbajo = ej.bloque != 0 && ej.bloque == siguiente?.bloque,
                                isDarkMode = isDarkMode,
                                onDelete = { listaEjercicios = listaEjercicios.toMutableList().apply { removeAt(index) } },
                                onUpdate = { nuevo -> listaEjercicios = listaEjercicios.toMutableList().apply { set(index, nuevo) } }
                            )
                        }
                        // Espacio extra al final para que el último elemento no se quede pegado al borde
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            } else {
                // DISEÑO VERTICAL NORMAL
                Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = tituloSesion,
                        onValueChange = { tituloSesion = it },
                        label = { Text("Nombre del entrenamiento") },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Agrupar últimos ejercicios:", style = MaterialTheme.typography.labelSmall)
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

                            EjercicioItemCard(
                                ejercicio = ej,
                                unidoArriba = ej.bloque != 0 && ej.bloque == anterior?.bloque,
                                unidoAbajo = ej.bloque != 0 && ej.bloque == siguiente?.bloque,
                                isDarkMode = isDarkMode,
                                onDelete = { listaEjercicios = listaEjercicios.toMutableList().apply { removeAt(index) } },
                                onUpdate = { nuevo -> listaEjercicios = listaEjercicios.toMutableList().apply { set(index, nuevo) } }
                            )
                        }
                        item {
                            TextButton(onClick = {
                                listaEjercicios = listaEjercicios + EjercicioDraft(nombre = "", series = "3", repeticiones = "10", peso = "", bloque = 0)
                            }, modifier = Modifier.padding(vertical = 16.dp)) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Añadir ejercicio")
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. FUNCIÓN AUXILIAR PARA COLORES (Igual que en DetalleSesionScreen)
@Composable
fun obtenerColorBloqueForm(bloque: Int, isDarkMode: Boolean): Color {
    return if (isDarkMode) {
        when (bloque) {
            1 -> Color(0xFF0D47A1)
            2 -> Color(0xFF1B5E20)
            3 -> Color(0xFFB71C1C)
            4 -> Color(0xFF4A148C)
            5 -> Color(0xFFE65100)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    } else {
        when (bloque) {
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
    ejercicio: EjercicioDraft,
    unidoArriba: Boolean,
    unidoAbajo: Boolean,
    isDarkMode: Boolean, // AÑADIDO
    onDelete: () -> Unit,
    onUpdate: (EjercicioDraft) -> Unit
) {
    val colorFondo = obtenerColorBloqueForm(ejercicio.bloque, isDarkMode)
    // Texto blanco solo si es modo oscuro Y pertenece a una biserie (>0)
    val colorTexto = if (isDarkMode && ejercicio.bloque > 0) Color.White else MaterialTheme.colorScheme.onSurface

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
                if (ejercicio.bloque != 0) {
                    val letra = (ejercicio.bloque + 64).toChar()
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                        Text("$letra", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                }

                // Los OutlinedTextField por defecto no cambian de color interior automáticamente
                // Forzamos el color del label y del texto de entrada
                OutlinedTextField(
                    value = ejercicio.nombre,
                    onValueChange = { onUpdate(ejercicio.copy(nombre = it)) },
                    label = { Text("Ejercicio", color = colorTexto.copy(alpha = 0.8f)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorTexto,
                        unfocusedTextColor = colorTexto,
                    )
                )
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = colorTexto.copy(alpha = 0.6f)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = ejercicio.series,
                    onValueChange = { onUpdate(ejercicio.copy(series = it)) },
                    label = { Text("S", color = colorTexto.copy(alpha = 0.8f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colorTexto, unfocusedTextColor = colorTexto)
                )
                OutlinedTextField(
                    value = ejercicio.repeticiones,
                    onValueChange = { onUpdate(ejercicio.copy(repeticiones = it)) },
                    label = { Text("R", color = colorTexto.copy(alpha = 0.8f)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colorTexto, unfocusedTextColor = colorTexto)
                )
                OutlinedTextField(
                    value = ejercicio.peso,
                    onValueChange = { onUpdate(ejercicio.copy(peso = it)) },
                    label = { Text("kg", color = colorTexto.copy(alpha = 0.8f)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colorTexto, unfocusedTextColor = colorTexto)
                )
            }
        }
    }
}