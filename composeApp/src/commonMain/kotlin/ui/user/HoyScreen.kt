package ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
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
import model.SesionEntrenamiento
import ui.components.CabeceraEstadoSesion
import ui.components.EjercicioUniversalCard
import ui.components.PantallaCargando
import ui.components.PantallaError
import ui.components.PantallaVacia
import ui.components.agruparEjercicios
import viewmodel.HoyUiState
import viewmodel.HoyViewModel

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

    val sesionActual = (uiState as? HoyUiState.Success)?.sesion

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
        },
        bottomBar = {
            if (sesionActual != null && !sesionActual.finalizada) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.finalizarEntrenamiento(sesionActual.idSesion, idUsuario) {
                                println("Finalizado")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("FINALIZAR ENTRENAMIENTO", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val isLandscape = maxWidth > maxHeight

            when (val state = uiState) {
                is HoyUiState.Loading -> PantallaCargando()
                is HoyUiState.Empty -> PantallaVacia(icono = "💤", mensaje = "Hoy toca descanso")
                is HoyUiState.Error -> PantallaError(mensaje = state.mensaje)
                is HoyUiState.Success -> {
                    ContenidoEntreno(
                        sesion = state.sesion,
                        isDarkMode = isDarkMode,
                        isLandscape = isLandscape
                    )
                }
            }
        }
    }
}

@Composable
fun ContenidoEntreno(
    sesion: SesionEntrenamiento,
    isDarkMode: Boolean,
    isLandscape: Boolean
) {
    val gruposDeEjercicios = remember(sesion.ejercicios) {
        agruparEjercicios(sesion.ejercicios)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 900.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CabeceraEstadoSesion(sesion = sesion, isDarkMode = isDarkMode)
            }

            itemsIndexed(gruposDeEjercicios) { indexGrupo, grupo ->
                val numeroBloque = indexGrupo + 1
                val letraBloque = (numeroBloque + 64).toChar()

                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (ejercicio in grupo) {
                            Box(modifier = Modifier.weight(1f)) {
                                EjercicioUniversalCard(
                                    ejercicio,
                                    isDarkMode,
                                    isLandscape = true,
                                    letraBloque,
                                    numeroBloque
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (ejercicio in grupo) {
                            EjercicioUniversalCard(
                                ejercicio,
                                isDarkMode,
                                isLandscape = false,
                                letraBloque,
                                numeroBloque
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}