package com.example.appsemestral.nativo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificacionHelper {
    private const val CHANNEL_ID = "wim100_evaluaciones"
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Evaluaciones WIM 100", NotificationManager.IMPORTANCE_DEFAULT).apply { description = "Avisos cuando se guarda una evaluación" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    fun permisoConcedido(context: Context): Boolean = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    fun mostrarEvaluacion(context: Context, texto: String) {
        crearCanal(context)
        if (!permisoConcedido(context)) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("WIM 100 · Evaluación registrada")
            .setContentText(texto.take(100)).setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true).build()
        runCatching { NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification) }
    }
}
