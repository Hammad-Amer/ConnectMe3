package com.example.connectme

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.firebase.messaging.FirebaseMessaging
class LoginPage : AppCompatActivity() {

    private val baseUrl = Globals.BASE_URL

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPassword: TextView
    private lateinit var registerText: TextView
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)

        email = findViewById(R.id.Username_login)
        password = findViewById(R.id.Password_login)
        loginButton = findViewById(R.id.LoginButton_login)
        forgotPassword = findViewById(R.id.ForgotPassword_login)
        registerText = findViewById(R.id.Register_login)

        loginButton.setOnClickListener { loginUser() }
        registerText.setOnClickListener {
            startActivity(Intent(this, RegisterPage::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // Auto-login if session exists
        if (sharedPref.getString("email", null) != null) {
            startActivity(Intent(this, MainFeedScreen::class.java))
            finish()
        }
    }

    private fun loginUser() {
        val userEmail = email.text.toString().trim()
        val userPassword = password.text.toString().trim()

        // Validate input fields
        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Regex for email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        // Use the apiService directly from ApiClient
        val call = ApiClient.apiService.loginUser(userEmail, userPassword)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    // Save user data
                    val editor = sharedPref.edit()
                    editor.putString("email", userEmail)
                    editor.putString("username", response.body()?.username)
                    editor.putInt("userId", response.body()?.userId ?: 0)
                    editor.putString("profilePicture", response.body()?.pfp)
                    editor.apply()

                    // Get and update FCM token
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            updateFCMTokenOnServer(
                                response.body()?.userId?.toString() ?: "",
                                token ?: ""
                            )
                        }
                    }

                    Toast.makeText(this@LoginPage, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginPage, MainFeedScreen::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginPage, "Login failed: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@LoginPage, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateFCMTokenOnServer(userId: String, token: String) {
        val url = "${baseUrl}update_fcm_token.php"
        val request = object : StringRequest(
            Request.Method.POST, url,
            { response -> Log.d("FCM", "Token updated") },
            { error -> Log.e("FCM", "Token update failed", error) }
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
