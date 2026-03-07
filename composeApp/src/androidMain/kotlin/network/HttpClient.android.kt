package network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

// Android usa la librería OkHttp para salir a Internet
actual fun crearMotorDeRed(): HttpClientEngine {
    return OkHttp.create()
}