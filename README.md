# 🏋️‍♂️ App de Gestión de Entrenamiento Personal (KMP)

Esta es la aplicación cliente (Frontend) construida con Kotlin Multiplatform. Forma parte de mi **Trabajo de Fin de Grado**, una solución integral diseñada para optimizar la gestión de entrenamientos personalizados.

## 📡 Fuente de Datos (API REST)

**IMPORTANTE:** Esta aplicación móvil no funciona de forma aislada. Consume los datos de una API RESTful propia (Backend) desarrollada en Node.js y MongoDB. 

Toda la información de usuarios, rutinas, biseries e historiales proviene de dicho servidor.

### ¿Cómo levantar el entorno completo?

Para que la app móvil muestre datos, es necesario tener el servidor backend en ejecución:

1. **Clona el repositorio de la API:** `[Añade aquí el link a tu repo del backend]`
2. **Instala las dependencias:** `npm install`
3. **Levanta el servidor localmente:** `npm run dev` (por defecto correrá en http://localhost:3000)
4. **Configura la IP del cliente:** En el código de la app (KMP), asegúrate de que la URL base de Ktor apunte a la IP local de tu ordenador si estás usando un dispositivo físico, o a `10.0.2.2` si estás utilizando el emulador de Android Studio (configurado en `App.kt`).

---

## 🛠️ Instalación y Configuración

1.  **Clonar el repositorio:** `git clone [URL_DE_ESTE_REPO]`
2.  **Configurar el Backend:** Asegúrate de tener el servidor Node.js corriendo y conectado a tu instancia de MongoDB Atlas (siguiendo los pasos de la sección anterior).
3.  **Ajustar API URL:** En `App.kt`, modifica la variable `serverUrl` con la IP de tu servidor.
4.  **Ejecutar la aplicación:** 
    *   **Android:** `./gradlew :composeApp:installDebug` o ejecutar desde Android Studio.
    *   **iOS:** Abrir el archivo `.xcworkspace` en Xcode o ejecutar directamente desde Android Studio si tienes instalado el plugin de KMP.

---

## 💻 Tecnologías utilizadas en el Cliente
* **Kotlin Multiplatform (KMP)** & **Compose Multiplatform**
* **MOKO MVVM**: Arquitectura Model-View-ViewModel.
* **Ktor Client**: Comunicación con la API (Content Negotiation & JSON Serialization).
* **Material Design 3**: UI 100% declarativa y responsiva.

---

## 🏗️ Estructura del Proyecto

La aplicación está organizada en capas claras para facilitar su mantenimiento:

### 📁 `commonMain/kotlin/model/`
Define el contrato de datos entre el cliente y el servidor.
* **`Persona.kt`**: Modelo de usuario y roles (Coach/Alumno).
* **`Entrenamiento.kt`**: Estructura de sesiones, ejercicios y bloques (Biseries/Triseries).
* **`Request.kt`**: Objetos de transferencia (DTOs) para creación de rutinas.
* **`Auth.kt`**: Modelos de Login y Registro.

### 📁 `commonMain/kotlin/network/`
* **`HttpClient.kt`**: Configuración global de Ktor.
* **`EntrenamientoRepository.kt`**: Gestión centralizada de llamadas a la API.

### 📁 `commonMain/kotlin/viewmodel/`
* **`LoginViewModel`**: Gestión de sesión.
* **`CoachViewModel`**: Panel del entrenador.
* **`HoyViewModel` / `SesionViewModel`**: Ejecución y creación de rutinas.
* **`HistorialViewModel`**: Registro histórico de sesiones.

### 📁 `commonMain/kotlin/ui/`
* **`login/`**: Acceso y registro.
* **`coach/`**: Herramientas para el entrenador.
* **`user/`**: Dashboard e interfaz del alumno.
* **📁 `components/`**: UI Kit global (Tarjetas, Estados de UI, Validaciones).

---

## 🚀 Funcionalidades Principales

1.  **Gestión de Alumnos:** Visualización y búsqueda de clientes en tiempo real.
2.  **Planificador de Rutinas:** Soporte para ejercicios agrupados (super-series) con códigos de colores.
3.  **Ejecución de Entrenamiento:** Interfaz para que el alumno reporte sus marcas reales.
4.  **Sincronización Cloud:** Persistencia en MongoDB Atlas a través de la API.
5.  **Multi-entorno:** URL del servidor configurable para facilitar el desarrollo (Casa/Trabajo).

---
*Este proyecto forma parte de un Trabajo de Fin de Grado enfocado en la aplicación de tecnologías modernas en el ámbito deportivo.*
