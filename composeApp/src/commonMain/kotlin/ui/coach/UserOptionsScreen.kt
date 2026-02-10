package ui.coach

import androidx.compose.foundation.layout.*
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
    Scaffold(
        topBar = {
            TopAppBar(
                // Mostramos el nombre del usuario seleccionado
                title = { Text(usuario.nombre + " " + usuario.apellidos) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gestión de Usuario",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "¿Qué quieres hacer hoy?",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // OPCIÓN 1: CREAR DESDE CERO
            MenuButton(
                text = "Nueva Sesión (Desde Cero)",
                icon = Icons.Default.Add,
                onClick = onNuevaSesion
            )

            Spacer(modifier = Modifier.height(16.dp))

            // OPCIÓN 2: DUPLICAR (PROGRESAR)
            MenuButton(
                text = "Copiar Última Sesión",
                icon = Icons.Default.ContentCopy,
                onClick = onDuplicarSesion
            )

            Spacer(modifier = Modifier.height(16.dp))

            // OPCIÓN 3: HISTORIAL
            MenuButton(
                text = "Ver Historial / Antiguas",
                icon = Icons.Default.History,
                onClick = onVerHistorial
            )
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
