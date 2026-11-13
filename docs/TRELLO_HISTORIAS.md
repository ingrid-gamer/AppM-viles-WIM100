# WIM 100 — Historias de usuario y propuesta de commits

Las historias sirven como tablero inicial para dividir el trabajo entre dos integrantes. Ajustar responsables y estados de Trello al equipo real. Las capas nombradas se refieren a `ui`, `viewmodel`, `domain`, `data` y `nativo`.

## HU01 — Ingresar con un perfil y validar el acceso
**Como** integrante del proceso WIM 100, **quiero** elegir Evaluadora o Dirección e ingresar con la contraseña correspondiente o biometría/PIN, **para** acceder a las funciones del perfil seleccionado.

- **Criterios de aceptación:** el selector de perfil está visible; se acepta cualquier nombre de usuario no vacío de hasta 60 caracteres; `Wim100Ev` valida Evaluadora y `Wim100Dir` valida Dirección; campos vacíos y claves equivocadas muestran errores visuales; la biometría/PIN permite continuar sin contraseña solo después del éxito de `BiometricPrompt`; los intentos, inicios y cierres se registran en la bitácora.
- **Capas/archivos:** `domain/auth/AutenticadorLocal.kt`, `ui/screens/LoginScreen.kt`, `ui/navigation/WimApp.kt`, `viewmodel/SesionViewModel.kt`, `nativo/BiometricHelper.kt`, `data/repository/HistorialRepository.kt`, `docs/VALIDACION_LOGIN.md`.
- **Responsable sugerido:** Integrante 1.
- **Commit sugerido:** `feat(auth): validar credenciales y restringir permisos por rol`

## HU02 — Registrar una candidata
**Como** evaluadora, **quiero** crear un perfil con antecedentes profesionales y CV, **para** centralizar su postulación.

- **Criterios de aceptación:** se validan nombre, correo único, teléfono chileno, área, años, resumen, LinkedIn y PDF de hasta 5 MB; el formulario muestra errores junto al campo; los datos se guardan en Room y se registra el evento.
- **Capas/archivos:** `ui/screens/CandidataFormScreen.kt`, `ui/components/Componentes.kt`, `viewmodel/CandidataFormViewModel.kt`, `domain/validation/CandidataValidator.kt`, `data/repository/CandidataRepository.kt`, `data/local/Daos.kt`.
- **Responsable sugerido:** Integrante 1.
- **Commit sugerido:** `feat(candidatas): agregar formulario validado y persistencia Room`

## HU03 — Explorar candidatas
**Como** usuaria, **quiero** buscar por nombre, correo o área y filtrar por estado, **para** encontrar rápidamente una candidatura.

- **Criterios de aceptación:** se muestra la lista reactiva desde Room; búsqueda y filtro trabajan al mismo tiempo; cada fila abre el detalle; las filas animan los cambios de posición.
- **Capas/archivos:** `ui/screens/CandidatasScreen.kt`, `viewmodel/CandidatasViewModel.kt`, `data/repository/CandidataRepository.kt`, `data/local/Daos.kt`.
- **Responsable sugerido:** Integrante 1.
- **Commit sugerido:** `feat(candidatas): agregar búsqueda y filtro por estado`

## HU04 — Consultar antecedentes y CV
**Como** evaluadora o integrante de Dirección, **quiero** consultar datos profesionales, LinkedIn y el PDF, **para** revisar los antecedentes sin buscar archivos externos.

- **Criterios de aceptación:** el detalle muestra información de contacto y resumen; permite abrir el perfil público de LinkedIn; permite renderizar el CV en un diálogo y compartirlo con una URI segura.
- **Capas/archivos:** `ui/screens/DetalleCandidataScreen.kt`, `ui/components/Componentes.kt`, `nativo/ArchivoHelper.kt`, `nativo/ShareHelper.kt`, `app/src/main/res/xml/file_paths.xml`.
- **Responsable sugerido:** Integrante 2.
- **Commit sugerido:** `feat(cv): visualizar y compartir currículums PDF`

## HU05 — Evaluar con matriz ponderada
**Como** evaluadora, **quiero** puntuar cinco criterios y dejar justificaciones, **para** obtener un resultado comparable entre candidatas.

