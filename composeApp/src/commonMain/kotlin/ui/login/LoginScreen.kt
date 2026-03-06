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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app_tfg.composeapp.generated.resources.Res
import app_tfg.composeapp.generated.resources.imagen_inicial
import org.jetbrains.compose.resources.painterResource

// AÑADIMOS NUESTROS COMPONENTES PROPIOS
import ui.components.DialogoAlerta
import ui.components.Validaciones
import ui.components.CamposRegistro

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegistroClick: (String, String, String, String) -> Unit,
    mensajeExito: String? = null,
    errorBackend: String? = null,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        Surface(color = MaterialTheme.colorScheme.background) {
            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.weight(0.4f).fillMaxHeight().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.imagen_inicial),
                            contentDescription = "Logo Aplicacion",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Column(
                        modifier = Modifier.weight(0.6f).fillMaxHeight().padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FormularioAuth(onLoginClick, onRegistroClick, mensajeExito, errorBackend)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(Res.drawable.imagen_inicial),
                        contentDescription = "Logo Aplicacion",
                        modifier = Modifier.fillMaxWidth().height(250.dp).padding(bottom = 16.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FormularioAuth(onLoginClick, onRegistroClick, mensajeExito, errorBackend)
                    }
                }
            }
            // EL INTERRUPTOR FLOTANTE
            Switch(
                checked = isDarkMode,
                onCheckedChange = { onThemeToggle() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                thumbContent = {
                    if (isDarkMode) {
                        Icon(Icons.Default.DarkMode, "Modo Oscuro", modifier = Modifier.size(SwitchDefaults.IconSize))
                    } else {
                        Icon(Icons.Default.LightMode, "Modo Claro", modifier = Modifier.size(SwitchDefaults.IconSize))
                    }
                }
            )
        }
    }
}


@Composable
fun FormularioAuth(
    onLoginClick: (String, String) -> Unit,
    onRegistroClick: (String, String, String, String) -> Unit,
    mensajeExito: String?,
    errorBackend: String?
) {
    var isRegistering by remember { mutableStateOf(false) }
    var nickname by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var mostrarErrorValidacion by remember { mutableStateOf(false) }
    var mensajeErrorValidacion by remember { mutableStateOf("") }

    LaunchedEffect(mensajeExito) {
        if (mensajeExito != null) {
            isRegistering = false
            mostrarErrorValidacion = false
            password = ""
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (isRegistering) "Registro de Usuario" else "Inicio de Sesion",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isRegistering) {
            // USAMOS NUESTRO COMPONENTE REUTILIZABLE
            CamposRegistro(
                nombre = nombre, onNombreChange = { nombre = it },
                apellidos = apellidos, onApellidosChange = { apellidos = it },
                nickname = nickname, onNicknameChange = { nickname = it },
                password = password, onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
            )
        } else {
            // Si es Login, solo pintamos los dos campos básicos
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Mostrar contrasena")
                    }
                }
            )
        }

        Button(
            onClick = {
                if (isRegistering) {
                    val error = Validaciones.validarRegistro(nickname, password, nombre, apellidos)
                    if (error != null) {
                        mensajeErrorValidacion = error
                        mostrarErrorValidacion = true
                    } else {
                        onRegistroClick(nickname, password, nombre, apellidos)
                    }
                } else {
                    if (nickname.isBlank() || password.isBlank()) {
                        mensajeErrorValidacion = "Debes rellenar tu Nickname y Password para poder entrar."
                        mostrarErrorValidacion = true
                    } else {
                        onLoginClick(nickname, password)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp)
        ) {
            Text(if (isRegistering) "CREAR CUENTA" else "ENTRAR")
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        TextButton(onClick = {
            isRegistering = !isRegistering
            mostrarErrorValidacion = false
        }) {
            Text(if (isRegistering) "Volver al inicio de sesion" else "No tengo cuenta, quiero registrarme")
        }

        DialogoAlerta(
            mostrarDialogo = mostrarErrorValidacion,
            titulo = "Campos incompletos",
            mensaje = mensajeErrorValidacion,
            onDismiss = { mostrarErrorValidacion = false }
        )
    }
}