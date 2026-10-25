package com.example.appsemestral.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.example.appsemestral.domain.validation.CandidataValidator

/** Campo telefónico con prefijo fijo y cursor estable mientras se insertan los espacios. */
@Composable
fun TelefonoChilenoField(
    valor: String,
    alCambiar: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    var campo by remember {
        mutableStateOf(TextFieldValue(valor, selection = TextRange(valor.length)))
    }

    // Sincroniza cambios externos (por ejemplo, al limpiar el formulario) sin mover
    // el cursor durante una edición normal del propio campo.
    LaunchedEffect(valor) {
        if (campo.text != valor) {
            campo = TextFieldValue(valor, selection = TextRange(valor.length))
        }
    }

    OutlinedTextField(
        value = campo,
        onValueChange = { nuevo ->
            val textoFormateado = CandidataValidator.formatearTelefonoChile(nuevo.text)
            val cursorInicio = mapearCursor(nuevo.text, nuevo.selection.start, textoFormateado)
            val cursorFin = mapearCursor(nuevo.text, nuevo.selection.end, textoFormateado)
            campo = TextFieldValue(
                text = textoFormateado,
                selection = TextRange(cursorInicio, cursorFin)
            )
            alCambiar(textoFormateado)
        },
        label = { Text("Teléfono chileno") },
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
        placeholder = { Text("+56 9 1234 5678") },
        supportingText = {
            if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
        },
        isError = error != null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = modifier.fillMaxWidth()
    )
}

/** Convierte el número de dígitos antes del cursor a una posición en el texto formateado. */
private fun mapearCursor(textoOriginal: String, offset: Int, textoFormateado: String): Int {
    val digitosTotales = textoOriginal.filter(Char::isDigit)
    val digitosPrefijo = when {
        digitosTotales.startsWith("569") -> 3
        digitosTotales.startsWith("56") && digitosTotales.drop(2).startsWith("9") -> 3
        digitosTotales.startsWith("56") -> 2
        digitosTotales.startsWith("9") && digitosTotales.length == 9 -> 1
        else -> 0
    }
    val digitosAntesCursor = textoOriginal.take(offset.coerceIn(0, textoOriginal.length)).count(Char::isDigit)
    val digitosLocales = CandidataValidator.extraerDigitosTelefonoChile(textoOriginal)
    val posicionLocal = (digitosAntesCursor - digitosPrefijo).coerceIn(0, digitosLocales.length)
    val posicion = CandidataValidator.TELEFONO_PREFIJO.length + posicionLocal + if (posicionLocal > 4) 1 else 0
    return posicion.coerceIn(CandidataValidator.TELEFONO_PREFIJO.length, textoFormateado.length)
}
