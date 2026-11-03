package com.example.cauca_resilente_app.ui.Navigation

import SolicitarAyudaScreen
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cauca_resilente_app.BienvenidaScreen
import com.example.cauca_resilente_app.api.RetrofitInstance
import com.example.cauca_resilente_app.api.SessionManager
import com.example.cauca_resilente_app.ui.Screens.HomeScreen
import com.example.cauca_resilente_app.ui.Screens.InformacionAlertasScreen
import com.example.cauca_resilente_app.ui.Screens.LoginScreen
import com.example.cauca_resilente_app.ui.Screens.MapaEmergenciaScreen
import com.example.cauca_resilente_app.ui.Screens.MiEstadoScreen
import com.example.cauca_resilente_app.ui.Screens.MiUbicacionScreen
import com.example.cauca_resilente_app.ui.Screens.MisContactosScreen
import com.example.cauca_resilente_app.ui.Screens.PerfilScreen
import com.example.cauca_resilente_app.ui.Screens.RegistroScreen
import com.example.cauca_resilente_app.ui.ViewModels.AlertasViewModel

import com.example.cauca_resilente_app.ui.ViewModels.AuthViewModel
import com.example.cauca_resilente_app.ui.ViewModels.ContactosViewModel
import com.example.cauca_resilente_app.ui.ViewModels.EstadoViewModel
import com.example.cauca_resilente_app.ui.ViewModels.SolicitarAyudaViewModel
import com.example.cauca_resilente_app.ui.ViewModels.UserViewModel
import com.example.cauca_resilente_app.ui.ViewModels.ViewModelFactory


