package ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DialogoCerrarSesion(
    mostrarDialogo: Boolean,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    // este dialogo solo se muestra si el estado lo indica.
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = onCancelar,
            title = {
                Text("Confirmar salida")
            },
            text = {
                Text("¿Seguro que quieres salir?")
            },
            confirmButton = {
                Button(onClick = onConfirmar) {
                    Text("Sí, salir")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelar) {
                    Text("Cancelar")
                }
            }
        )
    }
}