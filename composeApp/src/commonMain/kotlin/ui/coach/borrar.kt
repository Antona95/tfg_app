package ui.coach

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BotonBorrarCoach() {
    Button(
        onClick = { /* Lógica para borrar mañana */ },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
    ) {
        Text(text = "Borrar Perfil de Coach")
    }
}