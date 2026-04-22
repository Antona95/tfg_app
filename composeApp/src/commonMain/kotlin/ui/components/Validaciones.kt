package ui.components

import model.EjercicioDraft

object Validaciones {

    // Validación para crear o duplicar sesiones
    fun validarFormularioSesion(titulo: String, ejercicios: List<EjercicioDraft>): String? {
        if (titulo.trim().isBlank()) return "El entrenamiento debe tener un nombre."
        if (ejercicios.isEmpty()) return "Debes añadir al menos un ejercicio a la rutina."

        for (ej in ejercicios) {
            if (ej.nombre.trim().isBlank()) return "Todos los ejercicios deben tener un nombre."

            // aqui trabajo primero con el texto original para poder distinguir mejor
            // entre letras, simbolos, decimales y enteros fuera de rango.
            val textoSeries = ej.series.trim()
            val textoReps = ej.repeticiones.trim()
            val textoPeso = ej.peso.trim()

            // -----------------------------------------------------
            // VALIDACION DE SERIES
            // -----------------------------------------------------

            // primero compruebo si series es un entero valido.
            val seriesEnteras = textoSeries.toIntOrNull()

            if (seriesEnteras == null) {

                // si no se puede convertir a int, pruebo a ver si en realidad
                // el usuario ha escrito un decimal.
                val seriesDecimal = textoSeries.replace(',', '.').toDoubleOrNull()

                if (seriesDecimal != null) {
                    return "En '${ej.nombre}', las series no pueden ser decimales. Debes introducir un número entero."
                } else {
                    return "En '${ej.nombre}', en las series se deben introducir solo números."
                }
            }

            // -----------------------------------------------------
            // VALIDACION DE REPETICIONES
            // -----------------------------------------------------

            // hago la misma idea con repeticiones.
            val repsEnteras = textoReps.toIntOrNull()

            if (repsEnteras == null) {
                val repsDecimal = textoReps.replace(',', '.').toDoubleOrNull()

                if (repsDecimal != null) {
                    return "En '${ej.nombre}', las repeticiones no pueden ser decimales. Debes introducir un número entero."
                } else {
                    return "En '${ej.nombre}', en las repeticiones se deben introducir solo números."
                }
            }

            // -----------------------------------------------------
            // VALIDACION DE PESO
            // -----------------------------------------------------

            // en el peso permito que el campo venga vacio.
            // si viene vacio, lo interpreto como 0.0 porque no todos los ejercicios
            // tienen por que llevar carga externa.
            val peso = if (textoPeso.isBlank()) {
                0.0
            } else {
                textoPeso.replace(',', '.').toDoubleOrNull()
            }

            // aqui en peso no distingo entre entero y decimal,
            // porque ambos son validos.
            //
            // solo compruebo si el formato introducido puede convertirse
            // realmente en un numero.
            if (peso == null) {
                return "En '${ej.nombre}', en el peso se deben introducir solo números."
            }

            // -----------------------------------------------------
            // VALIDACION DE RANGOS
            // -----------------------------------------------------

            if (seriesEnteras !in 1..20) {
                return "Las series de '${ej.nombre}' deben estar entre 1 y 20."
            }

            if (repsEnteras !in 1..60) {
                return "Las repeticiones de '${ej.nombre}' deben estar entre 1 y 60."
            }

            if (peso < 0) {
                return "El peso de '${ej.nombre}' no puede ser negativo."
            }

            if (peso > 500) {
                return "El peso de '${ej.nombre}' parece demasiado alto."
            }
        }
        return null // Todo correcto
    }
}