package com.example.appsemestral.ui.navigation

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appsemestral.di.AppContainer
import com.example.appsemestral.domain.auth.CuentaAutorizada
import com.example.appsemestral.nativo.BiometricHelper
import com.example.appsemestral.ui.screens.*
import com.example.appsemestral.viewmodel.CandidatasViewModel
import com.example.appsemestral.viewmodel.SesionViewModel

private data class Destino(val ruta: String, val titulo: String, val icono: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun WimApp(container: AppContainer, activity: FragmentActivity) {
    val sesionVm: SesionViewModel = viewModel(factory = container.viewModelFactory)
    val sesion by sesionVm.sesion.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val destinos = buildList {
        add(Destino("candidatas", "Candidatas", Icons.Default.People))
        if (sesion?.rol?.puedeConsultarResultados == true) {
            add(Destino("resultados", "Ranking", Icons.Default.Assessment))
        }
        if (sesion?.rol?.puedeConsultarHistorial == true) {
            add(Destino("historial", "Historial", Icons.Default.History))
        }
        add(Destino("perfil", "Perfil", Icons.Default.Person))
    }
    LaunchedEffect(sesion?.rol) {
        if (sesion != null) {
            navController.navigate("candidatas") {
                popUpTo("candidatas") { inclusive = false }
                launchSingleTop = true
            }
        }
    }
    if (sesion == null) {
        LoginScreen(sesionVm) { cuenta: CuentaAutorizada ->
            // La cuenta solicitada se prepara en el login; la sesión solo se inicia si Android confirma la biometría/PIN.
            BiometricHelper.autenticar(activity, alExito = {
                sesionVm.iniciarSesionAutorizada(cuenta)
            }, alError = { sesionVm.registrarIntentoFallido(it, cuenta.usuarioId) })
        }
    } else {
        Scaffold(bottomBar = {
            NavigationBar {
                val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route
                destinos.forEach { destino ->
                    NavigationBarItem(
                        selected = rutaActual?.startsWith(destino.ruta) == true,
                        onClick = { navController.navigate(destino.ruta) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        } },
                        icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                        label = { Text(destino.titulo) }
                    )
                }
            }
        }) { padding ->
            NavHost(
                navController = navController,
                startDestination = "candidatas",
                modifier = Modifier.padding(padding),
                enterTransition = { fadeIn(tween(320)) + slideInHorizontally(tween(320)) { it / 12 } },
                exitTransition = { fadeOut(tween(320)) + slideOutHorizontally(tween(320)) { -it / 12 } }
            ) {
                composable("candidatas") {
                    val vm: CandidatasViewModel = viewModel(factory = container.viewModelFactory)
                    CandidatasScreen(vm, sesion!!.rol.puedeRegistrarCandidatas, { navController.navigate("formulario") }, { navController.navigate("detalle/$it") })
                }
                composable("formulario") {
                    if (sesion!!.rol.puedeRegistrarCandidatas) CandidataFormScreen(container, sesion!!.nombre) { navController.popBackStack() }
                    else LaunchedEffect(Unit) { navController.popBackStack() }
                }
                composable("detalle/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { backStack ->
                    val id = backStack.arguments?.getLong("id") ?: -1L
                    val vm: CandidatasViewModel = viewModel(factory = container.viewModelFactory)
                    DetalleCandidataScreen(id, vm, container, sesion!!) { navController.navigate("evaluar/$id") }
                }
                composable("evaluar/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { backStack ->
                    val id = backStack.arguments?.getLong("id") ?: -1L
                    if (sesion!!.rol.puedeEvaluar) EvaluacionScreen(id, container, sesion!!) { navController.popBackStack() }
                    else LaunchedEffect(Unit) { navController.popBackStack() }
                }
                composable("resultados") {
                    if (sesion!!.rol.puedeConsultarResultados) ResultadosScreen(container)
                    else LaunchedEffect(Unit) { navController.popBackStack() }
                }
                composable("historial") {
                    if (sesion!!.rol.puedeConsultarHistorial) HistorialScreen(container)
                    else LaunchedEffect(Unit) { navController.popBackStack() }
                }
                composable("perfil") { PerfilScreen(sesion!!, sesionVm, activity) }
            }
        }
    }
}
