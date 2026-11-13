package com.example.appsemestral

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.example.appsemestral.di.AppContainer
import com.example.appsemestral.nativo.NotificacionHelper
import com.example.appsemestral.ui.navigation.WimApp
import com.example.appsemestral.ui.theme.Wim100Theme

class MainActivity : FragmentActivity() {
    lateinit var container: AppContainer
        private set

    /** Android 13+ exige pedir POST_NOTIFICATIONS en tiempo de ejecución; declararlo en el manifest no basta. */
    private val pedirPermisoNotificaciones = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        container = (application as WimApplication).container
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificacionHelper.permisoConcedido(this)) {
            pedirPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent { Wim100Theme { WimApp(container = container, activity = this) } }
    }
}
