package model

import kotlinx.serialization.Serializable

// esta data class representa el cuerpo que envio al backend cuando hago login.
// la marco como serializable para que kotlin la pueda convertir a json automaticamente.
@Serializable
data class LoginRequest(

    // aqui guardo el nickname que escribe el usuario en el formulario de acceso.
    val nickname: String,

    // aqui guardo la contraseña que se envia al servidor para comprobar las credenciales.
    val pass: String
)

// esta data class representa el cuerpo que envio al backend cuando creo un usuario nuevo.
// tambien la marco como serializable porque se manda por red en formato json.
@Serializable
data class RegistroRequest(

    // nickname con el que el usuario se identificara en la aplicacion.
    val nickname: String,

    // contraseña del nuevo usuario.
    val pass: String,

    // nombre real de la persona.
    val nombre: String,

    // apellidos de la persona.
    val apellidos: String,

    // el rol lo dejo por defecto como "usuario" para que al registrar desde la app
    // no tenga que indicarlo siempre manualmente.
    // esto me viene bien porque la mayoria de registros normales son alumnos.
    val rol: String = "USUARIO"
)