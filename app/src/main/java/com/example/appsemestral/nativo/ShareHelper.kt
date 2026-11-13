package com.example.appsemestral.nativo

import android.content.Context
import android.content.Intent
import android.net.Uri

object ShareHelper {
    fun compartirTexto(context: Context, titulo: String, texto: String) {
        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_SUBJECT, titulo); putExtra(Intent.EXTRA_TEXT, texto) }
        context.startActivity(Intent.createChooser(intent, "Compartir $titulo").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    fun compartirPdf(context: Context, uri: Uri, titulo: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir $titulo").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    fun abrirUrl(context: Context, url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
    fun abrirAjustesAplicacion(context: Context) {
        context.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
