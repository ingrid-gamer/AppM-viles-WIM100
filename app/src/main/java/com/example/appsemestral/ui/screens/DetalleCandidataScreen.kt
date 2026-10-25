package com.example.appsemestral.ui.screens

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsemestral.di.AppContainer
import com.example.appsemestral.domain.model.EstadoCandidata
import com.example.appsemestral.nativo.ShareHelper
import com.example.appsemestral.ui.components.EstadoChip
import com.example.appsemestral.ui.components.PuntajeCircular
import com.example.appsemestral.ui.components.VisorCvDialog
import com.example.appsemestral.viewmodel.CandidatasViewModel
import com.example.appsemestral.viewmodel.DetalleEvaluacionesViewModel
import com.example.appsemestral.viewmodel.SesionActiva

@Composable
fun DetalleCandidataScreen(
    id: Long,
    vm: CandidatasViewModel,
    container: AppContainer,
    sesion: SesionActiva,
    iniciarEvaluacion: () -> Unit
) {
    val candidatas by vm.candidatas.collectAsStateWithLifecycle()
    val candidata = candidatas.firstOrNull { it.id == id }
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    val evaluacionesVm: DetalleEvaluacionesViewModel = viewModel(factory = container.viewModelFactory)
    val evaluaciones by evaluacionesVm.evaluaciones.collectAsStateWithLifecycle()
    LaunchedEffect(id) { evaluacionesVm.cargar(id) }
    var mostrarCv by remember { mutableStateOf(false) }
    var mostrarAntecedentes by remember { mutableStateOf(true) }
    var mostrarDecision by remember { mutableStateOf(true) }
    var mostrarEvaluaciones by remember { mutableStateOf(true) }
    val context = LocalContext.current

    if (candidata == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Detalle de candidata", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth().animateContentSize()) {
            Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(candidata.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(candidata.area, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp)); EstadoChip(candidata.estado)
                    Spacer(Modifier.height(12.dp)); Text("${candidata.aniosExperiencia} años de experiencia", fontWeight = FontWeight.SemiBold)
                    Text(candidata.correo); Text(candidata.telefono)
                }
                if (evaluaciones.isNotEmpty()) {
                    PuntajeCircular(evaluaciones.map { it.evaluacion.puntajeTotal }.average())
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Card(Modifier.fillMaxWidth().animateContentSize()) {
            Column(Modifier.padding(14.dp)) {
                TextButton(onClick = { mostrarAntecedentes = !mostrarAntecedentes }) {
                    Text(if (mostrarAntecedentes) "− Antecedentes profesionales" else "+ Antecedentes profesionales")
                }
                if (mostrarAntecedentes) {
                    Text(candidata.resumen)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { ShareHelper.abrirUrl(context, candidata.linkedin) }) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Abrir LinkedIn")
                    }
                    OutlinedButton(onClick = { mostrarCv = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Description, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Ver currículum PDF")
                    }
                    OutlinedButton(
                        onClick = { ShareHelper.compartirPdf(context, Uri.parse(candidata.cvUri), "CV de ${candidata.nombre}") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Compartir currículum PDF")
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Card(Modifier.fillMaxWidth().animateContentSize()) {
            Column(Modifier.padding(14.dp)) {
                TextButton(onClick = { mostrarEvaluaciones = !mostrarEvaluaciones }) {
                    Text(if (mostrarEvaluaciones) "− Evaluaciones y observaciones" else "+ Evaluaciones y observaciones")
                }
                if (mostrarEvaluaciones) {
                    if (evaluaciones.isEmpty()) {
                        Text("Esta candidata todavía no tiene evaluaciones guardadas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        evaluaciones.forEach { detalle ->
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            Text("Evaluadora: ${detalle.evaluacion.evaluadoraNombre}", fontWeight = FontWeight.Bold)
                            Text("Puntaje: ${"%.2f".format(detalle.evaluacion.puntajeTotal)} / 100 · ${detalle.evaluacion.categoria}")
                            Text("Observación general: ${detalle.evaluacion.observacionGeneral}")
                            detalle.notas.forEach { nota ->
                                Spacer(Modifier.height(5.dp))
                                Text("• ${nota.nombreCriterio}: ${nota.puntaje}/5", fontWeight = FontWeight.SemiBold)
                                if (nota.observacion.isNotBlank()) Text("  ${nota.observacion}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Card(Modifier.fillMaxWidth().animateContentSize()) {
            Column(Modifier.padding(14.dp)) {
                TextButton(onClick = { mostrarDecision = !mostrarDecision }) {
                    Text(if (mostrarDecision) "− Acciones según tu rol" else "+ Acciones según tu rol")
                }
                if (mostrarDecision) {
                    if (sesion.rol.puedeEvaluar) {
                        Button(
                            onClick = iniciarEvaluacion,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = candidata.estado != EstadoCandidata.Seleccionada && candidata.estado != EstadoCandidata.NoSeleccionada
                        ) { Text("Evaluar candidata") }
                    } else if (sesion.rol.puedeResolver) {
                        Button(
                            onClick = { vm.resolver(candidata.id, EstadoCandidata.Seleccionada, sesion.nombre) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Marcar como seleccionada") }
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { vm.resolver(candidata.id, EstadoCandidata.NoSeleccionada, sesion.nombre) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Marcar como no seleccionada") }
                    }
                }
            }
        }
        if (mensaje.isNotBlank()) {
            Spacer(Modifier.height(8.dp)); Text(mensaje, color = MaterialTheme.colorScheme.secondary)
            LaunchedEffect(mensaje) { kotlinx.coroutines.delay(2500); vm.limpiarMensaje() }
        }
        Spacer(Modifier.height(10.dp))
        Text("Registrada por: ${candidata.registradaPor}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
    }
    if (mostrarCv) VisorCvDialog(uriTexto = candidata.cvUri, alCerrar = { mostrarCv = false })
}
