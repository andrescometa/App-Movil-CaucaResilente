package com.example.cauca_resilente_app.ui.Screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cauca_resilente_app.ui.theme.AppDesign
import com.example.cauca_resilente_app.ui.theme.appTypography

@Composable
fun BottomNavBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    onSosClick: () -> Unit,
    mostrarSos: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .height(64.dp)
                .shadow(16.dp, RoundedCornerShape(50), spotColor = Color.Black.copy(alpha = 0.25f))
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.62f))
                .border(1.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavItem("Inicio", Icons.Filled.Shield, 0, selectedItem, onItemSelected)
            NavItem("Zonas", Icons.Filled.Map, 1, selectedItem, onItemSelected)
            Spacer(modifier = Modifier.width(72.dp))
            NavItem("Círculo", Icons.Filled.Groups, 2, selectedItem, onItemSelected)
            NavItem("Perfil", Icons.Filled.Person, 4, selectedItem, onItemSelected)
        }

        if (mostrarSos) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .shadow(10.dp, CircleShape, spotColor = AppDesign.Danger.copy(alpha = 0.45f))
                        .clip(CircleShape)
                        .background(AppDesign.Danger)
                        .border(3.dp, Color.White, CircleShape)
                        .clickable { onSosClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Emergency,
                        contentDescription = "SOS",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "SOS",
                    fontSize = appTypography.micro,
                    fontWeight = FontWeight.Black,
                    color = AppDesign.DangerDark
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icono: ImageVector,
    index: Int,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    val seleccionado = selectedItem == index
    val color = if (seleccionado) AppDesign.Primary else AppDesign.TextMuted

    Column(
        modifier = Modifier
            .width(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onItemSelected(index) }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icono,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(23.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = appTypography.micro,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
            color = color
        )
    }
}
