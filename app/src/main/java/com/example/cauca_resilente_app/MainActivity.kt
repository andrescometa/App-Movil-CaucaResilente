package com.example.cauca_resilente_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cauca_resilente_app.api.SessionManager
import com.example.cauca_resilente_app.ui.Navigation.AppNavigation
import com.example.cauca_resilente_app.ui.components.ShieldLogo
import com.example.cauca_resilente_app.ui.theme.AppColors
import com.example.cauca_resilente_app.ui.theme.Cauca_Resilente_AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(this)
        enableEdgeToEdge()
        setContent {
            Cauca_Resilente_AppTheme {
                AppNavigation()
            }
        }
    }
}

private val FondoNoche = Color(0xFF071329)

@Composable
fun BienvenidaScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF143A80),
                        Color(0xFF0A1B3A),
                        FondoNoche
                    ),
                    radius = 1400f,
                    center = Offset(540f, 620f)
                )
            )
    ) {
        // Rejilla y anillos de radar
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 90f
            var x = 0f
            while (x < size.width) {
                drawLine(Color(0x143B82F6), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(Color(0x143B82F6), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
            val center = Offset(size.width / 2f, size.height * 0.36f)
            listOf(290f, 390f, 490f, 590f).forEach { r ->
                drawCircle(Color(0x123B82F6), radius = r, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Spacer(modifier = Modifier.weight(1f))

            ShieldLogo(modifier = Modifier.size(210.dp))

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) { append("Cauca ") }
                    withStyle(SpanStyle(color = AppColors.Gold)) { append("Resiliente") }
                },
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(AppColors.Gold, AppColors.Accent)))
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Sistema de apoyo a la localización y gestión integral de emergencias",
                color = AppColors.OnDarkSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón primario
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(14.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFBBF24), AppColors.Amber, Color(0xFFD97706))
                        )
                    )
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(18.dp))
                    .clickable { onLoginClick() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Iniciar sesión", color = AppColors.NavyDeep, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = AppColors.NavyDeep.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botón secundario glass
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(18.dp))
                    .clickable { onRegisterClick() },
                contentAlignment = Alignment.Center
            ) {
                Text("Crear cuenta", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer institucional
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("U", color = Color(0xFF60A5FA), fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("A", color = AppColors.Gold, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(1.dp)
                        .height(16.dp)
                        .background(Color.White.copy(alpha = 0.25f))
                )
                Column {
                    Text("UNI AUTÓNOMA", color = Color(0xFFF8FAFC), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Text("DEL CAUCA", color = AppColors.OnDarkTertiary, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
