package com.example.cauca_resilente_app.ui.Navigation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cauca_resilente_app.location.obtenerUbicacionActual
import com.example.cauca_resilente_app.location.tienePermisoUbicacion
import com.example.cauca_resilente_app.ui.Screens.SolicitarAyudaScreen
import com.example.cauca_resilente_app.BienvenidaScreen
import com.example.cauca_resilente_app.api.AlertItem
import com.example.cauca_resilente_app.api.RetrofitInstance
import com.example.cauca_resilente_app.api.SessionManager
import com.example.cauca_resilente_app.ui.Screens.HomeScreen
import com.example.cauca_resilente_app.ui.Screens.InformacionAlertasScreen
import com.example.cauca_resilente_app.ui.Screens.DetalleAlertaScreen
import com.example.cauca_resilente_app.ui.Screens.LoginScreen
import com.example.cauca_resilente_app.ui.Screens.MapaEmergenciaScreen
import com.example.cauca_resilente_app.ui.Screens.MiEstadoScreen
import com.example.cauca_resilente_app.ui.Screens.MiUbicacionScreen
import com.example.cauca_resilente_app.ui.Screens.MisContactosScreen
import com.example.cauca_resilente_app.ui.Screens.PerfilScreen
import com.example.cauca_resilente_app.ui.Screens.EditarPerfilScreen
import com.example.cauca_resilente_app.ui.Screens.EstadoUsuario
import com.example.cauca_resilente_app.ui.Screens.RegistroScreen
import com.example.cauca_resilente_app.ui.Screens.RegistroSocorristaScreen
import com.example.cauca_resilente_app.ui.Screens.SolicitudesAsignadasScreen
import com.example.cauca_resilente_app.ui.components.NubeArrastrable
import com.example.cauca_resilente_app.ui.components.SolicitudSocorristaIsla
import com.example.cauca_resilente_app.ui.components.SolicitudSosIsla
import com.example.cauca_resilente_app.ui.components.SosResultadoDialog
import com.example.cauca_resilente_app.ui.ViewModels.AlertasViewModel

