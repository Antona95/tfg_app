package ui.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan // IMPORTANTE PARA EL TÍTULO
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    onBack: () -> Unit,
    onNuevaSesion: () -> Unit,
    onDuplicarSesion: () -> Unit,
    onVerHistorial: () -> Unit
) {
    // 1. EL ENVOLTORIO MULTIPLATAFORMA
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        Scaffold(
            topBar = {
                TopAppBar(
                    // Mostramos el nombre del usuario seleccionado
                    title = { Text(usuario.nombre + " " + usuario.apellidos, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            // 2. LA CUADRÍCULA MÁGICA
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isLandscape) 2 else 1),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp), // Quitamos el padding vertical general para que el scroll fluya mejor
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {

                // CABECERA: Ocupa todo el ancho (maxLineSpan)
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

                // OPCIÓN 1: CREAR DESDE CERO
                item {
                    MenuButton(
                        text = "Nueva Sesión (Desde Cero)",
                        icon = Icons.Default.Add,
                        onClick = onNuevaSesion
                    )
                }

                // OPCIÓN 2: DUPLICAR (PROGRESAR)
                item {
                    MenuButton(
                        text = "Copiar Última Sesión",
                        icon = Icons.Default.ContentCopy,
                        onClick = onDuplicarSesion
                    )
                }

                // OPCIÓN 3: HISTORIAL
                item {
                    MenuButton(
                        text = "Ver Historial / Antiguas",
                        icon = Icons.Default.History,
                        onClick = onVerHistorial
                    )
                }
            }
        }
    }
}

// Componente auxiliar para los botones (para no repetir código)
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