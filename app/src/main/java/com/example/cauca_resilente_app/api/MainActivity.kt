package com.example.cauca_resilente_app.api

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cauca_resilente_app.R
//import com.uniautonoma.caucaresiliente.data.model.HelpRequest
//import com.uniautonoma.caucaresiliente.data.model.LoginRequest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ejemplo 1: Llamar al Login
        realizarLogin("juan.perez@email.com", "123456")


    }

    private fun realizarLogin(email: String, pass: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.apiService.login(LoginRequest(email, pass))
                if (response.isSuccessful) {
                    val loginData = response.body()
                    Log.d("API", "Token: ${loginData?.token}")
                    Toast.makeText(this@MainActivity, "Bienvenido ${loginData?.user?.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("API", "Error en login: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API", "Excepción: ${e.message}")
            }
        }
    }


    private fun probarAPI() {
        lifecycleScope.launch {
            try {
                // 1. Login
                val loginResp = RetrofitInstance.apiService.login(
                    LoginRequest("juan.perez@email.com", "123456")
                )

                if (!loginResp.isSuccessful) {
                    Log.e("API", "Error login: ${loginResp.code()}")
                    return@launch
                }

                val token = "Bearer ${loginResp.body()?.token}"
                sessionManager.saveToken(loginResp.body()!!.token)
                Log.d("API", "✅ Login exitoso: ${loginResp.body()?.user?.name}")

                // 2. Obtener perfil
                val profileResp = RetrofitInstance.apiService.getUserProfile(token)
                Log.d("API", "Perfil: ${profileResp.body()?.email}")

                // 3. Actualizar estado
                val statusResp = RetrofitInstance.apiService.updateStatus(
                    loginResp.body()!!.user.id, StatusRequest("A salvo")
                )
                Log.d("API", "Estado: ${statusResp.mensaje}")

                // 4. Actualizar ubicación
                val locationResp = RetrofitInstance.apiService.updateLocation(
                    token,
                    LocationRequest(2.4448, -76.6147, "Calle 5 # 3-15, Popayán")
                )
                Log.d("API", "Ubicación: ${locationResp.body()?.success}")



                // 6. Obtener mapa
                val mapResp = RetrofitInstance.apiService.getMapPoints(token, "all")
                Log.d("API", "Puntos en mapa: ${mapResp.body()?.size}")

                // 7. Obtener alertas

                Toast.makeText(
                    this@MainActivity,
                    "✅ Todo funcionando",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Log.e("API", "❌ Error: ${e.message}", e)
                Toast.makeText(
                    this@MainActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}