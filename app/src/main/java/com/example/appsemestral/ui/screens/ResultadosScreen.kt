package com.example.appsemestral.ui.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsemestral.di.AppContainer
import com.example.appsemestral.nativo.ShareHelper
import com.example.appsemestral.ui.components.EstadoChip
import com.example.appsemestral.viewmodel.ResultadosViewModel

@Composable
fun ResultadosScreen(container: AppContainer) {
    val vm: ResultadosViewModel = viewModel(factory = container.viewModelFactory)
    val ranking by vm.ranking.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Ranking WIM 100", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Promedio de evaluaciones registradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = {
                val texto = ranking.mapIndexed { i, fila -> "${i + 1}. ${fila.candidata.nombre} · ${"%.2f".format(fila.promedio)} puntos · ${fila.candidata.estado.etiqueta}" }.joinToString("\n").ifBlank { "Aún no hay resultados." }
                ShareHelper.compartirTexto(context, "Ranking WIM 100", "Ranking WIM 100\n$texto")
            }) { Text("Compartir") }
        }
        Spacer(Modifier.height(12.dp))
        if (ranking.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            itemsIndexed(ranking, key = { _, item -> item.candidata.id }) { index, fila ->
                val progreso by animateFloatAsState(targetValue = (fila.promedio / 100.0).toFloat().coerceIn(0f, 1f), label = "barraRanking")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                                Text("#${index + 1}", modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(fila.candidata.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(fila.candidata.area, style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${"%.1f".format(fila.promedio)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("/ 100", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth().height(7.dp), color = MaterialTheme.colorScheme.secondary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("${fila.cantidadEvaluaciones} evaluación(es) · ${fila.categoria}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            EstadoChip(fila.candidata.estado)
                        }
                    }
                }
            }
        }
    }
}
