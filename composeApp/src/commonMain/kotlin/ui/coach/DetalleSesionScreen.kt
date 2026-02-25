package ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.SesionEntrenamiento
import model.DetalleSesion // 👈 Usamos tu nombre original para que no salga en rojo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSesionScreen(
    sesion: SesionEntrenamiento,
    isDarkMode: Boolean,
    onBack: () -> Unit
) {
    // Agrupamos los ejercicios respetando tu variable DetalleSesion
    val gruposDeEjercicios = remember(sesion.ejercicios) {
        val grupos = mutableListOf<List<DetalleSesion>>()
        var grupoActual = mutableListOf<DetalleSesion>()
        var bloqueActual = -1

        for (ej in sesion.ejercicios) {
            if (ej.bloque == 0) {
                if (grupoActual.isNotEmpty()) {
                    grupos.add(grupoActual)
                    grupoActual = mutableListOf()
                }
                grupos.add(listOf(ej))
                bloqueActual = -1
            } else {
                if (ej.bloque == bloqueActual) {
                    grupoActual.add(ej)
                } else {
                    if (grupoActual.isNotEmpty()) {
                        grupos.add(grupoActual)
                    }
                    grupoActual = mutableListOf(ej)
                    bloqueActual = ej.bloque
                }
            }
        }
        if (grupoActual.isNotEmpty()) {
            grupos.add(grupoActual)
        }
        grupos
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Detalle de Sesión", style = MaterialTheme.typography.titleMedium)
                        Text(sesion.fechaProgramada, style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        BoxWithConstraints(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isLandscape = maxWidth > maxHeight

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 900.dp)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(sesion.titulo, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (sesion.finalizada) {
                                Text("✅ FINALIZADA", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            } else {
                                Text("⏳ PENDIENTE", color = if(isDarkMode) Color(0xFFFFB74D) else Color(0xFFE65100), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text("Ejercicios Planificados", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }

                itemsIndexed(gruposDeEjercicios) { indexGrupo, grupo ->
                    val numeroBloque = indexGrupo + 1
                    val letraBloque = (numeroBloque + 64).toChar()

                    if (isLandscape) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (ejercicio in grupo) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ExerciseDetailCard(ejercicio, isDarkMode, isLandscape = true, letraBloque, numeroBloque)
                                }
                            }
                            if (grupo.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (ejercicio in grupo) {
                                ExerciseDetailCard(ejercicio, isDarkMode, isLandscape = false, letraBloque, numeroBloque)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun ExerciseDetailCard(ejercicio: DetalleSesion, isDarkMode: Boolean, isLandscape: Boolean, letraBloque: Char, numeroBloque: Int) {
    val colorFondo = obtenerColorBloque(numeroBloque, isDarkMode)
    val colorTexto = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondo, contentColor = colorTexto)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ejercicio.nombre ?: "Sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = colorTexto
                )
                Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = if (isLandscape) "$letraBloque" else "Bloque $letraBloque",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoBadge(if (isLandscape) "S" else "Series", "${ejercicio.series}", colorTexto)
                InfoBadge(if (isLandscape) "R" else "Reps", ejercicio.repeticiones, colorTexto)
                val pesoText = if (ejercicio.peso != null && ejercicio.peso > 0) "${ejercicio.peso}" else "--"
                InfoBadge(if (isLandscape) "Kg" else "Peso", pesoText, colorTexto)
            }
        }
    }
}

@Composable
private fun InfoBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

// Mantenemos tu función de colores intacta para que no falle nada
fun obtenerColorBloque(numeroBloque: Int, isDarkMode: Boolean): Color {
    val indexColor = ((numeroBloque - 1) % 5) + 1
    return if (isDarkMode) {
        when (indexColor) {
            1 -> Color(0xFF0D47A1); 2 -> Color(0xFF1B5E20); 3 -> Color(0xFFB71C1C)
            4 -> Color(0xFF4A148C); 5 -> Color(0xFFE65100); else -> Color(0xFF2C2C2C)
        }
    } else {
        when (indexColor) {
            1 -> Color(0xFFE3F2FD); 2 -> Color(0xFFE8F5E9); 3 -> Color(0xFFFFF3E0)
            4 -> Color(0xFFF3E5F5); 5 -> Color(0xFFEFEBE9); else -> Color(0xFFF5F5F5)
        }
    }
}