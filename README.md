# 🏋️‍♂️ App de Gestión de Entrenamiento Personal (KMP)

Esta es la aplicación cliente (Frontend) construida con Kotlin Multiplatform. Forma parte de mi **Trabajo de Fin de Grado**, una solución integral diseñada para optimizar la gestión de entrenamientos personalizados.

## 📡 1. Fuente de Datos (API REST)

**IMPORTANTE:** Esta aplicación móvil no funciona de forma aislada. Consume los datos de una API RESTful propia (Backend) desarrollada en Node.js y MongoDB. 

Toda la información de usuarios, rutinas, biseries e historiales proviene de dicho servidor.

### ¿Cómo levantar el entorno completo?

Para que la app móvil muestre datos, es necesario tener el servidor backend en ejecución:

1.  **Clona el repositorio de la API:** [https://github.com/Antona95/tfg_api.git](https://github.com/Antona95/tfg_api.git)
2.  **Instala las dependencias:** `npm install`
3.  **Levanta el servidor localmente:** `npm run dev` (por defecto correrá en http://localhost:3000)
4.  **Configura la IP del cliente:** En el código de la app (KMP), asegúrate de que la URL base de Ktor apunte a la IP local de tu ordenador si estás usando un dispositivo físico, o a `10.0.2.2` si estás utilizando el emulador de Android Studio (configurado en `App.kt`).

---

## 🛠️ 2. Stack Tecnológico

He seleccionado las siguientes herramientas buscando un equilibrio entre rendimiento nativo, escalabilidad y experiencia de desarrollo (DX):

*   **Kotlin Multiplatform (KMP) & Compose Multiplatform**
*   **MOKO MVVM**: Arquitectura Model-View-ViewModel.
*   **Ktor Client**: Comunicación con la API (Content Negotiation & JSON Serialization).
*   **Material Design 3**: UI 100% declarativa y responsiva.

---

## 🏗️ 3. Estructura del Proyecto (KMP)

La aplicación está organizada en capas claras para facilitar su mantenimiento:

### 3.1 `commonMain/kotlin/model/`
Define el contrato de datos entre el cliente y el servidor.
*   **`Persona.kt`**: Modelo de usuario y roles (Coach/Alumno).
*   **`Entrenamiento.kt`**: Estructura de sesiones, ejercicios y bloques (Biseries/Triseries).
*   **`Request.kt`**: Objetos de transferencia (DTOs) para creación de rutinas.
*   **`Auth.kt`**: Modelos de Login y Registro.

### 3.2 `commonMain/kotlin/network/`
*   **`HttpClient.kt`**: Configuración global de Ktor (serialización JSON, gestión de errores).
*   **`EntrenamientoRepository.kt`**: Repositorio centralizado que gestiona todas las llamadas a la API (CRUD de usuarios, sesiones y ejercicios).

### 3.3 `commonMain/kotlin/viewmodel/`
Lógica de negocio reactiva.
*   **`LoginViewModel`**: Gestión de la sesión y autenticación.
*   **`CoachViewModel`**: Lógica del panel de control del entrenador (listados, búsquedas).
*   **`HoyViewModel` / `SesionViewModel`**: Control de la ejecución y creación de entrenamientos.
*   **`HistorialViewModel`**: Gestión y visualización del registro histórico de sesiones.

### 3.4 `commonMain/kotlin/ui/`
Componentes visuales con Compose.
*   **`login/`**: Flujo de acceso y registro adaptable a pantallas horizontales.
*   **`coach/`**: Herramientas para el entrenador (Creación de biseries, historial de alumnos, opciones de usuario).
*   **`user/`**: Dashboard del alumno (`AlumnoHomeScreen`) e interfaz de entrenamiento diario (`HoyScreen`).

#### 3.4.1 `components/`: UI Kit global y componentes transversales.
*   **`EjercicioUniversalCard.kt`**: Tarjeta estandarizada para mostrar ejercicios.
*   **`SesionResumenCard.kt`**: Resumen visual de sesiones para listados.
*   **`EstadosUI.kt`**: Gestión de estados de carga, error y pantallas vacías.
*   **`Validaciones.kt` / `Formulario.kt`**: Utilidades para la entrada de datos.

---

## 🚀 4. Funcionalidades Principales

1.  **Gestión de Alumnos**: El entrenador puede visualizar, buscar y gestionar a sus clientes en tiempo real.
2.  **Planificador de Rutinas**: Creación de sesiones complejas con soporte para ejercicios agrupados (super-series) identificados por colores.
3.  **Ejecución de Entrenamiento**: El alumno puede ver su rutina diaria, seguir las indicaciones del coach y reportar sus marcas reales al finalizar.
4.  **Sincronización Cloud**: Los datos persisten en MongoDB Atlas, permitiendo el acceso desde cualquier dispositivo.
5.  **Multi-entorno**: Configuración flexible de la URL del servidor para facilitar el desarrollo en diferentes redes (Casa/Trabajo).

---

## ⚙️ 5. Instalación y Configuración

1.  **Clonar el repositorio:** `git clone https://github.com/Antona95/tfg_app.git`
2.  **Configurar el Backend:** Asegúrate de tener el servidor Node.js corriendo y conectado a tu instancia de MongoDB Atlas (siguiendo los pasos de la sección anterior).
3.  **Ajustar API URL:** En `App.kt`, modifica la variable `serverUrl` con la IP de tu servidor.
4.  **Ejecutar la aplicación:**
    *   **Android:** `./gradlew :composeApp:installDebug` o ejecutar desde Android Studio.
    *   **iOS:** Abrir el archivo `.xcworkspace` en Xcode o ejecutar directamente desde Android Studio si tienes instalado el plugin de KMP.

---
*Este proyecto forma parte de un Trabajo de Fin de Grado enfocado en la aplicación de tecnologías modernas en el ámbito deportivo.*
