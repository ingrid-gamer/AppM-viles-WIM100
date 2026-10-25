# WIM 100 — AppSemestral

Aplicación académica Android para digitalizar la postulación, evaluación y resolución de candidaturas del programa WIM 100 de DYM Cyber. Proyecto Kotlin + Jetpack Compose, arquitectura MVVM, Room/SQLite y dependencias con inyección manual.

> **Antes de entregar:** reemplaza los nombres de las integrantes en la sección de equipo, abre el proyecto en Android Studio, instala el SDK 37 y ejecuta la sincronización, las pruebas y la app en un teléfono real. Esta entrega incluye el código fuente y los tests, pero el entorno donde se generó no tenía Android SDK ni Gradle instalado; por eso no se pudo verificar una compilación Android completa desde aquí.

## 1. Funciones principales

- Dos perfiles de acceso: **Evaluadora** y **Dirección**. La persona escribe cualquier nombre de usuario no vacío y selecciona el perfil; una contraseña distinta valida cada perfil. Evaluadora puede registrar candidatas y guardar evaluaciones; Dirección puede consultar ranking/historial y resolver la selección.
- Ocho destinos de pantalla: Login, Candidatas, Formulario, Detalle, Evaluación, Resultados/Ranking, Historial y Perfil.
- Cinco criterios semilla con pesos de 25%, 25%, 20%, 15% y 15%. Las notas de 1 a 5 se convierten a una escala lineal de 0 a 100 y se combinan con la ponderación.
- Carga de candidatas, datos ficticios de demostración y currículums PDF locales creados al primer arranque.
- Bloqueo de evaluaciones duplicadas por evaluadora y candidata tanto en el repositorio como en un índice único de Room.
- Registro de actividad sin claves foráneas en `RegistroHistorial`.
- Biometría/PIN, permiso de notificaciones, selector de PDF con alternativa de compatibilidad, importación del archivo al almacenamiento privado de la app, comprobación de PDF real y límite de 5 MB, visor multipágina con `PdfRenderer`, apertura de LinkedIn y compartir documentos/textos con intents.
- Logotipo WIM 100 aplicado en la pantalla de inicio de sesión y como icono de la aplicación Android.
- Inicio de sesión con selector visible de Evaluadora/Dirección, contraseña específica para cada perfil, errores visuales en campos vacíos o contraseña incorrecta y registro de intentos fallidos. La opción biométrica/PIN solo exige el nombre de usuario y crea la sesión después de una confirmación exitosa del dispositivo.
- Tests unitarios de Kotlin puro para los validadores, la calculadora y la autenticación local.

## 2. Datos de demostración

Al iniciar por primera vez, `DatosSemilla.kt` crea cinco criterios, seis candidatas ficticias, currículums PDF y cuatro evaluaciones. Los datos semilla solo se vuelven a crear si no hay candidatas en la base de datos. Durante desarrollo, para reiniciar esta demostración, borra los datos de la app o desinstálala y vuelve a instalarla. **Esa operación elimina los datos locales de prueba**, por lo que no debe hacerse con información real.

## 3. Cuentas de prueba y validación del inicio de sesión

La pantalla muestra un selector para elegir **Evaluadora** o **Dirección**. Se puede escribir cualquier nombre de usuario no vacío; la contraseña válida depende del perfil seleccionado:

| Perfil seleccionado | Contraseña de demostración | Permisos principales |
|---|---|---|
| Evaluadora | `Wim100Ev` | Registrar candidatas y evaluar. |
| Dirección | `Wim100Dir` | Consultar ranking/historial y resolver selecciones. |

Si dejas vacío el usuario o la contraseña al entrar con contraseña, los campos se marcan en rojo con mensajes. Si la contraseña no corresponde al perfil seleccionado, el acceso se rechaza. Como alternativa, al escribir el nombre de usuario se puede elegir **Ingresar con biometría/PIN**; si el dispositivo confirma la autenticación, no se pide contraseña. En esta demostración la biometría confirma el dispositivo, pero no vincula por sí sola un nombre con una identidad corporativa.

