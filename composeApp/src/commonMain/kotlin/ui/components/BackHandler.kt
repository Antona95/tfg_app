package ui.components

import androidx.compose.runtime.Composable

// Le decimos a KMP: "Confía en mí, esta función existirá en cada plataforma"
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)