# Validación del inicio de sesión — WIM 100

## Propósito

El login valida que el nombre de usuario no esté vacío, que se seleccione un perfil y que, cuando se use contraseña, esta corresponda al perfil elegido. La interfaz muestra errores junto a los campos y registra los intentos rechazados en `RegistroHistorial`. La biometría/PIN es una vía alternativa: requiere un nombre de usuario y deja continuar solo después de que Android confirma el bloqueo del dispositivo.

## Perfiles y contraseñas de demostración

| Perfil seleccionado | Contraseña | Permisos principales |
|---|---|---|
| Evaluadora | `Wim100Ev` | Registrar candidatas y realizar evaluaciones. |
| Dirección | `Wim100Dir` | Consultar ranking e historial, y resolver candidaturas. |

Se permite cualquier nombre de usuario no vacío de hasta 60 caracteres. La contraseña no depende del nombre, sino del perfil seleccionado. La Evaluadora y Dirección siguen teniendo permisos diferentes en la navegación y en las acciones de detalle.

> **Limitación académica:** son contraseñas compartidas y locales dentro del APK. La autenticación biométrica confirma el dispositivo, pero el perfil se elige desde la pantalla; esto no vincula por sí solo la identidad real de la persona con una cuenta corporativa. No usar este método para datos reales o producción.

## Comportamiento esperado

1. **Usuario vacío con contraseña:** al pulsar `Ingresar con contraseña`, el usuario se marca en rojo y aparece `Ingresa tu nombre de usuario.`. No se inicia sesión.
2. **Contraseña vacía:** el campo de contraseña se marca en rojo y se indica que hay que ingresar la contraseña del perfil seleccionado.
3. **Contraseña equivocada:** el acceso se rechaza, la contraseña se marca en rojo y aparece un mensaje que indica que la clave no corresponde a Evaluadora o Dirección.
4. **Credencial de Evaluadora:** cualquier usuario no vacío puede entrar con `Wim100Ev` si tiene seleccionado Evaluadora. Solo se habilitan registro y evaluación.
5. **Credencial de Dirección:** cualquier usuario no vacío puede entrar con `Wim100Dir` si tiene seleccionado Dirección. Se habilitan ranking, historial y resolución final.
6. **Biometría/PIN:** tras escribir un nombre, se puede elegir `Ingresar con biometría/PIN` sin completar contraseña. Android solicita una huella/rostro o credencial del dispositivo admitida. Si falla o se cancela, no se crea la sesión.
7. **Auditoría:** se registran intentos fallidos, inicios y cierres de sesión. Nunca se registra la contraseña.

## Matriz de autorización

| Acción | Evaluadora | Dirección |
|---|:---:|:---:|
| Consultar candidatas y antecedentes | Sí | Sí |
| Registrar candidata | Sí | No |
| Guardar evaluación | Sí | No |
| Ver ranking consolidado | No | Sí |
| Consultar historial de auditoría | No | Sí |
| Resolver como Seleccionada / No seleccionada | No | Sí |

La matriz se define mediante propiedades del enum `RolUsuario` y se aplica en la navegación y en los controles de pantalla. La contraseña seleccionada habilita el perfil correspondiente durante la demo.

## Cómo demostrarlo ante el profesor

1. Deja los campos vacíos y pulsa `Ingresar con contraseña`: muestra los mensajes rojos.
2. Escribe `Ingrid` y una contraseña incorrecta bajo Evaluadora: se rechaza y aumenta el contador de intentos.
3. Escribe cualquier nombre, por ejemplo `Ingrid`, selecciona Evaluadora e ingresa `Wim100Ev`: se habilitan registro y evaluación, pero no Ranking ni Historial.
4. Cierra sesión y escribe cualquier otro nombre, selecciona Dirección e ingresa `Wim100Dir`: aparecen Ranking e Historial y las acciones para resolver candidaturas.
5. Cierra sesión, vuelve a escribir un nombre y pulsa `Ingresar con biometría/PIN` sin escribir contraseña. Confirma el diálogo del dispositivo para demostrar el acceso alternativo.
6. Abre Historial con Dirección y muestra intentos fallidos, inicios y cierres de sesión.

## Archivos implicados

- `domain/auth/AutenticadorLocal.kt`: verifica la contraseña según el perfil y prepara la cuenta solicitada para el flujo biométrico.
- `viewmodel/SesionViewModel.kt`: valida usuario/contraseña, mantiene sesión e historial de auditoría.
- `ui/screens/LoginScreen.kt`: selector de perfiles, campos, errores y botones de acceso.
- `nativo/BiometricHelper.kt` y `ui/navigation/WimApp.kt`: invocan la validación de Android y solo inician sesión tras el éxito.
- `domain/model/Models.kt`, `ui/navigation/WimApp.kt` y `ui/screens/DetalleCandidataScreen.kt`: roles y protección de acciones.
- `app/src/test/.../AutenticadorLocalTest.kt`: pruebas de ambas contraseñas, roles incompatibles, usuarios arbitrarios, campos vacíos y acceso biométrico preparado.

## Mejora necesaria antes de producción

Usar cuentas individuales almacenadas y validadas por un backend, contraseñas con algoritmo adaptativo con sal, tokens/sesiones protegidos y autorización aplicada en servidor. La clave compartida por rol y la selección local de perfil son únicamente una simplificación para demostrar el requisito académico.
