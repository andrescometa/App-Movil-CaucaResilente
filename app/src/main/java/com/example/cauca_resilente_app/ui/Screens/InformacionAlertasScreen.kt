package com.example.cauca_resilente_app.ui.Screens// ui/screens/InformacionAlertasScreen.kt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cauca_resilente_app.api.AlertItem
import com.example.cauca_resilente_app.ui.ViewModels.AlertasViewModel
import com.example.cauca_resilente_app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformacionAlertasScreen(
    onBackClick: () -> Unit,
    onVerMasClick: (AlertItem) -> Unit,
    viewModel: AlertasViewModel,
    tabInicial: Int = 0
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tabSeleccionada by remember { mutableIntStateOf(tabInicial) }
    val tabs = listOf("Noticias", "Alertas", "Recomendaciones")
    val categorias = listOf("Noticia", "Alerta", "Recomendación")
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar error
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        containerColor = TextoBlanco,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Información y alertas", color = TextoBlanco, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextoBlanco)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarAlertas() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recargar", tint = TextoBlanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // ===== TABS =====
            TabRow(
                selectedTabIndex = tabSeleccionada,
                containerColor = TextoBlanco,
                contentColor = AzulOscuro,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[tabSeleccionada])
                            .height(3.dp),
                        color = AzulOscuro
                    )
                },
                divider = { HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f)) }
            ) {
                tabs.forEachIndexed { index, titulo ->
                    Tab(
                        selected = tabSeleccionada == index,
                        onClick = { tabSeleccionada = index },
                        text = {
                            Text(
                                titulo,
                                fontSize = 14.sp,
                                fontWeight = if (tabSeleccionada == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (tabSeleccionada == index) AzulOscuro else TextoOscuro.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }

            // ===== CONTENIDO =====
            when {
                uiState.isLoading && uiState.alertas.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AzulOscuro)
                    }
                }
                else -> {
                    val listaActual = uiState.alertas.filter {
                        it.category.equals(categorias[tabSeleccionada], ignoreCase = true)
                    }

                    if (listaActual.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No hay contenido disponible en esta categoría.",
                                color = TextoOscuro.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                            contentPadding = PaddingValues(vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(listaActual) { item ->
                                TarjetaInformacion(item = item, onClick = { onVerMasClick(item) })
                            }

                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { viewModel.cargarAlertas() },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Ver más información", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaInformacion(item: AlertItem, onClick: () -> Unit) {
    val colorCategoria = when (item.category.lowercase()) {
        "alerta" -> RojoEmergencia
        "recomendación", "recomendacion" -> VerdeSeguro
        else -> Color(0xFFF9A825) // Noticia
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoClaro),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Chip categoría
            Box(
                modifier = Modifier
                    .background(colorCategoria, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(item.category, color = TextoBlanco, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(item.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
            Spacer(modifier = Modifier.height(6.dp))
            Text(item.description, fontSize = 13.sp, color = TextoOscuro.copy(alpha = 0.7f), lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("${item.date} • ${item.time}", fontSize = 11.sp, color = TextoOscuro.copy(alpha = 0.5f))
        }
    }
}