package ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app_tfg.composeapp.generated.resources.Res
import app_tfg.composeapp.generated.resources.imagen_inicial
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation


@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit
) {
    // Usamos BoxWithConstraints para saber el tamaño de la pantalla
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        // Detectamos si es horizontal (ancho > alto)
        val isLandscape = maxWidth > maxHeight

        Surface(color = MaterialTheme.colorScheme.background) {

            if (isLandscape) {
                // --- DISEÑO HORIZONTAL MEJORADO ---
                Row(modifier = Modifier.fillMaxSize()) {

                    // 1. PANEL IZQUIERDO (IMAGEN)
                    // Usamos un Box para poder ponerle color de fondo y centrar la imagen
                    Box(
                        modifier = Modifier
                            .weight(0.4f) // Ocupa el 40% del ancho
                            .fillMaxHeight() // Ocupa todo el alto
                            .padding(16.dp), // Un poco de margen para que "respire"
                        contentAlignment = Alignment.Center // Centra la imagen en el panel
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.imagen_inicial),
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize(), // Intenta ocupar lo que pueda
                            // VITAL: .Fit asegura que se vea ENTERA sin cortarse
                            contentScale = ContentScale.Fit
                        )
                    }

                    // 2. PANEL DERECHO (FORMULARIO)
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .padding(horizontal = 32.dp) // Más margen a los lados en horizontal
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FormularioLogin(onLoginClick)
                    }
                }

            } else {
                // --- DISEÑO VERTICAL (Columna: Arriba / Abajo) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Imagen arriba (Ocupa todo el ancho y 250dp de alto)
                    Image(
                        painter = painterResource(Res.drawable.imagen_inicial),
                        contentDescription = "Logo",
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
                        FormularioLogin(onLoginClick)
                    }
                }
            }
        }
    }
}

// --- COMPONENTE REUTILIZABLE (Para no escribir el código 2 veces) ---
@Composable
fun FormularioLogin(onLoginClick: (String, String) -> Unit) {
    var dni by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 1. NUEVO ESTADO: Controla si la contraseña es visible o no
    var passwordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Text(
        text = "Bienvenido",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Campo DNI (Sin cambios)
    OutlinedTextField(
        value = dni,
        onValueChange = { dni = it },
        label = { Text("DNI") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Campo Contraseña (MODIFICADO)
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Contraseña") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,

        // 2. TRANSFORMACIÓN DINÁMICA
        // Si es visible -> Texto normal (None). Si no -> Puntos (PasswordVisualTransformation)
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),

        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = MaterialTheme.shapes.medium,

        // 3. ICONO DEL OJO (NUEVO)
        trailingIcon = {
            // Elegimos el icono según el estado
            val image = if (passwordVisible)
                Icons.Filled.Visibility
            else
                Icons.Filled.VisibilityOff

            // Botón que al pulsar cambia el estado true <-> false
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = "Mostrar contraseña")
            }
        }
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Botón (Sin cambios)
    Button(
        onClick = {
            if (dni.isNotEmpty() && password.isNotEmpty()) {
                onLoginClick(dni, password)
            } else {
                errorMessage = "Por favor, rellena todos los campos"
            }
        },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text("INICIAR SESIÓN")
    }

    if (errorMessage != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = errorMessage!!,
            color = MaterialTheme.colorScheme.error
        )
    }
}