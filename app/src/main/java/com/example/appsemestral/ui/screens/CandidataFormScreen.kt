package com.example.appsemestral.ui.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsemestral.di.AppContainer
import com.example.appsemestral.ui.components.CampoValidado
import com.example.appsemestral.ui.components.TelefonoChilenoField
import com.example.appsemestral.viewmodel.CandidataFormViewModel

@Composable
fun CandidataFormScreen(container: AppContainer, usuario: String, alVolver: () -> Unit) {
    val vm: CandidataFormViewModel = viewModel(factory = container.viewModelFactory)
    val s by vm.estado.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    val guardado by vm.guardado.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var errorSelectorPdf by remember { mutableStateOf<String?>(null) }

    // Se intenta primero el selector documental nativo; algunos dispositivos/emuladores
    // no tienen DocumentsUI disponible, así que se conserva una alternativa compatible.
    val manejarPdfSeleccionado: (Uri?) -> Unit = { uri ->
        if (uri != null) {
            errorSelectorPdf = null
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            runCatching { vm.seleccionarCv(uri) }
                .onFailure {
                    errorSelectorPdf = "No se pudo leer ese archivo. Prueba con otro PDF desde Archivos."
                }
        }
    }
    val selectorPdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        manejarPdfSeleccionado(uri)
    }
    val selectorAlternativo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        manejarPdfSeleccionado(uri)
    }

    fun abrirSelectorPdf() {
        errorSelectorPdf = null

        try {
            selectorPdf.launch(arrayOf("application/pdf"))
        } catch (e: Exception) {
            Log.e(
                "WIM100_PDF",
                "Error al abrir OpenDocument",
                e
            )

            try {
                selectorAlternativo.launch("application/pdf")
            } catch (e2: Exception) {
                Log.e(
                    "WIM100_PDF",
                    "Error al abrir GetContent",
                    e2
                )

                errorSelectorPdf =
                    "No se pudo abrir el selector. " +
                            "Revisa Logcat para conocer la causa."
            }
        }
    }

    LaunchedEffect(guardado) { if (guardado) { vm.limpiarMensaje(); alVolver() } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(18.dp)) {
        Text("Registrar candidata", style = MaterialTheme.typography.headlineMedium)
        Text("Completa los antecedentes. Los campos se validan mientras escribes.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(14.dp))
        CampoValidado(s.nombre.valor, vm::cambiarNombre, "Nombre completo", s.nombre.error, Icons.Default.Badge, 60)
        Spacer(Modifier.height(8.dp))
        CampoValidado(s.correo.valor, vm::cambiarCorreo, "Correo electrónico", s.correo.error, Icons.Default.Email, 120, KeyboardType.Email)
        Spacer(Modifier.height(8.dp))
        TelefonoChilenoField(s.telefono.valor, vm::cambiarTelefono, s.telefono.error)
        Spacer(Modifier.height(8.dp))
        CampoValidado(s.area.valor, vm::cambiarArea, "Área profesional", s.area.error, Icons.Default.Badge, 80)
        Spacer(Modifier.height(8.dp))
        CampoValidado(s.anios.valor, vm::cambiarAnios, "Años de experiencia", s.anios.error, null, 2, KeyboardType.Number)
        Spacer(Modifier.height(8.dp))
        CampoValidado(s.resumen.valor, vm::cambiarResumen, "Resumen profesional", s.resumen.error, null, 500, KeyboardType.Text, unaLinea = false)
        Spacer(Modifier.height(8.dp))
        CampoValidado(s.linkedin.valor, vm::cambiarLinkedin, "Enlace a LinkedIn", s.linkedin.error, Icons.Default.Link, null, KeyboardType.Uri)
        Text(
            "Ejemplos aceptados: https://www.linkedin.com/in/tu-perfil/ · https://linkedin.com/in/ingrid-araya",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { abrirSelectorPdf() },
            enabled = !s.cvCargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (s.cvCargando) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else if (s.cvNombre.isNotBlank() && s.cvUri.error == null) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
            } else {
                Icon(Icons.Default.UploadFile, contentDescription = null)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                when {
                    s.cvCargando -> "Importando PDF..."
                    s.cvNombre.isBlank() -> "Adjuntar currículum PDF (máx. 5 MB)"
                    s.cvUri.error == null -> "PDF adjunto: ${s.cvNombre}"
                    else -> "Seleccionar otro PDF"
                }
            )
        }
        if (errorSelectorPdf != null) Text(errorSelectorPdf!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        if (s.cvUri.error != null) Text(s.cvUri.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        if (mensaje.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(mensaje, color = if (guardado) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = alVolver, modifier = Modifier.weight(1f)) { Text("Cancelar") }
            Button(onClick = { vm.guardar(usuario) }, enabled = !s.cvCargando, modifier = Modifier.weight(1f)) { Text("Guardar candidata") }
        }
        Spacer(Modifier.height(24.dp))
    }
}