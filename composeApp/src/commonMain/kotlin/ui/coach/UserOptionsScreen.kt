package ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.Persona

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserOptionsScreen(
    usuario: Persona,
    tieneSesiones: Boolean, // <--- 1. NUEVO PARÁMETRO AÑADIDO
    onBack: () -> Unit,
    onNuevaSesion: () -> Unit,
    onDuplicarSesion: () -> Unit,
    onVerHistorial: () -> Unit
) {
    // 2. ESTADO PARA CONTROLAR EL MENSAJE DE ERROR
    var mostrarErrorVacio by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(usuario.nombre + " " + usuario.apellidos, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isLandscape) 2 else 1),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "Gestión de Usuario",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "¿Qué quieres hacer hoy?",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                item {
                    MenuButton(
                        text = "Nueva Sesión (Desde Cero)",
                        icon = Icons.Default.Add,
                        onClick = onNuevaSesion
                    )
                }

                // 3. AQUÍ INTERCEPTAMOS EL CLIC DE DUPLICAR
                item {
                    MenuButton(
                        text = "Copiar Última Sesión",
                        icon = Icons.Default.ContentCopy,
                        onClick = {
                            if (tieneSesiones) {
                                onDuplicarSesion() // Todo OK, navegamos
                            } else {
                                mostrarErrorVacio = true // Ups, no hay sesiones, sacamos el aviso
                            }
                        }
                    )
                }

                item {
                    MenuButton(
                        text = "Ver Historial / Antiguas",
                        icon = Icons.Default.History,
                        onClick = onVerHistorial
                    )
                }
            }

            // 4. EL DIÁLOGO DE ALERTA EMERGENTE
            if (mostrarErrorVacio) {
                AlertDialog(
                    onDismissRequest = { mostrarErrorVacio = false },
                    title = {
                        Text("Acción no permitida", fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text("No hay ninguna sesión que copiar. Este alumno debe tener mínimo 1 sesión en su historial para poder duplicarla.")
                    },
                    confirmButton = {
                        Button(onClick = { mostrarErrorVacio = false }) {
                            Text("Entendido")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MenuButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}