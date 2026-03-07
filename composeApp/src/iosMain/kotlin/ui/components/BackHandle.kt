package ui.components

import androidx.compose.runtime.Composable

// En iOS no hay botón físico de retroceso, así que se queda vacío
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {

}