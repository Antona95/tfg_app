package ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import viewmodel.HoyUiState
import viewmodel.HoyViewModel
import model.SesionEntrenamiento
import ui.components.EjercicioUniversalCard
import ui.components.agruparEjercicios
import ui.components.CabeceraEstadoSesion
import ui.components.PantallaCargando
import ui.components.PantallaError
import ui.components.PantallaVacia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoyScreen(
    idUsuario: String,
    viewModel: HoyViewModel,
    isDarkMode: Boolean,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idUsuario) {
        viewModel.cargarEntrenamiento(idUsuario)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Entrenamiento de Hoy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight

            // ¡MIRA QUÉ LIMPIO QUEDA AHORA EL MANEJO DE ESTADOS!
            when (val state = uiState) {
                is HoyUiState.Loading -> PantallaCargando()
                is HoyUiState.Empty -> PantallaVacia(icono = "💤", mensaje = "Hoy toca descanso")
                is HoyUiState.Error -> PantallaError(mensaje = state.mensaje)
                is HoyUiState.Success -> {
                    ContenidoEntreno(state.sesion, isDarkMode, isLandscape) {
                        viewModel.finalizarEntrenamiento(state.sesion.idSesion, idUsuario) {
                            println("Finalizado")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContenidoEntreno(sesion: SesionEntrenamiento, isDarkMode: Boolean, isLandscape: Boolean, onFinalizar: () -> Unit) {
    val gruposDeEjercicios = remember(sesion.ejercicios) { agruparEjercicios(sesion.ejercicios) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight().widthIn(max = 900.dp).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // USAMOS LA NUEVA CABECERA CENTRALIZADA
            item {
                CabeceraEstadoSesion(sesion = sesion, isDarkMode = isDarkMode)
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

            item {
                if (!sesion.finalizada) {
                    Button(
                        onClick = onFinalizar,
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp)
                    ) {
                        Text("FINALIZAR ENTRENAMIENTO", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}