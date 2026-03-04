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
                        // SE MANTIENE TU SCROLL, PERO AJUSTAMOS PADDING PARA QUE OCUPE MÁS
                        modifier = Modifier.weight(0.6f).fillMaxHeight().padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pasamos errorBackend aquí
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
                        modifier = Modifier.fillMaxWidth().height(250.dp).padding(bottom = 16.dp), // Reducido el bottom padding
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), // Ajustado el padding vertical
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pasamos errorBackend aquí también
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
    var errorMessage by remember { mutableStateOf<String?>(null) }


    // Sincronizacion de exito: Vuelve al login al registrar correctamente
    LaunchedEffect(mensajeExito) {
        if (mensajeExito != null) {
            isRegistering = false
            errorMessage = null
            // Limpiamos campos para seguridad del usuario
            password = ""
        }
    }

    // AÑADIDO: Un Column que envuelve todo el formulario para compactarlo.
    // Usamos spacedBy(8.dp) para eliminar todos los Spacer() sueltos que comían mucha altura.
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp) // Espaciado automático y más pequeño
    ) {
        Text(
            text = if (isRegistering) "Registro de Usuario" else "Inicio de Sesion",
            style = MaterialTheme.typography.headlineSmall, // Reducido un poco para que quepa mejor en apaisado
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isRegistering) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre real") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

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

        // Botón principal
        Button(
            onClick = {
                // Limpiamos el error local al intentar de nuevo
                errorMessage = null

                if (isRegistering) {
                    if (nickname.isNotEmpty() && password.isNotEmpty() && nombre.isNotEmpty() && apellidos.isNotEmpty()) {
                        onRegistroClick(nickname, password, nombre, apellidos)
                    } else {
                        errorMessage = "Debe completar todos los campos"
                    }
                } else {
                    if (nickname.isNotEmpty() && password.isNotEmpty()) {
                        onLoginClick(nickname, password)
                    } else {
                        errorMessage = "Credenciales incompletas"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp) // Añadido padding superior en lugar de Spacer
        ) {
            Text(if (isRegistering) "CREAR CUENTA" else "ENTRAR")
        }

        // ✅ CORRECCIÓN 2: Mostrar el error (ya sea local o del backend)
        val errorAMostrar = errorMessage ?: errorBackend
        if (errorAMostrar != null) {
            Text(
                text = errorAMostrar,
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
            errorMessage = null
        }) {
            Text(if (isRegistering) "Volver al inicio de sesion" else "No tengo cuenta, quiero registrarme")
        }
    }
}