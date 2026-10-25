package com.example.appsemestral.domain.auth

import com.example.appsemestral.domain.model.RolUsuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class AutenticadorLocalTest {
    @Test
    fun `cualquier usuario puede entrar como evaluadora con la clave del perfil`() {
        val cuenta = AutenticadorLocal.autenticar("ingrid", "Wim100Ev", RolUsuario.Evaluadora)
        assertNotNull(cuenta)
        assertEquals("ingrid", cuenta?.usuarioId)
        assertEquals("ingrid", cuenta?.nombre)
        assertEquals(RolUsuario.Evaluadora, cuenta?.rol)
    }

    @Test
    fun `cualquier usuario puede entrar como direccion con la clave del perfil`() {
        val cuenta = AutenticadorLocal.autenticar("Directora de prueba", "Wim100Dir", RolUsuario.Direccion)
        assertNotNull(cuenta)
        assertEquals("directora de prueba", cuenta?.usuarioId)
        assertEquals("Directora de prueba", cuenta?.nombre)
        assertEquals(RolUsuario.Direccion, cuenta?.rol)
    }

    @Test
    fun `la contraseña de evaluadora no permite entrar como direccion`() {
        assertNull(AutenticadorLocal.autenticar("ingrid", "Wim100Ev", RolUsuario.Direccion))
    }

    @Test
    fun `la contraseña de direccion no permite entrar como evaluadora`() {
        assertNull(AutenticadorLocal.autenticar("ingrid", "Wim100Dir", RolUsuario.Evaluadora))
    }

    @Test
    fun `usuario vacio o contraseña incorrecta se rechazan`() {
        assertNull(AutenticadorLocal.autenticar("", "Wim100Ev", RolUsuario.Evaluadora))
        assertNull(AutenticadorLocal.autenticar("   ", "Wim100Dir", RolUsuario.Direccion))
        assertNull(AutenticadorLocal.autenticar("Pepito", "incorrecta", RolUsuario.Evaluadora))
        assertNull(AutenticadorLocal.autenticar("Pepito", "", RolUsuario.Direccion))
    }

    @Test
    fun `acceso biometrico permite cualquier usuario no vacio con el perfil seleccionado`() {
        val cuenta = AutenticadorLocal.prepararAccesoBiometrico("Camila", RolUsuario.Direccion)
        assertNotNull(cuenta)
        assertEquals(RolUsuario.Direccion, cuenta?.rol)
        assertNull(AutenticadorLocal.prepararAccesoBiometrico(" ", RolUsuario.Direccion))
    }

    @Test
    fun `usuario demasiado largo se rechaza`() {
        assertNull(AutenticadorLocal.autenticar("A".repeat(61), "Wim100Ev", RolUsuario.Evaluadora))
        assertNull(AutenticadorLocal.prepararAccesoBiometrico("A".repeat(61), RolUsuario.Evaluadora))
    }
}
