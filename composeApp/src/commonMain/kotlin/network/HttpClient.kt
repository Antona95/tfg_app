package network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine // <--- IMPORTANTE
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// aqui uso expect porque estoy en kotlin multiplatform.
// con esto declaro una funcion que existira en cada plataforma,
// pero cuya implementacion real se hara en android e ios por separado.
//
// en otras palabras, aqui estoy diciendo:
// "se que necesito un motor de red, pero cada sistema pondra el suyo".
expect fun crearMotorDeRed(): HttpClientEngine

// esta funcion crea y devuelve el cliente http global de la app.
// lo hago en un solo sitio para centralizar la configuracion de red.
fun createHttpClient(): HttpClient {

    // creo el cliente ktor y le inyecto el motor de red propio de la plataforma.
    // por ejemplo, en android podria usar okhttp y en ios darwin.
    return HttpClient(crearMotorDeRed()) {

        // instalo el plugin contentnegotiation para que ktor pueda trabajar con json
        // de forma automatica.
        install(ContentNegotiation) {

            // aqui configuro como quiero que se comporte el parser json.
            json(Json {

                // prettyprint hace que el json se vea mas bonito al depurarlo.
                // no es lo mas importante funcionalmente, pero ayuda a leerlo.
                prettyPrint = true

                // islenient hace el parser mas flexible con ciertos formatos json.
                // lo uso para evitar que falle por detalles pequeños del formato.
                isLenient = true

                // ignoreunknownkeys hace que, si el backend manda campos extra
                // que mi data class no tiene, la app no falle.
                // esto es muy util para evitar errores si backend y frontend
                // no van exactamente sincronizados en todos los campos.
                ignoreUnknownKeys = true

                // coerceinputvalues ayuda a convertir o corregir algunos valores
                // cuando no llegan exactamente como espero.
                // esto hace el cliente un poco mas tolerante y robusto.
                coerceInputValues = true
            })
        }
    }
}