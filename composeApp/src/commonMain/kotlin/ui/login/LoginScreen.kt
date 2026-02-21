package ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
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
    mensajeExito: String? = null
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
                        modifier = Modifier.weight(0.6f).fillMaxHeight().padding(horizontal = 32.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FormularioAuth(onLoginClick, onRegistroClick, mensajeExito)
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
                        modifier = Modifier.fillMaxWidth().height(250.dp).padding(bottom = 24.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FormularioAuth(onLoginClick, onRegistroClick, mensajeExito)
                    }
                }
            }
        }
    }
}

@Composable
fun FormularioAuth(
    onLoginClick: (String, String) -> Unit,
    onRegistroClick: (String, String, String, String) -> Unit,
    mensajeExito: String?
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

    Text(
        text = if (isRegistering) "Registro de Usuario" else "Inicio de Sesion",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(24.dp))

    if (isRegistering) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre real") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text("Apellidos") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    OutlinedTextField(
        value = nickname,
        onValueChange = { nickname = it },
        label = { Text("Nombre de usuario") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Contrasena") },
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

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = {
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
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Text(if (isRegistering) "CREAR CUENTA" else "ENTRAR")
    }

    if (errorMessage != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
    }

    if (mensajeExito != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = mensajeExito,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = {
        isRegistering = !isRegistering
        errorMessage = null
    }) {
        Text(if (isRegistering) "Volver al inicio de sesion" else "No tengo cuenta, quiero registrarme")
    }
}