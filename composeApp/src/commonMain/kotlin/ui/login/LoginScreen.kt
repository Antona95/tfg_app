package ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
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
import ui.components.DialogoAlerta
import ui.components.Validaciones
import ui.components.CamposRegistro

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
    // aqui guardo todos los estados del formulario en la pantalla padre.
    // lo hago asi porque si los dejo dentro de formularioauth,
    // al girar el movil cambia la rama del if/else y compose puede perderlos.
    var isRegistering by rememberSaveable { mutableStateOf(false) }
    var nickname by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    var mostrarErrorValidacion by rememberSaveable { mutableStateOf(false) }
    var mensajeErrorValidacion by rememberSaveable { mutableStateOf("") }

    // cuando el registro sale bien, vuelvo al modo login y limpio la contraseña.
    LaunchedEffect(mensajeExito) {
        if (mensajeExito != null) {
            isRegistering = false
            mostrarErrorValidacion = false
            password = ""
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // con esto detecto si estoy en horizontal o en vertical.
        val isLandscape = maxWidth > maxHeight

        Surface(color = MaterialTheme.colorScheme.background) {
            if (isLandscape) {
                // en horizontal reparto la pantalla en dos: imagen y formulario.
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.imagen_inicial),
                            contentDescription = "logo aplicación",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                            onMensajeErrorValidacionChange = { mensajeErrorValidacion = it }
                        )
                    }
                }
            } else {
                // en vertical pongo imagen arriba y formulario abajo.
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
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                            onMensajeErrorValidacionChange = { mensajeErrorValidacion = it }
                        )
                    }
                }
            }

            // este switch cambia entre modo claro y oscuro.
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
                            "modo oscuro",
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    } else {
                        Icon(
                            Icons.Default.LightMode,
                            "modo claro",
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
    onMensajeErrorValidacionChange: (String) -> Unit
) {
    // este composable ya no guarda estado propio.
    // ahora solo pinta la interfaz y usa los datos que le pasa loginscreen.

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
                onPasswordVisibilityChange = { onPasswordVisibleChange(!passwordVisible) }
            )
        } else {
            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                label = { Text("Nickname") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image =
                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { onPasswordVisibleChange(!passwordVisible) }) {
                        Icon(imageVector = image, contentDescription = "mostrar contraseña")
                    }
                }
            )
        }

        Button(
            onClick = {
                if (isRegistering) {
                    val error = Validaciones.validarRegistro(nickname, password, nombre, apellidos)
                    if (error != null) {
                        onMensajeErrorValidacionChange(error)
                        onMostrarErrorValidacionChange(true)
                    } else {
                        onRegistroClick(nickname, password, nombre, apellidos)
                    }
                } else {
                    if (nickname.isBlank() || password.isBlank()) {
                        onMensajeErrorValidacionChange(
                            "Debes rellenar tu Nickname y Password para poder entrar."
                        )
                        onMostrarErrorValidacionChange(true)
                    } else {
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
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (isRegistering) "CREAR CUENTA" else "ENTRAR")
            }
        }

        if (errorBackend != null) {
            Text(
                text = errorBackend,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (mensajeExito != null) {
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
                onIsRegisteringChange(!isRegistering)
                onMostrarErrorValidacionChange(false)
            },
            enabled = !isLoading
        ) {
            Text(
                if (isRegistering) {
                    "Volver al inicio de sesión"
                } else {
                    "No tengo cuenta, quiero registrarme"
                }
            )
        }

        DialogoAlerta(
            mostrarDialogo = mostrarErrorValidacion,
            titulo = "Campos incompletos",
            mensaje = mensajeErrorValidacion,
            onDismiss = { onMostrarErrorValidacionChange(false) }
        )
    }
}