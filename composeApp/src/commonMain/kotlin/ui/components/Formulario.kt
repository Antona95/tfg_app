package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ui.theme.ColoresApp

@Composable
fun CamposRegistro(
    // aqui recibo el valor actual del campo nombre
    // y tambien la funcion que se ejecuta cuando el usuario escribe algo nuevo.
    nombre: String,
    onNombreChange: (String) -> Unit,

    // aqui hago lo mismo con apellidos.
    apellidos: String,
    onApellidosChange: (String) -> Unit,

    // este campo guarda el nickname que el usuario quiere usar.
    nickname: String,
    onNicknameChange: (String) -> Unit,

    // este campo guarda la contraseña.
    // tambien recibo la funcion para actualizarla cuando cambia el texto.
    password: String,
    onPasswordChange: (String) -> Unit,

    // este booleano me dice si la contraseña se ve o no se ve.
    //
    // lo dejo con valor por defecto false por si en alguna pantalla
    // quiero reutilizar este componente sin controlar el ojito.
    passwordVisible: Boolean = false,

    // esta funcion callback se ejecuta cuando pulso el icono del ojito.
    //
    // la dejo con una funcion vacia por defecto para que el componente
    // no obligue siempre a pasarla si no hace falta.
    onPasswordVisibilityChange: () -> Unit = {},

    // añado isDarkMode para poder sacar los colores desde ColoresApp
    // y no dejar esta pantalla dependiendo solo de los colores por defecto.
    isDarkMode: Boolean
) {
    // aqui preparo una pequeña paleta centralizada para todos los textfield.
    //
    // con esto consigo que:
    // - el texto se vea mejor en oscuro
    // - las labels no queden demasiado apagadas
    // - los bordes tengan contraste suficiente
    // - los iconos mantengan coherencia con el resto de la app
    val colorTexto = ColoresApp.textoPrincipal(isDarkMode)
    val colorTextoSecundario = ColoresApp.textoSecundario(isDarkMode)

    // creo unos colores comunes para los OutlinedTextField.
    // asi no tengo que repetir la configuracion en cada campo.
    val coloresCampos = OutlinedTextFieldDefaults.colors(
        focusedTextColor = colorTexto,
        unfocusedTextColor = colorTexto,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = colorTextoSecundario.copy(alpha = 0.85f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = colorTextoSecundario,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = colorTextoSecundario,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedTrailingIconColor = colorTextoSecundario,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    )

    // uso BoxWithConstraints para saber el ancho disponible.
    // asi puedo decidir si pongo el formulario en una columna o en dos.
    BoxWithConstraints {

        // si tengo suficiente ancho, organizo el formulario en dos columnas.
        // esto viene bien en horizontal para aprovechar mejor el espacio.
        val dosColumnas = maxWidth >= 450.dp

        // aqui preparo medidas algo más compactas para horizontal.
        //
        // no cambio la lógica del formulario:
        // solo reduzco un poco huecos y altura visual para que entre mejor
        // cuando el móvil está apaisado.
        val espacioVertical = if (dosColumnas) 8.dp else 8.dp
        val espacioHorizontal = if (dosColumnas) 10.dp else 12.dp
        val alturaMinimaCampo = if (dosColumnas) 56.dp else 0.dp

        if (dosColumnas) {

            // en pantallas anchas coloco los campos en dos filas.
            // primera fila: nombre + apellidos
            // segunda fila: nickname + password
            Column(
                verticalArrangement = Arrangement.spacedBy(espacioVertical)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(espacioHorizontal)
                ) {
                    OutlinedTextField(
                        // value es el texto actual que se ve en el campo.
                        value = nombre,

                        // onValueChange se ejecuta cada vez que el usuario escribe o borra.
                        // aqui reutilizo directamente la funcion que me llega por parametro.
                        onValueChange = onNombreChange,

                        // label es el texto flotante que identifica el campo.
                        label = { Text("Nombre real") },

                        // añado un pictograma para indicar que este campo corresponde a una persona.
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "nombre"
                            )
                        },

                        // con weight reparto el ancho entre los dos campos de la fila.
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = alturaMinimaCampo),

                        // con singleLine obligo a que el textfield sea de una sola linea.
                        singleLine = true,

                        // aplico los colores centralizados.
                        colors = coloresCampos
                    )

                    OutlinedTextField(
                        value = apellidos,
                        onValueChange = onApellidosChange,
                        label = { Text("Apellidos") },

                        // reutilizo el mismo pictograma porque sigue siendo un dato personal.
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "apellidos"
                            )
                        },

                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = alturaMinimaCampo),
                        singleLine = true,

                        // aplico los colores centralizados.
                        colors = coloresCampos
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(espacioHorizontal)
                ) {
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = onNicknameChange,
                        label = { Text("Nickname") },

                        // este pictograma refuerza que es un identificador del usuario.
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = "nickname"
                            )
                        },

                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = alturaMinimaCampo),
                        singleLine = true,

                        // aplico los colores centralizados.
                        colors = coloresCampos
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = alturaMinimaCampo),
                        singleLine = true,

                        // añado un pictograma de candado para identificar la contraseña.
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "contraseña"
                            )
                        },

                        // visualTransformation cambia como se muestra el texto en pantalla.
                        //
                        // si passwordVisible es true, enseño la contraseña tal cual.
                        // si es false, la oculto con puntitos usando PasswordVisualTransformation.
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },

                        // aqui le digo al teclado del movil que este campo es de tipo contraseña.
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),

                        trailingIcon = {
                            // aqui decido qué icono enseño segun el estado actual.
                            val image = if (passwordVisible) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            }

                            // este IconButton es el boton del ojito.
                            // cuando lo pulso, llamo a la funcion que me llega por parametro.
                            IconButton(onClick = onPasswordVisibilityChange) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = "Mostrar contraseña"
                                )
                            }
                        },

                        // aplico los colores centralizados.
                        colors = coloresCampos
                    )
                }
            }
        } else {

            // este composable me sirve para reutilizar todos los campos del formulario de registro.
            //
            // en vez de escribir una y otra vez los cuatro textfields en distintas pantallas,
            // los agrupo aqui y asi el codigo queda mas limpio y mas reutilizable.
            Column(
                // dejo una separacion vertical fija entre los campos para que no queden pegados.
                verticalArrangement = Arrangement.spacedBy(espacioVertical)
            ) {
                OutlinedTextField(
                    // value es el texto actual que se ve en el campo.
                    value = nombre,

                    // onValueChange se ejecuta cada vez que el usuario escribe o borra.
                    // aqui reutilizo directamente la funcion que me llega por parametro.
                    onValueChange = onNombreChange,

                    // label es el texto flotante que identifica el campo.
                    label = { Text("Nombre real") },

                    // añado un pictograma para indicar que este campo corresponde a una persona.
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "nombre"
                        )
                    },

                    // hago que el campo ocupe todo el ancho disponible.
                    modifier = Modifier.fillMaxWidth(),

                    // con singleLine obligo a que el textfield sea de una sola linea.
                    singleLine = true,

                    // aplico los colores centralizados.
                    colors = coloresCampos
                )

                OutlinedTextField(
                    value = apellidos,
                    onValueChange = onApellidosChange,
                    label = { Text("Apellidos") },

                    // reutilizo el mismo pictograma porque sigue siendo un dato personal.
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "apellidos"
                        )
                    },

                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,

                    // aplico los colores centralizados.
                    colors = coloresCampos
                )

                OutlinedTextField(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    label = { Text("Nickname") },

                    // este pictograma refuerza que es un identificador del usuario.
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = "nickname"
                        )
                    },

                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,

                    // aplico los colores centralizados.
                    colors = coloresCampos
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,

                    // añado un pictograma de candado para identificar la contraseña.
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "contraseña"
                        )
                    },

                    // visualTransformation cambia como se muestra el texto en pantalla.
                    //
                    // si passwordVisible es true, enseño la contraseña tal cual.
                    // si es false, la oculto con puntitos usando PasswordVisualTransformation.
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },

                    // aqui le digo al teclado del movil que este campo es de tipo contraseña.
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),

                    trailingIcon = {
                        // aqui decido qué icono enseño segun el estado actual.
                        val image = if (passwordVisible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        }

                        // este IconButton es el boton del ojito.
                        // cuando lo pulso, llamo a la funcion que me llega por parametro.
                        IconButton(onClick = onPasswordVisibilityChange) {
                            Icon(
                                imageVector = image,
                                contentDescription = "Mostrar contraseña"
                            )
                        }
                    },

                    // aplico los colores centralizados.
                    colors = coloresCampos
                )
            }
        }
    }
}