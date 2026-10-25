package com.example.appsemestral.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import com.example.appsemestral.domain.model.EstadoCandidata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun CampoValidado(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    error: String? = null,
    icono: androidx.compose.ui.graphics.vector.ImageVector? = null,
    maxCaracteres: Int? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    unaLinea: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevo -> alCambiar(if (maxCaracteres == null) nuevo else nuevo.take(maxCaracteres)) },
        label = { Text(etiqueta) },
        leadingIcon = if (icono != null) ({ Icon(icono, contentDescription = null) }) else null,
        isError = error != null,
        singleLine = unaLinea,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        supportingText = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AnimatedVisibility(
                    visible = error != null,
                    enter = fadeIn(tween(160)) + expandVertically(tween(160)),
                    exit = fadeOut(tween(120)) + shrinkVertically(tween(120))
                ) {
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                if (maxCaracteres != null) Text("${valor.length}/$maxCaracteres", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun Avatar(nombre: String, modifier: Modifier = Modifier) {
    val iniciales = nombre.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
    Box(modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        Text(iniciales.ifBlank { "W" }, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EstadoChip(estado: EstadoCandidata) {
    val colorTarget = when (estado) {
        EstadoCandidata.Seleccionada -> Color(0xFF147D64)
        EstadoCandidata.NoSeleccionada -> Color(0xFF9F3434)
        EstadoCandidata.Evaluada -> Color(0xFF365AB5)
        EstadoCandidata.EnEvaluacion -> Color(0xFF9B6100)
        EstadoCandidata.Postulada -> Color(0xFF695A7C)
    }
    val color by animateColorAsState(colorTarget, animationSpec = tween(250), label = "estado")
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(50)) {
        Text(estado.etiqueta, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@Composable
fun PuntajeCircular(puntaje: Double, modifier: Modifier = Modifier) {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(puntaje) { progreso.animateTo((puntaje / 100.0).toFloat().coerceIn(0f, 1f), animationSpec = tween(700)) }
    Box(modifier.size(58.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(progress = { progreso.value }, strokeWidth = 5.dp, color = MaterialTheme.colorScheme.secondary, trackColor = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxSize())
        Text("${puntaje.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun Modifier.sacudir(disparador: Int): Modifier {
    val desplazamiento = remember { Animatable(0f) }
    LaunchedEffect(disparador) {
        if (disparador > 0) {
            desplazamiento.animateTo(12f, spring(stiffness = 900f)); desplazamiento.animateTo(-12f, spring(stiffness = 900f))
            desplazamiento.animateTo(6f, spring(stiffness = 900f)); desplazamiento.animateTo(0f, spring(stiffness = 900f))
        }
    }
    return this.offset(x = desplazamiento.value.dp)
}

@Composable
fun VisorCvDialog(uriTexto: String, alCerrar: () -> Unit) {
    val context = LocalContext.current
    var bitmap by remember(uriTexto) { mutableStateOf<Bitmap?>(null) }
    var error by remember(uriTexto) { mutableStateOf<String?>(null) }
    var paginaActual by remember(uriTexto) { mutableIntStateOf(0) }
    var totalPaginas by remember(uriTexto) { mutableIntStateOf(0) }
    LaunchedEffect(uriTexto, paginaActual) {
        bitmap = null
        error = null
        val resultado = withContext(Dispatchers.IO) {
            runCatching {
                val uri = Uri.parse(uriTexto)
                val descriptor: ParcelFileDescriptor = if (uri.scheme == "content") {
                    context.contentResolver.openFileDescriptor(uri, "r") ?: throw IllegalStateException("No se pudo abrir el documento.")
                } else {
                    val path = if (uri.scheme == "file") uri.path ?: throw IllegalStateException("Ruta inválida.") else uriTexto
                    ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
                }
                descriptor.use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        val cantidad = renderer.pageCount
                        if (cantidad < 1) throw IllegalStateException("El PDF no tiene páginas.")
                        val indice = paginaActual.coerceIn(0, cantidad - 1)
                        renderer.openPage(indice).use { page ->
                            val b = Bitmap.createBitmap(page.width.coerceAtMost(1800), page.height.coerceAtMost(2400), Bitmap.Config.ARGB_8888)
                            page.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            b to cantidad
                        }
                    }
                }
            }
        }
        resultado.onSuccess { (nuevaBitmap, cantidad) ->
            bitmap = nuevaBitmap
            totalPaginas = cantidad
        }.onFailure { error = it.message ?: "No se pudo mostrar el PDF." }
    }
    Dialog(onDismissRequest = alCerrar) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().fillMaxHeight(0.88f)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Currículum PDF", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    TextButton(onClick = alCerrar) { Text("Cerrar") }
                }
                Text(
                    if (totalPaginas > 0) "Página ${paginaActual + 1} de $totalPaginas" else "Abriendo documento…",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                when {
                    bitmap != null -> Image(bitmap!!.asImageBitmap(), contentDescription = "Página ${paginaActual + 1} del currículum", modifier = Modifier.fillMaxWidth().weight(1f), alignment = Alignment.TopCenter)
                    error != null -> Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                    else -> Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { paginaActual-- }, enabled = paginaActual > 0) { Text("Anterior") }
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(onClick = { paginaActual++ }, enabled = totalPaginas > 0 && paginaActual < totalPaginas - 1) { Text("Siguiente") }
                }
            }
        }
    }
}
