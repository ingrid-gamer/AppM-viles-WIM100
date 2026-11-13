# Práctica de defensa en vivo — WIM 100

Objetivo: ensayar cambios acotados frente al docente y explicar qué capas se modifican. No es necesario improvisar una refactorización grande: hacer el cambio, compilar, probarlo y describir por qué corresponde a esa capa.

## Antes de la defensa

1. Abrir `AppSemestral` en Android Studio y esperar `BUILD SUCCESSFUL` en la sincronización.
2. Ejecutar `app/src/test` y asegurar que `CandidataValidatorTest`, `EvaluacionValidatorTest`, `CalculadoraPuntajeTest` y `AutenticadorLocalTest` pasen.
3. Ejecutar la app en un teléfono o emulador; probar login con campos vacíos, contraseña errónea, ambos perfiles y biometría/PIN sin contraseña; después probar filtro, formulario, teléfono formateado, PDF, evaluación, ranking, historial y resolución final.
4. Tener abiertas las carpetas `domain`, `viewmodel`, `data` y `ui`; explicar que Compose muestra el estado y llama eventos, el ViewModel coordina, Repository aplica la operación y DAO persiste con Room.
5. No usar información real de candidatas; los perfiles semilla son inventados.

## Demostración rápida — Validación de login y roles

1. Deja usuario y contraseña vacíos y pulsa **Ingresar con contraseña**. Explica los mensajes rojos y que no se crea una sesión.
2. Selecciona Evaluadora, escribe un usuario cualquiera y prueba una contraseña errónea.
3. Entra con cualquier nombre y `Wim100Ev`. Muestra las funciones de registro/evaluación y la ausencia de Ranking/Historial.
4. Cierra sesión, selecciona Dirección e ingresa con otro nombre y `Wim100Dir`. Muestra Ranking, Historial y las acciones de resolución.
5. Para la vía alternativa, cierra sesión, escribe un usuario y usa **Ingresar con biometría/PIN** sin contraseña; confirma el diálogo del dispositivo.
6. Explica que `AutenticadorLocal.kt` valida la contraseña compartida del perfil seleccionado y que el ViewModel registra los intentos sin guardar contraseñas. Aclara que esta es una simplificación académica y no una autorización de producción.

## Cambio en vivo 1 — Añadir un campo al formulario

**Ejemplo propuesto:** agregar un campo `sitioPortafolio` opcional de hasta 200 caracteres.

1. Añadir la propiedad al modelo `CandidataInput` en `domain/model/Models.kt`.
2. Añadir `CampoState<String>` a `FormularioCandidataState` y una función `cambiarSitioPortafolio` en `CandidataFormViewModel.kt`.
3. Crear una regla pura en `CandidataValidator.kt` (por ejemplo, validar la URL solo cuando no esté vacía) y agregarla al mapa de errores.
4. Mostrar `CampoValidado` en `CandidataFormScreen.kt` y pasar el nuevo valor al `CandidataInput`.
5. Añadir un test unitario para la URL vacía válida y para una URL inválida.

**Qué explicar:** cada campo guarda su valor y error con `CampoState`; la regla está en Kotlin puro y no dentro del composable. Si el nuevo dato también debe conservarse después de cerrar la app, hay que incorporarlo a la entidad Room y considerar migración de esquema.

## Cambio en vivo 2 — Añadir una consulta local de Room

**Ejemplo propuesto:** contar cuántas candidatas están `Postulada`.

1. Agregar una consulta en `CandidataDao`, usando el nombre de tabla y el valor de estado persistido. Si el enum se almacena como `name`, consulta `estado = 'Postulada'`.
2. Exponer el resultado en `CandidataRepository` con una función suspendida o un `Flow`.
3. Si la UI necesita refresco automático, crear una propiedad `StateFlow` en el ViewModel y convertir/observar el `Flow` de Room.
4. Mostrar el número en Candidatas mediante un texto, sin ejecutar consultas directamente desde Compose.
5. Ejecutar la app y verificar que, al registrar una candidatura, el número cambie.

**Qué explicar:** DAO contiene SQL; Repository ofrece una operación de datos más clara; ViewModel convierte los datos en estado de pantalla. No se debe poner SQL ni reglas de persistencia dentro del composable.

## Cambio en vivo 3 — Agregar una animación

**Ejemplo propuesto:** animar el tamaño del indicador de puntaje o el color de un estado.

1. Abrir `ui/components/Componentes.kt`.
2. Usar `animateFloatAsState`, `animateDpAsState` o `animateColorAsState` para interpolar una propiedad a partir de un estado, o usar `animateContentSize()` si cambia el contenido de una tarjeta.
3. Dar una etiqueta a la animación cuando la API la soporte y mantener un movimiento breve (200–400 ms) que no dificulte la lectura.
4. Probar tanto el cambio de estado como el caso en que no hay datos.

**Qué explicar:** la animación es presentación y pertenece a UI; no modifica el puntaje calculado ni los datos guardados.

## Preguntas que conviene practicar

- ¿Por qué se separó UI / ViewModel / Repository / DAO?
- ¿Qué garantiza el índice único de Room y por qué el chequeo del repositorio no es suficiente por sí solo ante operaciones simultáneas?
- ¿Por qué guardar una evaluación dentro de una transacción?
- ¿Cómo se calcula el resultado ponderado y qué representan los umbrales 50, 70 y 85?
- ¿Por qué el historial no tiene claves foráneas?
- ¿Qué diferencia existe entre `StateFlow` del ViewModel y `Flow` de Room?
- ¿Cómo se evita que una URI de PDF quede expuesta mediante una ruta de archivo abierta?
- ¿Qué pruebas validan formatos y límites sin iniciar un emulador?
- ¿Qué faltaría para pasar de una demo local a un producto real? Respuesta esperada: autenticación/roles de backend, cifrado de datos personales, sincronización segura, políticas de retención, revisión de privacidad y lector multipágina si se necesita.

## Orden de demo de 5 a 7 minutos

1. **Acceso (45 s):** mostrar error por campos vacíos, rechazo de credenciales incorrectas y entrada de prueba en uno de los roles.
2. **Formulario + archivo (60 s):** abrir el registro, mostrar una validación, adjuntar un PDF menor o igual a 5 MB desde el selector del teléfono, demostrar el progreso de importación y mostrar el nombre del archivo adjunto.
3. **Evaluación (90 s):** marcar cinco notas, explicar pesos, observación de nota baja y total final.
4. **Persistencia/seguridad (60 s):** guardar, enseñar notificación, mostrar intento duplicado bloqueado.
5. **Dirección y auditoría (60 s):** ranking, detalle, decisión final e historial.
6. **Arquitectura/tests (60 s):** abrir una consulta DAO, una regla de dominio y el test correspondiente.


## Comprobación de importación de PDF

1. En un dispositivo real, abrir **Candidatas → Registrar candidata → Adjuntar currículum PDF**. Debe aparecer el selector documental del sistema.
2. Elegir un PDF válido de menos de 5 MB. El botón debe mostrar `Importando PDF...` durante la copia y después mostrar `PDF adjunto: ...`.
3. Intentar seleccionar un archivo que no sea PDF o uno mayor a 5 MB. La app debe permanecer abierta y mostrar un mensaje de error debajo del botón.
4. Guardar la candidata y abrir su detalle para verificar que el CV importado siga accesible desde el almacenamiento privado.
5. Si el selector `OpenDocument` no está disponible en el dispositivo/emulador, el código intenta `GetContent`; si tampoco existe un selector, informa el problema en pantalla en vez de cerrar la app.
