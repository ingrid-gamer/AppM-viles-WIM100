package com.example.appsemestral.ui.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsemestral.di.AppContainer
import com.example.appsemestral.viewmodel.HistorialViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistorialScreen(container: AppContainer) {
    val vm: HistorialViewModel = viewModel(factory = container.viewModelFactory)
    val registros by vm.registros.collectAsStateWithLifecycle()
    var busqueda by remember { mutableStateOf("") }
    val formato = remember { SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("es", "CL")) }
    val filtrados = registros.filter { it.usuario.contains(busqueda, true) || it.accion.contains(busqueda, true) || it.detalle.contains(busqueda, true) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial de auditoría", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Bitácora de acciones, intentos duplicados y decisiones", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = busqueda, onValueChange = { busqueda = it }, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, label = { Text("Buscar usuario, acción o detalle") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        if (filtrados.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { Text("No hay eventos para mostrar.") }
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(filtrados, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(item.accion.replace('_', ' '), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(formato.format(Date(item.fecha)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp)); Text(item.detalle, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(5.dp)); Text("Usuario: ${item.usuario}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
