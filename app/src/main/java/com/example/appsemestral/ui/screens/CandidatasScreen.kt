package com.example.appsemestral.ui.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appsemestral.domain.model.EstadoCandidata
import com.example.appsemestral.ui.components.Avatar
import com.example.appsemestral.ui.components.EstadoChip
import com.example.appsemestral.viewmodel.CandidatasViewModel

@Composable
fun CandidatasScreen(viewModel: CandidatasViewModel, puedeRegistrar: Boolean, abrirFormulario: () -> Unit, abrirDetalle: (Long) -> Unit) {
    val candidatas by viewModel.candidatas.collectAsStateWithLifecycle()
    var busqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf("Todas") }
    var menuAbierto by remember { mutableStateOf(false) }
    val estados = listOf("Todas") + EstadoCandidata.entries.map { it.etiqueta }
    val visibles = candidatas.filter { c ->
        (c.nombre.contains(busqueda, true) || c.area.contains(busqueda, true) || c.correo.contains(busqueda, true)) &&
            (filtro == "Todas" || c.estado.etiqueta == filtro)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Candidatas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("${candidatas.size} perfiles registrados", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (puedeRegistrar) FloatingActionButton(onClick = abrirFormulario, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
                Icon(Icons.Default.Add, contentDescription = "Registrar candidata")
            }
        }
        OutlinedTextField(value = busqueda, onValueChange = { busqueda = it }, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, label = { Text("Buscar por nombre, correo o área") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Box {
                OutlinedButton(onClick = { menuAbierto = true }) { Text("Estado: $filtro") }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    estados.forEach { estado -> DropdownMenuItem(text = { Text(estado) }, onClick = { filtro = estado; menuAbierto = false }) }
                }
            }
            Spacer(Modifier.weight(1f))
            Text("${visibles.size} resultados", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.CenterVertically))
        }
        Spacer(Modifier.height(8.dp))
        if (visibles.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay candidatas que coincidan con la búsqueda.") }
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(visibles, key = { it.id }) { candidata ->
                Card(modifier = Modifier.fillMaxWidth().clickable { abrirDetalle(candidata.id) }.animateItem(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Avatar(candidata.nombre)
                        Column(Modifier.weight(1f)) {
                            Text(candidata.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(candidata.area, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${candidata.aniosExperiencia} años · ${candidata.correo}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp)); EstadoChip(candidata.estado)
                        }
                    }
                }
            }
        }
    }
}
