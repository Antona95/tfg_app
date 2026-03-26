package network

// este objeto me sirve para centralizar la url base de la api.
// asi evito repetir la direccion del servidor en varios archivos.
object ApiConfig {

    // 10.0.2.2 es la ip especial que usa el emulador de android
    // para acceder al localhost de mi ordenador.
    // como mi api corre en el puerto 8080, aqui dejo montada la ruta base completa.
    const val BASE_URL = "http://10.0.2.2:8080"
}