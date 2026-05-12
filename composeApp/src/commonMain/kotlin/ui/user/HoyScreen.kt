package ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.IntrinsicSize
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
    // aqui observo el estado de la pantalla desde el viewmodel.
    // segun este estado luego muestro carga, error, vacio o exito.
    val uiState by viewModel.uiState.collectAsState()

    // este efecto se ejecuta cuando cambia el id del usuario.
    // lo uso para cargar su entrenamiento al entrar en la pantalla.
    LaunchedEffect(idUsuario) {
        viewModel.cargarEntrenamiento(idUsuario)
    }

    // aqui intento sacar la sesion actual solo si el estado es success.
    // me viene bien para decidir si tengo que mostrar el boton de finalizar o no.
    val sesionActual = (uiState as? HoyUiState.Success)?.sesion

    // scaffold me da la estructura general de la pantalla:
    // barra superior, barra inferior y contenido principal.
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Entrenamiento de Hoy",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    // este boton me permite volver a la pantalla anterior.
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        bottomBar = {

            // solo muestro el boton de finalizar si tengo una sesion cargada
            // y esa sesion aun no esta finalizada.
            if (sesionActual != null && !sesionActual.finalizada) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()

                        // esto sirve para que el boton no quede tapado por la barra
                        // inferior del movil ni por los botones del sistema.
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {

                            // al pulsar, llamo al viewmodel para finalizar la sesion.
                            // le paso el id de la sesion y el id del usuario.
                            viewModel.finalizarEntrenamiento(sesionActual.idSesion, idUsuario) {
                                println("Finalizado")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        // aqui añado un pictograma real para reforzar la accion de completar.
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "finalizar entrenamiento"
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "FINALIZAR ENTRENAMIENTO",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->

        // BoxWithConstraints me permite saber si estoy en vertical u horizontal.
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // si el ancho es mayor que el alto, considero que estoy en landscape.
            val isLandscape = maxWidth > maxHeight

            // aqui hago el cambio de interfaz segun el estado actual.
            when (val state = uiState) {

                // si esta cargando, muestro la pantalla de carga.
                is HoyUiState.Loading -> PantallaCargando(isDarkMode = isDarkMode)

                // si no hay ninguna sesion activa, muestro una pantalla vacia.
                // antes usaba un emoji, pero ahora uso un pictograma real.
                is HoyUiState.Empty -> PantallaVacia(
                    icono = Icons.Default.Hotel,
                    mensaje = "Hoy toca descanso",
                    isDarkMode = isDarkMode
                )

                // si hay error de red o del backend, muestro el mensaje de error.
                is HoyUiState.Error -> PantallaError(
                    mensaje = state.mensaje,
                    isDarkMode = isDarkMode
                )

                // si ha ido bien, muestro el contenido de la sesion.
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
    // aqui agrupo los ejercicios por bloques.
    // esto me sirve para representar bien ejercicios normales, biseries o triseries.
    // uso remember para no recalcularlo si la sesion no cambia.
    val gruposDeEjercicios = remember(sesion.ejercicios) {
        agruparEjercicios(sesion.ejercicios)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {

        // uso una LazyColumn para que la lista de ejercicios pueda hacer scroll.
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()

                // limito el ancho maximo para que en pantallas grandes no quede demasiado estirado.
                .widthIn(max = 900.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // arriba del todo muestro una cabecera con el titulo y el estado de la sesion.
            item {
                CabeceraEstadoSesion(
                    sesion = sesion,
                    isDarkMode = isDarkMode
                )
            }

            // aqui recorro cada grupo de ejercicios.
            itemsIndexed(gruposDeEjercicios) { indexGrupo, grupo ->

                // calculo el numero de bloque empezando en 1.
                val numeroBloque = indexGrupo + 1

                // convierto ese numero en letra para mostrar algo tipo A, B, C...
                val letraBloque = (numeroBloque + 64).toChar()

                if (isLandscape) {

                    // si estoy en horizontal, coloco los ejercicios del grupo en una fila.
                    //
                    // uso height(IntrinsicSize.Min) para que la fila adopte
                    // la altura del ejercicio mas alto del grupo.
                    //
                    // asi consigo que todas las tarjetas compartan la misma altura
                    // y que la zona de series, repeticiones y peso quede alineada.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (ejercicio in grupo) {

                            // uso weight para repartir el ancho por igual.
                            //
                            // ademas uso fillMaxHeight para que cada contenedor
                            // herede la altura maxima de la fila.
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                EjercicioUniversalCard(
                                    ejercicio = ejercicio,
                                    isDarkMode = isDarkMode,
                                    isLandscape = true,
                                    letraBloque = letraBloque,
                                    numeroBloque = numeroBloque,

                                    // aqui fuerzo que la card ocupe toda la altura disponible
                                    // dentro del grupo, igual que ya haciamos en detalle.
                                    modifier = Modifier.fillMaxHeight()
                                )
                            }
                        }
                    }
                } else {

                    // si estoy en vertical, coloco los ejercicios uno debajo de otro.
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (ejercicio in grupo) {
                            EjercicioUniversalCard(
                                ejercicio = ejercicio,
                                isDarkMode = isDarkMode,
                                isLandscape = false,
                                letraBloque = letraBloque,
                                numeroBloque = numeroBloque
                            )
                        }
                    }
                }
            }

            // dejo un pequeño espacio final para que el contenido no quede pegado abajo.
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}