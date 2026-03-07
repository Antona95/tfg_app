package network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

// Apple (iPhone/iPad) usa su motor nativo Darwin para salir a Internet
actual fun crearMotorDeRed(): HttpClientEngine {
    return Darwin.create()
}
