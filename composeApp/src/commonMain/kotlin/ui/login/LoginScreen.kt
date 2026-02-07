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
                // Ponemos la imagen a la izquierda y el formulario a la derecha
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
                            contentScale = ContentScale.Fit // Se ve entera sin cortarse
                        )
                    }

                    // 2. PANEL DERECHO (FORMULARIO)
                    Column(
                        modifier = Modifier
                            .weight(0.6f) // Ocupa el 60% del ancho restante
                            .fillMaxHeight()
                            .padding(horizontal = 32.dp)
                            .verticalScroll(rememberScrollState()), // Permite scroll si el teclado tapa campos
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Llamamos al componente del formulario pasándole todos los datos
                        FormularioAuth(onLoginClick, onRegistroClick, mensajeExito)
                    }
                }

            } else {
                // --- DISEÑO VERTICAL (PORTRAIT) ---
                // Imagen arriba y formulario abajo
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
                        contentScale = ContentScale.Crop // Recorta la imagen para llenar el ancho
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
 * Componente reutilizable que contiene los campos de texto y botones.
 * Cambia dinámicamente entre modo "Login" y modo "Registro".
 */
@Composable
fun FormularioAuth(
    onLoginClick: (String, String) -> Unit,
    onRegistroClick: (String, String, String, String) -> Unit,
    mensajeExito: String?
) {
    // --- ESTADOS DE LA INTERFAZ ---

    // Controla si estamos en modo Registro (true) o Login (false)
    var isRegistering by remember { mutableStateOf(false) }

    // Campos de texto comunes
    var nickname by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Campos extra (Solo para registro)
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }

    // Estado visual (Password oculta/visible y errores)
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --- EFECTO SECUNDARIO: ÉXITO ---
    // Si recibimos un mensaje de éxito (cuenta creada), volvemos automáticamente al modo Login
    LaunchedEffect(mensajeExito) {
        if (mensajeExito != null) {
            isRegistering = false
            errorMessage = null // Limpiamos errores antiguos
        }
    }

    // --- TÍTULO ---
    Text(
        text = if (isRegistering) "Crear Cuenta" else "Bienvenido",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(24.dp))

    // --- CAMPOS EXTRA (SOLO EN MODO REGISTRO) ---
    // Si isRegistering es true, mostramos Nombre y Apellidos primero
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

    // --- CAMPO NICKNAME (COMÚN) ---
    OutlinedTextField(
        value = nickname,
        onValueChange = { nickname = it },
        label = { Text("Nickname") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )

    Spacer(modifier = Modifier.height(16.dp))

    // --- CAMPO CONTRASEÑA (COMÚN) ---
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Contraseña") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,

        // Lógica visual: Puntos o Texto normal según el estado
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),

        // Icono del ojo para mostrar/ocultar contraseña
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = description)
            }
        }
    )

    Spacer(modifier = Modifier.height(32.dp))

    // --- BOTÓN PRINCIPAL (ACCIÓN) ---
    // El texto y la acción cambian según el modo
    Button(
        onClick = {
            if (isRegistering) {
                // --- LÓGICA DE REGISTRO ---
                // Validamos que los 4 campos tengan datos
                if (nickname.isNotEmpty() && password.isNotEmpty() && nombre.isNotEmpty() && apellidos.isNotEmpty()) {
                    onRegistroClick(nickname, password, nombre, apellidos)
                } else {
                    errorMessage = "Por favor, rellena todos los campos"
                }
            } else {
                // --- LÓGICA DE LOGIN ---
                // Validamos solo los 2 campos básicos
                if (nickname.isNotEmpty() && password.isNotEmpty()) {
                    onLoginClick(nickname, password)
                } else {
                    errorMessage = "Introduce usuario y contraseña"
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(if (isRegistering) "REGISTRARSE" else "INICIAR SESIÓN")
    }

    // --- MENSAJES DE ERROR O ÉXITO ---
    if (errorMessage != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = errorMessage!!,
            color = MaterialTheme.colorScheme.error
        )
    }

    if (mensajeExito != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = mensajeExito,
            color = MaterialTheme.colorScheme.primary // Usamos color primario para éxito
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // --- BOTÓN PARA CAMBIAR DE MODO ---
    // Permite al usuario alternar entre Login y Registro
    TextButton(onClick = {
        isRegistering = !isRegistering // Invertimos el valor (true <-> false)
        errorMessage = null // Limpiamos errores al cambiar de pantalla
    }) {
        Text(if (isRegistering) "¿Ya tienes cuenta? Inicia Sesión" else "¿No tienes cuenta? Regístrate")
    }
}