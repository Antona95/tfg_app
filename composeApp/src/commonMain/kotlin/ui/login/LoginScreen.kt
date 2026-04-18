package ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app_tfg.composeapp.generated.resources.Res
import app_tfg.composeapp.generated.resources.imagen_inicial
import org.jetbrains.compose.resources.painterResource
import ui.components.CamposRegistro
import ui.components.DialogoAlerta
import ui.components.Validaciones
import ui.theme.ColoresApp

@Composable
fun LoginScreen(
    isLoading: Boolean,
    onLoginClick: (String, String) -> Unit,
    onRegistroClick: (String, String, String, String) -> Unit,
    mensajeExito: String? = null,
    errorBackend: String? = null,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    // aqui guardo todos los estados del formulario en el composable padre.
    // esto significa que el estado "vive" en LoginScreen y no dentro de FormularioAuth.
    //
    // lo hago asi porque esta pantalla cambia de estructura cuando giro el movil:
    // - en vertical sale imagen arriba y formulario abajo
    // - en horizontal sale imagen a la izquierda y formulario a la derecha
    //
    // si el estado estuviera dentro de FormularioAuth, al cambiar la estructura
    // Compose podria reconstruirlo y perder lo escrito.
    //
    // rememberSaveable sirve para conservar estos datos incluso si hay rotacion.
    var isRegistering by rememberSaveable { mutableStateOf(false) }
    var nickname by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // estos dos estados controlan el cuadro emergente de error de validacion.
    //
    // mostrarErrorValidacion decide si el dialogo se ve o no.
    // mensajeErrorValidacion guarda el texto que quiero enseñar en ese dialogo.
    var mostrarErrorValidacion by rememberSaveable { mutableStateOf(false) }
    var mensajeErrorValidacion by rememberSaveable { mutableStateOf("") }

    // LaunchedEffect se ejecuta cuando cambia mensajeExito.
    //
    // aqui lo uso para detectar que el registro ha salido bien.
    // si hay mensaje de exito:
    // - vuelvo al modo login
    // - cierro posibles errores de validacion
    // - limpio la contraseña
    LaunchedEffect(mensajeExito) {
        if (mensajeExito != null) {
            isRegistering = false
            mostrarErrorValidacion = false
            password = ""
        }
    }

    // BoxWithConstraints me permite conocer el tamaño disponible del contenedor.
    // gracias a eso puedo saber si la pantalla esta en vertical u horizontal.
    //
    // añado safeDrawingPadding para que todo el contenido respete las zonas seguras
    // del dispositivo, como la barra de navegacion y la barra superior.
    // asi evito que en horizontal el contenido se meta debajo de los botones del movil.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        // si el ancho es mayor que el alto, considero que el movil está girado.
        val isLandscape = maxWidth > maxHeight

        // Surface es un contenedor visual con el color de fondo del tema.
        Surface(color = MaterialTheme.colorScheme.background) {
            if (isLandscape) {
                // en horizontal reparto la pantalla en dos zonas.
                // Row coloca elementos uno al lado del otro.
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            // weight reparte el espacio proporcionalmente.
                            // esta caja ocupa un 40 por ciento aproximadamente.
                            .weight(0.4f)
                            .fillMaxHeight()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.imagen_inicial),
                            contentDescription = "logo aplicación",
                            modifier = Modifier.fillMaxSize(),

                            // Fit intenta meter la imagen entera sin recortarla.
                            contentScale = ContentScale.Fit
                        )
                    }

                    Column(
                        modifier = Modifier
                            // esta columna ocupa el resto del espacio, un 60 por ciento.
                            .weight(0.6f)
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp)

                            // verticalScroll permite hacer scroll si no cabe el contenido.
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // llamo al formulario y le paso todos los datos y funciones.
                        //
                        // esto es importante:
                        // FormularioAuth no decide nada por si solo,
                        // solo pinta y usa lo que le manda LoginScreen.
                        FormularioAuth(
                            isLoading = isLoading,
                            onLoginClick = onLoginClick,
                            onRegistroClick = onRegistroClick,
                            mensajeExito = mensajeExito,
                            errorBackend = errorBackend,
                            isRegistering = isRegistering,

                            // este callback sirve para cambiar entre login y registro.
                            // cuando FormularioAuth lo invoque, realmente cambiara
                            // la variable isRegistering de arriba.
                            onIsRegisteringChange = { isRegistering = it },

                            nickname = nickname,

                            // onNicknameChange es una funcion que recibe un String.
                            // se usa para actualizar el estado cuando escribo en el textfield.
                            onNicknameChange = { nickname = it },

                            password = password,
                            onPasswordChange = { password = it },
                            nombre = nombre,
                            onNombreChange = { nombre = it },
                            apellidos = apellidos,
                            onApellidosChange = { apellidos = it },
                            passwordVisible = passwordVisible,
                            onPasswordVisibleChange = { passwordVisible = it },
                            mostrarErrorValidacion = mostrarErrorValidacion,
                            onMostrarErrorValidacionChange = { mostrarErrorValidacion = it },
                            mensajeErrorValidacion = mensajeErrorValidacion,
                            onMensajeErrorValidacionChange = { mensajeErrorValidacion = it },

                            // ahora paso tambien el modo oscuro al formulario
                            // para que el dialogo reutilizable y CamposRegistro reciban este dato.
                            isDarkMode = isDarkMode
                        )
                    }
                }
            } else {
                // en vertical pongo la imagen arriba y el formulario debajo.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(Res.drawable.imagen_inicial),
                        contentDescription = "logo aplicación",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(bottom = 16.dp),

                        // Crop llena el espacio aunque recorte un poco la imagen.
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // vuelvo a usar el mismo formulario.
                        // da igual vertical u horizontal: el estado es el mismo.
                        FormularioAuth(
                            isLoading = isLoading,
                            onLoginClick = onLoginClick,
                            onRegistroClick = onRegistroClick,
                            mensajeExito = mensajeExito,
                            errorBackend = errorBackend,
                            isRegistering = isRegistering,
                            onIsRegisteringChange = { isRegistering = it },
                            nickname = nickname,
                            onNicknameChange = { nickname = it },
                            password = password,
                            onPasswordChange = { password = it },
                            nombre = nombre,
                            onNombreChange = { nombre = it },
                            apellidos = apellidos,
                            onApellidosChange = { apellidos = it },
                            passwordVisible = passwordVisible,
                            onPasswordVisibleChange = { passwordVisible = it },
                            mostrarErrorValidacion = mostrarErrorValidacion,
                            onMostrarErrorValidacionChange = { mostrarErrorValidacion = it },
                            mensajeErrorValidacion = mensajeErrorValidacion,
                            onMensajeErrorValidacionChange = { mensajeErrorValidacion = it },

                            // igual que antes, tambien lo paso aqui.
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }

            // este switch cambia el tema claro/oscuro.
            //
            // checked indica el valor actual.
            // onCheckedChange es la funcion que se ejecuta cuando el usuario lo pulsa.
            //
            // en este caso no cambio yo el estado aqui directamente,
            // sino que llamo a onThemeToggle, que viene de la pantalla superior.
            Switch(
                checked = isDarkMode,
                onCheckedChange = { onThemeToggle() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                thumbContent = {
                    if (isDarkMode) {
                        Icon(
                            Icons.Default.DarkMode,
                            contentDescription = "modo oscuro",
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    } else {
                        Icon(
                            Icons.Default.LightMode,
                            contentDescription = "modo claro",
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun FormularioAuth(
    isLoading: Boolean,
    onLoginClick: (String, String) -> Unit,
    onRegistroClick: (String, String, String, String) -> Unit,
    mensajeExito: String?,
    errorBackend: String?,
    isRegistering: Boolean,
    onIsRegisteringChange: (Boolean) -> Unit,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    nombre: String,
    onNombreChange: (String) -> Unit,
    apellidos: String,
    onApellidosChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibleChange: (Boolean) -> Unit,
    mostrarErrorValidacion: Boolean,
    onMostrarErrorValidacionChange: (Boolean) -> Unit,
    mensajeErrorValidacion: String,
    onMensajeErrorValidacionChange: (String) -> Unit,
    isDarkMode: Boolean
) {
    // este composable no tiene estado propio.
    //
    // eso significa que:
    // - no guarda variables internas con remember
    // - solo muestra datos
    // - y avisa al padre cuando hay cambios
    //
    // este patrón me ayuda a separar:
    // - quien guarda el estado: LoginScreen
    // - quien dibuja la interfaz: FormularioAuth

    // aqui preparo una paleta de colores para los campos del login simple.
    //
    // antes solo CamposRegistro usaba ColoresApp, pero estos dos campos de login
    // se quedaban con los colores por defecto de Material.
    //
    // con esto dejo registro y login con el mismo criterio visual.
    val colorTexto = ColoresApp.textoPrincipal(isDarkMode)
    val colorTextoSecundario = ColoresApp.textoSecundario(isDarkMode)

    // creo unos colores comunes para los OutlinedTextField del login simple.
    // asi mantengo uniformidad con CamposRegistro.
    val coloresCamposLogin = OutlinedTextFieldDefaults.colors(
        focusedTextColor = colorTexto,
        unfocusedTextColor = colorTexto,
        focusedBorderColor = colorTextoSecundario,
        unfocusedBorderColor = colorTextoSecundario.copy(alpha = 0.65f),
        focusedLabelColor = colorTextoSecundario,
        unfocusedLabelColor = colorTextoSecundario,
        cursorColor = colorTexto,
        focusedLeadingIconColor = colorTextoSecundario,
        unfocusedLeadingIconColor = colorTextoSecundario,
        focusedTrailingIconColor = colorTextoSecundario,
        unfocusedTrailingIconColor = colorTextoSecundario,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (isRegistering) "Registro de Usuario" else "Inicio de Sesión",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isRegistering) {
            // si estoy en modo registro, muestro el componente con todos los campos.
            CamposRegistro(
                nombre = nombre,
                onNombreChange = onNombreChange,
                apellidos = apellidos,
                onApellidosChange = onApellidosChange,
                nickname = nickname,
                onNicknameChange = onNicknameChange,
                password = password,
                onPasswordChange = onPasswordChange,
                passwordVisible = passwordVisible,

                // aqui no paso directamente una variable,
                // sino una funcion que cambia el valor actual al contrario.
                //
                // si passwordVisible es true, lo pongo false.
                // si era false, lo pongo true.
                onPasswordVisibilityChange = { onPasswordVisibleChange(!passwordVisible) },

                // ahora CamposRegistro necesita tambien el modo oscuro.
                isDarkMode = isDarkMode
            )
        } else {
            OutlinedTextField(
                value = nickname,

                // onValueChange se ejecuta cada vez que el usuario escribe algo.
                // el nuevo texto entra como parametro "it".
                // yo lo reenvio al padre para actualizar nickname.
                onValueChange = onNicknameChange,
                label = { Text("Nickname") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,

                // añado un pictograma para reforzar visualmente
                // que este campo corresponde al identificador del usuario.
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = "nickname"
                    )
                },

                // aplico los colores centralizados para no dejar este campo
                // con la apariencia por defecto mientras el registro usa ColoresApp.
                colors = coloresCamposLogin
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,

                // visualTransformation cambia la forma de mostrar el texto.
                // no cambia el valor real, solo cómo se ve en pantalla.
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),

                // añado un pictograma para identificar claramente
                // que este campo corresponde a la contraseña.
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "contraseña"
                    )
                },

                trailingIcon = {
                    val image =
                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

                    IconButton(
                        onClick = {
                            // al pulsar el icono, cambio si se ve o no la contraseña.
                            onPasswordVisibleChange(!passwordVisible)
                        }
                    ) {
                        Icon(
                            imageVector = image,
                            contentDescription = "mostrar contraseña"
                        )
                    }
                },

                // aplico tambien la paleta centralizada en este campo
                // para que login y registro se comporten igual visualmente.
                colors = coloresCamposLogin
            )
        }

        Button(
            onClick = {
                if (isRegistering) {
                    // si estoy registrando, valido localmente antes de mandar nada.
                    val error = Validaciones.validarRegistro(nickname, password, nombre, apellidos)

                    if (error != null) {
                        // si hay error, guardo el mensaje y abro el dialogo.
                        onMensajeErrorValidacionChange(error)
                        onMostrarErrorValidacionChange(true)
                    } else {
                        // si no hay error, llamo al callback de registro.
                        // este callback lo definio el padre y normalmente acaba en el viewmodel.
                        onRegistroClick(nickname, password, nombre, apellidos)
                    }
                } else {
                    // si estoy en login, compruebo que al menos no estén vacios.
                    if (nickname.isBlank() || password.isBlank()) {
                        onMensajeErrorValidacionChange(
                            "Debes rellenar tu Nickname y Password para poder entrar."
                        )
                        onMostrarErrorValidacionChange(true)
                    } else {
                        // si todo está bien, lanzo el callback de login.
                        onLoginClick(nickname, password)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(top = 8.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                // si isLoading es true, enseño el spinner.
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRegistering) {
                        // en modo registro muestro un pictograma de alta de usuario.
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "crear cuenta"
                        )
                    } else {
                        // en modo login muestro un pictograma de entrar.
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = "entrar"
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(if (isRegistering) "CREAR CUENTA" else "ENTRAR")
                }
            }
        }

        if (errorBackend != null) {
            // este error no es de validacion local.
            // viene del backend o del viewmodel, por ejemplo credenciales incorrectas.
            Text(
                text = errorBackend,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (mensajeExito != null) {
            // este mensaje suele aparecer al registrarse correctamente.
            Text(
                text = mensajeExito,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        TextButton(
            onClick = {
                // aqui cambio entre login y registro.
                onIsRegisteringChange(!isRegistering)

                // y cierro cualquier dialogo de validacion que hubiera abierto.
                onMostrarErrorValidacionChange(false)
            },
            enabled = !isLoading
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isRegistering) {
                    // si estoy en registro, este boton me devuelve al login.
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "volver al inicio de sesión"
                    )
                } else {
                    // si estoy en login, este boton me lleva a la pantalla de registro.
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "ir a registro"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    if (isRegistering) {
                        "Volver al inicio de sesión"
                    } else {
                        "No tengo cuenta, quiero registrarme"
                    }
                )
            }
        }

        DialogoAlerta(
            mostrarDialogo = mostrarErrorValidacion,
            titulo = "Campos incompletos",
            mensaje = mensajeErrorValidacion,

            // onDismiss es una función callback que se ejecuta cuando el dialogo se cierra.
            //
            // dismiss significa "cerrar" o "descartar".
            // por ejemplo, se ejecuta cuando:
            // - pulso el boton del propio dialogo
            // - o el componente decide cerrarse
            //
            // aqui lo que hago es poner mostrarErrorValidacion en false
            // para que el dialogo deje de verse.
            onDismiss = { onMostrarErrorValidacionChange(false) },

            // ahora le paso el modo oscuro para que el dialogo
            // use la nueva logica centralizada de colores.
            isDarkMode = isDarkMode
        )
    }
}