package com.example.appsemestral.domain.auth

import com.example.appsemestral.domain.model.RolUsuario
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

/**
 * Autenticación local para demostración académica.
 * Se permite cualquier nombre de usuario no vacío, pero la contraseña debe
 * coincidir con el perfil seleccionado. En producción, la autorización debe
 * verificarse en un servidor y no mediante secretos incluidos en el APK.
 */
data class CuentaAutorizada(
    val usuarioId: String,
    val nombre: String,
    val rol: RolUsuario
)

object AutenticadorLocal {
    private const val HASH_EVALUADORA = "c7ab47e2d6aad4b1ae8400f9de0a4c7053c2714a59cbe6735b8bab3603f156d2"
    private const val HASH_DIRECCION = "9d628254510056a3a856e58da9c32aa93abc296d8ecc2c83ae163e74a6ec7b31"

    /** Acepta cualquier nombre; el rol se obtiene del selector y lo valida su contraseña. */
    fun autenticar(usuario: String, contrasena: String, rol: RolUsuario): CuentaAutorizada? {
        val nombre = usuario.trim()
        if (nombre.isBlank() || nombre.length > 60 || contrasena.isBlank()) return null

        val hashEsperado = when (rol) {
            RolUsuario.Evaluadora -> HASH_EVALUADORA
            RolUsuario.Direccion -> HASH_DIRECCION
        }
        val hashRecibido = sha256(contrasena)
        val coincide = MessageDigest.isEqual(
            hashEsperado.toByteArray(StandardCharsets.US_ASCII),
            hashRecibido.toByteArray(StandardCharsets.US_ASCII)
        )
        if (!coincide) return null
        return crearCuenta(nombre, rol)
    }

    /** Se usa únicamente después de que BiometricPrompt confirme el bloqueo del dispositivo. */
    fun prepararAccesoBiometrico(usuario: String, rol: RolUsuario): CuentaAutorizada? {
        val nombre = usuario.trim()
        if (nombre.isBlank() || nombre.length > 60) return null
        return crearCuenta(nombre, rol)
    }

    private fun crearCuenta(nombre: String, rol: RolUsuario): CuentaAutorizada = CuentaAutorizada(
        usuarioId = nombre.lowercase(Locale.ROOT),
        nombre = nombre,
        rol = rol
    )

    private fun sha256(valor: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(valor.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
