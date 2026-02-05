package network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Esta función configura el cliente HTTP para toda la app
fun createHttpClient(): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json {
                // Imprime el JSON bonito si lo logueas
                prettyPrint = true
                // Permite comillas relajadas o formatos ligeramente incorrectos
                isLenient = true
                // CRUCIAL: Si tu API de Mongo devuelve campos que no tienes en tu clase Kotlin,
                // esto evita que la app se rompa.
                ignoreUnknownKeys = true  // Si el servidor manda basura extra (__v), la ignora
                coerceInputValues = true  // Si viene un null y esperamos default, lo arregla
            })
        }
    }
}