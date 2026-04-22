package ui.theme

import androidx.compose.ui.graphics.Color

// con este objeto yo centralizo todos los colores personalizados de mi app.
//
// la idea es no ir repitiendo colores sueltos en cada pantalla.
// por ejemplo, en vez de poner en muchos archivos:
//
// if (isDarkMode) Color(...) else Color(...)
//
//// lo concentro todo aqui y luego desde cualquier pantalla llamo a una funcion.
//
// esto me da varias ventajas:
// - tengo el codigo mas limpio
// - mantengo una coherencia visual en toda la app
// - si mañana quiero cambiar un color, lo hago solo aqui
// - evito errores o diferencias entre pantallas
object ColoresApp {

    // =====================================================
    // COLORES DE TEXTO GENERALES
    // =====================================================

    // esta funcion me devuelve el color principal del texto.
    //
    // la usare para textos importantes:
    // - titulos
    // - nombres
    // - valores principales
    //
    // en modo oscuro no uso blanco puro porque visualmente cansa mas.
    // prefiero un blanco un poco suavizado.
    //
    // en modo claro uso un negro oscuro tipo material design.
    fun textoPrincipal(isDarkMode: Boolean): Color {
        return if (isDarkMode) {
            Color(0xFFF3F2F7)
        } else {
            Color(0xFF1C1B1F)
        }
    }

    // esta funcion me devuelve un color secundario de texto.
    //
    // la usare para:
    // - labels
    // - subtitulos
    // - texto menos importante
    // - iconos secundarios
    //
    // la idea es crear jerarquia visual:
    // el texto principal destaca mas y este queda un poco mas suave.
    fun textoSecundario(isDarkMode: Boolean): Color {
        return if (isDarkMode) {
            Color(0xFFD7D0E0)
        } else {
            Color(0xFF5F5F5F)
        }
    }

    // esta funcion me devuelve un color aun mas suave.
    //
    // la usare para:
    // - mensajes vacios
    // - texto de apoyo
    // - estados tipo "sin resultados"
    // - informacion poco destacada
    //
    // asi tengo un tercer nivel visual:
    // principal -> secundario -> suave
    fun textoSuave(isDarkMode: Boolean): Color {
        return if (isDarkMode) {
            Color(0xFFB8B2C2)
        } else {
            Color(0xFF757575)
        }
    }

    // =====================================================
    // COLORES DE ESTADO
    // =====================================================

    // esta funcion me devuelve el color que usare para estados positivos.
    //
    // por ejemplo:
    // - exito
    // - sesion finalizada
    // - correcto
    //
    // en oscuro uso un verde mas suave para que no sea tan agresivo.
    // en claro puedo usar un verde algo mas intenso.
    fun estadoExito(isDarkMode: Boolean): Color {
        return if (isDarkMode) {
            Color(0xFF7FD9A2)
        } else {
            Color(0xFF2E7D32)
        }
    }

    // esta funcion me devuelve el color para estados pendientes o en espera.
    //
    // por ejemplo:
    // - pendiente
    // - aun no finalizado
    // - aviso o espera
    //
    // en oscuro bajo un poco la dureza del naranja.
    fun estadoPendiente(isDarkMode: Boolean): Color {
        return if (isDarkMode) {
            Color(0xFFFFC978)
        } else {
            Color(0xFFE65100)
        }
    }

    // =====================================================
    // COLORES DE BORDES Y SELECCION
    // =====================================================

    // esta funcion me devuelve el color que usare cuando quiera remarcar
    // que algo esta seleccionado.
    //
    // por ejemplo:
    // - borde de una tarjeta marcada
    // - checkbox
    // - resaltado visual de seleccion
    //
    // en oscuro necesito un azul mas claro para que resalte bien.
    // en claro puedo usar un azul mas fuerte sin problema.
    fun bordeSeleccion(isDarkMode: Boolean): Color {
        return if (isDarkMode) {
            Color(0xFF8AB4FF)
        } else {
            Color(0xFF1976D2)
        }
    }

