package com.example.connectme

import RecyclerItemClickListener
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DMs : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterDMs
    private val dmList = mutableListOf<ModelDMs>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dms)

        recyclerView = findViewById(R.id.recyclerView_Dms)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdapterDMs(dmList)
        recyclerView.adapter = adapter

        fetchChatUsers()

        recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(this, recyclerView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val selectedUserId = dmList[position].userId // Get selected user's ID
                        val intent = Intent(this@DMs, ChatScreen::class.java)
                        intent.putExtra("USER_ID", selectedUserId) // Pass user ID dynamically
                        startActivity(intent)
                    }

                    override fun onLongItemClick(position: Int) {}
                })
        )
    }

    private fun fetchChatUsers() {
        val sharedPref = getSharedPreferences("ConnectMePref", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", 0)
        val db = dbHelper(this)

        if (Globals.isInternetAvailable(this)) {
            val url = Globals.BASE_URL+"get_dms.php?user_id=$currentUserId"
            val queue = Volley.newRequestQueue(this)

            val req = StringRequest(Request.Method.GET, url,
                { response ->
                    try {
                        val root = JSONObject(response)
                        val arr = root.getJSONArray("dms")
                        dmList.clear()
                        db.clearCachedDMs()

                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val userId = obj.getInt("user_id").toString()
                            val username = obj.getString("username")
                            val rawPfp   = if (!obj.isNull("pfp")) obj.getString("pfp") else null

                            // pass it into your model — adapter will fall back to imageRes when this is empty
                            dmList.add(
                                ModelDMs(
                                    image        = R.drawable.pf6,
                                    name         = username,
                                    userId       = userId,
                                    profileImage = rawPfp  // if null or empty, adapter uses R.drawable.pf6
                                )
                            )
                            db.cacheDM(userId, username,rawPfp)
                        }
                        adapter.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        Log.e("DMs", "bad json", e)
                    }
                },
                { error ->
                    Log.e("DMs", "Volley error", error)
                    Toast.makeText(this, "Volley failed: ${error.message}", Toast.LENGTH_LONG).show()
                })

            queue.add(req)
        } else {
            Toast.makeText(this, "No internet. Showing cached chats.", Toast.LENGTH_SHORT).show()
            dmList.clear()
            dmList.addAll(db.getCachedDMs())
            adapter.notifyDataSetChanged()
        }
    }






}
