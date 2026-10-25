package com.example.appsemestral.ui.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsemestral.di.AppContainer
import com.example.appsemestral.ui.components.CampoValidado
import com.example.appsemestral.ui.components.PuntajeCircular
import com.example.appsemestral.ui.components.sacudir
import com.example.appsemestral.viewmodel.EvaluacionViewModel
import com.example.appsemestral.viewmodel.SesionActiva

@Composable
fun EvaluacionScreen(id: Long, container: AppContainer, sesion: SesionActiva, alFinalizar: () -> Unit) {
    val vm: EvaluacionViewModel = viewModel(factory = container.viewModelFactory)
    val criterios by vm.criterios.collectAsStateWithLifecycle()
    val puntajes by vm.puntajes.collectAsStateWithLifecycle()
    val observaciones by vm.observaciones.collectAsStateWithLifecycle()
    val errores by vm.errores.collectAsStateWithLifecycle()
    val total by vm.puntajeTotal.collectAsStateWithLifecycle()
    val categoria by vm.categoria.collectAsStateWithLifecycle()
    val observacionGeneral by vm.observacionGeneral.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    val guardado by vm.guardado.collectAsStateWithLifecycle()
    val intentosInvalidos by vm.intentosInvalidos.collectAsStateWithLifecycle()
    var clickGuardar by remember { mutableIntStateOf(0) }
    val altoBoton by animateDpAsState(if (clickGuardar > 0) 55.dp else 52.dp, label = "reboteBoton")
    LaunchedEffect(id, sesion.clave) { vm.iniciar(id, sesion.clave, sesion.nombre) }
    LaunchedEffect(guardado) { if (guardado) { vm.limpiarMensaje(); alFinalizar() } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Matriz de evaluación", style = MaterialTheme.typography.headlineMedium)
        Text("Cada criterio se califica del 1 al 5 y utiliza el peso definido por el programa.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(14.dp))
        if (total != null) {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    PuntajeCircular(total!!)
                    Column {
                        Text("Puntaje ponderado", style = MaterialTheme.typography.titleMedium)
                        Text("${"%.2f".format(total)} / 100", style = MaterialTheme.typography.headlineSmall)
                        Text(categoria, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        criterios.forEach { criterio ->
            Card(Modifier.fillMaxWidth().padding(bottom = 10.dp).animateContentSize()) {
                Column(Modifier.padding(14.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(criterio.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("Peso ${criterio.peso}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text("${puntajes[criterio.id] ?: "—"}/5", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(4.dp)); Text(criterio.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                        (1..5).forEach { valor ->
                            val seleccionado = puntajes[criterio.id] == valor
                            val escalaSelector by animateFloatAsState(
                                targetValue = if (seleccionado) 1.08f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                label = "rebotePuntaje$valor"
                            )
                            FilterChip(
                                selected = seleccionado,
                                onClick = { vm.cambiarPuntaje(criterio.id, valor) },
                                label = { Text(valor.toString()) },
                                modifier = Modifier.weight(1f).scale(escalaSelector)
                            )
                        }
                    }
                    errores["criterio_${criterio.id}"]?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    if (puntajes[criterio.id] in 1..2) {
                        Spacer(Modifier.height(8.dp))
                        CampoValidado(observaciones[criterio.id].orEmpty(), { vm.cambiarObservacion(criterio.id, it) }, "Justificación obligatoria (mín. 10 caracteres)", errores["justificacion_${criterio.id}"], maxCaracteres = 250, unaLinea = false)
                    } else if (observaciones[criterio.id].orEmpty().isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        CampoValidado(observaciones[criterio.id].orEmpty(), { vm.cambiarObservacion(criterio.id, it) }, "Observación opcional", maxCaracteres = 250, unaLinea = false)
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        CampoValidado(observacionGeneral, vm::cambiarObservacionGeneral, "Observación general (20 a 400 caracteres)", errores["observacionGeneral"], maxCaracteres = 400, unaLinea = false)
        if (mensaje.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(mensaje, color = if (guardado) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { clickGuardar++; vm.guardar() }, modifier = Modifier.fillMaxWidth().height(altoBoton).sacudir(intentosInvalidos)) { Text("Guardar evaluación") }
        Spacer(Modifier.height(26.dp))
    }
}