    // =====================================================
    // COLORES DE BLOQUES DE EJERCICIOS
    // =====================================================

    // esta funcion me devuelve el color de fondo de cada bloque de ejercicios.
    //
    // la usare para:
    // - ejercicios sueltos agrupados visualmente
    // - biseries
    // - triseries
    //
    // como puedo tener muchos bloques, hago que los colores roten.
    // es decir, reutilizo una paleta de 5 colores.
    //
    // por ejemplo:
    // bloque 1 -> color 1
    // bloque 2 -> color 2
    // bloque 3 -> color 3
    // bloque 6 -> vuelve a color 1
    //
    // asi no necesito crear infinitos colores.
    fun colorBloque(numeroBloque: Int, isDarkMode: Boolean): Color {

        // aqui calculo el numero de color que toca dentro de la paleta.
        //
        // resto 1 porque el bloque empieza en 1, no en 0.
        // luego hago modulo 5 para reciclar los 5 colores.
        // y finalmente vuelvo a sumar 1 para trabajar con valores 1..5.
        val indexColor = ((numeroBloque - 1) % 5) + 1

        return if (isDarkMode) {

            // en modo oscuro uso ahora una paleta con mas presencia visual.
            //
            // antes los bloques quedaban demasiado apagados
            // y costaba percibir bien la diferencia entre unos y otros.
            //
            // con estos tonos sigo manteniendo un estilo sobrio,
            // pero consigo que cada bloque destaque mejor.
            when (indexColor) {
                1 -> Color(0xFF2F5D9A)
                2 -> Color(0xFF2F6B57)
                3 -> Color(0xFF8A4B4B)
                4 -> Color(0xFF664C99)
                5 -> Color(0xFF8A613A)

                // este else me sirve como color de seguridad
                // por si algo falla o entra un valor raro.
                else -> Color(0xFF3A3A3A)
            }
        } else {

            // en modo claro uso colores pastel o suaves.
            //
            // la idea es diferenciar bloques sin que el fondo moleste demasiado.
            when (indexColor) {
                1 -> Color(0xFFE3F2FD)
                2 -> Color(0xFFE8F5E9)
                3 -> Color(0xFFFFF3E0)
                4 -> Color(0xFFF3E5F5)
                5 -> Color(0xFFEFEBE9)

                // igual que antes, dejo un color de respaldo.
                else -> Color(0xFFF5F5F5)
            }
        }
    }

    // =====================================================
    // COLORES DE TARJETAS CONCRETAS
    // =====================================================

    // esta funcion me devuelve el color de fondo de la tarjeta
    // "entrenamiento de hoy".
    //
    // la separo en una funcion porque esa tarjeta tiene identidad propia
    // y asi puedo cambiar su color sin tocar el resto de la app.
    fun tarjetaHoy(isDarkMode: Boolean): Color {
        return if (isDarkMode) {

            // aqui uso un azul oscuro con mas presencia visual.
            // no es tan agresivo como el azul de prueba,
            // pero sigue destacando bien en home.
            Color(0xFF1D4E89)
        } else {
            Color(0xFF1976D2)
        }
    }

    fun tarjetaHoyTexto(isDarkMode: Boolean): Color {
        return if (isDarkMode) {
            Color(0xFFF8FBFF)
        } else {
            Color.White
        }
    }

    fun tarjetaHistorial(isDarkMode: Boolean): Color {
        return if (isDarkMode) {

            // aqui uso un morado oscuro elegante,
            // con mas contraste que antes pero sin llegar al tono de prueba.
            Color(0xFF4A2F6B)
        } else {
            Color(0xFFE8DEF8)
        }
    }

    fun tarjetaHistorialTexto(isDarkMode: Boolean): Color {
        return if (isDarkMode) {
            Color(0xFFFAF7FF)
        } else {
            Color(0xFF1D192B)
        }
    }
}