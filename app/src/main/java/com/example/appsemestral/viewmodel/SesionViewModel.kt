package com.example.appsemestral.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.data.repository.HistorialRepository
import com.example.appsemestral.domain.auth.AutenticadorLocal
import com.example.appsemestral.domain.auth.CuentaAutorizada
import com.example.appsemestral.domain.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SesionActiva(
    val nombre: String,
    val rol: RolUsuario,
    val usuarioId: String
) {
    val clave: String get() = usuarioId
}

class SesionViewModel(private val historial: HistorialRepository) : ViewModel() {
    private val _sesion = MutableStateFlow<SesionActiva?>(null)
    val sesion: StateFlow<SesionActiva?> = _sesion

    private val _intentosFallidos = MutableStateFlow(0)
    val intentosFallidos: StateFlow<Int> = _intentosFallidos

    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje

    /** Cualquier nombre no vacío puede identificarse; la contraseña determina el perfil elegido. */
    fun autenticarCredenciales(usuario: String, contrasena: String, rol: RolUsuario): CuentaAutorizada? {
        if (usuario.isBlank() || contrasena.isBlank()) {
            registrarIntentoFallido(
                "Acceso rechazado: completa el usuario y la contraseña.",
                usuario.trim().ifBlank { "Sin usuario" }
            )
            _mensaje.value = "Completa el usuario y la contraseña para continuar."
            return null
        }

        val cuenta = AutenticadorLocal.autenticar(usuario, contrasena, rol)
        if (cuenta == null) {
            registrarIntentoFallido("Contraseña incorrecta para el perfil seleccionado.", usuario.trim())
            _mensaje.value = "La contraseña no corresponde al perfil ${rol.etiqueta}."
            return null
        }
        _mensaje.value = ""
        return cuenta
    }

    /** Prepara la identidad solicitada; la sesión solo se crea después del éxito de BiometricPrompt. */
    fun prepararCuentaBiometrica(usuario: String, rol: RolUsuario): CuentaAutorizada? {
        if (usuario.isBlank()) {
            registrarIntentoFallido("Acceso biométrico rechazado: falta el nombre de usuario.", "Sin usuario")
            _mensaje.value = "Ingresa tu nombre de usuario antes de usar biometría/PIN."
            return null
        }
        val cuenta = AutenticadorLocal.prepararAccesoBiometrico(usuario, rol)
        if (cuenta == null) {
            _mensaje.value = "El nombre de usuario no puede superar los 60 caracteres."
            return null
        }
        _mensaje.value = ""
        return cuenta
    }

    fun iniciarSesionAutorizada(cuenta: CuentaAutorizada) {
        val activa = SesionActiva(
            nombre = cuenta.nombre,
            rol = cuenta.rol,
            usuarioId = cuenta.usuarioId
        )
        _sesion.value = activa
        _mensaje.value = "Bienvenida/o, ${activa.nombre}. Perfil: ${activa.rol.etiqueta}."
        _intentosFallidos.value = 0
        viewModelScope.launch {
            historial.registrar(
                activa.nombre,
                "LOGIN",
                "Inicio de sesión del usuario '${activa.usuarioId}' con perfil ${activa.rol.etiqueta}."
            )
        }
    }

    fun mostrarMensaje(texto: String) {
        _mensaje.value = texto
    }

    fun registrarIntentoFallido(texto: String, usuarioIntentado: String = "Acceso") {
        _intentosFallidos.value += 1
        _mensaje.value = texto
        val intento = _intentosFallidos.value
        viewModelScope.launch {
            historial.registrar(
                usuarioIntentado.ifBlank { "Sin usuario" },
                "LOGIN_FALLIDO",
                "Intento $intento: $texto"
            )
        }
    }

    fun cerrarSesion() {
        val actual = _sesion.value ?: return
        viewModelScope.launch {
            historial.registrar(
                actual.nombre,
                "LOGOUT",
                "Cierre de sesión del usuario '${actual.usuarioId}' con perfil ${actual.rol.etiqueta}."
            )
        }
        _sesion.value = null
        _mensaje.value = ""
    }
}
