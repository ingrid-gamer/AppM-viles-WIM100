package com.example.appsemestral.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.appsemestral.nativo.BiometricHelper
import com.example.appsemestral.nativo.NotificacionHelper
import com.example.appsemestral.nativo.ShareHelper
import com.example.appsemestral.viewmodel.SesionActiva
import com.example.appsemestral.viewmodel.SesionViewModel

@Composable
fun PerfilScreen(sesion: SesionActiva, sesionViewModel: SesionViewModel, activity: FragmentActivity) {
    val context = LocalContext.current
    var aviso by remember { mutableStateOf("") }
    val permisoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        aviso = if (concedido) "Permiso de notificaciones concedido." else "Notificaciones no autorizadas. Puedes cambiarlo en Ajustes."
    }
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("Perfil y permisos", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(14.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text(sesion.nombre, style = MaterialTheme.typography.titleLarge)
                Text("Rol activo: ${sesion.rol.etiqueta}", color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(8.dp)); Text("Clave de sesión: ${sesion.clave}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row { Icon(Icons.Default.Security, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Estado de funcionalidades nativas", style = MaterialTheme.typography.titleMedium) }
                Spacer(Modifier.height(10.dp))
                Text("Biometría / bloqueo: ${if (BiometricHelper.puedeAutenticar(activity)) "Disponible" else "Sin configurar o no disponible"}")
                Text("Notificaciones: ${if (NotificacionHelper.permisoConcedido(context)) "Permitidas" else "Sin permiso"}")
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = {
                    if (Build.VERSION.SDK_INT >= 33) permisoLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else aviso = "En esta versión no se necesita permiso de notificaciones en tiempo de ejecución."
                }, modifier = Modifier.fillMaxWidth()) { Text("Solicitar permiso de notificaciones") }
                OutlinedButton(onClick = { ShareHelper.abrirAjustesAplicacion(context) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Settings, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Abrir ajustes de la aplicación")
                }
                OutlinedButton(onClick = { ShareHelper.compartirTexto(context, "Prueba WIM 100", "Prueba de compartir texto desde WIM 100 · ${sesion.nombre}") }, modifier = Modifier.fillMaxWidth()) { Text("Probar compartir texto") }
                if (aviso.isNotBlank()) Text(aviso, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 8.dp))
            }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = { sesionViewModel.cerrarSesion() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
    }
}
