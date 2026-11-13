package com.example.appsemestral.ui.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appsemestral.domain.auth.CuentaAutorizada
import com.example.appsemestral.domain.model.RolUsuario
import com.example.appsemestral.viewmodel.SesionViewModel

@Composable
fun LoginScreen(
    sesionViewModel: SesionViewModel,
    alUsarBiometria: (CuentaAutorizada) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.Evaluadora) }
    var mostrarContrasena by remember { mutableStateOf(false) }
    var errorUsuario by remember { mutableStateOf<String?>(null) }
    var errorContrasena by remember { mutableStateOf<String?>(null) }
    val mensaje by sesionViewModel.mensaje.collectAsStateWithLifecycle()
    val intentos by sesionViewModel.intentosFallidos.collectAsStateWithLifecycle()

    val animacion = rememberInfiniteTransition(label = "pulsoEscudo")
    val escala by animacion.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "escala"
    )

    fun validarCredenciales(): CuentaAutorizada? {
        errorUsuario = if (usuario.isBlank()) "Ingresa tu nombre de usuario." else null
        errorContrasena = if (contrasena.isBlank()) "Ingresa la contraseña del perfil seleccionado." else null
        if (usuario.isBlank() || contrasena.isBlank()) {
            // El ViewModel registra el intento y conserva la validación fuera de la UI.
            sesionViewModel.autenticarCredenciales(usuario, contrasena, rolSeleccionado)
            return null
        }

        val cuenta = sesionViewModel.autenticarCredenciales(usuario, contrasena, rolSeleccionado)
        if (cuenta == null) {
            errorContrasena = "Contraseña incorrecta para ${rolSeleccionado.etiqueta}."
        } else {
            errorUsuario = null
            errorContrasena = null
        }
        return cuenta
    }

    Box(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp).heightIn(max = 760.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = com.example.appsemestral.R.drawable.wim100_logo),
                    contentDescription = "Logotipo WIM 100",
                    modifier = Modifier.size(104.dp).scale(escala)
                )
                Spacer(Modifier.height(10.dp))
                Text("WIM 100", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text("DYM Cyber · Reconocimiento de talento", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text("Ingresa tu usuario y selecciona tu perfil", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(16.dp))

                Text("Perfil de acceso", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = rolSeleccionado == RolUsuario.Evaluadora,
                        onClick = {
                            rolSeleccionado = RolUsuario.Evaluadora
                            contrasena = ""
                            errorContrasena = null
                            sesionViewModel.mostrarMensaje("")
                        },
                        label = { Text("Evaluadora") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = rolSeleccionado == RolUsuario.Direccion,
                        onClick = {
                            rolSeleccionado = RolUsuario.Direccion
                            contrasena = ""
                            errorContrasena = null
                            sesionViewModel.mostrarMensaje("")
                        },
                        label = { Text("Dirección") },
                        leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    if (rolSeleccionado == RolUsuario.Evaluadora)
                        "Este perfil permite registrar candidatas y realizar evaluaciones."
                    else "Este perfil permite consultar el ranking y resolver selecciones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = usuario,
                    onValueChange = {
                        usuario = it.take(60)
                        if (usuario.isNotBlank()) errorUsuario = null
                        sesionViewModel.mostrarMensaje("")
                    },
                    label = { Text("Nombre de usuario") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = errorUsuario != null,
                    supportingText = { errorUsuario?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = {
                        contrasena = it.take(100)
                        if (contrasena.isNotBlank()) errorContrasena = null
                        sesionViewModel.mostrarMensaje("")
                    },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                            Icon(
                                if (mostrarContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (mostrarContrasena) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = errorContrasena != null,
                    supportingText = { errorContrasena?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { validarCredenciales()?.let(sesionViewModel::iniciarSesionAutorizada) },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ingresar con contraseña")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        errorContrasena = null
                        errorUsuario = if (usuario.isBlank()) "Ingresa tu nombre de usuario para continuar." else null
                        if (usuario.isBlank()) {
                            sesionViewModel.prepararCuentaBiometrica(usuario, rolSeleccionado)
                        } else {
                            sesionViewModel.prepararCuentaBiometrica(usuario, rolSeleccionado)?.let(alUsarBiometria)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ingresar con biometría/PIN")
                }
                Text(
                    "Con biometría/PIN solo necesitas escribir tu usuario. Debes tener un método de bloqueo configurado en el dispositivo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                )

                if (mensaje.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (intentos > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text("Intentos fallidos registrados: $intentos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))
                Text(
                    "La contraseña depende del perfil seleccionado. Puedes usar cualquier nombre de usuario no vacío.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