@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val usuario by userViewModel.user.collectAsStateWithLifecycle()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val api = remember { RetrofitInstance.apiService }
    val estadoViewModel: EstadoViewModel = viewModel(factory = ViewModelFactory(api))
    val estadoUiState by estadoViewModel.uiState.collectAsStateWithLifecycle()
    val sesionExpirada by userViewModel.sessionExpired.collectAsStateWithLifecycle()
    val nombreMostrar = SessionManager.getUser()?.nombreCompleto?.takeIf { it.isNotBlank() } ?: "Usuario"
    val navController = rememberNavController()

    val usuarioActual = remember(uiState.isAuthenticated) {
        SessionManager.getUser()
    }

    val startDestination = remember {
        if (SessionManager.isLoggedIn()) Screen.Home.route else Screen.Bienvenida.route
    }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            // Si ya estamos en Home, no navegues (evita rebote)
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != Screen.Home.route) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Bienvenida.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

    }


        NavHost(navController = navController, startDestination = startDestination) {



        composable(Screen.Bienvenida.route) {
            BienvenidaScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Registro.route) }
            )
        }

        // composable para el inicio de sesion
        composable(Screen.Login.route) {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onLoginSuccess = {
                    userViewModel.reload()
                    navController.navigate(Screen.Home.route) {
                        // Evita volver al login con el botón atrás
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }

                },
                onRegisterClick = {navController.navigate(Screen.Registro.route) },
                onForgotPasswordClick = { /* TODO recuperación */ },
                viewModel = authViewModel

            )
        }


        // composable para el registro

            composable(Screen.Registro.route) {

                RegistroScreen(
                    onBackClick = { navController.popBackStack() },
                    onRegistroExitoso = { nombre ->
                        userViewModel.reload()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onYaTengoCuentaClick = {
                        navController.popBackStack()
                    },
                    viewModel = authViewModel
                )
            }




        // composable para la ruta de Home
        composable(Screen.Home.route) {
            val user = SessionManager.getUser()
            HomeScreen(
                user = user,
                estadoActual = estadoUiState.estadoActual,
                onNotificacionesClick = { /* TODO */ },
                onMiEstadoClick = { navController.navigate(Screen.MiEstado.route) },
                onSolicitarAyudaClick = { navController.navigate(Screen.SolicitarAyuda.route)},
                onMiUbicacionClick = { navController.navigate(Screen.MiUbicacion.route) },
                onInformacionClick = { navController.navigate(Screen.Informacion.route)},
                onVerEnMapaClick = { navController.navigate(Screen.Mapa.route) },
                onNavItemSelected = { index ->
                    when (index) {
                        1 -> navController.navigate(Screen.Mapa.route)
                        3 -> navController.navigate(Screen.Alertas.route)
                        4 -> navController.navigate(Screen.Perfil.route)
                    }
                },
                onSosClick = { navController.navigate(Screen.SolicitarAyuda.route)}
            )
        }

        // composable para la ruta de Mi Estado

        composable (Screen.MiEstado.route){
            MiEstadoScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = estadoViewModel

            )
        }

        // composable para la ruta de solicitar ayuda

        composable (Screen.SolicitarAyuda.route){
            val viewModel: SolicitarAyudaViewModel = viewModel(factory = ViewModelFactory(api))
            LaunchedEffect(Unit) {
                viewModel.resetEstado()

            }

            SolicitarAyudaScreen(
                onBackClick = { navController.popBackStack() },
                onSolicitudEnviada = {
                    navController.popBackStack()


                },
                viewModel = viewModel
            )
        }

        // composable para la seccion de informacion

            composable(Screen.Informacion.route) {
                val viewModel: AlertasViewModel = viewModel(factory = ViewModelFactory(api))
                InformacionAlertasScreen(
                    onBackClick = { navController.popBackStack() },
                    onVerMasClick = { /* TODO: detalle */ },
                    viewModel = viewModel,
                    tabInicial = 0
                )
            }


            composable(Screen.Alertas.route) {
                val viewModel: AlertasViewModel = viewModel(factory = ViewModelFactory(api))
                InformacionAlertasScreen(
                    onBackClick = { navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } },
                    onVerMasClick = { /* TODO: detalle */ },
                    viewModel = viewModel,
                    tabInicial = 1
                )
            }

        // composable para el perfil
        composable(Screen.Perfil.route) {
            val user = usuario ?: SessionManager.getUser()
            PerfilScreen(
                user = user,
                onBackClick = { navController.popBackStack() },
                onMisContactosClick = { navController.navigate(Screen.MisContactos.route) },
                onConfiguracionClick = { /* TODO */ },
                onPrivacidadClick = { /* TODO */ },
                onAcercaDeClick = { /* TODO */ },
                onCerrarSesionClick = {
                    userViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // limpia toda la pila
                    }
                },
                onNavItemSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(Screen.Home.route)
                        1 -> navController.navigate(Screen.Mapa.route)
                        3 -> navController.navigate(Screen.Alertas.route)
                    }
                },
                onSosClick = { /* TODO */ }
            )
        }

        // composable para los contactos de emergencia
        composable(Screen.MisContactos.route) {
            val contactosViewModel: ContactosViewModel = viewModel(factory = ViewModelFactory(api))
            MisContactosScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = contactosViewModel
            )
        }


        // composobale para Mi ubicacion
        composable(Screen.MiUbicacion.route) {
            val ubicacionViewModel: com.example.cauca_resilente_app.ui.ViewModels.UbicacionViewModel =
                viewModel(factory = ViewModelFactory(api))

            MiUbicacionScreen(
                onBackClick = { navController.popBackStack() },
                onVerEnMapaClick = { navController.navigate(Screen.Mapa.route) },
                onVerHistorialCompletoClick = { /* TODO: pantalla de historial completo */ },
                viewModel = ubicacionViewModel
            )
        }

        // composable para el Mapa de emergencia
        composable(Screen.Mapa.route) {
            val mapaViewModel: com.example.cauca_resilente_app.ui.ViewModels.MapaViewModel =
                viewModel(factory = ViewModelFactory(api))

            MapaEmergenciaScreen(
                onMenuClick = { /* TODO: abrir drawer */ },
                onNavItemSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(Screen.Home.route)
                        3 -> navController.navigate(Screen.Alertas.route)
                        4 -> navController.navigate(Screen.Perfil.route)
                    }
                },
                onSosClick = { navController.navigate(Screen.SolicitarAyuda.route) },
                viewModel = mapaViewModel
            )
        }
    }
}