# Sistema de Gestión de Entrenamiento Híbrido (TFG)

Este repositorio contiene el código fuente de mi **Trabajo de Fin de Grado**. Se trata de una solución integral multiplataforma diseñada para optimizar la gestión de entrenamientos personalizados, facilitando la comunicación entre entrenador y cliente tanto en sesiones presenciales como a distancia.

El proyecto destaca por el uso de tecnologías de vanguardia como **Kotlin Multiplatform (KMP)** para compartir lógica y UI entre Android e iOS, y un backend robusto en **Node.js** con **TypeScript**.

---

## Stack Tecnológico

He seleccionado las siguientes herramientas buscando un equilibrio entre rendimiento nativo, escalabilidad y experiencia de desarrollo (DX):

### Cliente Móvil (Frontend)
* **Lenguaje:** Kotlin (100%).
* **Framework:** [Kotlin Multiplatform (KMP)](https://kotlinlang.org/lp/multiplatform/) & **Jetpack Compose Multiplatform**.
* **Arquitectura:** MVVM (Model-View-ViewModel) usando **MOKO MVVM**.
* **UI:** 100% Declarativa con Material Design 3. Diseño **Responsivo** (adaptable a vertical/horizontal).
* **Red:** Ktor Client (Content Negotiation & JSON Serialization).
* **Gestión de Estado:** `StateFlow` y `uiState` reactivo.

### Servidor (Backend)
* **Runtime:** Node.js.
* **Lenguaje:** TypeScript (Tipado estático estricto).
* **Framework:** Express.js.
* **Base de Datos:** MongoDB (NoSQL) con **Mongoose** (ODM).
* **Validación:** Zod (Esquemas y validación de tipos en tiempo de ejecución).
* **Documentación API:** Swagger (OpenAPI) autogenerado.
* **Testing:** Jest & Supertest.

---

## Arquitectura del Proyecto

### Frontend: Patrón MVVM con KMP
La aplicación sigue estrictamente el patrón **MVVM** para desacoplar la lógica de negocio de la interfaz de usuario.
1.  **Model:** Definición de datos (`Persona`, `LoginRequest`) y Repositorios (`EntrenamientoRepository`) encargados de la comunicación HTTP.
2.  **ViewModel:** (`LoginViewModel`) Gestiona el estado de la UI (`isLoading`, `error`, `success`) y sobrevive a cambios de configuración (rotación de pantalla).
3.  **View:** (`LoginScreen`, `HomeScreen`) Funciones Composables puras que reaccionan a los cambios de estado del ViewModel.

### Backend: Arquitectura Modular
El servidor está estructurado para ser escalable y mantenible:
* **Controllers:** Manejan las peticiones HTTP y respuestas.
* **Services:** Contienen la lógica de