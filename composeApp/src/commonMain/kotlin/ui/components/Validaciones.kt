package ui.components

import model.EjercicioDraft

object Validaciones {

    // Validación para crear o duplicar sesiones
    fun validarFormularioSesion(titulo: String, ejercicios: List<EjercicioDraft>): String? {
        if (titulo.isBlank()) return "El entrenamiento debe tener un nombre."
        if (ejercicios.isEmpty()) return "Debes añadir al menos un ejercicio a la rutina."

        for (ej in ejercicios) {
            if (ej.nombre.isBlank()) return "Todos los ejercicios deben tener un nombre."

            val series = ej.series.toIntOrNull() ?: -1
            val reps = ej.repeticiones.toIntOrNull() ?: -1
            val peso = ej.peso.toDoubleOrNull() ?: -1.0

            if (series <= 0) return "Las series de '${ej.nombre}' deben ser un número mayor que 0."
            if (reps <= 0) return "Las repeticiones de '${ej.nombre}' deben ser un número mayor que 0."
            if (peso < 0) return "El peso de '${ej.nombre}' no puede ser negativo."
        }
        return null // Todo correcto
    }

    // NUEVO: Validación para el Registro de Usuarios
    fun validarRegistro(nick: String, pass: String, nombre: String, apellidos: String): String? {
        if (nombre.isBlank()) return "El nombre no puede estar vacío."
        if (apellidos.isBlank()) return "Los apellidos no pueden estar vacíos."
        if (nick.isBlank()) return "El nombre de usuario (nickname) no puede estar vacío."
        if (pass.isBlank()) return "La contraseña no puede estar vacía."
        if (pass.length < 2) return "La contraseña es muy corta. Debe tener al menos 2 caracteres."

        return null // Si llega aquí, todo correcto
    }
}