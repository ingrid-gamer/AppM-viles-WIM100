package com.example.appsemestral.nativo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/** Datos del PDF que ya quedó copiado al almacenamiento privado de la app. */
data class PdfImportado(
    val uri: Uri,
    val nombre: String,
    val tamanoBytes: Long
)

object ArchivoHelper {
    private const val MAX_PDF_BYTES = 5L * 1024L * 1024L

    fun carpetaCvs(context: Context): File = File(context.filesDir, "cvs").apply { mkdirs() }

    fun generarCvPrueba(context: Context, nombre: String, correo: String, area: String): Uri {
        val archivo = File(carpetaCvs(context), "cv_${nombre.lowercase().replace(Regex("[^a-z0-9]"), "_")}.pdf")
        if (!archivo.exists()) {
            val documento = PdfDocument()
            try {
                val pagina = documento.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
                val canvas: Canvas = pagina.canvas
                val paint = Paint().apply { textSize = 24f; isFakeBoldText = true }
                canvas.drawText("WIM 100 · Currículum de prueba", 42f, 70f, paint)
                paint.textSize = 18f; paint.isFakeBoldText = false
                canvas.drawText("Nombre: $nombre", 42f, 125f, paint)
                canvas.drawText("Correo: $correo", 42f, 165f, paint)
                canvas.drawText("Área profesional: $area", 42f, 205f, paint)
                canvas.drawText("Documento ficticio para demostración académica.", 42f, 270f, paint)
                canvas.drawText("No corresponde a antecedentes de una persona real.", 42f, 305f, paint)
                documento.finishPage(pagina)
                FileOutputStream(archivo).use { documento.writeTo(it) }
            } finally {
                documento.close()
            }
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    /**
     * Importa el archivo seleccionado al almacenamiento privado de la app.
     * Esto evita depender de permisos temporales del proveedor de archivos del teléfono
     * cuando Dirección abra el CV más adelante.
     */
    fun importarPdf(context: Context, uriOrigen: Uri): PdfImportado {
        val (nombreProveedor, tamanoInformado) = nombreYTamano(context, uriOrigen)
        if (tamanoInformado > MAX_PDF_BYTES) {
            throw IOException("El PDF supera el límite de 5 MB.")
        }

        val carpeta = carpetaCvs(context)
        val temporal = File.createTempFile("cv_import_", ".tmp", carpeta)
        try {
            var total = 0L
            val entrada = context.contentResolver.openInputStream(uriOrigen)
                ?: throw IOException("No se pudo leer el archivo seleccionado. Intenta con otra ubicación.")
            entrada.use { input ->
                FileOutputStream(temporal).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    while (true) {
                        val cantidad = input.read(buffer)
                        if (cantidad < 0) break
                        total += cantidad
                        if (total > MAX_PDF_BYTES) {
                            throw IOException("El PDF supera el límite de 5 MB.")
                        }
                        output.write(buffer, 0, cantidad)
                    }
                    output.flush()
                }
            }

            if (total < 8L || !tieneFirmaPdf(temporal)) {
                throw IOException("El archivo seleccionado no parece ser un PDF válido.")
            }

            val nombreOriginal = nombreProveedor.substringBeforeLast('.', nombreProveedor).ifBlank { "curriculum" }
            val nombreSeguro = nombreOriginal
                .replace(Regex("[^A-Za-z0-9_-]"), "_")
                .trim('_')
                .take(45)
                .ifBlank { "curriculum" }
            val destino = File(carpeta, "cv_${UUID.randomUUID().toString().take(8)}_${nombreSeguro}.pdf")
            if (!temporal.renameTo(destino)) {
                temporal.copyTo(destino, overwrite = false)
                temporal.delete()
            }

            val uriPrivada = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", destino)
            return PdfImportado(uri = uriPrivada, nombre = "$nombreSeguro.pdf", tamanoBytes = total)
        } catch (error: Exception) {
            temporal.delete()
            throw error
        }
    }

    private fun tieneFirmaPdf(archivo: File): Boolean {
        val cabecera = ByteArray(1024)
        val leidos = archivo.inputStream().use { it.read(cabecera) }
        if (leidos <= 0) return false
        val texto = String(cabecera, 0, leidos, Charsets.ISO_8859_1)
        return texto.contains("%PDF-")
    }

    fun nombreYTamano(context: Context, uri: Uri): Pair<String, Long> {
        var nombre = "curriculum.pdf"
        var tamano = 0L
        runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nombreIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val tamanoIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nombreIndex >= 0) nombre = cursor.getString(nombreIndex) ?: nombre
                    if (tamanoIndex >= 0 && !cursor.isNull(tamanoIndex)) tamano = cursor.getLong(tamanoIndex)
                }
            }
            if (tamano <= 0L) {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                    if (descriptor.length >= 0) tamano = descriptor.length
                }
            }
        }
        return nombre to tamano
    }

    fun uriParaCompartir(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}
