package ui.theme

import androidx.compose.ui.graphics.Color

// con este objeto yo centralizo todos los colores personalizados de mi app.
//
// la idea es no ir repitiendo colores sueltos en cada pantalla.
// por ejemplo, en vez de poner en muchos archivos:
//
// if (isDarkMode) Color(...) else Color(...)
////
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
            Color(0xFFF5F5F5)
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
            Color(0xFFD0D0D0)
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
            Color(0xFFBDBDBD)
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
            Color(0xFF81C784)
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
            Color(0xFFFFCC80)
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
            Color(0xFF90CAF9)
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

            // en modo oscuro uso colores mas apagados y menos saturados.
            //
            // esto me interesa porque:
            // - el contraste con el texto sigue siendo bueno
            // - la vista se cansa menos
            // - la pantalla queda mas elegante
            when (indexColor) {
                1 -> Color(0xFF24415A)
                2 -> Color(0xFF2D4A34)
                3 -> Color(0xFF5A3333)
                4 -> Color(0xFF4A3B5E)
                5 -> Color(0xFF5B442B)

                // este else me sirve como color de seguridad
                // por si algo falla o entra un valor raro.
                else -> Color(0xFF2C2C2C)
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

            // en oscuro uso un azul apagado.
            Color(0xFF2A3F55)
        } else {

            // en claro uso un azul mas vivo.
            Color(0xFF1976D2)
        }
    }

    // esta funcion me devuelve el color del texto de la tarjeta
    // "entrenamiento de hoy".
    //
    // la separo para asegurar que siempre haya buen contraste
    // entre texto y fondo.
    fun tarjetaHoyTexto(isDarkMode: Boolean): Color {
        return if (isDarkMode) {
            Color(0xFFF5F5F5)
        } else {
            Color.White
        }
    }

    // esta funcion me devuelve el color de fondo de la tarjeta
    // "historial".
    //
    // igual que antes, lo centralizo aqui para no dejarlo disperso.
    fun tarjetaHistorial(isDarkMode: Boolean): Color {
        return if (isDarkMode) {

            // en oscuro uso un gris azulado suave.
            Color(0xFF31363F)
        } else {

            // en claro uso un tono lila claro.
            Color(0xFFE8DEF8)
        }
    }

    // esta funcion me devuelve el color del texto de la tarjeta
    // "historial".
    fun tarjetaHistorialTexto(isDarkMode: Boolean): Color {
        return if (isDarkMode) {
            Color(0xFFF1F1F1)
        } else {
            Color(0xFF1D192B)
        }
    }
}