La Evaluadora puede registrar candidatas y evaluarlas. Dirección puede consultar Ranking e Historial y resolver una candidatura como Seleccionada o No seleccionada. Las acciones y destinos están condicionados por permisos definidos en `RolUsuario`. Los intentos inválidos, los inicios y los cierres de sesión se registran en el historial sin almacenar contraseñas.

**Solo para demostración académica:** las contraseñas de perfil están incluidas en el APK y son compartidas por todas las personas que seleccionan ese rol. La opción biométrica usa el bloqueo del dispositivo y permite continuar con el perfil seleccionado por la usuaria. Esto demuestra validación y flujo de roles, pero no constituye una autorización segura para producción. Una versión real requiere cuentas individuales, autenticación en servidor y permisos aplicados también en el backend. Consulta [`docs/VALIDACION_LOGIN.md`](docs/VALIDACION_LOGIN.md).

## 4. Adjuntar PDF y enlaces de LinkedIn

El botón **Adjuntar currículum PDF** intenta abrir el selector nativo `OpenDocument`. Si el dispositivo no dispone de un componente compatible, prueba como alternativa `GetContent` y muestra un mensaje en pantalla si ambos selectores fallan, en lugar de dejar la aplicación cerrarse por esa excepción. Después de seleccionar el archivo, la app lo copia en segundo plano a su carpeta privada `files/cvs/`, comprueba la firma real `%PDF-` y limita la importación a 5 MB. Así el CV no depende del permiso temporal del proveedor externo al abrirlo más adelante.

Ejemplos de URL de LinkedIn aceptadas por el validador actual:

- `https://www.linkedin.com/in/tu-perfil/`
- `https://linkedin.com/in/ingrid-araya`
- `https://www.linkedin.com/in/ingrid-araya-12345678/`

La ruta debe corresponder al perfil personal `/in/usuario`. Enlaces a empresas (`/company/`), búsquedas, publicaciones o la página principal de LinkedIn no se aceptan como URL de perfil en este campo.

Si un archivo no es un PDF válido, está vacío o supera 5 MB, aparece un mensaje bajo el botón y no se guarda como CV seleccionado. El archivo se lee/importa en un dispatcher de IO para no bloquear la interfaz durante la copia.

## 5. Configuración y ejecución

1. Instala una versión reciente de Android Studio y el **Android SDK Platform 37**, además de Build Tools compatibles. Instala JDK 17 (puede usarse el JBR de Android Studio).
2. Abre la carpeta `AppSemestral` como proyecto existente. El módulo Android es `:app`; el namespace y applicationId son `com.example.appsemestral`.
3. En la terminal de la carpeta raíz, ejecuta una vez `./gradlew wrapper --gradle-version 8.13` (macOS/Linux) o `.\gradlew.bat wrapper --gradle-version 8.13` en PowerShell. Los scripts `gradlew` incluidos descargan Gradle 8.13 al perfil del usuario y permiten generar el wrapper estándar. Luego abre/sincroniza el proyecto en Android Studio.
4. Espera la sincronización de Gradle. La versión solicitada de Kotlin es 2.2.10 y el proyecto está configurado para SDK 37. Si la combinación de herramientas disponible en tu instalación no reconoce alguna versión, actualiza Android Studio/SDK y revisa `gradle/libs.versions.toml` con la guía de la asignatura antes de cambiar versiones.
5. Selecciona un teléfono físico conectado por USB y activa Depuración USB. La autenticación biométrica se prueba mejor en un dispositivo con una huella/rostro registrado o bloqueo de pantalla configurado.
6. Ejecuta la configuración `app`.
7. Para las pruebas unitarias, ejecuta `./gradlew :app:testDebugUnitTest` o la tarea `testDebugUnitTest` desde el panel Gradle.

### Configuración técnica incluida

- `minSdk = 24`, `compileSdk = 37`, `targetSdk = 37`.
- Kotlin 2.2.10 y KSP 2.2.10-2.0.2.
- Compose BOM 2026.02.01, Room 2.8.4, Navigation Compose 2.9.8 y Biometric 1.1.0.
- Memoria del daemon de Gradle 2 GB, caché y configuración de caché activadas en `gradle.properties`.
- `allowBackup="false"`, `adjustResize`, edge-to-edge y `FileProvider` limitado a la carpeta privada `files/cvs/`.
- `dynamicColor = false` en el tema; paleta violeta, turquesa y magenta.

