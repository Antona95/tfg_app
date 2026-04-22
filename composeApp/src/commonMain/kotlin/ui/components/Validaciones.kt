package ui.components

import model.EjercicioDraft

object Validaciones {

    // Validación para crear o duplicar sesiones
    fun validarFormularioSesion(titulo: String, ejercicios: List<EjercicioDraft>): String? {
        if (titulo.trim().isBlank()) return "El entrenamiento debe tener un nombre."
        if (ejercicios.isEmpty()) return "Debes añadir al menos un ejercicio a la rutina."

        for (ej in ejercicios) {
            if (ej.nombre.trim().isBlank()) return "Todos los ejercicios deben tener un nombre."

            val series = ej.series.toIntOrNull() ?: -1
            val reps = ej.repeticiones.toIntOrNull() ?: -1
            val peso = ej.peso.replace(',', '.').toDoubleOrNull() ?: -1.0

            if (series !in 1..20) return "Las series de '${ej.nombre}' deben estar entre 1 y 20."
            if (reps !in 1..60) return "Las repeticiones de '${ej.nombre}' deben estar entre 1 y 60."
            if (peso < 0) return "El peso de '${ej.nombre}' no puede ser negativo."
            if (peso > 500) return "El peso de '${ej.nombre}' parece demasiado alto."
        }
        return null // Todo correcto
    }

    // Validación para el Registro de Usuarios
    fun validarRegistro(nick: String, pass: String, nombre: String, apellidos: String): String? {
        if (nombre.trim().isBlank()) return "El nombre no puede estar vacío."
        if (apellidos.trim().isBlank()) return "Los apellidos no pueden estar vacíos."
        if (nick.trim().isBlank()) return "El nombre de usuario (nickname) no puede estar vacío."
        if (pass.isBlank()) return "La contraseña no puede estar vacía."
        if (pass.length < 2) return "La contraseña es muy corta. Debe tener al menos 2 caracteres."

        return null // Si llega aquí, todo correcto
    }
}