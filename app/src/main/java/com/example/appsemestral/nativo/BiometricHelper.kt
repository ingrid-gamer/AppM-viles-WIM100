package com.example.appsemestral.nativo

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {
    fun puedeAutenticar(activity: FragmentActivity): Boolean {
        val authenticator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else BiometricManager.Authenticators.BIOMETRIC_WEAK
        return BiometricManager.from(activity).canAuthenticate(authenticator) == BiometricManager.BIOMETRIC_SUCCESS
    }
    fun autenticar(activity: FragmentActivity, alExito: () -> Unit, alError: (String) -> Unit) {
        val manager = BiometricManager.from(activity)
        if (!puedeAutenticar(activity)) {
            val estado = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            alError(when (estado) {
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Configura una huella o rostro en los ajustes del dispositivo."
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Este dispositivo no tiene biometría compatible."
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "La biometría está temporalmente ocupada."
                else -> "Configura un bloqueo de pantalla para usar esta función."
            }); return
        }
        val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { alExito() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { alError(errString.toString()) }
            override fun onAuthenticationFailed() { alError("No se reconoció la biometría. Inténtalo otra vez.") }
        })
        val builder = BiometricPrompt.PromptInfo.Builder().setTitle("Acceso WIM 100").setSubtitle("Confirma tu identidad")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        else builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK).setNegativeButtonText("Cancelar")
        prompt.authenticate(builder.build())
    }
}
