package com.example.connectme

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class EditProfile : AppCompatActivity() {

    private lateinit var profileImageView: ShapeableImageView
    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var headerNameText: TextView


    private val PICK_IMAGE_REQUEST = 1
    private var currentBase64Image: String? = null
    private var newImageSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImageView = findViewById(R.id.Main_profile_pic_editprofile)
        val cameraIcon = findViewById<ImageView>(R.id.cam)
        val doneButton = findViewById<TextView>(R.id.done_edit_profile)


        nameEditText = findViewById(R.id.nameEditText)
        usernameEditText = findViewById(R.id.usernameEditText)
        contactEditText = findViewById(R.id.contactEditText)
        headerNameText = findViewById(R.id.headerNameText)

        cameraIcon.setOnClickListener { openGallery() }

        doneButton.setOnClickListener {
            saveProfileDetails()
        }

        loadProfileInfo()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imageUri?.let {
                try {
                    val bitmap = uriToBitmap(it)
                    profileImageView.setImageBitmap(bitmap)
                    currentBase64Image = encodeToBase64(bitmap)
                    newImageSelected = true

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun uriToBitmap(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream) ?: throw IOException("Failed to decode image")
    }

    private fun encodeToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun saveProfileDetails() {
        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0)

        val username = usernameEditText.text.toString().trim()
        val fullname = nameEditText.text.toString().trim()
        val phone = contactEditText.text.toString().trim()

        if (username.isEmpty() || fullname.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val imageToSend = if (newImageSelected) currentBase64Image else currentBase64Image

        ApiClient.apiService.updateProfile(userId, username, fullname, phone, imageToSend)
            .enqueue(object : Callback<ApiResponsebetter> {
                override fun onResponse(
                    call: Call<ApiResponsebetter>,
                    response: Response<ApiResponsebetter>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            if (it.status == "success") {
                                Toast.makeText(this@EditProfile, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                headerNameText.text = fullname
                                newImageSelected = false
                                finish()
                            } else {
                                Toast.makeText(this@EditProfile, it.message ?: "Update failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@EditProfile, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponsebetter>, t: Throwable) {
                    Toast.makeText(this@EditProfile, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadProfileInfo() {
        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0)

        ApiClient.apiService.getUserProfile(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(
                call: Call<UserProfileResponse>,
                response: Response<UserProfileResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { userProfile ->
                        nameEditText.setText(userProfile.fullname)
                        usernameEditText.setText(userProfile.username)
                        contactEditText.setText(userProfile.phone)
                        headerNameText.text = userProfile.fullname

                        userProfile.pfp?.takeIf { it.isNotEmpty() }?.let { pfp ->
                            try {
                                val decodedByteArray = Base64.decode(pfp, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
                                bitmap?.let {
                                    profileImageView.setImageBitmap(it)
                                    currentBase64Image = pfp
                                }
                            } catch (e: Exception) {
                                Toast.makeText(this@EditProfile, "Error decoding image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@EditProfile, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}