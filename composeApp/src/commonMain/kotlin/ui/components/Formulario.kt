package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun CamposRegistro(
    // aqui recibo el valor actual del campo nombre
    // y tambien la funcion que se ejecuta cuando el usuario escribe algo nuevo.
    nombre: String, onNombreChange: (String) -> Unit,

    // aqui hago lo mismo con apellidos.
    apellidos: String, onApellidosChange: (String) -> Unit,

    // este campo guarda el nickname que el usuario quiere usar.
    nickname: String, onNicknameChange: (String) -> Unit,

    // este campo guarda la contraseña.
    // tambien recibo la funcion para actualizarla cuando cambia el texto.
    password: String, onPasswordChange: (String) -> Unit,

    // este booleano me dice si la contraseña se ve o no se ve.
    //
    // lo dejo con valor por defecto false por si en alguna pantalla
    // quiero reutilizar este componente sin controlar el ojito.
    passwordVisible: Boolean = false,

    // esta funcion callback se ejecuta cuando pulso el icono del ojito.
    //
    // la dejo con una funcion vacia por defecto para que el componente
    // no obligue siempre a pasarla si no hace falta.
    onPasswordVisibilityChange: () -> Unit = {}
) {
    // este composable me sirve para reutilizar todos los campos del formulario de registro.
    //
    // en vez de escribir una y otra vez los cuatro textfields en distintas pantallas,
    // los agrupo aqui y asi el codigo queda mas limpio y mas reutilizable.
    Column(
        // dejo una separacion vertical fija entre los campos para que no queden pegados.
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            // value es el texto actual que se ve en el campo.
            value = nombre,

            // onvaluechange se ejecuta cada vez que el usuario escribe o borra.
            // aqui reutilizo directamente la funcion que me llega por parametro.
            onValueChange = onNombreChange,

            // label es el texto flotante que identifica el campo.
            label = { Text("Nombre real") },

            // hago que el campo ocupe todo el ancho disponible.
            modifier = Modifier.fillMaxWidth(),

            // con singleline obligo a que el textfield sea de una sola linea.
            singleLine = true
        )

        OutlinedTextField(
            value = apellidos,
            onValueChange = onApellidosChange,
            label = { Text("Apellidos") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

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

            // visualtransformation cambia como se muestra el texto en pantalla.
            //
            // si passwordvisible es true, enseño la contraseña tal cual.
            // si es false, la oculto con puntitos usando passwordvisualtransformation.
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

                // este iconbutton es el boton del ojito.
                // cuando lo pulso, llamo a la funcion que me llega por parametro.
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        imageVector = image,
                        contentDescription = "Mostrar contraseña"
                    )
                }
            }
        )
    }
}