package com.example.connectme

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject


class ContactsRequests : AppCompatActivity() {

    private lateinit var adapter: AdapterRequests
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contacts_requests)

        val backButton = findViewById<ImageView>(R.id.back)
        backButton.setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.requestsRecyclerView)

        adapter = AdapterRequests(mutableListOf(),
            onAccept = { request -> acceptRequest(request) },
            onReject = { request -> rejectRequest(request) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchRequests()
    }

    private fun fetchRequests() {
        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", 0)
        val url = Globals.BASE_URL+"get_requests.php?user_id=$currentUserId"


        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        val requestsArray = jsonResponse.getJSONArray("requests")
                        val requestsList = mutableListOf<ModelRequest>()

                        for (i in 0 until requestsArray.length()) {
                            val obj = requestsArray.getJSONObject(i)
                            val userId = obj.getString("userId")
                            val username = obj.getString("username")
                            val profileImageUrl = obj.getString("profileImageUrl")

                            requestsList.add(ModelRequest(userId, username, profileImageUrl))
                        }

                        adapter.updateList(requestsList)
                    }
                } catch (e: Exception) {
                    Log.e("fetchRequests", "JSON parsing error: ${e.message}")
                }
            },
            { error ->
                Log.e("fetchRequests", "Volley error: ${error.message}")
            })

        requestQueue.add(stringRequest)
    }

    private fun acceptRequest(request: ModelRequest) {
        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", 0)
        val url = "http://10.0.2.2/connectme/accept_request.php"

        val stringRequest = object : StringRequest(Method.POST, url,
            { response ->
                Toast.makeText(this, "Request accepted", Toast.LENGTH_SHORT).show()
                Log.d("acceptRequest", "Response: $response")
                adapter.removeItem(request)
            },
            { error ->
                Toast.makeText(this, "Failed to accept request", Toast.LENGTH_SHORT).show()
                Log.e("acceptRequest", "Error: ${error.message}")
            }) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "receiver_id" to currentUserId.toString(),
                    "sender_id" to request.userId
                )
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }


    private fun rejectRequest(request: ModelRequest) {

        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", 0)
        val url = "http://10.0.2.2/connectme/reject_request.php"

        val stringRequest = object : StringRequest(Method.POST, url,
            { response ->
                Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show()
                Log.d("rejectRequest", "Response: $response")
                adapter.removeItem(request)
            },
            { error ->
                Toast.makeText(this, "Failed to reject request", Toast.LENGTH_SHORT).show()
                Log.e("rejectRequest", "Error: ${error.message}")
            }) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "receiver_id" to currentUserId.toString(),
                    "sender_id" to request.userId
                )
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }
}