- **Criterios de aceptación:** cada criterio acepta una nota 1–5; peso total del programa es 100%; puntaje se actualiza en vivo; las notas 1 y 2 necesitan justificación; observación general entre 20 y 400 caracteres; se registra una notificación al guardar.
- **Capas/archivos:** `ui/screens/EvaluacionScreen.kt`, `viewmodel/EvaluacionViewModel.kt`, `domain/calculation/CalculadoraPuntaje.kt`, `domain/validation/EvaluacionValidator.kt`, `data/repository/EvaluacionRepository.kt`, `nativo/NotificacionHelper.kt`.
- **Responsable sugerido:** Integrante 2.
- **Commit sugerido:** `feat(evaluacion): calcular puntaje ponderado y validar notas`

## HU06 — Impedir evaluaciones duplicadas
**Como** responsable del concurso, **quiero** que una evaluadora no califique dos veces a la misma candidata, **para** evitar sesgos y doble contabilización.

- **Criterios de aceptación:** el repositorio comprueba duplicados; el índice único `(candidataId, evaluadoraClave)` sirve de segunda barrera; el guardado es transaccional; un intento duplicado queda registrado aunque se revierta la operación.
- **Capas/archivos:** `data/local/Entities.kt`, `data/local/Daos.kt`, `data/repository/EvaluacionRepository.kt`, `data/repository/HistorialRepository.kt`.
- **Responsable sugerido:** Integrante 2.
- **Commit sugerido:** `fix(evaluacion): bloquear duplicados y auditar intentos`

## HU07 — Revisar ranking consolidado
**Como** integrante de Dirección, **quiero** consultar candidatas y puntajes ordenados, **para** comparar resultados antes de tomar una decisión.

- **Criterios de aceptación:** el ranking se alimenta de datos persistidos; muestra puntaje y categoría; las barras presentan animación de carga; puede compartirse el resumen de ranking.
- **Capas/archivos:** `ui/screens/ResultadosScreen.kt`, `viewmodel/ResultadosViewModel.kt`, `data/repository/EvaluacionRepository.kt`, `data/repository/CandidataRepository.kt`, `nativo/ShareHelper.kt`.
- **Responsable sugerido:** Integrante 1.
- **Commit sugerido:** `feat(ranking): mostrar resultados ordenados y compartir resumen`

## HU08 — Resolver la selección final
**Como** integrante de Dirección, **quiero** cambiar el estado final de una candidata a Seleccionada o No seleccionada, **para** formalizar la decisión del proceso.

- **Criterios de aceptación:** las opciones aparecen al rol Dirección; el estado se persiste; cada resolución queda en historial; Evaluadora no ve controles de decisión.
- **Capas/archivos:** `ui/screens/DetalleCandidataScreen.kt`, `viewmodel/CandidatasViewModel.kt`, `data/repository/CandidataRepository.kt`, `domain/model/Models.kt`.
- **Responsable sugerido:** Integrante 1.
- **Commit sugerido:** `feat(direccion): guardar resolución final de candidatas`

## HU09 — Auditar acciones del proceso
**Como** responsable de trazabilidad, **quiero** revisar acciones de acceso, evaluación y resolución, **para** entender quién realizó cada operación y cuándo.

- **Criterios de aceptación:** el historial ordena los eventos por fecha; tiene búsqueda; no usa claves foráneas hacia las tablas operativas; quedan registrados accesos, cierres, alta de candidatura, evaluación, resolución e intentos duplicados.
- **Capas/archivos:** `ui/screens/HistorialScreen.kt`, `viewmodel/HistorialViewModel.kt`, `data/local/Entities.kt`, `data/local/Daos.kt`, `data/repository/HistorialRepository.kt`.
- **Responsable sugerido:** Integrante 2.
- **Commit sugerido:** `feat(auditoria): registrar y consultar historial de acciones`

## HU10 — Comprobar permisos y ejecutar pruebas
**Como** integrante del equipo, **quiero** comprobar permisos nativos y tests, **para** demostrar que las funciones esenciales se pueden verificar antes de la entrega.

- **Criterios de aceptación:** Perfil informa disponibilidad de biometría/notificaciones; existe acceso a Ajustes; los tests cubren límites correctos e incorrectos; README relaciona los requisitos con el código; la defensa en vivo está documentada.
- **Capas/archivos:** `ui/screens/PerfilScreen.kt`, `nativo/BiometricHelper.kt`, `nativo/NotificacionHelper.kt`, `app/src/test/`, `README.md`, `docs/PRACTICA_DEFENSA.md`.
- **Responsable sugerido:** ambas integrantes.
- **Commit sugerido:** `test(docs): cubrir lógica de dominio y documentar defensa`
