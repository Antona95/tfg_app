package network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine // <--- IMPORTANTE
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// 1. LA PROMESA: Le decimos a KMP que cada sistema (Android/iOS) pondrá su motor
expect fun crearMotorDeRed(): HttpClientEngine

// 2. CONFIGURACIÓN GLOBAL: Sigue siendo común, pero ahora le inyectamos el motor
fun createHttpClient(): HttpClient {
    return HttpClient(crearMotorDeRed()) { // <--- Le pasamos el motor específico
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }
}