package com.example.appsemestral.domain.validation

import com.example.appsemestral.domain.model.CandidataInput

object CandidataValidator {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val dominiosPermitidos = setOf("gmail.com", "outlook.com", "hotmail.com", "yahoo.com", "proton.me", "dymcyber.cl", "duocuc.cl")
    private val telefonoRegex = Regex("^\\+56 9 \\d{4} \\d{4}$")
    const val TELEFONO_PREFIJO = "+56 9 "
    private val linkedinRegex = Regex("^https?://(www\\.)?linkedin\\.com/in/[A-Za-z0-9_%.-]+/?$")
    const val MAX_CV_BYTES = 5L * 1024L * 1024L

    fun validarNombre(valor: String): String? {
        val limpio = valor.trim()
        return when {
            limpio.length < 3 -> "Ingresa al menos 3 caracteres."
            limpio.length > 60 -> "El nombre admite hasta 60 caracteres."
            !limpio.matches(Regex("^[\\p{L}][\\p{L} .'-]*$")) -> "Usa letras y separadores habituales."
            limpio.split(Regex("\\s+")).count { palabra -> palabra.any { it.isLetter() } } < 2 ->
                "Ingresa tu nombre y al menos un apellido."
            else -> null
        }
    }
    fun validarCorreo(valor: String): String? {
        val correo = valor.trim().lowercase()
        if (!emailRegex.matches(correo)) return "Ingresa un correo válido."
        val dominio = correo.substringAfter('@')
        return if (dominio in dominiosPermitidos) null else "El dominio del correo no está permitido."
    }
    /** Extrae los ocho dígitos locales. Solo elimina el 9 inicial si se pegó el móvil nacional completo de 9 dígitos. */
    fun extraerDigitosTelefonoChile(valor: String): String {
        val digitos = valor.filter(Char::isDigit)
        val numeroLocal = when {
            digitos.startsWith("569") -> digitos.drop(3)
            digitos.startsWith("56") -> digitos.drop(2).removePrefix("9")
            digitos.startsWith("9") && digitos.length == 9 -> digitos.drop(1)
            else -> digitos
        }
        return numeroLocal.take(8)
    }

    /** Mantiene +56 9 y agrega automáticamente el espacio entre grupos de cuatro dígitos. */
    fun formatearTelefonoChile(valor: String): String {
        val numeroLocal = extraerDigitosTelefonoChile(valor)
        return buildString {
            append(TELEFONO_PREFIJO)
            append(numeroLocal.take(4))
            if (numeroLocal.length > 4) {
                append(' ')
                append(numeroLocal.drop(4))
            }
        }
    }

    fun validarTelefono(valor: String): String? = if (telefonoRegex.matches(valor.trim())) null else "Ingresa los 8 dígitos del teléfono después de +56 9."
    fun validarArea(valor: String): String? = if (valor.trim().length in 2..80) null else "Ingresa un área de 2 a 80 caracteres."
    fun validarAnios(valor: Int?): String? = if (valor != null && valor in 0..50) null else "Los años de experiencia deben estar entre 0 y 50."
    fun validarResumen(valor: String): String? = when {
        valor.trim().length < 30 -> "El resumen debe tener al menos 30 caracteres."
        valor.trim().length > 500 -> "El resumen admite hasta 500 caracteres."
        else -> null
    }
    fun validarLinkedin(valor: String): String? = if (linkedinRegex.matches(valor.trim())) null else "Ingresa una URL de LinkedIn del tipo linkedin.com/in/usuario."
    fun validarCv(uri: String, nombre: String, tamanoBytes: Long): String? = when {
        uri.isBlank() -> "Adjunta el currículum en PDF."
        !nombre.trim().lowercase().endsWith(".pdf") -> "El archivo debe ser PDF."
        tamanoBytes <= 0 -> "No se pudo leer el tamaño del archivo. Vuelve a seleccionarlo."
        tamanoBytes > MAX_CV_BYTES -> "El PDF no puede superar los 5 MB."
        else -> null
    }
    fun validar(input: CandidataInput): Map<String, String> {
        val e = linkedMapOf<String, String>()
        validarNombre(input.nombre)?.let { e["nombre"] = it }
        validarCorreo(input.correo)?.let { e["correo"] = it }
        validarTelefono(input.telefono)?.let { e["telefono"] = it }
        validarArea(input.area)?.let { e["area"] = it }
        validarAnios(input.aniosExperiencia)?.let { e["anios"] = it }
        validarResumen(input.resumen)?.let { e["resumen"] = it }
        validarLinkedin(input.linkedin)?.let { e["linkedin"] = it }
        validarCv(input.cvUri, input.cvNombre, input.cvTamanoBytes)?.let { e["cv"] = it }
        return e
    }
}
