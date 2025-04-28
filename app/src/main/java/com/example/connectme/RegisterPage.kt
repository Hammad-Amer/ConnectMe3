package com.example.connectme

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterPage : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var username: EditText
    private lateinit var phone: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerButton: Button
    private lateinit var loginText: TextView
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)

        sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)

        name = findViewById(R.id.Name_Register)
        username = findViewById(R.id.UserName_Register)
        phone = findViewById(R.id.Phonenum_Register)
        email = findViewById(R.id.Email_Register)
        password = findViewById(R.id.Password_Register)
        registerButton = findViewById(R.id.RegisterButton_Register)
        loginText = findViewById(R.id.Login_Register)

        registerButton.setOnClickListener { registerUser() }
        loginText.setOnClickListener {
            startActivity(Intent(this, LoginPage::class.java))
        }
    }

    private fun registerUser() {
        val fullName = name.text.toString().trim()
        val userName = username.text.toString().trim()
        val phoneNumber = phone.text.toString().trim()
        val userEmail = email.text.toString().trim()
        val userPassword = password.text.toString().trim()

        // Validate fields
        if (userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Use the apiService directly from ApiClient
        val call = ApiClient.apiService.registerUser(userName, userEmail, userPassword, fullName, phoneNumber)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {


                    Toast.makeText(this@RegisterPage, "User registered successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterPage, LoginPage::class.java))
                    finish()
                } else {
                    Toast.makeText(this@RegisterPage, "Registration failed: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@RegisterPage, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
