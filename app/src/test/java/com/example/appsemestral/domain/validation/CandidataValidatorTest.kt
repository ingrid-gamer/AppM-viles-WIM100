package com.example.appsemestral.domain.validation

import com.example.appsemestral.domain.model.CandidataInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CandidataValidatorTest {
    private fun candidata(
        nombre: String = "Camila Rojas", correo: String = "camila@gmail.com", telefono: String = "+56 9 1234 5678",
        anios: Int? = 5, resumen: String = "Profesional de tecnología con experiencia en proyectos de software.",
        linkedin: String = "https://www.linkedin.com/in/camila-rojas", cvNombre: String = "cv.pdf", cvTamano: Long = 1024
    ) = CandidataInput(nombre, correo, telefono, "Desarrollo", anios, resumen, linkedin, "content://demo/cv", cvNombre, cvTamano)

    @Test fun candidataValidaNoTieneErrores() { assertTrue(CandidataValidator.validar(candidata()).isEmpty()) }
    @Test fun nombreDebeIncluirNombreYApellido() {
        assertNotNull(CandidataValidator.validarNombre("Pepito"))
        assertNotNull(CandidataValidator.validarNombre("Pepito -"))
        assertNull(CandidataValidator.validarNombre("Pepito Araya"))
        assertNull(CandidataValidator.validarNombre("María José Araya"))
        assertNotNull(CandidataValidator.validarNombre("Al"))
    }
    @Test fun nombreMayorA60CaracteresFalla() { assertNotNull(CandidataValidator.validarNombre("A".repeat(61))) }
    @Test fun correoConFormatoIncorrectoFalla() { assertNotNull(CandidataValidator.validarCorreo("camila@@gmail")) }
    @Test fun dominioNoPermitidoFalla() { assertNotNull(CandidataValidator.validarCorreo("persona@servidor.invalid")) }
    @Test fun telefonoSeFormateaAutomaticamenteYValidaSoloCompleto() {
        assertEquals("+56 9 1234 5678", CandidataValidator.formatearTelefonoChile("+56 9 12345678"))
        assertEquals("+56 9 1234 5678", CandidataValidator.formatearTelefonoChile("912345678"))
        assertEquals("+56 9 9123 4567", CandidataValidator.formatearTelefonoChile("91234567"))
        assertEquals("+56 9 1234 5678", CandidataValidator.formatearTelefonoChile("123456789012"))
        assertEquals(CandidataValidator.TELEFONO_PREFIJO, CandidataValidator.formatearTelefonoChile(""))
        assertEquals(8, CandidataValidator.extraerDigitosTelefonoChile("+56 9 123456789").length)
        assertNull(CandidataValidator.validarTelefono("+56 9 1234 5678"))
        assertNotNull(CandidataValidator.validarTelefono("+56 9 1234"))
    }
    @Test fun experienciaAceptaLimitesYRechazaMayorA50() { assertNull(CandidataValidator.validarAnios(0)); assertNull(CandidataValidator.validarAnios(50)); assertNotNull(CandidataValidator.validar(candidata(anios = 51))["anios"]) }
    @Test fun resumenMuyCortoFalla() { assertNotNull(CandidataValidator.validarResumen("Resumen breve")) }
    @Test fun resumenSobre500CaracteresFalla() { assertNotNull(CandidataValidator.validarResumen("x".repeat(501))) }
    @Test fun linkedinSoloAceptaEnlacesDePerfil() {
        assertNotNull(CandidataValidator.validarLinkedin("https://linkedin.com/company/ejemplo"))
        assertNull(CandidataValidator.validarLinkedin("https://www.linkedin.com/in/tu-perfil/"))
        assertNull(CandidataValidator.validarLinkedin("https://linkedin.com/in/ingrid-araya"))
        assertNull(CandidataValidator.validarLinkedin("https://www.linkedin.com/in/ingrid-araya-12345678/"))
    }
    @Test fun archivoQueNoEsPdfFalla() { assertNotNull(CandidataValidator.validar(candidata(cvNombre = "curriculum.docx"))["cv"]) }
    @Test fun pdfDe5MbSeAceptaPeroSobreElLimiteFalla() { assertTrue(CandidataValidator.validar(candidata(cvTamano = CandidataValidator.MAX_CV_BYTES)).isEmpty()); assertNotNull(CandidataValidator.validar(candidata(cvTamano = CandidataValidator.MAX_CV_BYTES + 1))["cv"]) }
}
