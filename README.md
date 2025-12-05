💰**ControlGastos_AM_SP: Aplicación de Gestión de Finanzas Personales**

Bienvenido al repositorio de ControlGastos_AM_SP, una aplicación móvil simple diseñada para llevar un seguimiento de ingresos y gastos diarios.

Esta aplicación te permite clasificar tus transacciones, ver tu balance y mantener un control claro sobre tu salud financiera personal.

✨**Características Principales**

**Registro Rápido de Transacciones:** Registra ingresos y gastos con campos esenciales como Monto, Categoría, Descripción, Fecha y Método de Pago.

**Gestión Unificada de Categorías:** Las categorías de Ingreso y Gasto se muestran en un solo Spinner para agilizar la entrada de datos.

**Actualización de Transacciones:** Permite editar y actualizar detalles de transacciones existentes.

**Conversión de Divisas (Simulada):** Incluye una funcionalidad para simular la conversión de divisas.

**Listado de Transacciones:** Visualización clara y organizada de todas las transacciones registradas.

**Persistencia de Datos:** Utiliza SQLite para el almacenamiento local y persistente de la información.

🛠️ **Tecnologías Utilizadas**

El proyecto está desarrollado en el entorno nativo de Android, utilizando las siguientes tecnologías clave:

**Lenguaje de Programación:** Java

**Plataforma:** Android SDK

**Base de Datos Local:** SQLite

**Interfaz de Usuario:** XML para layouts con ConstraintLayout y componentes estándar de Android (Buttons, EditTexts, Spinners).

🚀 **Configuración y Uso**

Sigue estos pasos para clonar y ejecutar el proyecto en tu máquina local:

**Requisitos Previos**

Asegúrate de tener instalado y configurado lo siguiente:

**Android Studio:** La última versión estable.

**Dispositivo o Emulador Android:** Un dispositivo físico o un emulador configurado en Android Studio para ejecutar la aplicación.

**Pasos de Instalación**

**Clonar el Repositorio:**

git clone [https://github.com/sepazminot/ControlGastos_AM_SP.git](https://github.com/sepazminot/ControlGastos_AM_SP.git)

**Abrir en Android Studio:**

Inicia Android Studio.

Selecciona File > Open... y navega hasta la carpeta ControlGastos_AM_SP.

**Sincronizar Proyecto:**

Espera a que Gradle sincronice las dependencias del proyecto. Si hay algún error, asegúrate de que tu versión de Java y la configuración de Gradle sean compatibles.

**Ejecutar la Aplicación:**

Conecta un dispositivo Android o inicia un emulador.

Haz clic en el botón Run ( ▶️ ) en Android Studio.

**Estructura de la Base de Datos (SQLite)**

La aplicación utiliza una base de datos local llamada transaccion.db. Las tablas principales son:

**categories:** Almacena el ID, nombre y tipo (Ingreso o Gasto) de cada categoría.

**transactions:** Almacena el registro de cada movimiento financiero, incluyendo la referencia al ID de la categoría.
