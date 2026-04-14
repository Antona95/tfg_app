package ui.coach

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
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
import model.EjercicioDraft
import model.SesionEntrenamiento
import ui.components.DialogoAlerta
import ui.components.Validaciones
import ui.components.obtenerColorBloqueUniversal
import ui.theme.ColoresApp
import viewmodel.SesionUiState
import viewmodel.SesionViewModel

fun obtenerInfoVisualBloqueDraft(lista: List<EjercicioDraft>, indexActual: Int): Pair<Char, Int> {
    // esta funcion me sirve para calcular la letra y el numero visual del bloque.
    //
    // no uso directamente el valor real de "bloque" porque:
    // - puede haber ejercicios sueltos con bloque 0
    // - puede haber bloques agrupados mezclados con ejercicios normales
    //
    // por eso recorro la lista hasta la posicion actual y voy construyendo
    // un numero de bloque visual que sea estable para la interfaz.
    var currentBlock = 1
    var lastDbBlock = -1

    for (i in 0..indexActual) {
        val ej = lista.getOrNull(i) ?: continue

        if (ej.bloque == 0) {
            // si el ejercicio va solo, normalmente avanza a un nuevo bloque visual.
            if (i > 0 && lastDbBlock != -1) currentBlock++
            else if (i > 0 && lista[i - 1].bloque == 0) currentBlock++

            lastDbBlock = -1
        } else {
            // si el ejercicio pertenece a un grupo, solo cambio de bloque visual
            // cuando cambia el id de bloque real.
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
    // aqui observo el estado general de la pantalla:
    // idle, loading, success o error.
    val uiState by viewModel.uiState.collectAsState()

    // aqui observo la lista editable de ejercicios que estoy montando.
    val listaEjercicios by viewModel.listaEjercicios.collectAsState()

    // este estado local guarda el titulo de la sesion.
    // si estoy duplicando una sesion, pongo un titulo inicial basado en ella.
    var tituloSesion by rememberSaveable {
        mutableStateOf(if (sesionBase != null) "Copia de ${sesionBase.titulo}" else "")
    }

    // estos estados controlan un dialogo simple de validacion.
    var mostrarErrorValidacion by remember { mutableStateOf(false) }
    var mensajeErrorValidacion by remember { mutableStateOf("") }

    // cuando entro con una sesion base, inicializo el formulario con sus ejercicios.
    LaunchedEffect(sesionBase) {
        viewModel.inicializarConSesionBase(sesionBase)
    }

    // si guardar la sesion sale bien, vuelvo atras y limpio el estado del viewmodel.
    LaunchedEffect(uiState) {
        if (uiState is SesionUiState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    // esta lambda me sirve para validar y guardar.
    val intentarGuardar = {
        val error = Validaciones.validarFormularioSesion(tituloSesion, listaEjercicios)

        if (error != null) {
            mensajeErrorValidacion = error
            mostrarErrorValidacion = true
        } else {
            viewModel.guardarSesion(idUsuario, tituloSesion)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // si el ancho es mayor que el alto, considero que estoy en horizontal.
        val isLandscape = maxWidth > maxHeight

        Scaffold(
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
                            Icon(Icons.Default.ArrowBack, "Volver")
                        }
                    }
                )
            },
            bottomBar = {
                // en vertical dejo el boton guardar abajo fijo para que sea mas accesible.
                if (!isLandscape) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding()
                    ) {
                        Button(
                            onClick = intentarGuardar,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = uiState !is SesionUiState.Loading
                        ) {
                            if (uiState is SesionUiState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text("GUARDAR RUTINA", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        ) { padding ->

            if (isLandscape) {
                // en horizontal reparto la pantalla en dos zonas:
                // izquierda para acciones y derecha para lista de ejercicios.
                Row(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.38f)
                            .fillMaxHeight()
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = tituloSesion,
                                onValueChange = { tituloSesion = it },
                                label = { Text("Nombre del entrenamiento") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Agrupar seleccionados:",
                                style = MaterialTheme.typography.labelSmall,
                                color = ColoresApp.textoSecundario(isDarkMode)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val ok = viewModel.agruparSeleccionados(2)

                                        if (!ok) {
                                            mensajeErrorValidacion =
                                                "debes seleccionar exactamente 2 ejercicios para crear una biserie."
                                            mostrarErrorValidacion = true
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 6.dp,
                                        vertical = 6.dp
                                    )
                                ) {
                                    Text("Biserie")
                                }

                                Button(
                                    onClick = {
                                        val ok = viewModel.agruparSeleccionados(3)

                                        if (!ok) {
                                            mensajeErrorValidacion =
                                                "debes seleccionar exactamente 3 ejercicios para crear una triserie."
                                            mostrarErrorValidacion = true
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 6.dp,
                                        vertical = 6.dp
                                    )
                                ) {
                                    Text("Triserie")
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    val ok = viewModel.desagruparSeleccionados()

                                    if (!ok) {
                                        mensajeErrorValidacion =
                                            "selecciona al menos un ejercicio para dejarlo como ejercicio único."
                                        mostrarErrorValidacion = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 6.dp,
                                    vertical = 6.dp
                                )
                            ) {
                                Text("Ejercicio único")
                            }

                            OutlinedButton(
                                onClick = { viewModel.agregarEjercicio() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 6.dp,
                                    vertical = 6.dp
                                )
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Añadir ejercicio")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = intentarGuardar,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = uiState !is SesionUiState.Loading
                        ) {
                            if (uiState is SesionUiState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text("GUARDAR RUTINA", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(0.62f)
                            .fillMaxHeight()
                    ) {
                        itemsIndexed(listaEjercicios) { index, ej ->
                            val (letra, numBloque) =
                                obtenerInfoVisualBloqueDraft(listaEjercicios, index)

                            EjercicioItemCard(
                                ejercicio = ej,
                                unidoArriba = ej.bloque != 0 &&
                                        ej.bloque == listaEjercicios.getOrNull(index - 1)?.bloque,
                                unidoAbajo = ej.bloque != 0 &&
                                        ej.bloque == listaEjercicios.getOrNull(index + 1)?.bloque,
                                isDarkMode = isDarkMode,
                                letraBloque = letra,
                                numeroBloque = numBloque,
                                onDelete = { viewModel.eliminarEjercicio(index) },
                                onUpdate = { nuevo -> viewModel.actualizarEjercicio(index, nuevo) },
                                onToggleSeleccion = {
                                    viewModel.toggleSeleccionEjercicio(index)
                                }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            } else {
                // en vertical coloco todo en columna.
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = tituloSesion,
                        onValueChange = { tituloSesion = it },
                        label = { Text("Nombre del entrenamiento") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val ok = viewModel.agruparSeleccionados(2)

                                if (!ok) {
                                    mensajeErrorValidacion =
                                        "debes seleccionar exactamente 2 ejercicios para crear una biserie."
                                    mostrarErrorValidacion = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Biserie")
                        }

                        Button(
                            onClick = {
                                val ok = viewModel.agruparSeleccionados(3)

                                if (!ok) {
                                    mensajeErrorValidacion =
                                        "debes seleccionar exactamente 3 ejercicios para crear una triserie."
                                    mostrarErrorValidacion = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Triserie")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val ok = viewModel.desagruparSeleccionados()

                            if (!ok) {
                                mensajeErrorValidacion =
                                    "selecciona al menos un ejercicio para dejarlo como ejercicio único."
                                mostrarErrorValidacion = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ejercicio único")
                    }

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(listaEjercicios) { index, ej ->
                            val (letra, numBloque) =
                                obtenerInfoVisualBloqueDraft(listaEjercicios, index)

                            EjercicioItemCard(
                                ejercicio = ej,
                                unidoArriba = ej.bloque != 0 &&
                                        ej.bloque == listaEjercicios.getOrNull(index - 1)?.bloque,
                                unidoAbajo = ej.bloque != 0 &&
                                        ej.bloque == listaEjercicios.getOrNull(index + 1)?.bloque,
                                isDarkMode = isDarkMode,
                                letraBloque = letra,
                                numeroBloque = numBloque,
                                onDelete = { viewModel.eliminarEjercicio(index) },
                                onUpdate = { nuevo -> viewModel.actualizarEjercicio(index, nuevo) },
                                onToggleSeleccion = {
                                    viewModel.toggleSeleccionEjercicio(index)
                                }
                            )
                        }

                        item {
                            TextButton(
                                onClick = { viewModel.agregarEjercicio() },
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Añadir ejercicio")
                            }
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }

            DialogoAlerta(
                mostrarDialogo = mostrarErrorValidacion,
                titulo = "Revisa los datos",
                mensaje = mensajeErrorValidacion,
                onDismiss = { mostrarErrorValidacion = false },

                // ahora tambien le paso el modo oscuro para que el dialogo
                // use la nueva logica centralizada de colores.
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
fun EjercicioItemCard(
    ejercicio: EjercicioDraft,
    unidoArriba: Boolean,
    unidoAbajo: Boolean,
    isDarkMode: Boolean,
    letraBloque: Char,
    numeroBloque: Int,
    onDelete: () -> Unit,
    onUpdate: (EjercicioDraft) -> Unit,
    onToggleSeleccion: () -> Unit
) {
    // si el ejercicio no tiene bloque, uso un color neutro.
    // si pertenece a un bloque, reutilizo el color del bloque visual.
    val colorFondo =
        if (ejercicio.bloque == 0) MaterialTheme.colorScheme.surfaceVariant
        else obtenerColorBloqueUniversal(numeroBloque, isDarkMode)

    // ahora saco todos los colores importantes desde ColoresApp.
    val colorTexto = ColoresApp.textoPrincipal(isDarkMode)
    val colorTextoSecundario = ColoresApp.textoSecundario(isDarkMode)
    val colorBordeSeleccion = ColoresApp.bordeSeleccion(isDarkMode)

    // esta shape me deja unir visualmente tarjetas del mismo bloque.
    val shape = RoundedCornerShape(
        topStart = if (unidoArriba) 0.dp else 12.dp,
        topEnd = if (unidoArriba) 0.dp else 12.dp,
        bottomStart = if (unidoAbajo) 0.dp else 12.dp,
        bottomEnd = if (unidoAbajo) 0.dp else 12.dp
    )

    // aqui preparo una paleta de colores para los textfield.
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = colorTexto,
        unfocusedTextColor = colorTexto,
        focusedBorderColor = colorTextoSecundario,
        unfocusedBorderColor = colorTextoSecundario.copy(alpha = 0.65f),
        focusedLabelColor = colorTextoSecundario,
        unfocusedLabelColor = colorTextoSecundario,
        cursorColor = colorTexto,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent
    )

    Card(
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (unidoArriba) 0.dp else 8.dp)
            .clickable { onToggleSeleccion() }
            .border(
                width = if (ejercicio.seleccionado) 2.dp else 0.dp,
                color = if (ejercicio.seleccionado) {
                    colorBordeSeleccion
                } else {
                    Color.Transparent
                },
                shape = shape
            ),
        colors = CardDefaults.cardColors(
            containerColor = colorFondo,
            contentColor = colorTexto
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = ejercicio.seleccionado,
                    onCheckedChange = { onToggleSeleccion() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colorBordeSeleccion,
                        uncheckedColor = colorTextoSecundario,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "$letraBloque",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(8.dp))

                OutlinedTextField(
                    value = ejercicio.nombre,
                    onValueChange = { onUpdate(ejercicio.copy(nombre = it)) },
                    label = { Text("Ejercicio") },
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = colorTextoSecundario
                    )
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
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = ejercicio.repeticiones,
                    onValueChange = { onUpdate(ejercicio.copy(repeticiones = it)) },
                    label = { Text("R") },
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = ejercicio.peso,
                    onValueChange = { onUpdate(ejercicio.copy(peso = it)) },
                    label = { Text("kg") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors
                )
            }
        }
    }
}