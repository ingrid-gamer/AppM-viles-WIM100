package com.example.appsemestral

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.appsemestral.di.AppContainer
import com.example.appsemestral.ui.navigation.WimApp
import com.example.appsemestral.ui.theme.Wim100Theme

class MainActivity : FragmentActivity() {
    lateinit var container: AppContainer
        private set
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        container = (application as WimApplication).container
        setContent { Wim100Theme { WimApp(container = container, activity = this) } }
    }
}
