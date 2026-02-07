package ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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

/**
 * Pantalla principal de Autenticación.
 * Gestiona tanto el Login como el Registro de nuevos usuarios.
 * Recibe funciones (lambdas) para que el ViewModel se encargue de la lógica.
 */
@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit, // Callback para iniciar sesión
    onRegistroClick: (String, String, String, String) -> Unit, // Callback para registrarse (Nickname, Pass, Nombre, Apellidos)
    mensajeExito: String? = null // Mensaje opcional para mostrar cuando se crea la cuenta con éxito
) {
    // Usamos BoxWithConstraints para saber el tamaño de la pantalla y adaptar el diseño
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        // Detectamos si es horizontal (ancho > alto) para cambiar la disposición
        val isLandscape = maxWidth > maxHeight

        Surface(color = MaterialTheme.colorScheme.background) {

            if (isLandscape) {
                // --- DISEÑO HORIZONTAL (LANDSCAPE) ---
                Row(modifier = Modifier.fillMaxSize()) {

                    // 1. PANEL IZQUIERDO (IMAGEN)
                    Box(
                        modifier = Modifier
                            .weight(0.4f) // Ocupa el 40% del ancho
                            .fillMaxHeight()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.imagen_inicial),
                            contentDescription = "Logo Gym",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // 2. PANEL DERECHO (FORMULARIO)
                    Column(
                        modifier = Modifier
                            .weight(0.6f) // Ocupa el 60% del ancho restante
                            .fillMaxHeight()
                            .padding(horizontal = 32.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FormularioAuth(onLoginClick, onRegistroClick, mensajeExito)
                    }
                }

            } else {
                // --- DISEÑO VERTICAL (PORTRAIT) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Imagen arriba
                    Image(
                        painter = painterResource(Res.drawable.imagen_inicial),
                        contentDescription = "Logo Gym",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(bottom = 24.dp),
                        contentScale = ContentScale.Crop
                    )

                    // 2. Formulario abajo
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

/**
 * 🎓 APUNTES DE COMPOSE:
 * Esta función es un COMPONENTE (@Composable).
 * Piensa en ella como una pieza de Lego que se dibuja en la pantalla.
 * Recibe "lambdas" (funciones) para avisar al padre cuando pasa algo (onClick).
 */
@Composable
fun FormularioAuth(
    onLoginClick: (String, String) -> Unit,
    onRegistroClick: (String, String, String, String) -> Unit,
    mensajeExito: String? // Dato que viene de fuera (del ViewModel)
) {
    // ---------------------------------------------------------
    // EL CEREBRO (ESTADO)
    // ---------------------------------------------------------
    var isRegistering by remember { mutableStateOf(false) } // ¿Estamos registrando o logueando?
    var nickname by remember { mutableStateOf("") }         // Lo que escribe el usuario
    var password by remember { mutableStateOf("") }

    // Estos solo se usan si isRegistering es true
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) } // ¿Se ven los puntitos o las letras?
    var errorMessage by remember { mutableStateOf<String?>(null) } // Errores locales

    // ---------------------------------------------------------
    // EFECTOS SECUNDARIOS (LÓGICA PURA)
    // ---------------------------------------------------------
    // LaunchedEffect: Código que NO DIBUJA, solo PIENSA.
    LaunchedEffect(mensajeExito) {
        if (mensajeExito != null) {
            // LÓGICA: Si hay éxito, cambiamos el modo automáticamente.
            isRegistering = false
            errorMessage = null
        }
    }

    // ---------------------------------------------------------
    // EL DIBUJO (UI)
    // ---------------------------------------------------------

    // 1. TÍTULO DINÁMICO
    Text(
        text = if (isRegistering) "Crear Cuenta" else "Bienvenido",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(24.dp))

    // 2. CAMPOS EXTRA (Solo se dibujan si estamos registrando)
    if (isRegistering) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text("Apellidos") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    // 3. CAMPOS COMUNES
    OutlinedTextField(
        value = nickname,
        onValueChange = { nickname = it },
        label = { Text("Nickname") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Contraseña") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = "Ojo contraseña")
            }
        }
    )

    Spacer(modifier = Modifier.height(32.dp))

    // 4. EL BOTÓN GORDO (ACCIÓN)
    Button(
        onClick = {
            if (isRegistering) {
                if (nickname.isNotEmpty() && password.isNotEmpty() && nombre.isNotEmpty() && apellidos.isNotEmpty()) {
                    onRegistroClick(nickname, password, nombre, apellidos)
                } else {
                    errorMessage = "Por favor, rellena todos los campos"
                }
            } else {
                if (nickname.isNotEmpty() && password.isNotEmpty()) {
                    onLoginClick(nickname, password)
                } else {
                    errorMessage = "Introduce usuario y contraseña"
                }
            }
        },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(if (isRegistering) "REGISTRARSE" else "INICIAR SESIÓN")
    }

    // 5. MENSAJES DE AVISO (ERROR O ÉXITO)
    if (errorMessage != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
    }

    // Mensaje de éxito (Estilizado)
    if (mensajeExito != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = mensajeExito,
            color = androidx.compose.ui.graphics.Color(0xFF2E7D32),
            fontSize = 18.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 6. BOTÓN PARA CAMBIAR MODO
    TextButton(onClick = {
        isRegistering = !isRegistering
        errorMessage = null
    }) {
        Text(if (isRegistering) "¿Ya tienes cuenta? Inicia Sesión" else "¿No tienes cuenta? Regístrate")
    }
}