import com.example.cauca_resilente_app.ui.ViewModels.AuthViewModel
import com.example.cauca_resilente_app.ui.ViewModels.ContactosViewModel
import com.example.cauca_resilente_app.ui.ViewModels.EstadoSosIsla
import com.example.cauca_resilente_app.ui.ViewModels.EstadoViewModel
import com.example.cauca_resilente_app.ui.ViewModels.MapaViewModel
import com.example.cauca_resilente_app.ui.ViewModels.NotificacionSocorristaViewModel
import com.example.cauca_resilente_app.ui.ViewModels.SolicitarAyudaViewModel
import com.example.cauca_resilente_app.ui.ViewModels.SolicitudesAsignadasViewModel
import com.example.cauca_resilente_app.ui.ViewModels.UbicacionViewModel
import com.example.cauca_resilente_app.ui.ViewModels.UserViewModel
import com.example.cauca_resilente_app.ui.ViewModels.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val usuario by userViewModel.user.collectAsStateWithLifecycle()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val api = remember { RetrofitInstance.apiService }
    val mapaViewModel: MapaViewModel = viewModel(factory = ViewModelFactory(api))
    val estadoViewModel: EstadoViewModel = viewModel(factory = ViewModelFactory(api))
    val estadoUiState by estadoViewModel.uiState.collectAsStateWithLifecycle()
    val contactosViewModel: ContactosViewModel = viewModel(factory = ViewModelFactory(api))
    val ubicacionViewModel: UbicacionViewModel = viewModel(factory = ViewModelFactory(api))
    val contactosUiState by contactosViewModel.uiState.collectAsStateWithLifecycle()
    val sesionExpirada by userViewModel.sessionExpired.collectAsStateWithLifecycle()
    val nombreMostrar = SessionManager.getUser()?.nombreCompleto?.takeIf { it.isNotBlank() } ?: "Usuario"
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route
    val rutasPublicas = remember {
        setOf(
            Screen.Bienvenida.route,
            Screen.Login.route,
            Screen.Registro.route,
            Screen.RegistroSocorrista.route
        )
    }
    val mostrarNubes = rutaActual !in rutasPublicas
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val solicitarAyudaViewModel: SolicitarAyudaViewModel = viewModel(factory = ViewModelFactory(api))
    val solicitarAyudaUiState by solicitarAyudaViewModel.uiState.collectAsStateWithLifecycle()

    val notificacionSocorristaViewModel: NotificacionSocorristaViewModel =
        viewModel(factory = ViewModelFactory(api))
    val notificacionSocorristaUiState by
        notificacionSocorristaViewModel.uiState.collectAsStateWithLifecycle()

    val esSocorrista = usuario?.esSocorrista == true

    val startDestination = remember {
        if (SessionManager.isLoggedIn()) Screen.Home.route else Screen.Bienvenida.route
    }

    val enviarSosConUbicacion: () -> Unit = {
        scope.launch {
            val ubicacion = obtenerUbicacionActual(context)
            if (ubicacion != null) {
                solicitarAyudaViewModel.enviarSosRapido(
                    ubicacion.latitude,
                    ubicacion.longitude,
                    ubicacion.accuracy.toDouble().takeIf { ubicacion.hasAccuracy() }
                )
            } else {
                solicitarAyudaViewModel.notificarSos(
                    "Activa el GPS para enviar tu ubicación con el SOS"
                )
            }
        }
    }

    val permisoUbicacionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos.values.any { it } || tienePermisoUbicacion(context)) {
            enviarSosConUbicacion()
        } else {
            solicitarAyudaViewModel.notificarSos(
                "Se necesita el permiso de ubicación para enviar el SOS"
            )
        }
    }

    val onSosClick: () -> Unit = {
        estadoViewModel.cambiarEstado(EstadoUsuario.ATRAPADO)
        if (tienePermisoUbicacion(context)) {
            enviarSosConUbicacion()
        } else {
            permisoUbicacionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val onVerDetalleAlerta: (AlertItem) -> Unit = { alerta ->
        val id = alerta.id
        if (!id.isNullOrBlank()) {
            navController.navigate(Screen.DetalleAlerta.crearRuta(id))
        }
    }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            userViewModel.reload()
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

    LaunchedEffect(uiState.isAuthenticated, esSocorrista) {
        if (SessionManager.isLoggedIn() && esSocorrista) {
            notificacionSocorristaViewModel.iniciar()
        } else {
            notificacionSocorristaViewModel.detener()
        }
    }

    LaunchedEffect(notificacionSocorristaUiState.asignada) {
        val estado = notificacionSocorristaUiState
        val solicitud = estado.solicitud
        if (estado.asignada && solicitud != null) {
            val latitud = solicitud.latitudReal
            val longitud = solicitud.longitudReal
            if (latitud != null && longitud != null) {
                mapaViewModel.enfocarSolicitud(
                    latitud = latitud,
                    longitud = longitud,
                    titulo = "Solicitud asignada · ${solicitud.tipoAyuda ?: "Ayuda"}",
                    descripcion = listOfNotNull(
                        solicitud.usuario?.nombreCompleto,
                        solicitud.ubicacion?.direccionAproximada,
                        solicitud.nivelPrioridad?.let { "Prioridad $it" }
                    ).joinToString(" • "),
                    foto = solicitud.usuario?.foto,
                    iniciales = solicitud.usuario?.nombreCompleto
                        ?.trim()
                        ?.split(" ")
                        ?.filter { it.isNotBlank() }
                        ?.take(2)
                        ?.joinToString("") { it.first().uppercase() }
                )
                navController.navigate(Screen.Mapa.route) {
                    launchSingleTop = true
                }
                notificacionSocorristaViewModel.descartarActual()
            }
        }
    }

    // Mientras una solicitud SOS está activa, compartimos la ubicación del
    // usuario cada pocos segundos para que el socorrista la vea actualizada.
    LaunchedEffect(solicitarAyudaUiState.sosSolicitudId) {
        if (solicitarAyudaUiState.sosSolicitudId == null) return@LaunchedEffect

        while (true) {
            delay(15_000)
            if (solicitarAyudaViewModel.uiState.value.estadoSosIsla == EstadoSosIsla.FINALIZADO) {
                break
            }
            val ubicacion = obtenerUbicacionActual(context) ?: continue
            solicitarAyudaViewModel.actualizarUbicacionSos(
                ubicacion.latitude,
                ubicacion.longitude,
                ubicacion.accuracy.toDouble().takeIf { ubicacion.hasAccuracy() }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                onRegistroSocorristaClick = { navController.navigate(Screen.RegistroSocorrista.route) },
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
                    onRegistroSocorristaClick = {
                        navController.navigate(Screen.RegistroSocorrista.route) {
                            popUpTo(Screen.Registro.route) { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }

        // composable para el registro de socorristas
        composable(Screen.RegistroSocorrista.route) {
            RegistroSocorristaScreen(
                onBackClick = { navController.popBackStack() },
                onYaTengoCuentaClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.RegistroSocorrista.route) { inclusive = true }
                    }
                },
                onRegistroUsuarioClick = {
                    navController.navigate(Screen.Registro.route) {
                        popUpTo(Screen.RegistroSocorrista.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }


        // composable para la ruta de Home
        composable(Screen.Home.route) {
            val user = usuario ?: SessionManager.getUser()
            val socorrista = user?.esSocorrista == true

            LaunchedEffect(Unit) {
                contactosViewModel.cargarContactos()
            }

            HomeScreen(
                user = user,
                estadoActual = estadoUiState.estadoActual,
                ubicacionViewModel = ubicacionViewModel,
                contactos = contactosUiState.contactos,
                esSocorrista = socorrista,
                onSolicitudesAsignadasClick = {
                    navController.navigate(Screen.SolicitudesAsignadas.route)
                },
                onNotificacionesClick = { navController.navigate(Screen.Alertas.route) },
                onMiEstadoClick = { navController.navigate(Screen.MiEstado.route) },
                onSolicitarAyudaClick = { navController.navigate(Screen.SolicitarAyuda.route) },
                onMiUbicacionClick = { navController.navigate(Screen.MiUbicacion.route) },
                onInformacionClick = { navController.navigate(Screen.Informacion.route) },
                onVerEnMapaClick = { navController.navigate(Screen.Mapa.route) },
                onCirculoProtectorClick = { navController.navigate(Screen.MisContactos.route) },
                onNavItemSelected = { index ->
                    when (index) {
                        1 -> navController.navigate(Screen.Mapa.route)
                        2 -> navController.navigate(Screen.MisContactos.route)
                        4 -> navController.navigate(Screen.Perfil.route)
                    }
                },
                onSosClick = onSosClick
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
                viewModel = viewModel,
                onNavItemSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(Screen.Home.route)
                        2 -> navController.navigate(Screen.MisContactos.route)
                        4 -> navController.navigate(Screen.Perfil.route)
                    }
                },
                onSosClick = onSosClick
            )
        }

        // composable para las solicitudes asignadas al socorrista
        composable(Screen.SolicitudesAsignadas.route) {
            val viewModel: SolicitudesAsignadasViewModel = viewModel(factory = ViewModelFactory(api))
            SolicitudesAsignadasScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        // composable para la seccion de informacion

            composable(Screen.Informacion.route) {
                val viewModel: AlertasViewModel = viewModel(factory = ViewModelFactory(api))
                InformacionAlertasScreen(
                    onBackClick = { navController.popBackStack() },
                    onVerMasClick = onVerDetalleAlerta,
                    viewModel = viewModel,
                    tabInicial = 0,
                    onNavItemSelected = { index ->
                        when (index) {
                            0 -> navController.navigate(Screen.Home.route)
                            1 -> navController.navigate(Screen.Mapa.route)
                            2 -> navController.navigate(Screen.MisContactos.route)
                            4 -> navController.navigate(Screen.Perfil.route)
                        }
                    },
                    onSosClick = onSosClick
                )
            }


            composable(Screen.Alertas.route) {
                val viewModel: AlertasViewModel = viewModel(factory = ViewModelFactory(api))
                InformacionAlertasScreen(
                    onBackClick = { navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } },
                    onVerMasClick = onVerDetalleAlerta,
                    viewModel = viewModel,
                    tabInicial = 1,
                    onNavItemSelected = { index ->
                        when (index) {
                            0 -> navController.navigate(Screen.Home.route)
                            1 -> navController.navigate(Screen.Mapa.route)
                            2 -> navController.navigate(Screen.MisContactos.route)
                            4 -> navController.navigate(Screen.Perfil.route)
                        }
                    },
                    onSosClick = onSosClick
                )
            }

            composable(
                route = Screen.DetalleAlerta.route,
                arguments = listOf(navArgument("alertaId") { type = NavType.StringType })
            ) { entrada ->
                val alertaId = entrada.arguments?.getString("alertaId").orEmpty()
                val viewModel: AlertasViewModel = viewModel(factory = ViewModelFactory(api))
                DetalleAlertaScreen(
                    alertaId = alertaId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onVerEnMapa = { alerta ->
                        val latitud = alerta.zonaAfectada?.latitud
                        val longitud = alerta.zonaAfectada?.longitud
                        if (latitud != null && longitud != null) {
                            mapaViewModel.enfocarSolicitud(
                                latitud = latitud,
                                longitud = longitud,
                                titulo = "Alerta: ${alerta.title}",
                                descripcion = listOfNotNull(
                                    alerta.category,
                                    alerta.nivelSeveridad?.let { "Severidad $it" },
                                    alerta.mensaje?.take(120)
                                ).joinToString(" • ")
                            )
                            navController.navigate(Screen.Mapa.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

        // composable para el perfil
        composable(Screen.Perfil.route) {
            val user = usuario ?: SessionManager.getUser()
            PerfilScreen(
                user = user,
                onBackClick = { navController.popBackStack() },
                onMisContactosClick = { navController.navigate(Screen.MisContactos.route) },
                onEditarPerfilClick = { navController.navigate(Screen.EditarPerfil.route) },
                onConfiguracionClick = { /* TODO */ },
                onPrivacidadClick = { /* TODO */ },
                onAcercaDeClick = { /* TODO */ },
                onCerrarSesionClick = {
                    userViewModel.logout()
                    authViewModel.resetEstado()
                    solicitarAyudaViewModel.descartarIslaSos()
                    notificacionSocorristaViewModel.detener()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // limpia toda la pila
                    }
                },
                onNavItemSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(Screen.Home.route)
                        1 -> navController.navigate(Screen.Mapa.route)
                        2 -> navController.navigate(Screen.MisContactos.route)
                    }
                },
                onSosClick = onSosClick
            )
        }

        // composable para editar el perfil
        composable(Screen.EditarPerfil.route) {
            EditarPerfilScreen(
                user = usuario ?: SessionManager.getUser(),
                viewModel = userViewModel,
                onBackClick = { navController.popBackStack() },
                onGuardado = {
                    navController.popBackStack()
                }
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
            MiUbicacionScreen(
                onBackClick = { navController.popBackStack() },
                onVerEnMapaClick = { navController.navigate(Screen.Mapa.route) },
                onVerHistorialCompletoClick = { /* TODO: pantalla de historial completo */ },
                viewModel = ubicacionViewModel,
                onNavItemSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(Screen.Home.route)
                        2 -> navController.navigate(Screen.MisContactos.route)
                        4 -> navController.navigate(Screen.Perfil.route)
                    }
                },
                onSosClick = onSosClick
            )
        }

        // composable para el Mapa de emergencia
        composable(Screen.Mapa.route) {
            MapaEmergenciaScreen(
                onMenuClick = { /* TODO: abrir drawer */ },
                onNavItemSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(Screen.Home.route)
                        2 -> navController.navigate(Screen.MisContactos.route)
                        4 -> navController.navigate(Screen.Perfil.route)
                    }
                },
                onSosClick = onSosClick,
                viewModel = mapaViewModel
            )
        }
        }

        val solicitudIdSos = solicitarAyudaUiState.sosSolicitudId
        val solicitudSocorrista = notificacionSocorristaUiState.solicitud

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NubeArrastrable {
                AnimatedVisibility(
                    visible = mostrarNubes && solicitudIdSos != null &&
                        rutaActual != Screen.EditarPerfil.route,
                    enter = fadeIn() + scaleIn(initialScale = 0.85f),
                    exit = fadeOut() + scaleOut(targetScale = 0.85f)
                ) {
                    solicitudIdSos?.let { id ->
                        SolicitudSosIsla(
                            estado = solicitarAyudaUiState.estadoSosIsla,
                            conOrganismo = solicitarAyudaUiState.sosConOrganismo,
                            solicitudId = id,
                            onDismiss = { solicitarAyudaViewModel.descartarIslaSos() }
                        )
                    }
                }
            }

            NubeArrastrable {
                AnimatedVisibility(
                    visible = mostrarNubes && esSocorrista && solicitudSocorrista != null &&
                        rutaActual != Screen.Mapa.route &&
                        rutaActual != Screen.Perfil.route &&
                        rutaActual != Screen.EditarPerfil.route,
                    enter = fadeIn() + scaleIn(initialScale = 0.85f),
                    exit = fadeOut() + scaleOut(targetScale = 0.85f)
                ) {
                    solicitudSocorrista?.let { solicitud ->
                        SolicitudSocorristaIsla(
                            solicitud = solicitud,
                            asignando = notificacionSocorristaUiState.asignando,
                            asignada = notificacionSocorristaUiState.asignada,
                            marcandoSalvo = notificacionSocorristaUiState.marcandoSalvo,
                            errorAsignacion = notificacionSocorristaUiState.errorAsignacion,
                            onAsignarme = { notificacionSocorristaViewModel.asignarme() },
                            onMarcarSalvo = { notificacionSocorristaViewModel.marcarUsuarioSalvo() },
                            onDismiss = { notificacionSocorristaViewModel.descartarActual() }
                        )
                    }
                }
            }
        }
    }

    val mensajeSos = solicitarAyudaUiState.mensajeSosRapido
    val sosEnviado = solicitarAyudaUiState.sosRapidoEnviado
    if (mensajeSos != null && sosEnviado != null) {
        SosResultadoDialog(
            enviado = sosEnviado,
            enCamino = solicitarAyudaUiState.sosRapidoEnCamino,
            mensaje = mensajeSos,
            onDismiss = { solicitarAyudaViewModel.consumirMensajeSos() }
        )
    }
}