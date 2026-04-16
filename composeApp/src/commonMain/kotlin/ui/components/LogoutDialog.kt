package ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
                    // añado un pictograma real para reforzar la accion de salir.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "salir"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sí, salir")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelar) {
                    // añado un pictograma real para reforzar la accion de cancelar.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "cancelar"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancelar")
                    }
                }
            }
        )
    }
}