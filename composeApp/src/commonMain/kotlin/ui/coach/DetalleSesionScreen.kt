package ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import ui.components.EjercicioUniversalCard
import ui.components.agruparEjercicios

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSesionScreen(
    sesion: SesionEntrenamiento,
    isDarkMode: Boolean,
    onBack: () -> Unit
) {
    // Usamos la lógica universal
    val gruposDeEjercicios = remember(sesion.ejercicios) { agruparEjercicios(sesion.ejercicios) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Detalle de Sesión", style = MaterialTheme.typography.titleMedium)
                        Text(sesion.titulo ?: "Sin título", style = MaterialTheme.typography.bodyLarge)
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
                modifier = Modifier.fillMaxHeight().widthIn(max = 900.dp).fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (sesion.finalizada) {
                                Text("✅ FINALIZADA", color = Color(0xFF2E7D32), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            } else {
                                Text("⏳ PENDIENTE", color = if(isDarkMode) Color(0xFFFFB74D) else Color(0xFFE65100), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                                    EjercicioUniversalCard(ejercicio, isDarkMode, isLandscape = true, letraBloque, numeroBloque)
                                }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (ejercicio in grupo) {
                                EjercicioUniversalCard(ejercicio, isDarkMode, isLandscape = false, letraBloque, numeroBloque)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}