package com.example.connectme

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import android.Manifest
import android.content.SharedPreferences
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.connectme.FirebaseConsts.USER_PATH
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private val baseUrl = Globals.BASE_URL
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
            setContentView(R.layout.activity_main)

            sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
            Handler(Looper.getMainLooper()).postDelayed({
                val userId = sharedPref.getInt("userId", 0)
                if (userId != 0) {
                    updateFCMToken(userId.toString())
                    startActivity(Intent(this, MainFeedScreen::class.java))
                } else {
                    startActivity(Intent(this, LoginPage::class.java))
                }
                finish()
            }, 5000)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
        }
    }

    private fun updateFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && !task.result.isNullOrEmpty()) {
                val token = task.result
                sendTokenToServer(userId, token)
            }
        }
    }

    private fun sendTokenToServer(userId: String, token: String) {
        val url = "${baseUrl}update_fcm_token.php"
        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                Log.d("FCM", "Token updated successfully")
            },
            { error ->
                Log.e("FCM", "Token update failed: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                return hashMapOf(
                    "user_id" to userId,
                    "fcm_token" to token
                )
            }
        }
        Volley.newRequestQueue(this).add(request)
    }


}