> Nota de compatibilidad: AGP 8.13.2/Gradle 8.13 es la combinación que mejor encaja con Kotlin 2.2.10 y KSP 2.2.10-2.0.2 solicitados. La matriz oficial actual indica que API 37 necesita AGP 9.1.1 o posterior; por eso puede aparecer una advertencia o ser necesario ajustar el plugin si tu entorno exige validación estricta de SDK 37. No se actualizó silenciosamente la versión de Kotlin/KSP porque son requisitos explícitos de la pauta.
>
> Los paquetes de Gradle están configurados para descargarse desde los repositorios Maven/Google. La carpeta `local.properties` **no se incluye**: Android Studio la genera con la ruta del SDK local. `build/`, `.gradle/`, `.kotlin/` y los archivos locales se excluyen mediante `.gitignore`.

## 6. Mapeo de requisitos a archivos

| Requisito | Archivos principales |
|---|---|
| Configuración, SDK, plugins y versiones | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties` |
| Manifest, icono de app, permisos, `allowBackup=false`, `adjustResize`, proveedor PDF | `app/src/main/AndroidManifest.xml`, `app/src/main/res/mipmap-*`, `app/src/main/res/drawable-nodpi/wim100_logo.png`, `app/src/main/res/xml/file_paths.xml`, `app/src/main/res/values/styles.xml` |
| `FragmentActivity`, edge-to-edge | `app/src/main/java/com/example/appsemestral/MainActivity.kt` |
| Inicialización y datos semilla | `WimApplication.kt`, `data/local/DatosSemilla.kt`, `nativo/ArchivoHelper.kt` |
| MVVM y estados de pantalla | `viewmodel/` |
| Inyección manual | `di/AppContainer.kt` |
| Entidades Room, DAO y base `wim100.db` | `data/local/Entities.kt`, `Daos.kt`, `WimDatabase.kt` |
| Guardado transaccional, restricción de duplicados e historial | `data/repository/EvaluacionRepository.kt`, `CandidataRepository.kt`, `HistorialRepository.kt` |
| Cálculo ponderado | `domain/calculation/CalculadoraPuntaje.kt` |
| Validación de formularios | `domain/validation/CandidataValidator.kt`, `EvaluacionValidator.kt` |
| Autenticación local y validación por perfil | `domain/auth/AutenticadorLocal.kt`, `viewmodel/SesionViewModel.kt`, `ui/screens/LoginScreen.kt` |
| Autorización por rol | `domain/model/Models.kt`, `ui/navigation/WimApp.kt`, `ui/screens/DetalleCandidataScreen.kt` |
| Justificación y casos de prueba del login | `docs/VALIDACION_LOGIN.md`, `app/src/test/.../AutenticadorLocalTest.kt` |
| Validación de estado de campo | `domain/model/Models.kt` (`CampoState`) y `viewmodel/CandidataFormViewModel.kt` |
| Biometría, importación/validación de PDF, notificaciones e intents | `nativo/BiometricHelper.kt`, `ArchivoHelper.kt`, `NotificacionHelper.kt`, `ShareHelper.kt`, `ui/screens/CandidataFormScreen.kt` |
| Componentes reutilizables, campo telefónico y tema | `ui/components/Componentes.kt`, `ui/components/TelefonoChilenoField.kt`, `ui/theme/Theme.kt` |
| Las 8 pantallas y navegación | `ui/screens/`, `ui/navigation/WimApp.kt` |
| 12 tests de candidata | `app/src/test/.../CandidataValidatorTest.kt` |
| 3 tests de evaluación | `app/src/test/.../EvaluacionValidatorTest.kt` |
| 4 tests de cálculo | `app/src/test/.../CalculadoraPuntajeTest.kt` |
| Tests de autenticación por perfil y acceso biométrico | `app/src/test/.../AutenticadorLocalTest.kt` |
| Historias, commits y defensa | `docs/TRELLO_HISTORIAS.md`, `docs/PRACTICA_DEFENSA.md` |

## 7. Guion de demo sugerido

1. Abrir WIM 100 e intentar enviar los campos vacíos para demostrar la validación visual del formulario.
2. Intentar enviar el login vacío, seleccionar Evaluadora y probar una contraseña incorrecta. Luego entrar con cualquier nombre de usuario y `Wim100Ev`.
3. En Candidatas, buscar una persona, cambiar el filtro por estado y entrar a un detalle.
4. Abrir LinkedIn y el currículum PDF local. Compartir el CV para mostrar el intent implícito.
5. Volver a Candidatas y registrar un perfil de prueba con datos válidos. Probar brevemente un formato inválido para demostrar los mensajes de validación.
6. Abrir Evaluación. Asignar puntajes a los cinco criterios, explicar los pesos y la actualización del puntaje; mostrar que las notas 1 o 2 requieren justificación y completar observación general.
7. Guardar evaluación y mostrar la notificación local. Intentar otra evaluación desde la misma sesión para ilustrar que el duplicado está bloqueado. La validación se aplica también en Room.
8. Cerrar sesión y entrar con un nombre cualquiera como Dirección usando `Wim100Dir`; abrir Ranking e Historial para mostrar los permisos de ese perfil. También se puede demostrar el acceso biométrico/PIN después de escribir un nombre.
9. Abrir un detalle como Dirección y cambiar el resultado a Seleccionada o No seleccionada.
10. En Perfil, mostrar el estado de la biometría/notificaciones y la opción de Ajustes. En un teléfono compatible, probar el flujo biométrico.
11. Ejecutar los tests unitarios y enseñar `docs/PRACTICA_DEFENSA.md` como preparación para la modificación en vivo.

## 8. Equipo

- Integrante 1: **[REEMPLAZAR CON NOMBRE]**
- Integrante 2: **[REEMPLAZAR CON NOMBRE]**

## 9. Consideraciones de alcance

El login se implementó como demostración académica con perfiles seleccionables y una contraseña compartida por perfil (`Wim100Ev` / `Wim100Dir`). Se permite cualquier nombre no vacío y, alternativamente, se puede entrar con biometría/PIN después de la confirmación del dispositivo. Esta solución es útil para demostrar validaciones y navegación diferenciada, pero no identifica de forma individual ni asegura los permisos en un entorno real. Antes de producción se requiere autenticación individual y autorización del lado servidor, almacenamiento robusto de contraseñas, protección de sesiones, cifrado de datos sensibles y una revisión de seguridad. Consulta `docs/VALIDACION_LOGIN.md`.


## Correcciones de revisión aplicadas

- El nombre de la candidata exige nombre y al menos un apellido; un solo nombre queda marcado como error.
- El teléfono comienza con el prefijo fijo `+56 9 `, limita el número local a ocho dígitos y formatea automáticamente como `1234 5678` sin mover el cursor durante la escritura. No muestra contador de caracteres.
- La etiqueta del campo queda como `Años de experiencia`, sin mostrar el rango en el texto. La validación del rango continúa en la lógica de negocio.
- Se quitó el contador de caracteres de LinkedIn.
- El inicio de sesión muestra los perfiles Evaluadora/Dirección. Acepta cualquier usuario no vacío si ingresa la contraseña del perfil (`Wim100Ev` o `Wim100Dir`); la biometría/PIN permite continuar sin contraseña tras validar el dispositivo y después de escribir un usuario.


## Correcciones de PDF e identidad visual

- El selector documental usa `OpenDocument` y una alternativa `GetContent` si el sistema no tiene un selector nativo disponible. Los errores se muestran en pantalla.
- El PDF elegido se copia en segundo plano al almacenamiento privado, se valida por su firma y tamaño máximo de 5 MB y se expone después mediante el `FileProvider` interno.
- Se añadió retroalimentación de progreso durante la importación y se deshabilita Guardar mientras el archivo se está copiando.
- Se reemplazó el icono genérico de Android por el logotipo recibido para WIM 100, tanto en el launcher como en la pantalla de inicio de sesión.
- Se muestran ejemplos de URL válidas de LinkedIn debajo del campo.
